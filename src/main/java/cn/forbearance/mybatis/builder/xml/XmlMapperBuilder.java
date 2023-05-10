package cn.forbearance.mybatis.builder.xml;

import cn.forbearance.mybatis.builder.BaseBuilder;
import cn.forbearance.mybatis.builder.MapperBuilderAssistant;
import cn.forbearance.mybatis.builder.ResultMapResolver;
import cn.forbearance.mybatis.cache.Cache;
import cn.forbearance.mybatis.io.Resources;
import cn.forbearance.mybatis.mapping.ResultFlag;
import cn.forbearance.mybatis.mapping.ResultMap;
import cn.forbearance.mybatis.mapping.ResultMapping;
import cn.forbearance.mybatis.session.Configuration;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * XML映射构建起
 *
 * @author cristina
 */
public class XmlMapperBuilder extends BaseBuilder {

    private Element element;

    private String resource;

    /**
     * 映射器建造助手
     */
    private MapperBuilderAssistant builderAssistant;

    public XmlMapperBuilder(InputStream inputStream, Configuration configuration, String resource) throws DocumentException {
        this(new SAXReader().read(inputStream), configuration, resource);
    }

    private XmlMapperBuilder(Document document, Configuration configuration, String resource) {
        super(configuration);
        this.element = document.getRootElement();
        this.resource = resource;
        this.builderAssistant = new MapperBuilderAssistant(configuration, resource);
    }

    /**
     * 解析
     *
     * @throws Exception
     */
    public void parse() throws Exception {
        // 防止重复加载
        if (!configuration.isResourceLoaded(resource)) {
            configurationElement(element);
            // 标记已加载
            configuration.addLoadedResource(resource);
            // 绑定映射器到 namespace
            configuration.addMapper(Resources.classForName(builderAssistant.getCurrentNamespace()));
        }
    }

    /**
     * // 配置mapper元素
     * // <mapper namespace="org.mybatis.example.BlogMapper">
     * //   <select id="selectBlog" parameterType="int" resultType="Blog">
     * //    select * from Blog where id = #{id}
     * //   </select>
     * // </mapper>
     *
     * @param element
     */
    private void configurationElement(Element element) {
        String namespace = element.attributeValue("namespace");
        if ("".equals(namespace)) {
            throw new RuntimeException("Mapper's namespace cannot be empty");
        }
        builderAssistant.setCurrentNamespace(namespace);

        cacheElement(element.element("cache"));

        resultMapElements(element.elements("resultMap"));

        // select|insert|update|delete
        buildStatementFromContext(element.elements("select"),
                element.elements("insert"),
                element.elements("update"),
                element.elements("delete"));
    }

    private void cacheElement(Element context) {
        if (context == null) return;
        // 基础配置信息
        String type = context.attributeValue("type", "PERPETUAL");
        Class<? extends Cache> typeClass = typeAliasRegistry.resolveAlias(type);
        // 缓存队列 FIFO
        String eviction = context.attributeValue("eviction", "FIFO");
        Class<? extends Cache> evictionClass = typeAliasRegistry.resolveAlias(eviction);
        Long flushInterval = Long.valueOf(context.attributeValue("flushInterval"));
        Integer size = Integer.valueOf(context.attributeValue("size"));
        boolean readWrite = !Boolean.parseBoolean(context.attributeValue("readOnly", "false"));
        boolean blocking = !Boolean.parseBoolean(context.attributeValue("blocking", "false"));

        // 解析额外属性信息；<property name="cacheFile" value="/tmp/xxx-cache.tmp"/>
        List<Element> elements = context.elements();
        Properties props = new Properties();
        for (Element element : elements) {
            props.setProperty(element.attributeValue("name"), element.attributeValue("value"));
        }
        // 构建缓存
        builderAssistant.useNewCache(typeClass, evictionClass, flushInterval, size, readWrite, blocking, props);
    }

    private void resultMapElements(List<Element> element) {
        for (Element node : element) {
            try {
                resultMapElement(node, Collections.emptyList());
            } catch (Exception e) {
                // ignore, it will be retried
            }
        }
    }

    /**
     * <resultMap id="activityMap" type="cn.bugstack.mybatis.test.po.Activity">
     * <id column="id" property="id"/>
     * <result column="activity_id" property="activityId"/>
     * <result column="activity_name" property="activityName"/>
     * <result column="activity_desc" property="activityDesc"/>
     * <result column="create_time" property="createTime"/>
     * <result column="update_time" property="updateTime"/>
     * </resultMap>
     *
     * @param resultMapNode
     * @param additionalResultMappings
     * @throws Exception
     */
    private ResultMap resultMapElement(Element resultMapNode, List<ResultMapping> additionalResultMappings) throws Exception {
        String id = resultMapNode.attributeValue("id");
        String type = resultMapNode.attributeValue("type");
        Class<?> typeClass = resolveAlias(type);

        List<ResultMapping> resultMappings = new ArrayList<>();
        resultMappings.addAll(additionalResultMappings);

        List<Element> resultChildren = resultMapNode.elements();
        for (Element child : resultChildren) {
            List<ResultFlag> flags = new ArrayList<>();
            if ("id".equals(child.getName())) {
                flags.add(ResultFlag.ID);
            }
            resultMappings.add(buildResultMappingFromContext(child, typeClass, flags));
        }

        ResultMapResolver resolver = new ResultMapResolver(builderAssistant, id, typeClass, resultMappings);
        return resolver.resolve();
    }

    /**
     * <id column="id" property="id"/>
     * <result column="activity_id" property="activityId"/>
     *
     * @param context
     * @param resultType
     * @param flags
     * @return
     * @throws Exception
     */
    private ResultMapping buildResultMappingFromContext(Element context, Class<?> resultType, List<ResultFlag> flags) throws Exception {
        String property = context.attributeValue("property");
        String column = context.attributeValue("column");
        return builderAssistant.buildResultMapping(resultType, property, column, flags);
    }

    /**
     * select|insert|update|delete
     *
     * @param lists
     */
    private void buildStatementFromContext(List<Element>... lists) {
        for (List<Element> list : lists) {
            for (Element element : list) {
                final XmlStatementBuilder builder = new XmlStatementBuilder(configuration, builderAssistant, element);
                builder.parseStatementNode();
            }
        }
    }
}
