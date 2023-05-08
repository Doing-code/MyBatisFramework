package cn.forbearance.mybatis.executor.keygen;

import cn.forbearance.mybatis.executor.Executor;
import cn.forbearance.mybatis.mapping.MappedStatement;
import cn.forbearance.mybatis.refection.MetaObject;
import cn.forbearance.mybatis.session.Configuration;
import cn.forbearance.mybatis.session.RowBounds;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * @author cristina
 */
public class SelectKeyGenerator implements KeyGenerator {

    public static final String SELECT_KEY_SUFFIX = "!selectKey";
    private boolean executorBefore;
    private MappedStatement keyStatement;

    public SelectKeyGenerator(boolean executorBefore, MappedStatement keyStatement) {
        this.executorBefore = executorBefore;
        this.keyStatement = keyStatement;
    }


    @Override
    public void processBefore(Executor executor, MappedStatement ms, Statement stmt, Object parameter) {
        if (executorBefore) {
            processGeneratedKeys(executor, ms, parameter);
        }
    }

    @Override
    public void processAfter(Executor executor, MappedStatement ms, Statement stmt, Object parameter) {
        if (!executorBefore) {
            processGeneratedKeys(executor, ms, parameter);
        }
    }

    private void processGeneratedKeys(Executor executor, MappedStatement ms, Object parameter) {
        try {
            if (parameter != null && keyStatement != null && keyStatement.getKeyProperties() != null) {
                String[] keyProperties = keyStatement.getKeyProperties();
                final Configuration configuration = ms.getConfiguration();
                final MetaObject metaParam = configuration.newMetaObject(parameter);
                Executor keyExecutor = configuration.newExecutor(executor.getTransaction());
                List<Object> values = keyExecutor.query(keyStatement, parameter, RowBounds.DEFAULT, Executor.NO_RESULT_HANDLER);
                if (values.size() == 0) {
                    throw new RuntimeException("SelectKey returned no data.");
                } else if (values.size() > 1) {
                    throw new RuntimeException("SelectKey returned more than one value.");
                }
                MetaObject metaResult = configuration.newMetaObject(values.get(0));
                if (keyProperties.length == 1) {
                    if (metaResult.hasGetter(keyProperties[0])) {
                        setValue(metaParam, keyProperties[0], metaResult.getValue(keyProperties[0]));
                    } else {
                        setValue(metaParam, keyProperties[0], values.get(0));
                    }
                } else {
                    handleMultipleProperties(keyProperties, metaParam, metaResult);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error selecting key or setting result to parameter object. Cause: " + e);
        }
    }

    private void handleMultipleProperties(String[] keyProperties,
                                          MetaObject metaParam, MetaObject metaResult) {
        String[] keyColumns = keyStatement.getKeyColumns();

        if (keyColumns == null || keyColumns.length == 0) {
            for (String keyProperty : keyProperties) {
                setValue(metaParam, keyProperty, metaResult.getValue(keyProperty));
            }
        } else {
            if (keyColumns.length != keyProperties.length) {
                throw new RuntimeException("If SelectKey has key columns, the number must match the number of key properties.");
            }
            for (int i = 0; i < keyProperties.length; i++) {
                setValue(metaParam, keyProperties[i], metaResult.getValue(keyColumns[i]));
            }
        }
    }

    private void setValue(MetaObject metaParam, String property, Object value) {
        if (metaParam.hasSetter(property)) {
            metaParam.setValue(property, value);
        } else {
            throw new RuntimeException("No setter found for the keyProperty '" + property + "' in " + metaParam.getOriginalObject().getClass().getName() + ".");
        }
    }
}
