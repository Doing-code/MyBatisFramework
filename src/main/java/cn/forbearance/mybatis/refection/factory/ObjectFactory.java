package cn.forbearance.mybatis.refection.factory;

import java.util.List;
import java.util.Properties;

/**
 * 对象工厂接口
 * <p>
 * Mybatis每次在创建Mapper映射结果对象实例的时候，就会使用ObjectFactory来完成对象实例
 * <p>
 * 或者利用反射给环境变量赋值，如 dataSource 连接信息
 *
 * @author cristina
 */
public interface ObjectFactory {

    /**
     * Sets configuration properties.
     * 设置属性
     *
     * @param properties configuration properties
     */
    void setProperties(Properties properties);

    /**
     * Creates a new object with default constructor.
     * 生产对象
     *
     * @param type Object type
     * @return <T>
     */
    <T> T create(Class<T> type);

    /**
     * Creates a new object with the specified constructor and params.
     * 生产对象，使用指定的构造函数和构造函数参数
     *
     * @param type                Object type
     * @param constructorArgTypes Constructor argument types
     * @param constructorArgs     Constructor argument values
     * @return <T>
     */
    <T> T create(Class<T> type, List<Class<?>> constructorArgTypes, List<Object> constructorArgs);

    /**
     * Returns true if this object can have a set of other objects.
     * It's main purpose is to support non-java.util.Collection objects like Scala collections.
     * 返回这个对象是否是集合，为了支持 Scala collections
     *
     * @param type Object type
     * @return whether it is a collection or not
     * @since 3.1.0
     */
    <T> boolean isCollection(Class<T> type);

}
