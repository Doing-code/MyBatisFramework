package cn.forbearance.mybatis.refection.wapper;

import cn.forbearance.mybatis.refection.MetaObject;

/**
 * 对象包装工厂
 *
 * @author cristina
 */
public interface ObjectWrapperFactory {

    /**
     * 是否有包装器
     *
     * @param obj
     * @return
     */
    boolean hasWrapperFor(Object obj);

    /**
     * 获取包装器
     *
     * @param metaObject
     * @param obj
     * @return
     */
    ObjectWrapper getWrapperFor(MetaObject metaObject, Object obj);
}
