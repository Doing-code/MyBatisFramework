package cn.forbearance.mybatis.test.session.defaults;

import cn.forbearance.mybatis.test.binding.MapperRegistry;
import cn.forbearance.mybatis.test.mapping.MappedStatement;
import cn.forbearance.mybatis.test.session.Configuration;
import cn.forbearance.mybatis.test.session.SqlSession;

/**
 * 默认 SqlSession 实现
 *
 * @author cristina
 */
public class DefaultSqlSession implements SqlSession {

    private Configuration configuration;

    public DefaultSqlSession(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * 映射器注册机
     */
    private MapperRegistry mapperRegistry;

    public DefaultSqlSession(MapperRegistry mapperRegistry) {
        this.mapperRegistry = mapperRegistry;
    }

    @Override
    public <T> T selectOne(String statement) {
        return (T) ("你被代理了！" + statement);
    }

    @Override
    public <T> T selectOne(String statement, Object parameter) {
        MappedStatement mappedStatement = configuration.getMappedStatement(statement);
        return (T) ("你被代理了！" + "\n方法：" + statement + "\n入参：" + parameter + "\n待执行SQL：" + mappedStatement.getSql());
    }

    @Override
    public <T> T getMapper(Class<T> type) {
        return configuration.getMapper(type, this);
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }
}
