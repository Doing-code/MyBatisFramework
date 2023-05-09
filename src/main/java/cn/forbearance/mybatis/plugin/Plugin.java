package cn.forbearance.mybatis.plugin;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 代理模式插件
 *
 * @author cristina
 */
public class Plugin implements InvocationHandler {

    private Object target;
    private Interceptor interceptor;
    private Map<Class<?>, Set<Method>> signatureMap;

    public Plugin(Object target, Interceptor interceptor, Map<Class<?>, Set<Method>> signatureMap) {
        this.target = target;
        this.interceptor = interceptor;
        this.signatureMap = signatureMap;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 获取声明的方法列表
        Set<Method> methods = signatureMap.get(method.getDeclaringClass());
        // 过滤需要拦截的方法
        if (method != null && methods.contains(method)) {
            // 调用 Interceptor#intercept 插入自己的反射逻辑
            return interceptor.intercept(new Invocation(target, method, args));
        }
        return method.invoke(target, args);
    }

    public static Object wrap(Object target, Interceptor interceptor) {
        Map<Class<?>, Set<Method>> signatureMap = getSignatureMap(interceptor);
        // 取得要改变行为的类(ParameterHandler|ResultSetHandler|StatementHandler|Executor)，只实现了 StatementHandler
        Class<?> type = target.getClass();
        Class<?>[] interfaces = getInterfaces(type, signatureMap);
        // 创建代理 StatementHandler
        if (interfaces.length > 0) {
            // Proxy.newProxyInstance(ClassLoader loader, Class<?>[] interfaces, InvocationHandler h)
            return Proxy.newProxyInstance(type.getClassLoader(), interfaces, new Plugin(target, interceptor, signatureMap));
        }
        return target;
    }

    /**
     * 获取方法签名
     *
     * @param interceptor
     * @return
     */
    private static Map<Class<?>, Set<Method>> getSignatureMap(Interceptor interceptor) {
        Intercepts interceptsAnnotation = interceptor.getClass().getAnnotation(Intercepts.class);
        // 必须得有 Intercepts 注解，没有报错
        if (interceptsAnnotation == null) {
            throw new RuntimeException("No @Intercepts annotation was found in interceptor " + interceptor.getClass().getName());
        }
        // value是数组型，Signature的数组
        Signature[] signs = interceptsAnnotation.value();
        // 每个 class 类有多个可能有多个 Method 需要被拦截
        Map<Class<?>, Set<Method>> signatureMap = new HashMap<>();
        for (Signature sign : signs) {
            try {
                // 例如获取到方法；StatementHandler.prepare(Connection connection)、StatementHandler.parameterize(Statement statement)...
                Set<Method> methods = signatureMap.computeIfAbsent(sign.type(), k -> new HashSet<>());
                Method method = sign.type().getMethod(sign.method(), sign.args());
                methods.add(method);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Could not find method on " + sign.type() + " named " + sign.method() + ". Cause: " + e, e);
            }
        }
        return signatureMap;
    }

    /**
     * 获取接口
     *
     * @param type
     * @param signatureMap
     * @return
     */
    public static Class<?>[] getInterfaces(Class<?> type, Map<Class<?>, Set<Method>> signatureMap) {
        Set<Class<?>> interfaces = new HashSet<>();
        while (type != null) {
            for (Class<?> clazz : type.getInterfaces()) {
                // 拦截 ParameterHandler|ResultSetHandler|StatementHandler|Executor
                if (signatureMap.containsKey(clazz)) {
                    interfaces.add(clazz);
                }
            }
            type = type.getSuperclass();
        }
        return interfaces.toArray(new Class<?>[interfaces.size()]);
    }
}
