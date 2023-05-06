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
import cn.forbearance.mybatis.refection.MetaObject;
import cn.forbearance.mybatis.refection.factory.DefaultObjectFactory;
import cn.forbearance.mybatis.refection.factory.ObjectFactory;
import cn.forbearance.mybatis.refection.wapper.DefaultObjectWrapperFactory;
import cn.forbearance.mybatis.refection.wapper.ObjectWrapperFactory;
import cn.forbearance.mybatis.scripting.LanguageDriverRegistry;
import cn.forbearance.mybatis.scripting.xmltags.XmlLanguageDriver;
import cn.forbearance.mybatis.transaction.Transaction;
import cn.forbearance.mybatis.transaction.jdbc.JdbcTransactionFactory;
import cn.forbearance.mybatis.type.TypeAliasRegistry;
import cn.forbearance.mybatis.type.TypeHandlerRegistry;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

    /**
     * 类型处理器
     */
    protected final TypeHandlerRegistry typeHandlerRegistry = new TypeHandlerRegistry();

    protected final LanguageDriverRegistry languageRegistry = new LanguageDriverRegistry();

    /**
     * 对象工厂和对象包装器工厂
     */
    protected ObjectFactory objectFactory = new DefaultObjectFactory();
    protected ObjectWrapperFactory objectWrapperFactory = new DefaultObjectWrapperFactory();

    protected final Set<String> loadedResources = new HashSet<>();

    protected String databaseId;

    public Configuration() {
        typeAliasRegistry.registerAlias("JDBC", JdbcTransactionFactory.class);
        typeAliasRegistry.registerAlias("DRUID", DruidDataSourceFactory.class);
        typeAliasRegistry.registerAlias("UNPOOLED", UnPooledDataSourceFactory.class);
        typeAliasRegistry.registerAlias("POOLED", PooledDataSourceFactory.class);

        languageRegistry.setDefaultDriverClass(XmlLanguageDriver.class);
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

    /**
     * 创建元对象
     *
     * @param object
     * @return
     */
    public MetaObject newMetaObject(Object object) {
        return MetaObject.forObject(object, objectFactory, objectWrapperFactory);
    }

    /**
     * 类型处理器注册机
     *
     * @return
     */
    public TypeHandlerRegistry getTypeHandlerRegistry() {
        return typeHandlerRegistry;
    }

    public boolean isResourceLoaded(String resource) {
        return loadedResources.contains(resource);
    }

    public void addLoadedResource(String resource) {
        loadedResources.add(resource);
    }

    public LanguageDriverRegistry getLanguageRegistry() {
        return languageRegistry;
    }

    public String getDatabaseId() {
        return databaseId;
    }

    public void setDatabaseId(String databaseId) {
        this.databaseId = databaseId;
    }
}