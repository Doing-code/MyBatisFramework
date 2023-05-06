package cn.forbearance.mybatis.mapping;

import cn.forbearance.mybatis.session.Configuration;
import cn.forbearance.mybatis.type.JdbcType;

/**
 * 参数映射：#{property,javaType=int,jdbcType=NUMERIC}
 *
 * @author cristina
 */
public class ParameterMapping {

    private Configuration configuration;

    /**
     * property
     */
    private String property;

    /**
     * javaType=int
     */
    private Class<?> javaType = Object.class;

    /**
     * jdbcType=NUMERIC
     */
    private JdbcType jdbcType;

    public ParameterMapping() {
    }

    public static class Builder {
        private final ParameterMapping parameterMapping = new ParameterMapping();

        public Builder(Configuration configuration, String property, Class<?> javaType) {
            parameterMapping.configuration = configuration;
            parameterMapping.property = property;
            parameterMapping.javaType = javaType;
        }

        public Builder javaType(Class<?> javaType) {
            parameterMapping.javaType = javaType;
            return this;
        }

        public Builder javaType(JdbcType jdbcType) {
            parameterMapping.jdbcType = jdbcType;
            return this;
        }

        public ParameterMapping build() {
            return parameterMapping;
        }
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public String getProperty() {
        return property;
    }

    public Class<?> getJavaType() {
        return javaType;
    }

    public JdbcType getJdbcType() {
        return jdbcType;
    }
}
