package cn.forbearance.mybatis.cache.decorators;

import cn.forbearance.mybatis.cache.Cache;

import java.util.Deque;
import java.util.LinkedList;

/**
 * FIFO (first in, first out) cache decorator
 *
 * @author cristina
 */
public class FifoCache implements Cache {

    private final Cache delegate;
    private Deque<Object> keys;
    private int size;

    public FifoCache(Cache delegate) {
        this.delegate = delegate;
        this.keys = new LinkedList<>();
        this.size = 1024;
    }

    @Override
    public String getId() {
        return delegate.getId();
    }

    @Override
    public void putObject(Object key, Object value) {
        cycleKeyList(key);
        delegate.putObject(key, value);
    }

    @Override
    public Object getObject(Object key) {
        return delegate.getObject(key);
    }

    @Override
    public Object removeObject(Object key) {
        return delegate.removeObject(key);
    }

    @Override
    public void clear() {
        delegate.clear();
        keys.clear();
    }

    @Override
    public int getSize() {
        return delegate.getSize();
    }

    public void setSize(int size) {
        this.size = size;
    }

    private void cycleKeyList(Object key) {
        keys.addLast(key);
        if (keys.size() > size) {
            Object oldestKey = keys.removeFirst();
            delegate.removeObject(oldestKey);
        }
    }

}
