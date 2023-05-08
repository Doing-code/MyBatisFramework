package cn.forbearance.mybatis.mapping;

import cn.forbearance.mybatis.refection.MetaObject;
import cn.forbearance.mybatis.session.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 绑定 SQL【SqlSource】，将动态内容都处理完成得到的SQL语句字符串，其中包括?,还有绑定的参数
 * @author cristina
 */
public class BoundSql {

    private String sql;

    private List<ParameterMapping> parameterMappings;

    private Object parameterObject;

    private Map<String, Object> additionalParameters;

    private MetaObject metaParameters;

    public BoundSql(Configuration configuration, String sql, List<ParameterMapping> parameterMappings, Object parameterObject) {
        this.sql = sql;
        this.parameterMappings = parameterMappings;
        this.parameterObject = parameterObject;
        this.additionalParameters = new HashMap<>();
        this.metaParameters = configuration.newMetaObject(additionalParameters);
    }

    public String getSql() {
        return sql;
    }

    public List<ParameterMapping> getParameterMappings() {
        return parameterMappings;
    }

    public Object getParameterObject() {
        return parameterObject;
    }

    public Map<String, Object> getAdditionalParameters() {
        return additionalParameters;
    }

    public void setAdditionalParameters(String key, Object value) {
        additionalParameters.put(key, value);
    }

    public MetaObject getMetaParameters() {
        return metaParameters;
    }
}
