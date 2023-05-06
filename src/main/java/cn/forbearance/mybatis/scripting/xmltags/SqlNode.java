package cn.forbearance.mybatis.scripting.xmltags;

/**
 * SQL 节点
 *
 * @author cristina
 */
public interface SqlNode {

    /**
     * #
     *
     * @param context
     * @return
     */
    boolean apply(DynamicContext context);
}
