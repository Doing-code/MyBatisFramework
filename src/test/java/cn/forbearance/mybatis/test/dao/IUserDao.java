package cn.forbearance.mybatis.test.dao;

import cn.forbearance.mybatis.test.po.User;

import java.util.List;

/**
 * @author cristina
 */
public interface IUserDao {

    User queryUserInfoById(Long id);

    User queryUserInfo(User req);

    List<User> queryUserInfoList();

    int updateUserInfo(User req);

    void insertUserInfo(User req);

    int deleteUserInfoByUserId(String userId);

}
