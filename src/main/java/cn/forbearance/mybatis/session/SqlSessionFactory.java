package cn.forbearance.mybatis.session;

/**
 * 工厂模式接口，构建 SqlSession 的工厂
 *
 * @author cristina
 */
public interface SqlSessionFactory {

    /**
     * #
     *
     * @return
     */
    SqlSession openSession();
}
