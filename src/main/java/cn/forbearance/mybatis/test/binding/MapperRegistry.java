package cn.forbearance.mybatis.test.binding;

import cn.forbearance.mybatis.test.session.Configuration;
import cn.forbearance.mybatis.test.session.SqlSession;
import cn.hutool.core.lang.ClassScanner;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 映射器注册机
 * <p>
 * 提供包路径的扫描和映射器代理类注册机服务，完成接口对象的代理类注册处理
 *
 * @author cristina
 */
public class MapperRegistry {

    private Configuration configuration;

    public MapperRegistry(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * 添加映射器代理
     */
    private final Map<Class<?>, MapperProxyFactory<?>> knownMappers = new HashMap<>();

    public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
        final MapperProxyFactory<T> mapperProxyFactory = (MapperProxyFactory<T>) knownMappers.get(type);
        if (null == mapperProxyFactory) {
            throw new RuntimeException("Type " + type + " is not known to the MapperRegistry.");
        }
        try {
            return mapperProxyFactory.newInstance(sqlSession);
        } catch (Exception e) {
            throw new RuntimeException("Error getting mapper instance. Cause: " + e, e);
        }
    }

    public <T> void addMapper(Class<T> type) {
        // Mapper 必须时接口才会注册
        if (type.isInterface()) {
            // 检查是否重复中注册
            if (hasMapper(type)) {
                throw new RuntimeException("Type " + type + " is already known to the MapperRegistry.");
            }
            // 注册映射器代理工厂
            knownMappers.put(type, new MapperProxyFactory<>(type));
        }
    }

    public <T> boolean hasMapper(Class<T> type) {
        return knownMappers.containsKey(type);
    }

    public void addMappers(String packageName) {
        Set<Class<?>> mappers = ClassScanner.scanPackage(packageName);
        for (Class<?> mapperClass : mappers) {
            addMapper(mapperClass);
        }
    }
}
