package cn.forbearance.mybatis.test.dao;

import cn.forbearance.mybatis.annotations.Select;
import cn.forbearance.mybatis.test.po.User;

import java.util.List;

/**
 * @author cristina
 */
public interface IUserDao {

//    @Select("SELECT id, user_id, user_head, user_name FROM user")
    List<User> queryUserInfoList();

    Integer insert(User user);

}
