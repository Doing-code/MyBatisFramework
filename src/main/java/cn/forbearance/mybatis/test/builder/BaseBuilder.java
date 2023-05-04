package cn.forbearance.mybatis.test.builder;

import cn.forbearance.mybatis.test.session.Configuration;

/**
 * 构建器的基类，建造者模式
 *
 * @author cristina
 */
public abstract class BaseBuilder {

    protected final Configuration configuration;

    public BaseBuilder(Configuration configuration) {
        this.configuration = configuration;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

}
