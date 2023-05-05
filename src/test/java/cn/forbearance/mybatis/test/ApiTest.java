package cn.forbearance.mybatis.test;

import cn.forbearance.mybatis.test.dao.IUserDao;
import cn.forbearance.mybatis.io.Resources;
import cn.forbearance.mybatis.test.po.User;
import cn.forbearance.mybatis.session.SqlSession;
import cn.forbearance.mybatis.session.SqlSessionFactory;
import cn.forbearance.mybatis.session.SqlSessionFactoryBuilder;
import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author cristina
 */
public class ApiTest {

    private final Logger logger = LoggerFactory.getLogger(ApiTest.class);

    @Test
    public void test_MapperProxyFactory() throws IOException {
        // 1. 从SqlSessionFactory中获取SqlSession
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(Resources.getResourceAsReader("mybatis-config.xml"));
        SqlSession sqlSession = sqlSessionFactory.openSession();

        // 2. 获取映射器对象
        IUserDao userDao = sqlSession.getMapper(IUserDao.class);

        // 3. 测试验证
        for (int i = 0; i < 50; i++) {
            User user = userDao.queryUserInfoById(1L);
            logger.info("测试结果：{}", JSON.toJSONString(user));
        }
    }

}
