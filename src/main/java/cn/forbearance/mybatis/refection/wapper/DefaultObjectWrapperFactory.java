package cn.forbearance.mybatis.refection.wapper;

import cn.forbearance.mybatis.refection.MetaObject;

/**
 * 默认对象包装工厂
 *
 * @author cristina
 */
public class DefaultObjectWrapperFactory implements ObjectWrapperFactory {

    @Override
    public boolean hasWrapperFor(Object obj) {
        return false;
    }

    @Override
    public ObjectWrapper getWrapperFor(MetaObject metaObject, Object obj) {
        throw new RuntimeException("The DefaultObjectWrapperFactory should never be called to provide an ObjectWrapper.");
    }
}
