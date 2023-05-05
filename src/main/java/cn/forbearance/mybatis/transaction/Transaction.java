package cn.forbearance.mybatis.transaction;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 事务接口
 * <p>
 * 定义标准的事务接口，链接、提交、回滚、关闭，
 * 具体可以由不同的事务方式进行实现，包括：JDBC和托管事务，托管事务是交给 Spring 这样的容器来管理。
 *
 * @author cristina
 */
public interface Transaction {

    /**
     * #
     *
     * @return
     * @throws SQLException
     */
    Connection getConnection() throws SQLException;

    /**
     * #
     *
     * @throws SQLException
     */
    void commit() throws SQLException;

    /**
     * #
     *
     * @throws SQLException
     */
    void rollback() throws SQLException;

    /**
     * #
     *
     * @throws SQLException
     */
    void close() throws SQLException;
}
