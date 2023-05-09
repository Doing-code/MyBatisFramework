package cn.forbearance.mybatis.plugin;

/**
 * 方法签名
 *
 * @author cristina
 */
public @interface Signature {

    Class<?> type();

    String method();

    Class<?>[] args();
}
