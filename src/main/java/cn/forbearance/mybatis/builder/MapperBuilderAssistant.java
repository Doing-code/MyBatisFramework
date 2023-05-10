package cn.forbearance.mybatis.builder;

import cn.forbearance.mybatis.cache.Cache;
import cn.forbearance.mybatis.cache.decorators.FifoCache;
import cn.forbearance.mybatis.cache.impl.PerpetualCache;
import cn.forbearance.mybatis.executor.keygen.KeyGenerator;
import cn.forbearance.mybatis.mapping.*;
import cn.forbearance.mybatis.refection.MetaClass;
import cn.forbearance.mybatis.scripting.LanguageDriver;
import cn.forbearance.mybatis.session.Configuration;
import cn.forbearance.mybatis.type.TypeHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 映射构建器助手，建造者
 * org.apache.ibatis.builder.MapperBuilderAssistant
 *
 * @author cristina
 */
public class MapperBuilderAssistant extends BaseBuilder {

    private String currentNamespace;
    private String resource;
    private Cache currentCache;

    public MapperBuilderAssistant(Configuration configuration, String resource) {
        super(configuration);
        this.resource = resource;
    }

    public String applyCurrentNamespace(String base, boolean isReference) {
        if (base == null) {
            return null;
        }
        if (isReference) {
            if (base.contains(".")) return base;
        } else {
            if (base.startsWith(currentNamespace + ".")) {
                return base;
            }
            if (base.contains(".")) {
                throw new RuntimeException("Dots are not allowed in element names, please remove it from " + base);
            }
        }
        return currentNamespace + "." + base;
    }

    /**
     * 添加映射器语句
     *
     * @param id
     * @param sqlSource
     * @param sqlCommandType
     * @param parameterType
     * @param resultMap
     * @param resultType
     * @param keyGenerator
     * @param keyProperty
     * @param lang
     * @return
     */
    public MappedStatement addMappedStatement(String id,
                                              SqlSource sqlSource,
                                              SqlCommandType sqlCommandType,
                                              Class<?> parameterType,
                                              String resultMap,
                                              Class<?> resultType,
                                              boolean flushCache,
                                              boolean useCache,
                                              KeyGenerator keyGenerator,
                                              String keyProperty,
                                              LanguageDriver lang) {
        // 给id加上 namespace 前缀
        id = applyCurrentNamespace(id, false);
        MappedStatement.Builder statementBuilder = new MappedStatement.Builder(configuration,
                id,
                sqlCommandType,
                sqlSource,
                resultType);

        statementBuilder.resource(resource);
        statementBuilder.keyGenerator(keyGenerator);
        statementBuilder.keyProperty(keyProperty);

        //是否是select语句
        boolean isSelect = sqlCommandType == SqlCommandType.SELECT;
        setStatementCache(isSelect, flushCache, useCache, currentCache, statementBuilder);

        // 结果映射，MappedStatement#resultMaps
        setStatementResultMap(resultMap, resultType, statementBuilder);

        MappedStatement mappedStatement = statementBuilder.build();
        // 映射语句信息，建造完成存放到配置项中
        configuration.addMappedStatement(mappedStatement);
        return mappedStatement;
    }

    private void setStatementCache(
            boolean isSelect,
            boolean flushCache,
            boolean useCache,
            Cache cache,
            MappedStatement.Builder statementBuilder) {
        flushCache = valueOrDefault(flushCache, !isSelect);
        useCache = valueOrDefault(useCache, isSelect);
        statementBuilder.flushCacheRequired(flushCache);
        statementBuilder.useCache(useCache);
        statementBuilder.cache(cache);
    }

    private void setStatementResultMap(String resultMap,
                                       Class<?> resultType,
                                       MappedStatement.Builder statementBuilder) {
        // 因为暂时还没有在 Mapper XML 中配置 Map 返回结果，所以这里返回的是 null
        resultMap = applyCurrentNamespace(resultMap, true);

        List<ResultMap> resultMaps = new ArrayList<>();
        if (resultMap != null) {
            String[] resultMapNames = resultMap.split(",");
            for (String resultMapName : resultMapNames) {
                resultMaps.add(configuration.getResultMap(resultMapName));
            }
        } else {
            /*
             * 通常使用 resultType 即可满足大部分场景
             * <select id="queryUserInfoById" resultType="cn.forbearance.mybatis.test.po.User">
             * 使用 resultType 的情况下，Mybatis 会自动创建一个 ResultMap，基于属性名称映射列到 JavaBean 的属性上。
             */
            ResultMap.Builder inlineResultMapBuilder = new ResultMap.Builder(
                    configuration,
                    statementBuilder.id() + "-Inline",
                    resultType,
                    new ArrayList<>());
            resultMaps.add(inlineResultMapBuilder.build());
        }
        statementBuilder.resultMaps(resultMaps);
    }

    public String getCurrentNamespace() {
        return currentNamespace;
    }

    public void setCurrentNamespace(String currentNamespace) {
        this.currentNamespace = currentNamespace;
    }

    public ResultMap addResultMap(String id, Class<?> type, List<ResultMapping> resultMappings) {
        // 补全ID全路径，如：cn.forbearance.mybatis.test.dao.IUserDao + activityMap
        id = applyCurrentNamespace(id, false);

        ResultMap.Builder inlineResultMapBuilder = new ResultMap.Builder(
                configuration,
                id,
                type,
                resultMappings);

        ResultMap resultMap = inlineResultMapBuilder.build();
        configuration.addResultMap(resultMap);
        return resultMap;
    }

    public ResultMapping buildResultMapping(Class<?> resultType,
                                            String property,
                                            String column,
                                            List<ResultFlag> flags) {
        Class<?> javaTypeClass = resolveResultJavaType(resultType, property, null);
        TypeHandler<?> typeHandlerInstance = resolveTypeHandler(javaTypeClass, null);

        ResultMapping.Builder builder = new ResultMapping.Builder(configuration, property, column, javaTypeClass);
        builder.typeHandler(typeHandlerInstance);
        builder.flags(flags);
        return builder.build();
    }

    private Class<?> resolveResultJavaType(Class<?> resultType, String property, Class<?> javaType) {
        if (javaType == null && property != null) {
            MetaClass metaResultType = MetaClass.forClass(resultType);
            javaType = metaResultType.getSetterType(property);
        }
        if (javaType == null) {
            return Object.class;
        }
        return javaType;
    }

    public Cache useNewCache(Class<? extends Cache> typeClass,
                             Class<? extends Cache> evictionClass,
                             Long flushInterval,
                             Integer size,
                             boolean readWrite,
                             boolean blocking,
                             Properties props) {
        // 判断为null，则用默认值
        typeClass = valueOrDefault(typeClass, PerpetualCache.class);
        evictionClass = valueOrDefault(evictionClass, FifoCache.class);

        // 建造者模式构建 Cache [currentNamespace=cn.forbearance.mybatis.test.dao.IActivityDao]
        Cache cache = new CacheBuilder(currentNamespace)
                .implementation(typeClass)
                .addDecorator(evictionClass)
                .clearInterval(flushInterval)
                .size(size)
                .readWrite(readWrite)
                .blocking(blocking)
                .properties(props)
                .build();

        // 添加缓存
        configuration.addCache(cache);
        currentCache = cache;
        return cache;
    }

    private <T> T valueOrDefault(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }
}
