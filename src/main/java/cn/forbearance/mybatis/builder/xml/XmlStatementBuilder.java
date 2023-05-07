package cn.forbearance.mybatis.builder.xml;

import cn.forbearance.mybatis.builder.BaseBuilder;
import cn.forbearance.mybatis.builder.MapperBuilderAssistant;
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

    private MapperBuilderAssistant builderAssistant;

    private Element element;

    public XmlStatementBuilder(Configuration configuration, MapperBuilderAssistant builderAssistant, Element element) {
        super(configuration);
        this.element = element;
        this.builderAssistant = builderAssistant;
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
        // 参数类型
        String parameterType = element.attributeValue("parameterType");
        Class<?> parameterTypeClass = resolveAlias(parameterType);
        // resultMap
        String resultMap = element.attributeValue("resultMap");
        // 结果类型
        String resultType = element.attributeValue("resultType");
        Class<?> resultTypeClass = resolveAlias(resultType);
        // (select|update|insert|delete)
        String nodeName = element.getName();
        SqlCommandType sqlCommandType = SqlCommandType.valueOf(nodeName.toUpperCase(Locale.ENGLISH));

        // 获取默认语言驱动器
        Class<?> landClass = configuration.getLanguageRegistry().getDefaultDriverClass();
        LanguageDriver langDriver = configuration.getLanguageRegistry().getDriver(landClass);

        // 解析成SqlSource，DynamicSqlSource/RawSqlSource【#{}替换为?】
        SqlSource sqlSource = langDriver.createSqlSource(configuration, element, parameterTypeClass);
        builderAssistant.addMappedStatement(id,
                sqlSource,
                sqlCommandType,
                parameterTypeClass,
                resultMap,
                resultTypeClass,
                langDriver);
    }
}
