package org.hunter.pocket.utils;

import org.hunter.pocket.annotation.Column;
import org.hunter.pocket.annotation.Join;
import org.hunter.pocket.annotation.ManyToOne;
import org.hunter.pocket.criteria.Modern;
import org.hunter.pocket.criteria.ParameterTranslator;
import org.hunter.pocket.criteria.Restrictions;
import org.hunter.pocket.criteria.SqlBean;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * @author wujianchuan 2019/1/3
 */
public class FieldTypeStrategy {

    private class PreparedSupplierValue {
        private final PreparedStatement preparedStatement;
        private final Integer index;
        private Restrictions restrictions;
        private Modern modern;
        private ParameterTranslator parameterTranslator;

        PreparedSupplierValue(PreparedStatement preparedStatement, Integer index, Restrictions restrictions) {
            this.preparedStatement = preparedStatement;
            this.index = index;
            this.restrictions = restrictions;
        }

        PreparedSupplierValue(PreparedStatement preparedStatement, Integer index, Modern modern) {
            this.preparedStatement = preparedStatement;
            this.index = index;
            this.modern = modern;
        }

        PreparedSupplierValue(PreparedStatement preparedStatement, Integer index, ParameterTranslator parameterTranslator) {
            this.preparedStatement = preparedStatement;
            this.index = index;
            this.parameterTranslator = parameterTranslator;
        }

        PreparedStatement getPreparedStatement() {
            return preparedStatement;
        }

        Integer getIndex() {
            return index;
        }

        SqlBean getSqlBean() {
            if (this.restrictions != null) {
                return restrictions;
            } else if (this.modern != null) {
                return this.modern;
            } else {
                return this.parameterTranslator;
            }
        }
    }

    private static final FieldTypeStrategy STRATEGY = new FieldTypeStrategy();
    private static final Map<String, BiFunction<ResultSet, String, Object>> RESULT_STRATEGY_POOL = new ConcurrentHashMap<>(20);
    private static final Map<String, Consumer<PreparedSupplierValue>> PREPARED_STRATEGY_POOL = new ConcurrentHashMap<>(20);

