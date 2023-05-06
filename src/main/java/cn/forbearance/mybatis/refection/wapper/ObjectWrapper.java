package cn.forbearance.mybatis.refection.wapper;

import cn.forbearance.mybatis.refection.MetaObject;
import cn.forbearance.mybatis.refection.factory.ObjectFactory;
import cn.forbearance.mybatis.refection.property.PropertyTokenizer;

import java.util.List;

/**
 * 对象包装器
 *
 * @author cristina
 */
public interface ObjectWrapper {

    /**
     * get
     *
     * @param prop
     * @return
     */
    Object get(PropertyTokenizer prop);

    /**
     * set
     *
     * @param prop
     * @param value
     */
    void set(PropertyTokenizer prop, Object value);

    /**
     * 查找属性
     *
     * @param name
     * @param useCamelCaseMapping
     * @return
     */
    String findProperty(String name, boolean useCamelCaseMapping);

    /**
     * 取得getter的名字列表
     *
     * @return
     */
    String[] getGetterNames();

    /**
     * 取得setter的名字列表
     *
     * @return
     */
    String[] getSetterNames();

    /**
     * 取得setter的类型
     *
     * @param name
     * @return
     */
    Class<?> getSetterType(String name);

    /**
     * 取得getter的类型
     *
     * @param name
     * @return
     */
    Class<?> getGetterType(String name);

    /**
     * 是否有指定的setter
     *
     * @param name
     * @return
     */
    boolean hasSetter(String name);

    /**
     * 是否有指定的getter
     *
     * @param name
     * @return
     */
    boolean hasGetter(String name);

    /**
     * 实例化属性
     *
     * @param name
     * @param prop
     * @param objectFactory
     * @return
     */
    MetaObject instantiatePropertyValue(String name, PropertyTokenizer prop, ObjectFactory objectFactory);

    /**
     * 是否是集合
     *
     * @return
     */
    boolean isCollection();

    /**
     * 添加属性
     *
     * @param element
     */
    void add(Object element);

    /**
     * 添加属性
     *
     * @param element
     * @param <E>
     */
    <E> void addAll(List<E> element);

}
