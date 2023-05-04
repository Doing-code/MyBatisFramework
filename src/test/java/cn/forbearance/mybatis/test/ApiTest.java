package cn.forbearance.mybatis.test;

import cn.forbearance.mybatis.test.dao.IUserDao;
import cn.forbearance.mybatis.test.io.Resources;
import cn.forbearance.mybatis.test.session.SqlSession;
import cn.forbearance.mybatis.test.session.SqlSessionFactory;
import cn.forbearance.mybatis.test.session.SqlSessionFactoryBuilder;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;

/**
 * @author cristina
 */
public class ApiTest {

    private final Logger logger = LoggerFactory.getLogger(ApiTest.class);

    @Test
    public void test_MapperProxyFactory() throws IOException {
        // 1. 从SqlSessionFactory中获取SqlSession
        Reader reader = Resources.getResourceAsReader("mybatis-config.xml");
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
        SqlSession sqlSession = sqlSessionFactory.openSession();

        // 2. 获取映射器对象
        IUserDao userDao = sqlSession.getMapper(IUserDao.class);

        // 3. 测试验证
        String res = userDao.queryUserName("10001");
        logger.info("测试结果：{}", res);

    }

}
