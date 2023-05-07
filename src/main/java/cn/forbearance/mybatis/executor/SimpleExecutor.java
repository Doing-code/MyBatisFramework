package cn.forbearance.mybatis.executor;

import cn.forbearance.mybatis.executor.statement.StatementHandler;
import cn.forbearance.mybatis.mapping.BoundSql;
import cn.forbearance.mybatis.mapping.MappedStatement;
import cn.forbearance.mybatis.session.Configuration;
import cn.forbearance.mybatis.session.ResultHandler;
import cn.forbearance.mybatis.session.RowBounds;
import cn.forbearance.mybatis.transaction.Transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * 简单执行器
 *
 * @author cristina
 */
public class SimpleExecutor extends BaseExecutor {

    public SimpleExecutor(Configuration configuration, Transaction transaction) {
        super(configuration, transaction);
    }

    @Override
    protected <E> List<E> doQuery(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
        try {
            Configuration configuration = ms.getConfiguration();
            // 创建一个 StatementHandler 【MyBatis四大对象之一】
            StatementHandler handler = configuration.newStatementHandler(this, ms, parameter, rowBounds, resultHandler, boundSql);
            Connection connection = transaction.getConnection();
            // 准备sql语句
            Statement stmt = handler.prepare(connection);
            handler.parameterize(stmt);
            // 返回结果
            return handler.query(stmt, resultHandler);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected int doUpdate(MappedStatement ms, Object parameter) throws SQLException {
        Statement stmt = null;
        try {
            Configuration configuration = ms.getConfiguration();
            // 新建一个 StatementHandler
            StatementHandler handler = configuration.newStatementHandler(this, ms, parameter, RowBounds.DEFAULT, null, null);
            // 准备语句
            stmt = prepareStatement(handler);
            // StatementHandler.update
            return handler.update(stmt);
        } finally {
            closeStatement(stmt);
        }
    }

    private Statement prepareStatement(StatementHandler handler) throws SQLException {
        Statement stmt;
        Connection connection = transaction.getConnection();
        // 准备语句
        stmt = handler.prepare(connection);
        handler.parameterize(stmt);
        return stmt;
    }
}
