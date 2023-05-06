package cn.forbearance.mybatis.test.dao;

import cn.forbearance.mybatis.test.po.User;

/**
 * @author cristina
 */
public interface IUserDao {

    User queryUserInfoById(Long id);

    User queryUserInfo(User req);

}
