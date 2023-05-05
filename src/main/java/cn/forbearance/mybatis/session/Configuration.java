package cn.forbearance.mybatis.session;

import cn.forbearance.mybatis.binding.MapperRegistry;
import cn.forbearance.mybatis.datasource.druid.DruidDataSourceFactory;
import cn.forbearance.mybatis.datasource.pooled.PooledDataSourceFactory;
import cn.forbearance.mybatis.datasource.unpooled.UnPooledDataSourceFactory;
import cn.forbearance.mybatis.executor.Executor;
import cn.forbearance.mybatis.executor.SimpleExecutor;
import cn.forbearance.mybatis.executor.resultset.DefaultResultSetHandler;
import cn.forbearance.mybatis.executor.resultset.ResultSetHandler;
import cn.forbearance.mybatis.executor.statement.PreparedStatementHandler;
import cn.forbearance.mybatis.executor.statement.StatementHandler;
import cn.forbearance.mybatis.mapping.BoundSql;
import cn.forbearance.mybatis.mapping.Environment;
import cn.forbearance.mybatis.mapping.MappedStatement;
import cn.forbearance.mybatis.transaction.Transaction;
import cn.forbearance.mybatis.transaction.jdbc.JdbcTransactionFactory;
import cn.forbearance.mybatis.type.TypeAliasRegistry;

import java.util.HashMap;
import java.util.Map;

/**
 * @author cristina
 */
public class Configuration {

    /**
     * 运行环境
     */
    protected Environment environment;

    /**
     * 映射注册机
     */
    protected MapperRegistry mapperRegistry = new MapperRegistry(this);

    /**
     * 映射的sql
     */
    protected final Map<String, MappedStatement> mappedStatements = new HashMap<>();

    /**
     * 类型别名注册机
     */
    protected final TypeAliasRegistry typeAliasRegistry = new TypeAliasRegistry();

    public Configuration() {
        typeAliasRegistry.registerAlias("JDBC", JdbcTransactionFactory.class);
        typeAliasRegistry.registerAlias("DRUID", DruidDataSourceFactory.class);
        typeAliasRegistry.registerAlias("UNPOOLED", UnPooledDataSourceFactory.class);
        typeAliasRegistry.registerAlias("POOLED", PooledDataSourceFactory.class);
    }

    public void addMappers(String packageName) {
        mapperRegistry.addMappers(packageName);
    }

    public <T> void addMapper(Class<T> type) {
        mapperRegistry.addMapper(type);
    }

    public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
        return mapperRegistry.getMapper(type, sqlSession);
    }

    public boolean hasMapper(Class<?> type) {
        return mapperRegistry.hasMapper(type);
    }

    public void addMappedStatement(MappedStatement ms) {
        mappedStatements.put(ms.getId(), ms);
    }

    public MappedStatement getMappedStatement(String id) {
        return mappedStatements.get(id);
    }

    public TypeAliasRegistry getTypeAliasRegistry() {
        return typeAliasRegistry;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public ResultSetHandler newResultSetHandler(Executor executor, MappedStatement ms, BoundSql boundSql) {
        return new DefaultResultSetHandler(executor, ms, boundSql);
    }

    public Executor newExecutor(Transaction transaction) {
        return new SimpleExecutor(this, transaction);
    }

    public StatementHandler newStatementHandler(Executor executor, MappedStatement ms, Object parameter, ResultHandler resultHandler, BoundSql boundSql) {
        return new PreparedStatementHandler(executor, ms, parameter, resultHandler, boundSql);
    }
}
