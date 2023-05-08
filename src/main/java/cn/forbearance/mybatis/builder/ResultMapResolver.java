package cn.forbearance.mybatis.builder;

import cn.forbearance.mybatis.mapping.ResultMap;
import cn.forbearance.mybatis.mapping.ResultMapping;

import java.util.List;

/**
 * 映射解析
 *
 * @author cristina
 */
public class ResultMapResolver {

    private final MapperBuilderAssistant assistant;
    private String id;
    private Class<?> type;
    private List<ResultMapping> resultMappings;

    public ResultMapResolver(MapperBuilderAssistant assistant, String id, Class<?> type, List<ResultMapping> resultMappings) {
        this.assistant = assistant;
        this.id = id;
        this.type = type;
        this.resultMappings = resultMappings;
    }

    public ResultMap resolve() {
        return assistant.addResultMap(this.id, this.type, this.resultMappings);
    }
}
