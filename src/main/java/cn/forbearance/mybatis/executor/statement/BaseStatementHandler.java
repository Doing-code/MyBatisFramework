package cn.forbearance.mybatis.executor.statement;

import cn.forbearance.mybatis.executor.Executor;
import cn.forbearance.mybatis.executor.keygen.KeyGenerator;
import cn.forbearance.mybatis.executor.parameter.ParameterHandler;
import cn.forbearance.mybatis.executor.resultset.ResultSetHandler;
import cn.forbearance.mybatis.mapping.BoundSql;
import cn.forbearance.mybatis.mapping.MappedStatement;
import cn.forbearance.mybatis.session.Configuration;
import cn.forbearance.mybatis.session.ResultHandler;
import cn.forbearance.mybatis.session.RowBounds;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * SQL语句处理器抽象基类
 *
 * @author cristina
 */
public abstract class BaseStatementHandler implements StatementHandler {

    protected final Configuration configuration;
    protected final Executor executor;
    protected final MappedStatement mappedStatement;
    protected final Object parameterObject;
    protected final ResultSetHandler resultSetHandler;
    protected final ParameterHandler parameterHandler;
    protected BoundSql boundSql;
    protected final RowBounds rowBounds;

    public BaseStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
        this.configuration = mappedStatement.getConfiguration();
        this.executor = executor;
        this.mappedStatement = mappedStatement;
        this.rowBounds = rowBounds;

        // update 不会传入 boundSql 参数，所以这里要做初始化处理
        if (boundSql == null) {
            generateKeys(parameterObject);
            boundSql = mappedStatement.getBoundSql(parameterObject);
        }
        this.boundSql = boundSql;

        this.parameterObject = parameterObject;
        this.resultSetHandler = configuration.newResultSetHandler(executor, mappedStatement, rowBounds, resultHandler, boundSql);
        this.parameterHandler = configuration.newParameterHandler(mappedStatement, parameterObject, boundSql);
    }

    @Override
    public Statement prepare(Connection connection) throws SQLException {
        try {
            // 实例化 Statement
            Statement statement = instantiateStatement(connection);
            statement.setQueryTimeout(350);
            statement.setFetchSize(10000);
            return statement;
        } catch (SQLException e) {
            throw new RuntimeException("Error preparing statement.  Cause: " + e, e);
        }
    }

    @Override
    public BoundSql getBoundSql() {
        return boundSql;
    }

    /**
     * # 实例化 Statement
     *
     * @param connection
     * @return
     * @throws SQLException
     */
    protected abstract Statement instantiateStatement(Connection connection) throws SQLException;

    protected void generateKeys(Object parameter) {
        KeyGenerator keyGenerator = mappedStatement.getKeyGenerator();
        keyGenerator.processBefore(executor, mappedStatement, null, parameter);
    }
}
