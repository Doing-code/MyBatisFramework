package cn.forbearance.mybatis.executor;

import cn.forbearance.mybatis.mapping.BoundSql;
import cn.forbearance.mybatis.mapping.MappedStatement;
import cn.forbearance.mybatis.session.ResultHandler;
import cn.forbearance.mybatis.session.RowBounds;
import cn.forbearance.mybatis.transaction.Transaction;

import java.sql.SQLException;
import java.util.List;

/**
 * 执行器
 * <p>
 * 执行器分为接口、抽象类、简单执行器实现类三部分. 模板模式
 * <p>
 * 抽象类的存在。它负责提供共性功能逻辑，以及对接口方法的执行过程进行定义和处理
 * <p>
 * SQL执行器封装事务、连接和检测环境等
 * <p>
 * 执行器完成整个过程的处理
 * <p>
 * 而执行器中又包括了对 JDBC 处理的拆解，链接、准备语句、封装参数、处理结果，
 * 所有的这些过程经过解耦后的类和方法，就都可以在以后的功能迭代中非常方便的完成扩展了
 *
 * @author cristina
 */
public interface Executor {

    ResultHandler NO_RESULT_HANDLER = null;

    /**
     * #
     *
     * @param ms
     * @param parameter
     * @param rowBounds
     * @param resultHandler
     * @param boundSql
     * @param <E>
     * @return
     */
    <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException;

    /**
     * #
     *
     * @param ms
     * @param parameter
     * @return
     * @throws SQLException
     */
    int update(MappedStatement ms, Object parameter) throws SQLException;

    /**
     * #
     *
     * @return
     */
    Transaction getTransaction();

    /**
     * #
     *
     * @param required
     * @throws SQLException
     */
    void commit(boolean required) throws SQLException;

    /**
     * #
     *
     * @param required
     * @throws SQLException
     */
    void rollback(boolean required) throws SQLException;

    /**
     * #
     *
     * @param forceRollback
     */
    void close(boolean forceRollback);
}
