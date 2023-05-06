package cn.forbearance.mybatis.datasource.pooled;

import cn.forbearance.mybatis.datasource.unpooled.UnPooledDataSourceFactory;

import javax.sql.DataSource;

/**
 * 池化数据源工厂
 *
 * @author cristina
 */
public class PooledDataSourceFactory extends UnPooledDataSourceFactory {

    /**
     * 在 Mybatis 源码中这部分则是进行了大量的反射字段处理的方式进行存放和获取的
     *
     * @return
     */
    public PooledDataSourceFactory() {
        this.dataSource = new PooledDataSource();
    }
}
