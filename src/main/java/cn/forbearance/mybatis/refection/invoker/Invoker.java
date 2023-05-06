package cn.forbearance.mybatis.refection.invoker;

/**
 * 调用者
 *
 * @author cristina
 */
public interface Invoker {

    /**
     * 执行 Field 或者 Method
     *
     * @param target
     * @param args
     * @return
     * @throws Exception
     */
    Object invoke(Object target, Object[] args) throws Exception;

    /**
     * #
     *
     * @return
     */
    Class<?> getType();
}
