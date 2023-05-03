package cn.forbearance.mybatis.test.session;

/**
 * SqlSession 用来执行SQL，获取映射器，管理事务
 * <p>
 * 标准定义
 *
 * @author cristina
 */
public interface SqlSession {

    /**
     * 根据指定的 SqlID 获取获取一条记录的封装对象
     *
     * @param statement sqlID 简单理解为【接口名+方法名】
     * @param <T>       封装之后的对象类型
     * @return 封装之后的对象
     */
    <T> T selectOne(String statement);

    /**
     * 根据指定的 SqlID 获取获取一条记录的封装对象，允许传递参数
     *
     * @param statement
     * @param parameter 参数
     * @param <T>
     * @return
     */
    <T> T selectOne(String statement, Object parameter);

    /**
     * 得到映射器，运用泛型方法，使得类型安全
     *
     * @param type Mapper interface class
     * @param <T>  mapper type
     * @return 绑定到这个SqlSession的映射器
     */
    <T> T getMapper(Class<T> type);
}
