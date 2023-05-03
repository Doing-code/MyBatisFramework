package cn.forbearance.mybatis.test.dao;

/**
 * @author cristina
 */
public interface IUserDao {

    String queryUserName(String uId);

    String queryUserAge(String uId);
}
