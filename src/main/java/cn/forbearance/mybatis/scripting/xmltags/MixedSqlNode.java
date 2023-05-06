package cn.forbearance.mybatis.scripting.xmltags;

import java.util.List;

/**
 * 混合 SQL 节点
 *
 * @author cristina
 */
public class MixedSqlNode implements SqlNode {

    /**
     * 组合模式，拥有一个SqlNode的List
     */
    private List<SqlNode> contents;

    public MixedSqlNode(List<SqlNode> contents) {
        this.contents = contents;
    }

    @Override
    public boolean apply(DynamicContext context) {
        contents.forEach(node -> node.apply(context));
        return true;
    }
}
