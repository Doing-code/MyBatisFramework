package cn.forbearance.mybatis.test.session;

import cn.forbearance.mybatis.test.builder.xml.XmlConfigBuilder;
import cn.forbearance.mybatis.test.session.defaults.DefaultSqlSessionFactory;

import java.io.Reader;

/**
 * 构建 SqlSessionFactory 的工厂
 *
 * @author cristina
 */
public class SqlSessionFactoryBuilder {

    public SqlSessionFactory build(Reader reader) {
        XmlConfigBuilder xmlConfigBuilder = new XmlConfigBuilder(reader);
        return build(xmlConfigBuilder.parse());
    }

    public SqlSessionFactory build(Configuration configuration) {
        return new DefaultSqlSessionFactory(configuration);
    }
}
