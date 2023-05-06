package cn.forbearance.mybatis.parsing;

/**
 * 记号处理器
 *
 * @author cristina
 */
public interface TokenHandler {

    /**
     * #
     *
     * @param context
     * @return
     */
    String handleToken(String context);
}
