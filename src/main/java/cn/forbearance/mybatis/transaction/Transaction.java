package cn.forbearance.mybatis.transaction;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 事务接口
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
