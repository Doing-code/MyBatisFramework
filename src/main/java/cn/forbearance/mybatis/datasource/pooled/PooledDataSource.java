package cn.forbearance.mybatis.datasource.pooled;

import cn.forbearance.mybatis.datasource.unpooled.UnPooledDataSource;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.logging.Logger;

/**
 * 有连接池的数据源
 * TODO 需要考虑 SqlSession 的资源关闭
 *
 * @author cristina
 */
public class PooledDataSource implements DataSource {

    private final org.slf4j.Logger log = LoggerFactory.getLogger(PooledDataSource.class);

    /**
     * 池状态
     */
    private final PoolState state = new PoolState(this);

    /**
     * 创建新连接的任务委托给 UnPooledDataSource
     */
    private final UnPooledDataSource dataSource;

    /**
     * 活跃连接数
     */
    protected int poolMaximumActiveConnections = 10;

    /**
     * 空闲连接数
     */
    protected int poolMaximumIdleConnections = 5;

    /**
     * 【最大可回收时间】在被强制返回之前，池中连接被检出时间，20s
     */
    protected int poolMaximumCheckoutTime = 20000;

    /**
     * 【无连接可用时的等待时间】这是一个底层设置，如果获取连接花费相当长的时间，连接池会打印状态日志并重新尝试获取一个连接
     * 【避免在错误配置的情况下一致失败且不打印日志】，20s
     */
    protected int poolTimeToWait = 20000;

    /**
     * 发送到数据库的侦测查询，用来验证连接是否正常工作，并准备接受请求。
     * 这会导致多数的数据库驱动出错时返回恰当的错误信息。
     */
    protected String poolPingQuery = "NO PING QUERY SET";

    /**
     * 是否启用侦测查询，若开启，则需要设置【poolPingQuery】属性为一个可执行的SQL语句
     * （最好是一个速度非常快的SQL语句）
     */
    protected boolean poolPingEnabled = false;

    /**
     * 配置【poolPingQuery】的频率，可以被设置为和数据库连接超时时间一样，来避免不必要的侦察
     * 默认值：0[毫秒]（即所有连接每一时刻都被侦测 — 当然仅当 poolPingEnabled 为 true 时适用）。
     */
    protected int poolPingConnectionsNotUsedFor = 0;

    /**
     * 缓存的连接标识，为【url+username+password】的哈希码
     * 避免将其他 Connection push 到连接池
     */
    protected int expectedConnectionTypeCode;

    public PooledDataSource() {
        this.dataSource = new UnPooledDataSource();
    }

    /**
     * 回收连接
     *
     * @param connection
     * @throws SQLException
     */
    protected void pushConnection(PooledConnection connection) throws SQLException {
        synchronized (state) {
            // 从活跃连接池中移除此连接
            state.activeConnections.remove(connection);
            // 判断连接是否有效
            if (connection.isValid()) {
                // 判断空闲连接池资源是否达到上限【空闲连接小于设定数量】
                if (state.idleConnections.size() < poolMaximumIdleConnections
                        && connection.getConnectionTypeCode() == expectedConnectionTypeCode) {
                    // 没有达到上限，进行回收
                    state.accumulatedCheckoutTime += connection.getCheckoutTime();
                    /*
                     * 检查数据库连接是否处于自动提交模式，若不是，则调用rollback()方法执行回滚操作
                     * 在 MyBatis 中，如果没有开启自动提交模式，则需要手动提交或回滚事务
                     * 用于保证数据库的一致性，确保操作完成后，如果未开启自动提交模式，则执行回滚
                     * */
                    if (!connection.getRealConnection().getAutoCommit()) {
                        connection.getRealConnection().rollback();
                    }
                    // 基于该连接，创建一个新的连接资源，并刷新连接状态，加入到 idleConnections 中
                    PooledConnection newConnection = new PooledConnection(connection.getRealConnection(), this);
                    state.idleConnections.add(newConnection);
                    newConnection.setCreatedTimestamp(connection.getCreatedTimestamp());
                    newConnection.setLastUsedTimestamp(connection.getLastUsedTimestamp());
                    // 老连接失效
                    connection.invalidate();
                    log.info("Returned connection " + newConnection.getRealHashCode() + " to pool.");

                    // 唤醒其他被阻塞线程，可以来抢占DB连接了
                    state.notifyAll();
                } else {
                    // 空闲连接池达到上限，将连接真实关闭
                    state.accumulatedCheckoutTime += connection.getCheckoutTime();
                    if (!connection.getRealConnection().getAutoCommit()) {
                        connection.getRealConnection().rollback();
                    }
                    // 关闭真的数据库连接
                    connection.getRealConnection().close();
                    log.info("Closed connection " + connection.getRealHashCode() + ".");
                    connection.invalidate();
                }
            } else {
                log.info("A bad connection (" + connection.getRealHashCode() + ") attempted to return to the pool, discarding connection.");
                state.badConnectionCount++;
            }
        }
    }

