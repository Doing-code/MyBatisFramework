package cn.forbearance.middleware.mybatis.spring;

import cn.forbearance.mybatis.session.SqlSessionFactory;
import org.springframework.beans.factory.FactoryBean;

/**
 * Mapper 工厂对象
 *
 * @author cristina
 */
public class MapperFactoryBean<T> implements FactoryBean<T> {

    private Class<T> mapperInterface;
    private SqlSessionFactory sqlSessionFactory;

    public MapperFactoryBean(Class<T> mapperInterface, SqlSessionFactory sqlSessionFactory) {
        this.mapperInterface = mapperInterface;
        this.sqlSessionFactory = sqlSessionFactory;
    }

    /**
     * <pre>
     * SqlSession sqlSession1 = sqlSessionFactory.openSession();
     * IUserDao userDao1 = sqlSession1.getMapper(IUserDao.class);
     * </pre>
     *
     * @return
     * @throws Exception
     */
    @Override
    public T getObject() throws Exception {
        return sqlSessionFactory.openSession().getMapper(mapperInterface);
    }

    @Override
    public Class<?> getObjectType() {
        return mapperInterface;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
