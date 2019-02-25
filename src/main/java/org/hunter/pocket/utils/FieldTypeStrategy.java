package org.hunter.pocket.utils;

import org.hunter.pocket.annotation.Column;
import org.hunter.pocket.annotation.ManyToOne;
import org.hunter.pocket.criteria.Modern;
import org.hunter.pocket.criteria.Restrictions;
import org.hunter.pocket.criteria.SqlBean;

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
        private PreparedStatement preparedStatement;
        private Integer index;
        private Restrictions restrictions;
        private Modern modern;

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

        PreparedStatement getPreparedStatement() {
            return preparedStatement;
        }

        public void setPreparedStatement(PreparedStatement preparedStatement) {
            this.preparedStatement = preparedStatement;
        }

        Integer getIndex() {
            return index;
        }

        public void setIndex(Integer index) {
            this.index = index;
        }

        SqlBean getSqlBean() {
            if (this.restrictions != null) {
                return restrictions;
            } else {
                return this.modern;
            }
        }

        public void setRestrictions(Restrictions restrictions) {
            this.restrictions = restrictions;
        }
    }

    private static FieldTypeStrategy strategy = new FieldTypeStrategy();
    private static final Map<String, BiFunction<ResultSet, String, Object>> RESULT_STRATEGY_POOL = new ConcurrentHashMap<>(20);
    private static final Map<String, Consumer<PreparedSupplierValue>> PREPARED_STRATEGY_POOL = new ConcurrentHashMap<>(20);

    static {
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
    }

    private FieldTypeStrategy() {
    }

    public static FieldTypeStrategy getInstance() {
        return strategy;
    }

    public Object getColumnValue(Field field, ResultSet resultSet) {
        Column column = field.getAnnotation(Column.class);
        ManyToOne manyToOne = field.getAnnotation(ManyToOne.class);
        String columnName;
        if (column != null) {
            columnName = column.name();
        } else if (manyToOne != null) {
            columnName = manyToOne.name();
        } else {
            throw new NullPointerException("未找到注解");
        }
        return RESULT_STRATEGY_POOL.get(field.getType().getName()).apply(resultSet, columnName);
    }

    public void setPreparedStatement(PreparedStatement preparedStatement, List<Modern> modernList, List<Restrictions> restrictionsList) {
        for (int index = 0; index < modernList.size(); index++) {
            Modern modern = modernList.get(index);
            PreparedSupplierValue preparedSupplierValue = new PreparedSupplierValue(preparedStatement, index + 1, modern);
            PREPARED_STRATEGY_POOL.get(modern.getTarget().getClass().getName()).accept(preparedSupplierValue);
        }
        for (int index = 0; index < restrictionsList.size(); index++) {
            Restrictions restrictions = restrictionsList.get(index);
            PreparedSupplierValue preparedSupplierValue = new PreparedSupplierValue(preparedStatement, modernList.size() + index + 1, restrictions);
            PREPARED_STRATEGY_POOL.get(restrictions.getTarget().getClass().getName()).accept(preparedSupplierValue);
        }
    }
}
