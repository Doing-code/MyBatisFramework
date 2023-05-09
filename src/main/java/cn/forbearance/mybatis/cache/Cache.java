package cn.forbearance.mybatis.cache;

/**
 * SPI(Service Provider Interface) for cache providers. 缓存接口
 *
 * @author cristina
 */
public interface Cache {

    /**
     * 获取缓存的唯一ID标识
     *
     * @return
     */
    String getId();

    /**
     * 存入值
     *
     * @param key
     * @param value
     */
    void putObject(Object key, Object value);

    /**
     * 获取值
     *
     * @param key
     * @return
     */
    Object getObject(Object key);

    /**
     * 移除值
     *
     * @param key
     * @return
     */
    Object removeObject(Object key);

    /**
     * 清空
     */
    void clear();

    /**
     * 获取缓存大小
     *
     * @return
     */
    int getSize();
}
