package cn.forbearance.mybatis.session;

/**
 * 结果处理器
 *
 * @author cristina
 */
public interface ResultHandler {

    /**
     * 处理结果
     *
     * @param context
     */
    void handleResult(ResultContext context);
}
