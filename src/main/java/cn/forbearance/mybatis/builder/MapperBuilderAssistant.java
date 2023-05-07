package cn.forbearance.mybatis.builder;

import cn.forbearance.mybatis.mapping.MappedStatement;
import cn.forbearance.mybatis.mapping.ResultMap;
import cn.forbearance.mybatis.mapping.SqlCommandType;
import cn.forbearance.mybatis.mapping.SqlSource;
import cn.forbearance.mybatis.scripting.LanguageDriver;
import cn.forbearance.mybatis.session.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * 映射构建器助手，建造者
 * org.apache.ibatis.builder.MapperBuilderAssistant
 *
 * @author cristina
 */
public class MapperBuilderAssistant extends BaseBuilder {

    private String currentNamespace;

    private String resource;

    public MapperBuilderAssistant(Configuration configuration, String resource) {
        super(configuration);
        this.resource = resource;
    }

    public String applyCurrentNamespace(String base, boolean isReference) {
        if (base == null) {
            return null;
        }
        if (isReference && base.contains(".")) {
            return base;
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
     * @param lang
     * @return
     */
    public MappedStatement addMappedStatement(String id,
                                               SqlSource sqlSource,
                                               SqlCommandType sqlCommandType,
                                               Class<?> parameterType,
                                               String resultMap,
                                               Class<?> resultType,
                                               LanguageDriver lang) {
        // 给id加上 namespace 前缀
        id = applyCurrentNamespace(id, false);
        MappedStatement.Builder statementBuilder = new MappedStatement.Builder(configuration,
                id,
                sqlCommandType,
                sqlSource,
                resultType);

        // 结果映射，MappedStatement#resultMaps
        setStatementResultMap(resultMap, resultType, statementBuilder);

        MappedStatement mappedStatement = statementBuilder.build();
        // 映射语句信息，建造完成存放到配置项中
        configuration.addMappedStatement(mappedStatement);
        return mappedStatement;
    }

    private void setStatementResultMap(String resultMap,
                                       Class<?> resultType,
                                       MappedStatement.Builder statementBuilder) {
        // 因为暂时还没有在 Mapper XML 中配置 Map 返回结果，所以这里返回的是 null
        resultMap = applyCurrentNamespace(resultMap, true);

        List<ResultMap> resultMaps = new ArrayList<>();
        if (resultMap != null) {
            // TODO：暂无Map结果映射配置，
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
}
