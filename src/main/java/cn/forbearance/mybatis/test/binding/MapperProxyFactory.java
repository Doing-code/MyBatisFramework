package cn.forbearance.mybatis.test.binding;

import cn.forbearance.mybatis.test.session.SqlSession;

import java.lang.reflect.Proxy;

/**
 * 代理工厂
 * <p>
 * 工厂操作相当于把代理的创建给封装起来了。
 *
 * @author cristina
 */
public class MapperProxyFactory<T> {

    private final Class<T> mapperInterface;

    public MapperProxyFactory(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    @SuppressWarnings("unchecked")
    public T newInstance(SqlSession sqlSession) {
        final MapperProxy<T> mapperProxy = new MapperProxy<>(sqlSession, mapperInterface);
        return (T) Proxy.newProxyInstance(mapperInterface.getClassLoader(), new Class[]{mapperInterface}, mapperProxy);
    }
}
