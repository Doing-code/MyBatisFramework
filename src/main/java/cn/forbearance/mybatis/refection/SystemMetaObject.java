package cn.forbearance.mybatis.refection;

import cn.forbearance.mybatis.refection.factory.DefaultObjectFactory;
import cn.forbearance.mybatis.refection.factory.ObjectFactory;
import cn.forbearance.mybatis.refection.wapper.DefaultObjectWrapperFactory;
import cn.forbearance.mybatis.refection.wapper.ObjectWrapperFactory;

/**
 * 系统级别的元对象
 *
 * @author cristina
 */
public class SystemMetaObject {

    public static final ObjectFactory DEFAULT_OBJECT_FACTORY = new DefaultObjectFactory();

    public static final ObjectWrapperFactory DEFAULT_OBJECT_WRAPPER_FACTORY = new DefaultObjectWrapperFactory();

    public static final MetaObject NULL_META_OBJECT = MetaObject.forObject(NullObject.class, DEFAULT_OBJECT_FACTORY, DEFAULT_OBJECT_WRAPPER_FACTORY);

    private SystemMetaObject() {
        // 防止静态类的实例化
    }

    private static class NullObject {
    }

    public static MetaObject forObject(Object obj) {
        return MetaObject.forObject(obj, DEFAULT_OBJECT_FACTORY, DEFAULT_OBJECT_WRAPPER_FACTORY);
    }
}
