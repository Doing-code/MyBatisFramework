package cn.forbearance.mybatis.transaction.jdbc;

import cn.forbearance.mybatis.session.TransactionIsolationLevel;
import cn.forbearance.mybatis.transaction.Transaction;
import cn.forbearance.mybatis.transaction.TransactionFactory;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * TransactionFactory 工厂
 *
 * @author cristina
 */
public class JdbcTransactionFactory implements TransactionFactory {

    @Override
    public Transaction newTransaction(Connection conn) {
        return new JdbcTransaction(conn);
    }

    @Override
    public Transaction newTransaction(DataSource dataSource, TransactionIsolationLevel level, boolean autoCommit) {
        return new JdbcTransaction(dataSource, level, autoCommit);
    }
}
