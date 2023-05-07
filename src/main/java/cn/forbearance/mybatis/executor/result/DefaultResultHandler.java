package cn.forbearance.mybatis.executor.result;

import cn.forbearance.mybatis.refection.factory.ObjectFactory;
import cn.forbearance.mybatis.session.ResultContext;
import cn.forbearance.mybatis.session.ResultHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * 默认结果处理器
 *
 * @author cristina
 */
public class DefaultResultHandler implements ResultHandler {

    private final List<Object> list;

    /**
     * 通过 ObjectFactory 反射工具类，产生特定的 List
     * @param objectFactory
     */
    @SuppressWarnings("unchecked")
    public DefaultResultHandler(ObjectFactory objectFactory) {
        this.list = objectFactory.create(List.class);
    }


    public DefaultResultHandler() {
        this.list = new ArrayList<>();
    }

    @Override
    public void handleResult(ResultContext context) {
        list.add(context.getResultObject());
    }

    public List<Object> getResultList() {
        return list;
    }
}
