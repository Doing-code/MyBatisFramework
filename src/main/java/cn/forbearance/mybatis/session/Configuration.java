package cn.forbearance.mybatis.session;

import cn.forbearance.mybatis.binding.MapperRegistry;
import cn.forbearance.mybatis.cache.Cache;
import cn.forbearance.mybatis.cache.decorators.FifoCache;
import cn.forbearance.mybatis.cache.impl.PerpetualCache;
import cn.forbearance.mybatis.datasource.druid.DruidDataSourceFactory;
import cn.forbearance.mybatis.datasource.pooled.PooledDataSourceFactory;
import cn.forbearance.mybatis.datasource.unpooled.UnPooledDataSourceFactory;
import cn.forbearance.mybatis.executor.CachingExecutor;
import cn.forbearance.mybatis.executor.Executor;
import cn.forbearance.mybatis.executor.SimpleExecutor;
import cn.forbearance.mybatis.executor.keygen.KeyGenerator;
import cn.forbearance.mybatis.executor.parameter.ParameterHandler;
import cn.forbearance.mybatis.executor.resultset.DefaultResultSetHandler;
import cn.forbearance.mybatis.executor.resultset.ResultSetHandler;
import cn.forbearance.mybatis.executor.statement.PreparedStatementHandler;
import cn.forbearance.mybatis.executor.statement.StatementHandler;
import cn.forbearance.mybatis.mapping.BoundSql;
import cn.forbearance.mybatis.mapping.Environment;
import cn.forbearance.mybatis.mapping.MappedStatement;
import cn.forbearance.mybatis.mapping.ResultMap;
import cn.forbearance.mybatis.plugin.Interceptor;
import cn.forbearance.mybatis.plugin.InterceptorChain;
import cn.forbearance.mybatis.refection.MetaObject;
import cn.forbearance.mybatis.refection.factory.DefaultObjectFactory;
import cn.forbearance.mybatis.refection.factory.ObjectFactory;
import cn.forbearance.mybatis.refection.wapper.DefaultObjectWrapperFactory;
import cn.forbearance.mybatis.refection.wapper.ObjectWrapperFactory;
import cn.forbearance.mybatis.scripting.LanguageDriver;
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
     * 缓存机制，默认不配置的情况是 SESSION
     */
    protected LocalCacheScope localCacheScope = LocalCacheScope.SESSION;

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

    protected boolean useGeneratedKeys = false;

    /**
     * 默认启用缓存
     */
    protected boolean cacheEnabled = true;

    /**
     * 缓存
     */
    protected final Map<String, Cache> caches = new HashMap<>();

    /**
     * 存放键值生成器
     */
    protected final Map<String, KeyGenerator> keyGenerators = new HashMap<>();

    /**
     * 结果映射
     */
    protected final Map<String, ResultMap> resultMaps = new HashMap<>();

    /**
     * 插件拦截器链
     */
    protected final InterceptorChain interceptorChain = new InterceptorChain();

    public Configuration() {
        typeAliasRegistry.registerAlias("JDBC", JdbcTransactionFactory.class);
        typeAliasRegistry.registerAlias("DRUID", DruidDataSourceFactory.class);
        typeAliasRegistry.registerAlias("UNPOOLED", UnPooledDataSourceFactory.class);
        typeAliasRegistry.registerAlias("POOLED", PooledDataSourceFactory.class);

        languageRegistry.setDefaultDriverClass(XmlLanguageDriver.class);

        typeAliasRegistry.registerAlias("PERPETUAL", PerpetualCache.class);
        typeAliasRegistry.registerAlias("FIFO", FifoCache.class);
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

    public ResultSetHandler newResultSetHandler(Executor executor, MappedStatement mappedStatement, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
        return new DefaultResultSetHandler(executor, mappedStatement, resultHandler, rowBounds, boundSql);
    }

    public Executor newExecutor(Transaction transaction) {
        Executor executor = new SimpleExecutor(this, transaction);
        if (cacheEnabled) {
            executor = new CachingExecutor(executor);
        }
        return executor;
    }

    public StatementHandler newStatementHandler(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
        // org.apache.ibatis.executor.statement.RoutingStatementHandler#RoutingStatementHandler
        // 创建语句处理器，Mybatis 这里加了路由 STATEMENT、PREPARED、CALLABLE 我们默认只根据预处理进行实例化
        StatementHandler statementHandler = new PreparedStatementHandler(executor, ms, parameter, rowBounds, resultHandler, boundSql);
        statementHandler = (StatementHandler) interceptorChain.pluginAll(statementHandler);
        return statementHandler;
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

    /**
     * 创建参数处理器， 插件的一些参数，也是在这里处理，暂时不添加这部分内容 interceptorChain.pluginAll(parameterHandler);
     *
     * @param mappedStatement
     * @param parameterObject
     * @param boundSql
     * @return
     */
    public ParameterHandler newParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql) {
        return mappedStatement.getLang().createParameterHandler(mappedStatement, parameterObject, boundSql);
    }

    public LanguageDriver getDefaultScriptingLanguageInstance() {
        return languageRegistry.getDefaultDriver();
    }

    public ObjectFactory getObjectFactory() {
        return objectFactory;
    }

    public ResultMap getResultMap(String id) {
        return resultMaps.get(id);
    }

    public void addResultMap(ResultMap resultMap) {
        resultMaps.put(resultMap.getId(), resultMap);
    }

    public void addKeyGenerator(String id, KeyGenerator keyGenerator) {
        keyGenerators.put(id, keyGenerator);
    }

    public KeyGenerator getKeyGenerator(String id) {
        return keyGenerators.get(id);
    }

    public boolean hasKeyGenerator(String id) {
        return keyGenerators.containsKey(id);
    }

    public boolean isUseGeneratedKeys() {
        return useGeneratedKeys;
    }

    public void setUseGeneratedKeys(boolean useGeneratedKeys) {
        this.useGeneratedKeys = useGeneratedKeys;
    }

    public void addInterceptor(Interceptor interceptorInstance) {
        interceptorChain.addInterceptor(interceptorInstance);
    }

    public LocalCacheScope getLocalCacheScope() {
        return localCacheScope;
    }

    public void setLocalCacheScope(LocalCacheScope localCacheScope) {
        this.localCacheScope = localCacheScope;
    }

    public void setCacheEnabled(boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
    }

    public void addCache(Cache cache) {
        caches.put(cache.getId(), cache);
    }

    public Cache getCache(String id) {
        return caches.get(id);
    }
}
