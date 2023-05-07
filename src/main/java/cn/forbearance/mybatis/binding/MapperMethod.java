package cn.forbearance.mybatis.binding;

import cn.forbearance.mybatis.mapping.MappedStatement;
import cn.forbearance.mybatis.mapping.SqlCommandType;
import cn.forbearance.mybatis.session.Configuration;
import cn.forbearance.mybatis.session.SqlSession;

import java.lang.reflect.Method;
import java.util.*;

/**
 * 映射器方法
 *
 * @author cristina
 */
public class MapperMethod {

    private final SqlCommand command;

    private final MethodSignature method;

    public MapperMethod(Class<?> mapperInterface, Method method, Configuration configuration) {
        this.command = new SqlCommand(configuration, mapperInterface, method);
        this.method = new MethodSignature(configuration, method);
    }

    public Object execute(SqlSession sqlSession, Object[] args) {
        Object result = null;
        switch (command.getType()) {
            case INSERT: {
                Object param = method.convertArgsToSqlCommandParam(args);
                result = sqlSession.insert(command.getName(), param);
                break;
            }
            case DELETE: {
                Object param = method.convertArgsToSqlCommandParam(args);
                result = sqlSession.delete(command.getName(), param);
                break;
            }
            case UPDATE: {
                Object param = method.convertArgsToSqlCommandParam(args);
                result = sqlSession.update(command.getName(), param);
                break;
            }
            case SELECT: {
                Object param = method.convertArgsToSqlCommandParam(args);
                if (method.resultMany) {
                    result = sqlSession.selectList(command.getName(), param);
                } else {
                    result = sqlSession.selectOne(command.getName(), param);
                }
                break;
            }
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

    /**
     * 方法签名
     */
    public static class MethodSignature {

        private final boolean resultMany;
        private final Class<?> returnType;
        private final SortedMap<Integer, String> params;

        public MethodSignature(Configuration configuration, Method method) {
            this.returnType = method.getReturnType();
            this.resultMany = configuration.getObjectFactory().isCollection(this.returnType) || this.returnType.isArray();
            this.params = Collections.unmodifiableSortedMap(getParams(method));
        }

        /**
         * select * from tb  where userId = #{0} and name = #{1}
         * select * from tb  where userId = #{param1} and name = #{param2}
         *
         * @param args
         * @return
         */
        public Object convertArgsToSqlCommandParam(Object[] args) {
            final int paramCount = params.size();
            if (args == null || paramCount == 0) {
                return null;
            } else if (paramCount == 1) {
                return args[params.keySet().iterator().next()];
            } else {
                final Map<String, Object> param = new ParamMap<Object>();
                int i = 0;
                for (Map.Entry<Integer, String> entry : params.entrySet()) {
                    // 先加一个#{0},#{1},#{2}...参数
                    param.put(entry.getValue(), args[entry.getKey()]);
                    final String genericParamName = "param" + (i + 1);
                    if (!param.containsKey(genericParamName)) {
                        // 再加一个#{param1},#{param2}...参数
                        // 默认情况下它们将会以它们在参数列表中的位置来命名,比如:#{param1},#{param2}等。
                        // 如果你想改变参数的名称(只在多参数情况下) ,那么你可以在参数上使用@Param(“paramName”)注解。
                        param.put(genericParamName, args[entry.getKey()]);
                    }
                    i++;
                }
                return param;
            }
        }

        private SortedMap<Integer, String> getParams(Method method) {
            // 用 TreeMap 保证参数的先后顺序
            final SortedMap<Integer, String> params = new TreeMap<Integer, String>();
            final Class<?>[] argTypes = method.getParameterTypes();
            for (int i = 0; i < argTypes.length; i++) {
                // TODO 不做@Param的实现，如果扩展学习，需要添加 Param 注解并做扩展实现
                String paramName = String.valueOf(params.size());
                params.put(i, paramName);
            }
            return params;
        }
    }

    /**
     * 参数map，镜头太内部类
     *
     * @param <V>
     */
    private static class ParamMap<V> extends HashMap<String, V> {

        private static final long serialVersionUID = 1113644503278868642L;

        @Override
        public V get(Object key) {
            if (!super.containsKey(key)) {
                throw new RuntimeException("Parameter '" + key + "' not found. Available parameters are " + keySet());
            }
            return super.get(key);
        }
    }
}
