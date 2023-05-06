package cn.forbearance.mybatis.scripting.defaults;

import cn.forbearance.mybatis.builder.SqlSourceBuilder;
import cn.forbearance.mybatis.mapping.BoundSql;
import cn.forbearance.mybatis.mapping.SqlSource;
import cn.forbearance.mybatis.scripting.xmltags.DynamicContext;
import cn.forbearance.mybatis.scripting.xmltags.SqlNode;
import cn.forbearance.mybatis.session.Configuration;

import java.util.HashMap;

/**
 * org.apache.ibatis.scripting.defaults.RawSqlSource
 * <p>
 * 静态SqlSource。它比DynamicSqlSource快，因为映射是在启动时计算的。
 *
 * @author cristina
 */
public class RawSqlSource implements SqlSource {

    private final SqlSource sqlSource;

    public RawSqlSource(Configuration configuration, SqlNode rootSqlNode, Class<?> parameterType) {
        this(configuration, getSql(configuration, rootSqlNode), parameterType);
    }

    public RawSqlSource(Configuration configuration, String sql, Class<?> parameterType) {
        SqlSourceBuilder sqlSourceParser = new SqlSourceBuilder(configuration);
        Class<?> clazz = parameterType == null ? Object.class : parameterType;
        sqlSource = sqlSourceParser.parse(sql, clazz, new HashMap<>());
    }

    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        return sqlSource.getBoundSql(parameterObject);
    }

    private static String getSql(Configuration configuration, SqlNode rootSqlNode) {
        DynamicContext context = new DynamicContext(configuration, null);
        rootSqlNode.apply(context);
        return context.getSql();
    }
}
