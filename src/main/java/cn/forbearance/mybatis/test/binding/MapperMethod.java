package cn.forbearance.mybatis.test.binding;

import cn.forbearance.mybatis.test.mapping.MappedStatement;
import cn.forbearance.mybatis.test.mapping.SqlCommandType;
import cn.forbearance.mybatis.test.session.Configuration;
import cn.forbearance.mybatis.test.session.SqlSession;

import java.lang.reflect.Method;

/**
 * 映射器方法
 *
 * @author cristina
 */
public class MapperMethod {

    private final SqlCommand command;

    public MapperMethod(Class<?> mapperInterface, Method method, Configuration configuration) {
        this.command = new SqlCommand(configuration, mapperInterface, method);
    }

    public Object execute(SqlSession sqlSession, Object[] args) {
        Object result = null;
        switch (command.getType()) {
            case INSERT:
                break;
            case DELETE:
                break;
            case UPDATE:
                break;
            case SELECT:
                result = sqlSession.selectOne(command.getName(), args);
                break;
            default:
                throw new RuntimeException("Unknown execution method for: " + command.getName());
        }
        return result;
    }

    private static class SqlCommand {
        private final String name;

        private final SqlCommandType type;

        public SqlCommand(Configuration configuration, Class<?> mapperInterface, Method method) {
            String statementNme = mapperInterface.getName() + "." + method.getName();
            MappedStatement ms = configuration.getMappedStatement(statementNme);
            name = ms.getId();
            type = ms.getSqlCommandType();
        }

        public String getName() {
            return name;
        }

        public SqlCommandType getType() {
            return type;
        }
    }
}
