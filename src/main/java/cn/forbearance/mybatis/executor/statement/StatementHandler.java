package cn.forbearance.mybatis.executor.statement;

import cn.forbearance.mybatis.mapping.BoundSql;
import cn.forbearance.mybatis.session.ResultHandler;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * SQL语句处理器：准备SQL语句、参数化传递、执行SQL、结果封装
 *
 * @author cristina
 */
public interface StatementHandler {

    /**
     * 准备SQL语句
     *
     * @param connection
     * @return
     * @throws SQLException
     */
    Statement prepare(Connection connection) throws SQLException;

    /**
     * 参数化
     *
     * @param statement
     * @throws SQLException
     */
    void parameterize(Statement statement) throws SQLException;

    /**
     * 执行查询
     *
     * @param statement
     * @param resultHandler
     * @param <E>
     * @return
     * @throws SQLException
     */
    <E> List<E> query(Statement statement, ResultHandler resultHandler) throws SQLException;

    /**
     * 更新
     *
     * @param statement
     * @return
     * @throws SQLException
     */
    int update(Statement statement) throws SQLException;

    /**
     * 获取绑定SQL
     *
     * @return
     */
    BoundSql getBoundSql();
}