    /**
     * 获取连接
     *
     * @param username
     * @param password
     * @return
     * @throws SQLException
     */
    private PooledConnection popConnection(String username, String password) throws SQLException {
        boolean countedWait = false;
        PooledConnection conn = null;
        // 记录尝试获取连接的起始时间戳
        long t = System.currentTimeMillis();
        // 初始化获取到无效连接的次数
        int localBadConnectionCount = 0;

        while (conn == null) {
            synchronized (state) {
                // 判断是否有空闲连接
                if (!state.idleConnections.isEmpty()) {
                    // Pool has available connection【返回第一个】
                    conn = state.idleConnections.remove(0);
                    log.info("Checked out connection " + conn.getRealHashCode() + " from pool.");

                    // 没有空闲连接，则会创建新的连接
                } else {
                    // 判断活跃连接池中的数量是否小于最大连接数
                    if (state.activeConnections.size() < poolMaximumActiveConnections) {
                        conn = new PooledConnection(dataSource.getConnection(), this);
                        log.info("Created connection " + conn.getRealHashCode() + ".");

                        // 如果已经等于最大连接数，则不能创建新的连接
                    } else {
                        // 获取最早创建的连接
                        PooledConnection oldestActiveConnection = state.activeConnections.get(0);
                        long longestCheckoutTime = oldestActiveConnection.getCheckoutTime();
                        // 检测是否已经超过最长使用时间
                        if (longestCheckoutTime > poolMaximumCheckoutTime) {
                            // 如果超时，对超时连接的信息进行统计
                            // 超时连接数+1
                            state.claimedOverdueConnectionCount++;
                            // 累计超时时间增加
                            state.accumulatedCheckoutTimeOfOverdueConnections += longestCheckoutTime;
                            // 累计的使用连接的时间增加
                            state.accumulatedCheckoutTime += longestCheckoutTime;
                            // 从活跃集合中移除
                            state.activeConnections.remove(oldestActiveConnection);
                            // 如果超时连接未提交，则手动回滚
                            if (!oldestActiveConnection.getRealConnection().getAutoCommit()) {
                                oldestActiveConnection.getRealConnection().rollback();
                            }
                            // 重新实例化创建一个新的连接
                            conn = new PooledConnection(oldestActiveConnection.getRealConnection(), this);
                            // 让老连接失效
                            oldestActiveConnection.invalidate();
                            log.info("Claimed overdue connection " + conn.getRealHashCode() + ".");
                        } else {
                            // 无空闲连接，最早创建的连接也没有失效，无法创建新连接，只能阻塞
                            try {
                                if (!countedWait) {
                                    // 累计等待次数+1
                                    state.hadToWaitCount++;
                                    countedWait = true;
                                }
                                log.info("Waiting as long as " + poolTimeToWait + " milliseconds for connection.");
                                long wt = System.currentTimeMillis();
                                // 阻塞等待指定时间
                                state.wait(poolTimeToWait);
                                // 累计等待时间增加
                                state.accumulatedWaitTime += System.currentTimeMillis() - wt;
                            } catch (InterruptedException e) {
                                break;
                            }
                        }
                    }
                }
                // 获取到连接，要测试连接是否有效，同时更新统计数据
                if (conn != null) {
                    // 连接是否有效
                    if (conn.isValid()) {
                        if (!conn.getRealConnection().getAutoCommit()) {
                            // 如果遗留历史的事务，回滚
                            conn.getRealConnection().rollback();
                        }
                        // 连接池相关统计信息更新
                        conn.setConnectionTypeCode(assembleConnectionTypeCode(dataSource.getUrl(), username, password));
                        conn.setCheckoutTimestamp(System.currentTimeMillis());
                        conn.setLastUsedTimestamp(System.currentTimeMillis());
                        state.activeConnections.add(conn);
                        state.requestCount++;
                        state.accumulatedRequestTime += System.currentTimeMillis() - t;
                    } else {
                        // 连接无效
                        log.info("A bad connection (" + conn.getRealHashCode() + ") was returned from the pool, getting another connection.");
                        // 失败连接+1
                        state.badConnectionCount++;
                        localBadConnectionCount++;
                        conn = null;
                        // 拿到无效连接，但没有超过重试次数，允许再次尝试获取连接，否则抛出异常
                        if (localBadConnectionCount > (poolMaximumIdleConnections + 3)) {
                            log.debug("PooledDataSource: Could not get a good connection to the database.");
                            throw new SQLException("PooledDataSource: Could not get a good connection to the database.");
                        }
                    }
                }
            }
        }
        if (conn == null) {
            log.debug("PooledDataSource: Unknown severe error condition.  The connection pool returned a null connection.");
            throw new SQLException("PooledDataSource: Unknown severe error condition.  The connection pool returned a null connection.");
        }
        return conn;
    }

