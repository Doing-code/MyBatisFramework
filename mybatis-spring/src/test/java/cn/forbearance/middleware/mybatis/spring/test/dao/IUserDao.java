package cn.forbearance.middleware.mybatis.spring.test.dao;


import cn.forbearance.middleware.mybatis.spring.test.po.User;

import java.util.List;

/**
 * @author cristina
 */
public interface IUserDao {
    List<User> queryUserById(User user);
}
