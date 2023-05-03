package cn.forbearance.mybatis.test;

import cn.forbearance.mybatis.test.binding.MapperProxyFactory;
import cn.forbearance.mybatis.test.binding.MapperRegistry;
import cn.forbearance.mybatis.test.dao.IUserDao;
import cn.forbearance.mybatis.test.session.SqlSession;
import cn.forbearance.mybatis.test.session.defaults.DefaultSqlSessionFactory;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author cristina
 */
public class ApiTest {

    private final Logger logger = LoggerFactory.getLogger(ApiTest.class);

    @Test
    public void test_MapperProxyFactory() {
        MapperRegistry registry = new MapperRegistry();
        registry.addMappers("cn.forbearance.mybatis.test.dao");

        DefaultSqlSessionFactory sqlSessionFactory = new DefaultSqlSessionFactory(registry);
        SqlSession sqlSession = sqlSessionFactory.openSession();
        IUserDao userDao = sqlSession.getMapper(IUserDao.class);

        String res = userDao.queryUserName("10001");
        logger.info("测试结果：{}", res);
    }

}