    static {
        RESULT_STRATEGY_POOL.put((int.class.getName()), (resultSet, columnName) -> {
            try {
                return resultSet.getInt(columnName);
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        });
        RESULT_STRATEGY_POOL.put((float.class.getName()), (resultSet, columnName) -> {
            try {
                return resultSet.getFloat(columnName);
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        });
        RESULT_STRATEGY_POOL.put((double.class.getName()), (resultSet, columnName) -> {
            try {
                return resultSet.getDouble(columnName);
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        });
        RESULT_STRATEGY_POOL.put((long.class.getName()), (resultSet, columnName) -> {
            try {
                return resultSet.getLong(columnName);
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        });
        RESULT_STRATEGY_POOL.put((boolean.class.getName()), (resultSet, columnName) -> {
            try {
                return resultSet.getBoolean(columnName);
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        });

        RESULT_STRATEGY_POOL.put(String.class.getName(), (resultSet, columnName) -> {
            try {
                return resultSet.getString(columnName);
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        });
        RESULT_STRATEGY_POOL.put(BigDecimal.class.getName(), (resultSet, columnName) -> {
            try {
                return resultSet.getBigDecimal(columnName);
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        });
        RESULT_STRATEGY_POOL.put(Long.class.getName(), (resultSet, columnName) -> {
            try {
                return resultSet.getLong(columnName);
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        });
        RESULT_STRATEGY_POOL.put(Date.class.getName(), (resultSet, columnName) -> {
            try {
                return resultSet.getTimestamp(columnName);
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        });
        RESULT_STRATEGY_POOL.put(Boolean.class.getName(), (resultSet, columnName) -> {
            try {
                return resultSet.getBoolean(columnName);
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        });
        RESULT_STRATEGY_POOL.put(Integer.class.getName(), (resultSet, columnName) -> {
            try {
                return resultSet.getInt(columnName);
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        });
        RESULT_STRATEGY_POOL.put(Double.class.getName(), (resultSet, columnName) -> {
            try {
                return resultSet.getDouble(columnName);
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        });
        RESULT_STRATEGY_POOL.put(Serializable.class.getName(), (resultSet, columnName) -> {
            try {
                return resultSet.getObject(columnName);
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        });
// ====================================================================================================//
        PREPARED_STRATEGY_POOL.put(int.class.getName(), (value) -> {
            PreparedStatement preparedStatement = value.getPreparedStatement();
            SqlBean sqlBean = value.getSqlBean();
            try {
                preparedStatement.setInt(value.getIndex(), (Integer) sqlBean.getTarget());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        PREPARED_STRATEGY_POOL.put(float.class.getName(), (value) -> {
            PreparedStatement preparedStatement = value.getPreparedStatement();
            SqlBean sqlBean = value.getSqlBean();
            try {
                preparedStatement.setFloat(value.getIndex(), (Float) sqlBean.getTarget());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        PREPARED_STRATEGY_POOL.put(double.class.getName(), (value) -> {
            PreparedStatement preparedStatement = value.getPreparedStatement();
            SqlBean sqlBean = value.getSqlBean();
            try {
                preparedStatement.setDouble(value.getIndex(), (Double) sqlBean.getTarget());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        PREPARED_STRATEGY_POOL.put(long.class.getName(), (value) -> {
            PreparedStatement preparedStatement = value.getPreparedStatement();
            SqlBean sqlBean = value.getSqlBean();
            try {
                preparedStatement.setLong(value.getIndex(), (Long) sqlBean.getTarget());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        PREPARED_STRATEGY_POOL.put(boolean.class.getName(), (value) -> {
            PreparedStatement preparedStatement = value.getPreparedStatement();
            SqlBean sqlBean = value.getSqlBean();
            try {
                preparedStatement.setBoolean(value.getIndex(), (Boolean) sqlBean.getTarget());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        PREPARED_STRATEGY_POOL.put(String.class.getName(), (value) -> {
            PreparedStatement preparedStatement = value.getPreparedStatement();
            SqlBean sqlBean = value.getSqlBean();
            try {
                preparedStatement.setString(value.getIndex(), (String) sqlBean.getTarget());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        PREPARED_STRATEGY_POOL.put(BigDecimal.class.getName(), (value) -> {
            PreparedStatement preparedStatement = value.getPreparedStatement();
            SqlBean sqlBean = value.getSqlBean();
            try {
                preparedStatement.setBigDecimal(value.getIndex(), (BigDecimal) sqlBean.getTarget());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        PREPARED_STRATEGY_POOL.put(Long.class.getName(), (value) -> {
            PreparedStatement preparedStatement = value.getPreparedStatement();
            SqlBean sqlBean = value.getSqlBean();
            try {
                preparedStatement.setLong(value.getIndex(), (Long) sqlBean.getTarget());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        PREPARED_STRATEGY_POOL.put(Date.class.getName(), (value) -> {
            PreparedStatement preparedStatement = value.getPreparedStatement();
            SqlBean sqlBean = value.getSqlBean();
            try {
                Date date = (Date) sqlBean.getTarget();
                preparedStatement.setTimestamp(value.getIndex(), new java.sql.Timestamp(date.getTime()));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        PREPARED_STRATEGY_POOL.put(Integer.class.getName(), (value) -> {
            PreparedStatement preparedStatement = value.getPreparedStatement();
            SqlBean sqlBean = value.getSqlBean();
            try {
                preparedStatement.setInt(value.getIndex(), (Integer) sqlBean.getTarget());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        PREPARED_STRATEGY_POOL.put(Double.class.getName(), (value) -> {
            PreparedStatement preparedStatement = value.getPreparedStatement();
            SqlBean sqlBean = value.getSqlBean();
            try {
                preparedStatement.setDouble(value.getIndex(), (Double) sqlBean.getTarget());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        PREPARED_STRATEGY_POOL.put(Boolean.class.getName(), (value) -> {
            PreparedStatement preparedStatement = value.getPreparedStatement();
            SqlBean sqlBean = value.getSqlBean();
            try {
                preparedStatement.setBoolean(value.getIndex(), (Boolean) sqlBean.getTarget());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        PREPARED_STRATEGY_POOL.put(Integer.class.getName(), (value) -> {
            PreparedStatement preparedStatement = value.getPreparedStatement();
            SqlBean sqlBean = value.getSqlBean();
            try {
                preparedStatement.setInt(value.getIndex(), (Integer) sqlBean.getTarget());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        PREPARED_STRATEGY_POOL.put(Double.class.getName(), (value) -> {
            PreparedStatement preparedStatement = value.getPreparedStatement();
            SqlBean sqlBean = value.getSqlBean();
            try {
                preparedStatement.setDouble(value.getIndex(), (Double) sqlBean.getTarget());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        PREPARED_STRATEGY_POOL.put(Serializable.class.getName(), (value) -> {
            PreparedStatement preparedStatement = value.getPreparedStatement();
            SqlBean sqlBean = value.getSqlBean();
            try {
                preparedStatement.setObject(value.getIndex(), sqlBean.getTarget());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private FieldTypeStrategy() {
    }

    public static FieldTypeStrategy getInstance() {
        return STRATEGY;
    }

    public Object getColumnValue(Field field, ResultSet resultSet) {
        field.setAccessible(true);
        return RESULT_STRATEGY_POOL.get(field.getType().getName()).apply(resultSet, field.getName());
    }

    public Object getMappingColumnValue(Field field, ResultSet resultSet) {
        Column column = field.getAnnotation(Column.class);
        ManyToOne manyToOne = field.getAnnotation(ManyToOne.class);
        Join join = field.getAnnotation(Join.class);
        String columnName;
        if (column != null) {
            columnName = column.name();
        } else if (manyToOne != null) {
            columnName = manyToOne.columnName();
        } else if (join != null) {
            String columnSurname = join.columnSurname().trim();
            columnName = columnSurname.length() > 0 ? columnSurname : join.columnName();
        } else {
            throw new NullPointerException("未找到注解");
        }
        field.setAccessible(true);
        return RESULT_STRATEGY_POOL.get(field.getType().getName()).apply(resultSet, columnName);
    }

    public void setPreparedStatement(PreparedStatement preparedStatement, List<ParameterTranslator> parameters, List<Restrictions> restrictionsList) {
        this.setPreparedStatement(preparedStatement, parameters);
        for (int index = 0; index < restrictionsList.size(); index++) {
            Restrictions restrictions = restrictionsList.get(index);
            PreparedSupplierValue preparedSupplierValue = new PreparedSupplierValue(preparedStatement, parameters.size() + index + 1, restrictions);
            this.apply(preparedStatement, preparedSupplierValue);
        }
    }

    public void setPreparedStatement(PreparedStatement preparedStatement, List<ParameterTranslator> parameterTranslatorList) {
        for (int index = 0; index < parameterTranslatorList.size(); index++) {
            ParameterTranslator parameterTranslator = parameterTranslatorList.get(index);
            PreparedSupplierValue preparedSupplierValue = new PreparedSupplierValue(preparedStatement, index + 1, parameterTranslator);
            this.apply(preparedStatement, preparedSupplierValue);
        }
    }

    private void apply(PreparedStatement preparedStatement, PreparedSupplierValue preparedSupplierValue) {
        SqlBean sqlBean = preparedSupplierValue.getSqlBean();
        if (sqlBean.getTarget() != null) {
            PREPARED_STRATEGY_POOL.get(sqlBean.getTarget().getClass().getName()).accept(preparedSupplierValue);
        } else {
            try {
                preparedStatement.setObject(preparedSupplierValue.getIndex(), sqlBean.getTarget());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
