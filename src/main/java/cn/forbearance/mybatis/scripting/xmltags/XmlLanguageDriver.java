package cn.forbearance.mybatis.scripting.xmltags;

import cn.forbearance.mybatis.mapping.SqlSource;
import cn.forbearance.mybatis.scripting.LanguageDriver;
import cn.forbearance.mybatis.session.Configuration;
import org.dom4j.Element;

/**
 * XML语言驱动器
 *
 * @author cristina
 */
public class XmlLanguageDriver implements LanguageDriver {

    @Override
    public SqlSource createSqlSource(Configuration configuration, Element script, Class<?> parameterType) {
        XmlScriptBuilder builder = new XmlScriptBuilder(configuration, script, parameterType);
        return builder.parseScriptNode();
    }
}
