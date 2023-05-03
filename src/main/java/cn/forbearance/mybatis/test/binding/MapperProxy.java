package cn.forbearance.mybatis.test.binding;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * 映射器代理类
 * <p>
 * 通过实现 InvocationHandler#invoke 代理类接口，封装操作逻辑的方式，对外接口提供数据库操作对象。
 *
 * @author cristina
 */
public class MapperProxy<T> implements InvocationHandler, Serializable {

    private static final long serialVersionUID = 4434257806332045649L;

    /**
     * 可以理解为【接口名称+方法名称作为key】
     */
    private Map<String, String> sqlSession;

    private final Class<T> mapperInterface;

    public MapperProxy(Map<String, String> sqlSession, Class<T> mapperInterface) {
        this.sqlSession = sqlSession;
        this.mapperInterface = mapperInterface;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (Object.class.equals(method.getDeclaringClass())) {
            return method.invoke(this, args);
        }
        return "你的被代理了！" + sqlSession.get(mapperInterface.getName() + "." + method.getName());
    }
}
