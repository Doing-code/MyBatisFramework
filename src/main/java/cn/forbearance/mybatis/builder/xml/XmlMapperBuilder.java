package cn.forbearance.mybatis.builder.xml;

import cn.forbearance.mybatis.builder.BaseBuilder;
import cn.forbearance.mybatis.io.Resources;
import cn.forbearance.mybatis.session.Configuration;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.InputStream;
import java.util.List;

/**
 * XML映射构建起
 *
 * @author cristina
 */
public class XmlMapperBuilder extends BaseBuilder {

    private Element element;

    private String resource;

    private String currentNamespace;

    public XmlMapperBuilder(InputStream inputStream, Configuration configuration, String resource) throws DocumentException {
        this(new SAXReader().read(inputStream), configuration, resource);
    }

    private XmlMapperBuilder(Document document, Configuration configuration, String resource) {
        super(configuration);
        this.element = document.getRootElement();
        this.resource = resource;
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
            configuration.addMapper(Resources.classForName(currentNamespace));
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
        currentNamespace = element.attributeValue("namespace");
        if ("".equals(currentNamespace)) {
            throw new RuntimeException("Mapper's namespace cannot be empty");
        }
        buildStatementFromContext(element.elements("select"));
    }

    /**
     * select|insert|update|delete
     *
     * @param list
     */
    private void buildStatementFromContext(List<Element> list) {
        for (Element element : list) {
            final XmlStatementBuilder builder = new XmlStatementBuilder(configuration, element, currentNamespace);
            builder.parseStatementNode();
        }
    }
}