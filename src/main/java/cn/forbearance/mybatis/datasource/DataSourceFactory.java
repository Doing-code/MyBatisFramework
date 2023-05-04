package cn.forbearance.mybatis.datasource;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * 数据源工厂
 *
 * @author cristina
 */
public interface DataSourceFactory {

    /**
     * #
     *
     * @param properties
     */
    void setProperties(Properties properties);

    /**
     * #
     *
     * @return
     */
    DataSource getDataSource();
}
