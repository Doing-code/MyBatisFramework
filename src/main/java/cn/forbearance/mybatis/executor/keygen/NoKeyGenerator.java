package cn.forbearance.mybatis.executor.keygen;

import cn.forbearance.mybatis.executor.Executor;
import cn.forbearance.mybatis.mapping.MappedStatement;

import java.sql.Statement;

/**
 * @author cristina
 */
public class NoKeyGenerator implements KeyGenerator{

    @Override
    public void processBefore(Executor executor, MappedStatement ms, Statement stmt, Object parameter) {

    }

    @Override
    public void processAfter(Executor executor, MappedStatement ms, Statement stmt, Object parameter) {

    }
}
