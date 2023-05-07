package cn.forbearance.mybatis.session;

/**
 * 分页
 *
 * @author cristina
 */
public class RowBounds {

    public static final int NO_ROW_OFFSET = 0;
    public static final int NO_ROW_LIMIT = Integer.MAX_VALUE;
    public static final RowBounds DEFAULT = new RowBounds();

    /**
     * offset,limit 相当于一般分页的 start,limit
     */
    private int offset;
    private int limit;

    /**
     * 默认一页 Integer.MAX_VALUE
     */
    public RowBounds() {
        this.offset = NO_ROW_OFFSET;
        this.limit = NO_ROW_LIMIT;
    }

    public RowBounds(int offset, int limit) {
        this.offset = offset;
        this.limit = limit;
    }

    public int getOffset() {
        return offset;
    }

    public int getLimit() {
        return limit;
    }
}
