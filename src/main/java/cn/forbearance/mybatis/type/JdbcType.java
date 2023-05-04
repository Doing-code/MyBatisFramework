package cn.forbearance.mybatis.type;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

/**
 * JDBC 类型枚举
 *
 * @author cristina
 */
public enum JdbcType {
    /**
     * JDBC 类型
     */
    INTEGER(Types.INTEGER),
    FLOAT(Types.FLOAT),
    DOUBLE(Types.DOUBLE),
    DECIMAL(Types.DECIMAL),
    VARCHAR(Types.VARCHAR),
    TIMESTAMP(Types.TIMESTAMP);


    private final int typeCode;

    private static Map<Integer, JdbcType> codeLookUp = new HashMap<>();

    static {
        for (JdbcType type : JdbcType.values()) {
            codeLookUp.put(type.typeCode, type);
        }
    }

    JdbcType(int code) {
        this.typeCode = code;
    }

    public static JdbcType forCode(int code) {
        return codeLookUp.get(code);
    }
}
