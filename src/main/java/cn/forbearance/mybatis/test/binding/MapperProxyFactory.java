package cn.forbearance.mybatis.test.binding;

import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * 代理工厂
 *
 * 工厂操作相当于把代理的创建给封装起来了。
 *
 * @author cristina
 */
public class MapperProxyFactory<T> {

    private final Class<T> mapperInterface;

    public MapperProxyFactory(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    public T newInstance(Map<String, String> sqlSession) {
        MapperProxy<T> mapperProxy = new MapperProxy<>(sqlSession, mapperInterface);
        return (T) Proxy.newProxyInstance(mapperInterface.getClassLoader(), new Class[]{mapperInterface}, mapperProxy);
    }
}
