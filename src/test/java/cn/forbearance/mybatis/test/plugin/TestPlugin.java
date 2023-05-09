package cn.forbearance.mybatis.test.plugin;

import cn.forbearance.mybatis.executor.statement.StatementHandler;
import cn.forbearance.mybatis.plugin.Interceptor;
import cn.forbearance.mybatis.plugin.Intercepts;
import cn.forbearance.mybatis.plugin.Invocation;
import cn.forbearance.mybatis.plugin.Signature;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Properties;

/**
 *
 * @author cristina
 */
@Intercepts({
        @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class}),
        @Signature(type = StatementHandler.class, method = "parameterize", args = {Statement.class})
})
public class TestPlugin implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler stmt = (StatementHandler) invocation.getTarget();
        String sql = stmt.getBoundSql().getSql();
        System.out.println(sql);
        return invocation.proceed();
    }

    @Override
    public void setProperties(Properties properties) {
        System.out.println("参数输出：" + properties.getProperty("test00"));
    }
}
