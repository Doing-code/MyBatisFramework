package cn.forbearance.mybatis.test;

/**
 * @author cristina
 */
public interface IUserDao {

    String queryUserName(String uId);

    String queryUserAge(String uId);
}
