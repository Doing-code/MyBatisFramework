package cn.forbearance.mybatis.builder;

import cn.forbearance.mybatis.mapping.BoundSql;
import cn.forbearance.mybatis.mapping.ParameterMapping;
import cn.forbearance.mybatis.mapping.SqlSource;
import cn.forbearance.mybatis.session.Configuration;

import java.util.List;

/**
 * 静态SQL源码：org.apache.ibatis.builder.StaticSqlSource
 *
 * @author cristina
 */
public class StaticSqlSource implements SqlSource {

    private final String sql;
    private final List<ParameterMapping> parameterMappings;
    private final Configuration configuration;

    public StaticSqlSource(Configuration configuration, String sql) {
        this(configuration, sql, null);
    }

    public StaticSqlSource(Configuration configuration, String sql, List<ParameterMapping> parameterMappings) {
        this.sql = sql;
        this.parameterMappings = parameterMappings;
        this.configuration = configuration;
    }

    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        return new BoundSql(configuration, sql, parameterMappings, parameterObject);
    }
}
