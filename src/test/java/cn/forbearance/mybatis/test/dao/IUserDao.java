package cn.forbearance.mybatis.test.dao;

import cn.forbearance.mybatis.annotations.Select;
import cn.forbearance.mybatis.test.po.User;

import java.util.List;

/**
 * @author cristina
 */
public interface IUserDao {
    List<User> queryUserById(User user);
}
