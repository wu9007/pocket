package org.homo.pocket.utils;

import org.homo.pocket.annotation.Column;
import org.homo.pocket.annotation.ManyToOne;
import org.homo.pocket.criteria.Restrictions;

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

        PreparedSupplierValue(PreparedStatement preparedStatement, Integer index, Restrictions restrictions) {
            this.preparedStatement = preparedStatement;
            this.index = index;
            this.restrictions = restrictions;
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

        Restrictions getRestrictions() {
            return restrictions;
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

        PREPARED_STRATEGY_POOL.put(String.class.getName(), (value) -> {
            PreparedStatement preparedStatement = value.getPreparedStatement();
            Restrictions restrictions = value.getRestrictions();
            try {
                preparedStatement.setString(value.getIndex(), (String) restrictions.getTarget());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        PREPARED_STRATEGY_POOL.put(BigDecimal.class.getName(), (value) -> {
            PreparedStatement preparedStatement = value.getPreparedStatement();
            Restrictions restrictions = value.getRestrictions();
            try {
                preparedStatement.setBigDecimal(value.getIndex(), (BigDecimal) restrictions.getTarget());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        PREPARED_STRATEGY_POOL.put(Long.class.getName(), (value) -> {
            PreparedStatement preparedStatement = value.getPreparedStatement();
            Restrictions restrictions = value.getRestrictions();
            try {
                preparedStatement.setLong(value.getIndex(), (Long) restrictions.getTarget());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        PREPARED_STRATEGY_POOL.put(Date.class.getName(), (value) -> {
            PreparedStatement preparedStatement = value.getPreparedStatement();
            Restrictions restrictions = value.getRestrictions();
            try {
                Date date = (Date) restrictions.getTarget();
                preparedStatement.setTimestamp(value.getIndex(), new java.sql.Timestamp(date.getTime()));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        PREPARED_STRATEGY_POOL.put(Integer.class.getName(), (value) -> {
            PreparedStatement preparedStatement = value.getPreparedStatement();
            Restrictions restrictions = value.getRestrictions();
            try {
                preparedStatement.setInt(value.getIndex(), (Integer) restrictions.getTarget());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        PREPARED_STRATEGY_POOL.put(Double.class.getName(), (value) -> {
            PreparedStatement preparedStatement = value.getPreparedStatement();
            Restrictions restrictions = value.getRestrictions();
            try {
                preparedStatement.setDouble(value.getIndex(), (Double) restrictions.getTarget());
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

    public void setPreparedStatement(PreparedStatement preparedStatement, List<Restrictions> restrictionsList) {
        for (int index = 0; index < restrictionsList.size(); index++) {
            Restrictions restrictions = restrictionsList.get(index);
            PreparedSupplierValue preparedSupplierValue = new PreparedSupplierValue(preparedStatement, index + 1, restrictions);
            PREPARED_STRATEGY_POOL.get(restrictions.getTarget().getClass().getName()).accept(preparedSupplierValue);
        }
        restrictionsList.clear();
    }
}
