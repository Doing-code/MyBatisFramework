package cn.forbearance.mybatis.scripting.xmltags;

import cn.forbearance.mybatis.executor.parameter.ParameterHandler;
import cn.forbearance.mybatis.mapping.BoundSql;
import cn.forbearance.mybatis.mapping.MappedStatement;
import cn.forbearance.mybatis.mapping.SqlSource;
import cn.forbearance.mybatis.scripting.LanguageDriver;
import cn.forbearance.mybatis.scripting.defaults.DefaultParameterHandler;
import cn.forbearance.mybatis.scripting.defaults.RawSqlSource;
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
        // 用XML脚本构建器解析
        XmlScriptBuilder builder = new XmlScriptBuilder(configuration, script, parameterType);
        return builder.parseScriptNode();
    }

    @Override
    public SqlSource createSqlSource(Configuration configuration, String script, Class<?> parameterType) {
        return new RawSqlSource(configuration, script, parameterType);
    }

    @Override
    public ParameterHandler createParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql) {
        return new DefaultParameterHandler(mappedStatement, parameterObject, boundSql);
    }
}
