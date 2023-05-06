package cn.forbearance.mybatis.builder.xml;

import cn.forbearance.mybatis.builder.BaseBuilder;
import cn.forbearance.mybatis.mapping.MappedStatement;
import cn.forbearance.mybatis.mapping.SqlCommandType;
import cn.forbearance.mybatis.mapping.SqlSource;
import cn.forbearance.mybatis.scripting.LanguageDriver;
import cn.forbearance.mybatis.session.Configuration;
import org.dom4j.Element;

import java.util.Locale;


/**
 * XML语句映射器
 *
 * @author cristina
 */
public class XmlStatementBuilder extends BaseBuilder {

    private String currentNamespace;

    private Element element;

    public XmlStatementBuilder(Configuration configuration, Element element, String currentNamespace) {
        super(configuration);
        this.element = element;
        this.currentNamespace = currentNamespace;
    }

    /**
     * //解析语句(select|insert|update|delete)
     * //<select
     * //  id="selectPerson"
     * //  parameterType="int"
     * //  parameterMap="deprecated"
     * //  resultType="hashmap"
     * //  resultMap="personResultMap"
     * //  flushCache="false"
     * //  useCache="true"
     * //  timeout="10000"
     * //  fetchSize="256"
     * //  statementType="PREPARED"
     * //  resultSetType="FORWARD_ONLY">
     * //  SELECT * FROM PERSON WHERE ID = #{id}
     * //</select>
     */
    public void parseStatementNode() {
        String id = element.attributeValue("id");
        String parameterType = element.attributeValue("parameterType");
        Class<?> parameterTypeClass = resolveAlias(parameterType);
        String resultType = element.attributeValue("resultType");
        Class<?> resultTypeClass = resolveAlias(resultType);
        // (select|update|insert|delete)
        String nodeName = element.getName();
        SqlCommandType sqlCommandType = SqlCommandType.valueOf(nodeName.toUpperCase(Locale.ENGLISH));
        // 获取默认语言驱动器
        Class<?> landClass = configuration.getLanguageRegistry().getDefaultDriverClass();
        LanguageDriver langDriver = configuration.getLanguageRegistry().getDriver(landClass);

        SqlSource sqlSource = langDriver.createSqlSource(configuration, element, parameterTypeClass);
        MappedStatement mappedStatement = new MappedStatement.Builder(configuration,
                currentNamespace + "." + id,
                sqlCommandType,
                sqlSource,
                resultTypeClass).build();
        // 添加【解析SQL】
        configuration.addMappedStatement(mappedStatement);
    }
}
