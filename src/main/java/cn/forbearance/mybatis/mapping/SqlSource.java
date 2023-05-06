package cn.forbearance.mybatis.mapping;

/**
 * SQL源码：org.apache.ibatis.mapping.SqlSource
 * 表示从XML文件或注释中读取的映射语句的内容。它创建将从用户接收到的输入参数传递到数据库的SQL。
 * @author cristina
 */
public interface SqlSource {

    /**
     * #
     * @param parameterObject
     * @return
     */
    BoundSql getBoundSql(Object parameterObject);
}
