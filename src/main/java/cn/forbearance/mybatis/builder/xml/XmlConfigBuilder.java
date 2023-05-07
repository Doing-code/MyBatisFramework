package cn.forbearance.mybatis.builder.xml;

import cn.forbearance.mybatis.datasource.DataSourceFactory;
import cn.forbearance.mybatis.builder.BaseBuilder;
import cn.forbearance.mybatis.io.Resources;
import cn.forbearance.mybatis.mapping.BoundSql;
import cn.forbearance.mybatis.mapping.Environment;
import cn.forbearance.mybatis.mapping.MappedStatement;
import cn.forbearance.mybatis.mapping.SqlCommandType;
import cn.forbearance.mybatis.session.Configuration;
import cn.forbearance.mybatis.transaction.TransactionFactory;
import jdk.internal.util.xml.impl.Input;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.InputSource;

import javax.sql.DataSource;
import java.io.InputStream;
import java.io.Reader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * XML 配置构建器，建造者模式
 *
 * @author cristina
 */
public class XmlConfigBuilder extends BaseBuilder {

    private Element root;

    public XmlConfigBuilder(Reader reader) {
        // 初始化 Configuration
        super(new Configuration());
        // dom4j 处理 xml
        SAXReader saxReader = new SAXReader();
        try {
            Document document = saxReader.read(new InputSource(reader));
            root = document.getRootElement();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    /**
     * 解析配置；类型别名、插件、对象工厂、对象包装工厂、设置、环境、类型转换、映射器
     *
     * @return
     */
    public Configuration parse() {
        try {
            environmentElement(root.element("environments"));
            // 解析映射器
            mapperElement(root.element("mappers"));
        } catch (Exception e) {
            throw new RuntimeException("Error parsing SQL Mapper Configuration. Cause: " + e, e);
        }
        return configuration;
    }

    /**
     * <environments default="development">
     * <environment id="development">
     * <transactionManager type="JDBC">
     * <property name="..." value="..."/>
     * </transactionManager>
     * <dataSource type="POOLED">
     * <property name="driver" value="${driver}"/>
     * <property name="url" value="${url}"/>
     * <property name="username" value="${username}"/>
     * <property name="password" value="${password}"/>
     * </dataSource>
     * </environment>
     * </environments>
     */
    private void environmentElement(Element context) throws Exception {
        String environment = context.attributeValue("default");
        List<Element> environments = context.elements("environment");
        for (Element e : environments) {
            String id = e.attributeValue("id");
            if (!environment.equals(id)) {
                continue;
            }
            // 事务管理器
            TransactionFactory tx = (TransactionFactory) typeAliasRegistry.resolveAlias(e.element("transactionManager").attributeValue("type")).newInstance();
            // 数据源
            Element dataSourceElement = e.element("dataSource");
            DataSourceFactory dataSourceFactory = (DataSourceFactory) typeAliasRegistry.resolveAlias(dataSourceElement.attributeValue("type")).newInstance();
            List<Element> properties = dataSourceElement.elements("property");
            Properties props = new Properties();
            for (Element property : properties) {
                props.setProperty(property.attributeValue("name"), property.attributeValue("value"));
            }
            dataSourceFactory.setProperties(props);
            DataSource dataSource = dataSourceFactory.getDataSource();
            // 构建 Environment
            Environment.Builder environmentBuilder = new Environment.Builder(id)
                    .transactionFactory(tx)
                    .dataSource(dataSource);
            configuration.setEnvironment(environmentBuilder.build());
        }
    }

    /**
     * <mappers>
     * <mapper resource="org/mybatis/builder/AuthorMapper.xml"/>
     * <mapper resource="org/mybatis/builder/BlogMapper.xml"/>
     * <mapper resource="org/mybatis/builder/PostMapper.xml"/>
     *
     * <mapper class="cn.forbearance.mybatis.test.dao.IUserDao"/>
     * </mappers>
     */
    private void mapperElement(Element element) throws Exception {
        List<Element> mappers = element.elements("mapper");
        for (Element e : mappers) {
            String resource = e.attributeValue("resource");
            String mapperClass = e.attributeValue("class");
            if (resource != null && mapperClass == null) {
                // XML 解析
                InputStream is = Resources.getResourceAsStream(resource);
                // 每个 mapper 都会对应一个 XmlMapperBuilder 来解析
                XmlMapperBuilder parse = new XmlMapperBuilder(is, configuration, resource);
                parse.parse();
            } else if (resource == null && mapperClass != null) {
                // 注解解析
                Class<?> mapperInterface = Resources.classForName(mapperClass);
                configuration.addMapper(mapperInterface);
            }
        }
    }
}
