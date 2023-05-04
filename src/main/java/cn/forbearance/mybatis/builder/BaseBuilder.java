package cn.forbearance.mybatis.builder;

import cn.forbearance.mybatis.session.Configuration;
import cn.forbearance.mybatis.type.TypeAliasRegistry;

/**
 * 构建器的基类，建造者模式
 *
 * @author cristina
 */
public abstract class BaseBuilder {

    protected final Configuration configuration;

    protected final TypeAliasRegistry typeAliasRegistry;

    public BaseBuilder(Configuration configuration) {
        this.configuration = configuration;
        this.typeAliasRegistry = this.configuration.getTypeAliasRegistry();
    }

    public Configuration getConfiguration() {
        return configuration;
    }

}
