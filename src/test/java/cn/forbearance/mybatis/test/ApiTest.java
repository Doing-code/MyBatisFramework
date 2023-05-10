package cn.forbearance.mybatis.test;

import cn.forbearance.mybatis.io.Resources;
import cn.forbearance.mybatis.session.SqlSession;
import cn.forbearance.mybatis.session.SqlSessionFactory;
import cn.forbearance.mybatis.session.SqlSessionFactoryBuilder;
import cn.forbearance.mybatis.test.dao.IUserDao;
import cn.forbearance.mybatis.test.po.User;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * @author cristina
 */
public class ApiTest {

    private final Logger logger = LoggerFactory.getLogger(ApiTest.class);

    public void test_MapperProxyFactory() throws IOException {
        // 1. 从SqlSessionFactory中获取SqlSession
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(Resources.getResourceAsReader("mybatis-config.xml"));

        User user = new User();
        user.setId(null);

        SqlSession sqlSession1 = sqlSessionFactory.openSession();
        IUserDao userDao1 = sqlSession1.getMapper(IUserDao.class);
        List<User> u1 = userDao1.queryUserById(user);
//        sqlSession1.close();

//        SqlSession sqlSession2 = sqlSessionFactory.openSession();
        IUserDao userDao2 = sqlSession1.getMapper(IUserDao.class);
        List<User> u2 = userDao2.queryUserById(user);
        sqlSession1.close();

        logger.info("测试结果：{}, ", JSON.toJSONString(u2));
    }


    /*
    * 1084456190:-1334347089:cn.forbearance.mybatis.test.dao.IUserDao.queryUserById:0:2147483647:SELECT id, user_id, user_head, user_name FROM user:development
    * 1084456190:-1334347089:cn.forbearance.mybatis.test.dao.IUserDao.queryUserById:0:2147483647:SELECT id, user_id, user_head, user_name FROM user:development
    * */
}
