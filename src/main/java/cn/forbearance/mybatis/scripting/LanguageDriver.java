package cn.forbearance.mybatis.scripting;

import cn.forbearance.mybatis.executor.parameter.ParameterHandler;
import cn.forbearance.mybatis.mapping.BoundSql;
import cn.forbearance.mybatis.mapping.MappedStatement;
import cn.forbearance.mybatis.mapping.SqlSource;
import cn.forbearance.mybatis.session.Configuration;
import org.dom4j.Element;

/**
 * 脚本语言驱动
 *
 * @author cristina
 */
public interface LanguageDriver {

    /**
     * #
     *
     * @param configuration
     * @param script
     * @param parameterType
     * @return
     */
    SqlSource createSqlSource(Configuration configuration, Element script, Class<?> parameterType);

    /**
     * 创建参数处理器
     *
     * @param mappedStatement
     * @param parameterObject
     * @param boundSql
     * @return
     */
    ParameterHandler createParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql);
}