    /**
     * Closes all active and idle connections in the pool.
     */
    public void forceCloseAll() {
        synchronized (state) {
            expectedConnectionTypeCode = assembleConnectionTypeCode(dataSource.getUrl(), dataSource.getUsername(), dataSource.getPassword());
            // 关闭活跃连接
            for (int i = state.activeConnections.size(); i > 0; i++) {
                try {
                    PooledConnection conn = state.activeConnections.remove(i - 1);
                    conn.invalidate();
                    Connection realConn = conn.getRealConnection();
                    if (!realConn.getAutoCommit()) {
                        realConn.rollback();
                    }
                    realConn.close();
                } catch (SQLException ignore) {

                }
            }
            // 关闭空闲连接
            for (int i = state.idleConnections.size(); i > 0; i++) {
                try {
                    PooledConnection conn = state.idleConnections.remove(i - 1);
                    conn.invalidate();
                    Connection realConn = conn.getRealConnection();
                    if (!realConn.getAutoCommit()) {
                        realConn.rollback();
                    }
                    realConn.close();
                } catch (SQLException ignore) {

                }
            }
            log.info("PooledDataSource forcefully closed/removed all connections.");
        }
    }

    protected boolean pingConnection(PooledConnection connection) {
        boolean result = true;

        try {
            result = !connection.getRealConnection().isClosed();
        } catch (SQLException e) {
            log.info("Connection " + connection.getRealHashCode() + " is BAD: " + e.getMessage());
            result = false;
        }
        if (result) {
            if (poolPingEnabled) {
                if (poolPingConnectionsNotUsedFor >= 0 &&
                        connection.getTimeElapsedSinceLastUse() > poolPingConnectionsNotUsedFor) {
                    log.info("Testing connection " + connection.getRealHashCode() + " ...");
                    Connection realConn = connection.getRealConnection();
                    try {
                        Statement statement = realConn.createStatement();
                        ResultSet resultSet = statement.executeQuery(poolPingQuery);
                        resultSet.close();
                        if (!realConn.getAutoCommit()) {
                            realConn.rollback();
                        }
                        result = true;
                        log.info("Connection " + connection.getRealHashCode() + " is GOOD!");
                    } catch (SQLException e) {
                        log.info("Execution of ping query '" + poolPingQuery + "' failed: " + e.getMessage());
                        try {
                            connection.getRealConnection().close();
                        } catch (SQLException ignore) {
                        }
                        result = false;
                        log.info("Connection " + connection.getRealHashCode() + " is BAD: " + e.getMessage());
                    }
                }
            }
        }
        return result;
    }

