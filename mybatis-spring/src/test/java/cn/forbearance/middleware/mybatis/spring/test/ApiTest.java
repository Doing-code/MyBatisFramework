package cn.forbearance.middleware.mybatis.spring.test;

import cn.forbearance.middleware.mybatis.spring.test.dao.IUserDao;
import cn.forbearance.middleware.mybatis.spring.test.po.User;
import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.util.List;

/**
 * @author cristina
 */
public class ApiTest {

    private final Logger logger = LoggerFactory.getLogger(ApiTest.class);

    @Test
    public void test_MapperProxyFactory() throws IOException {
        BeanFactory beanFactory = new ClassPathXmlApplicationContext("spring-config.xml");
        IUserDao dao = beanFactory.getBean("IUserDao", IUserDao.class);
        List<User> res = dao.queryUserById(new User(null));
        logger.info("测试结果：{}", JSON.toJSONString(res));
    }


    /*
    * 1084456190:-1334347089:cn.forbearance.mybatis.test.dao.IUserDao.queryUserById:0:2147483647:SELECT id, user_id, user_head, user_name FROM user:development
    * 1084456190:-1334347089:cn.forbearance.mybatis.test.dao.IUserDao.queryUserById:0:2147483647:SELECT id, user_id, user_head, user_name FROM user:development
    * */
}
