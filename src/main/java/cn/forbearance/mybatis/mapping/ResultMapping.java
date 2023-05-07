package cn.forbearance.mybatis.mapping;

import cn.forbearance.mybatis.session.Configuration;
import cn.forbearance.mybatis.type.JdbcType;
import cn.forbearance.mybatis.type.TypeHandler;

/**
 * 结果映射
 *
 * @author cristina
 */
public class ResultMapping {

    private Configuration configuration;
    private String property;
    private String column;
    private Class<?> javaType;
    private JdbcType jdbcType;
    private TypeHandler<?> typeHandler;

    ResultMapping() {
    }

    public static class Builder {
        private ResultMapping resultMapping = new ResultMapping();


    }

}
