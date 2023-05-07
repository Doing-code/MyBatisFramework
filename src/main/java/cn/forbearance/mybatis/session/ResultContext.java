package cn.forbearance.mybatis.session;

/**
 * 结果上下文
 *
 * @author cristina
 */
public interface ResultContext {

    /**
     * 获取结果
     *
     * @return
     */
    Object getResultObject();

    /**
     * 获取记录数
     *
     * @return
     */
    int getResultCount();

}