    public static Connection unwrapConnection(Connection connection) {
        if (Proxy.isProxyClass(connection.getClass())) {
            InvocationHandler handler = Proxy.getInvocationHandler(connection);
            if (handler instanceof PooledConnection) {
                return ((PooledConnection) handler).getRealConnection();
            }
        }
        return connection;
    }

    private int assembleConnectionTypeCode(String url, String username, String password) {
        return ("" + url + username + password).hashCode();
    }

    @Override
    public Connection getConnection() throws SQLException {
        return popConnection(dataSource.getUsername(), dataSource.getPassword()).getProxyConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return popConnection(username, password).getProxyConnection();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new SQLException(getClass().getName() + " is not a wrapper.");
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return DriverManager.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        DriverManager.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        DriverManager.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return DriverManager.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    }

    public void setDriver(String driver) {
        dataSource.setDriver(driver);
        forceCloseAll();
    }

    public void setUrl(String url) {
        dataSource.setUrl(url);
        forceCloseAll();
    }

    public void setUsername(String username) {
        dataSource.setUsername(username);
        forceCloseAll();
    }

    public void setPassword(String password) {
        dataSource.setPassword(password);
        forceCloseAll();
    }


    public void setDefaultAutoCommit(boolean defaultAutoCommit) {
        dataSource.setAutoCommit(defaultAutoCommit);
        forceCloseAll();
    }

    public int getPoolMaximumActiveConnections() {
        return poolMaximumActiveConnections;
    }

    public void setPoolMaximumActiveConnections(int poolMaximumActiveConnections) {
        this.poolMaximumActiveConnections = poolMaximumActiveConnections;
    }

    public int getPoolMaximumIdleConnections() {
        return poolMaximumIdleConnections;
    }

    public void setPoolMaximumIdleConnections(int poolMaximumIdleConnections) {
        this.poolMaximumIdleConnections = poolMaximumIdleConnections;
    }

    public int getPoolMaximumCheckoutTime() {
        return poolMaximumCheckoutTime;
    }

    public void setPoolMaximumCheckoutTime(int poolMaximumCheckoutTime) {
        this.poolMaximumCheckoutTime = poolMaximumCheckoutTime;
    }

    public int getPoolTimeToWait() {
        return poolTimeToWait;
    }

    public void setPoolTimeToWait(int poolTimeToWait) {
        this.poolTimeToWait = poolTimeToWait;
    }

    public String getPoolPingQuery() {
        return poolPingQuery;
    }

    public void setPoolPingQuery(String poolPingQuery) {
        this.poolPingQuery = poolPingQuery;
    }

    public boolean isPoolPingEnabled() {
        return poolPingEnabled;
    }

    public void setPoolPingEnabled(boolean poolPingEnabled) {
        this.poolPingEnabled = poolPingEnabled;
    }

    public int getPoolPingConnectionsNotUsedFor() {
        return poolPingConnectionsNotUsedFor;
    }

    public void setPoolPingConnectionsNotUsedFor(int poolPingConnectionsNotUsedFor) {
        this.poolPingConnectionsNotUsedFor = poolPingConnectionsNotUsedFor;
    }

    public int getExpectedConnectionTypeCode() {
        return expectedConnectionTypeCode;
    }

    public void setExpectedConnectionTypeCode(int expectedConnectionTypeCode) {
        this.expectedConnectionTypeCode = expectedConnectionTypeCode;
    }
}
