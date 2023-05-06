package cn.forbearance.mybatis.executor.parameter;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 参数处理器
 *
 * @author cristina
 */
public interface ParameterHandler {

    /**
     * 获取参数
     *
     * @return
     */
    Object getParameterObject();

    /**
     * 设置参数
     *
     * @param ps
     * @throws SQLException
     */
    void setParameters(PreparedStatement ps) throws SQLException;
}
