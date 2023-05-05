package cn.forbearance.mybatis.session.defaults;

import cn.forbearance.mybatis.executor.Executor;
import cn.forbearance.mybatis.mapping.Environment;
import cn.forbearance.mybatis.session.SqlSession;
import cn.forbearance.mybatis.session.Configuration;
import cn.forbearance.mybatis.session.SqlSessionFactory;
import cn.forbearance.mybatis.session.TransactionIsolationLevel;
import cn.forbearance.mybatis.transaction.Transaction;
import cn.forbearance.mybatis.transaction.TransactionFactory;

import java.sql.SQLException;

/**
 * 默认的简单工厂实现，处理开启 SqlSession 时，
 * 对 DefaultSqlSession 的创建以及传递 mapperRegistry，
 * 这样就可以在使用 SqlSession 时获取每个代理类的映射器对象了
 *
 * @author cristina
 */
public class DefaultSqlSessionFactory implements SqlSessionFactory {

    private final Configuration configuration;

    public DefaultSqlSessionFactory(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public SqlSession openSession() {
        Transaction tx = null;
        try {
            final Environment environment = configuration.getEnvironment();
            final TransactionFactory transactionFactory = environment.getTransactionFactory();
            tx = transactionFactory.newTransaction(environment.getDataSource(), TransactionIsolationLevel.READ_COMMITTED, false);
            // 创建执行器
            final Executor executor = configuration.newExecutor(tx);
            // 创建 DefaultSqlSession
            return new DefaultSqlSession(configuration, executor);
        } catch (Exception e) {
            try {
                assert tx != null;
                tx.close();
            } catch (SQLException ignore) {
            }
            throw new RuntimeException("Error opening session.  Cause: " + e);
        }
    }
}
