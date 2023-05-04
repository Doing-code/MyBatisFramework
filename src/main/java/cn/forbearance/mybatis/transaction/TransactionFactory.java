package cn.forbearance.mybatis.transaction;

import cn.forbearance.mybatis.session.TransactionIsolationLevel;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * 事务工厂
 * @author cristina
 */
public interface TransactionFactory {

    /**
     * 根据 Connection 创建 Transaction
     *
     * @param conn
     * @return
     */
    Transaction newTransaction(Connection conn);

    /**
     * 根据数据源、事务隔离级别擦创建 Transaction
     *
     * @param dataSource 数据源
     * @param level      隔离级别
     * @param autoCommit 自动提交
     * @return
     */
    Transaction newTransaction(DataSource dataSource, TransactionIsolationLevel level, boolean autoCommit);
}
