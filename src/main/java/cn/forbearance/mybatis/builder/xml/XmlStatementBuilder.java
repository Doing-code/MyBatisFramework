package cn.forbearance.mybatis.builder.xml;

import cn.forbearance.mybatis.builder.BaseBuilder;
import cn.forbearance.mybatis.builder.MapperBuilderAssistant;
import cn.forbearance.mybatis.executor.keygen.Jdbc3KeyGenerator;
import cn.forbearance.mybatis.executor.keygen.KeyGenerator;
import cn.forbearance.mybatis.executor.keygen.NoKeyGenerator;
import cn.forbearance.mybatis.executor.keygen.SelectKeyGenerator;
import cn.forbearance.mybatis.mapping.MappedStatement;
import cn.forbearance.mybatis.mapping.SqlCommandType;
import cn.forbearance.mybatis.mapping.SqlSource;
import cn.forbearance.mybatis.scripting.LanguageDriver;
import cn.forbearance.mybatis.session.Configuration;
import org.dom4j.Element;

import java.util.List;
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

        boolean isSelect = sqlCommandType == SqlCommandType.SELECT;
        boolean flushCache = Boolean.parseBoolean(element.attributeValue("flushCache", String.valueOf(!isSelect)));
        boolean useCache = Boolean.parseBoolean(element.attributeValue("useCache", String.valueOf(isSelect)));

        // 获取默认语言驱动器
        Class<?> landClass = configuration.getLanguageRegistry().getDefaultDriverClass();
        LanguageDriver langDriver = configuration.getLanguageRegistry().getDriver(landClass);

        // 解析<selectKey>
        processSelectKeyNodes(id, parameterTypeClass, langDriver);
        // 属性标记【仅对 insert 有用】, MyBatis 会通过 getGeneratedKeys 或者通过 insert 语句的 selectKey 子元素设置它的值 step-14 新增
        String keyProperty = element.attributeValue("keyProperty");
        KeyGenerator keyGenerator;
        String keyStatementId = id + SelectKeyGenerator.SELECT_KEY_SUFFIX;
        keyStatementId = builderAssistant.applyCurrentNamespace(keyStatementId, true);

        if (configuration.hasKeyGenerator(keyStatementId)) {
            keyGenerator = configuration.getKeyGenerator(keyStatementId);
        } else {
            keyGenerator = configuration.isUseGeneratedKeys() && SqlCommandType.INSERT.equals(sqlCommandType) ? new Jdbc3KeyGenerator() : new NoKeyGenerator();
        }

        // 解析成SqlSource，DynamicSqlSource/RawSqlSource【#{}替换为?】
        SqlSource sqlSource = langDriver.createSqlSource(configuration, element, parameterTypeClass);
        builderAssistant.addMappedStatement(id,
                sqlSource,
                sqlCommandType,
                parameterTypeClass,
                resultMap,
                resultTypeClass,
                flushCache,
                useCache,
                keyGenerator,
                keyProperty,
                langDriver);
    }

    private void processSelectKeyNodes(String id, Class<?> parameterTypeClass, LanguageDriver langDriver) {
        List<Element> selectKeyNodes = element.elements("selectKey");
        parseSelectKeyNodes(id, selectKeyNodes, parameterTypeClass, langDriver);
    }

    private void parseSelectKeyNodes(String parentId, List<Element> list, Class<?> parameterTypeClass, LanguageDriver languageDriver) {
        for (Element nodeToHandle : list) {
            String id = parentId + SelectKeyGenerator.SELECT_KEY_SUFFIX;
            parseSelectKeyNode(id, nodeToHandle, parameterTypeClass, languageDriver);
        }
    }

    /**
     * <selectKey keyProperty="id" order="AFTER" resultType="long">
     * SELECT LAST_INSERT_ID()
     * </selectKey>
     */
    private void parseSelectKeyNode(String id, Element nodeToHandle, Class<?> parameterTypeClass, LanguageDriver langDriver) {
        String resultType = nodeToHandle.attributeValue("resultType");
        Class<?> resultTypeClass = resolveClass(resultType);
        boolean executeBefore = "BEFORE".equals(nodeToHandle.attributeValue("order", "AFTER"));
        String keyProperty = nodeToHandle.attributeValue("keyProperty");

        // default
        String resultMap = null;
        boolean flushCache = false;
        boolean useCache = false;
        KeyGenerator keyGenerator = new NoKeyGenerator();

        // 解析成SqlSource，DynamicSqlSource/RawSqlSource
        SqlSource sqlSource = langDriver.createSqlSource(configuration, nodeToHandle, parameterTypeClass);
        SqlCommandType sqlCommandType = SqlCommandType.SELECT;

        // 调用助手类
        builderAssistant.addMappedStatement(id,
                sqlSource,
                sqlCommandType,
                parameterTypeClass,
                null,
                resultTypeClass,
                flushCache,
                useCache,
                keyGenerator,
                keyProperty,
                langDriver);

        // 给id加上namespace前缀
        id = builderAssistant.applyCurrentNamespace(id, false);

        // 存放键值生成器配置
        MappedStatement keyStatement = configuration.getMappedStatement(id);
        configuration.addKeyGenerator(id, new SelectKeyGenerator(executeBefore, keyStatement));
    }

}
