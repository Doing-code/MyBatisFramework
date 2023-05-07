package cn.forbearance.mybatis.test.dao;

import cn.forbearance.mybatis.annotations.Select;
import cn.forbearance.mybatis.test.po.User;

import java.util.List;

/**
 * @author cristina
 */
public interface IUserDao {

    @Select("SELECT id, userId, userName, userHead FROM user where id = #{id}")
    User queryUserInfoById(Long id);

    User queryUserInfo(User req);

    @Select("SELECT id, userId, userName, userHead FROM user")
    List<User> queryUserInfoList();

    int updateUserInfo(User req);

    void insertUserInfo(User req);

    int deleteUserInfoByUserId(String userId);

}
