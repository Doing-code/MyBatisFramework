package cn.forbearance.mybatis.session.defaults;

import cn.forbearance.mybatis.executor.Executor;
import cn.forbearance.mybatis.mapping.MappedStatement;
import cn.forbearance.mybatis.session.Configuration;
import cn.forbearance.mybatis.session.SqlSession;

import java.util.List;

/**
 * 默认 SqlSession 实现
 *
 * @author cristina
 */
public class DefaultSqlSession implements SqlSession {

    private Configuration configuration;

    private Executor executor;

    public DefaultSqlSession(Configuration configuration, Executor executor) {
        this.configuration = configuration;
        this.executor = executor;
    }

    @Override
    public <T> T selectOne(String statement) {
        return this.selectOne(statement, null);
    }

    @Override
    public <T> T selectOne(String statement, Object parameter) {
        MappedStatement ms = configuration.getMappedStatement(statement);
        List<T> list = executor.query(ms, parameter, Executor.NO_RESULT_HANDLER, ms.getSqlSource().getBoundSql(parameter));
        return list.get(0);
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
