package cn.forbearance.mybatis.plugin;

import java.util.Properties;

/**
 * 拦截器接口
 *
 * @author cristina
 */
public interface Interceptor {

    /**
     * 拦截方法，使用方实现
     *
     * @param invocation
     * @return
     * @throws Throwable
     */
    Object intercept(Invocation invocation) throws Throwable;

    /**
     * 代理
     *
     * @param target
     * @return
     */
    default Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    /**
     * 设置属性
     *
     * @param properties
     */
    default void setProperties(Properties properties) {

    }
}
