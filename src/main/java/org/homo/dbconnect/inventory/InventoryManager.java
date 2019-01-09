package org.homo.dbconnect.inventory;

import org.homo.core.annotation.Column;
import org.homo.core.model.BaseEntity;
import org.homo.dbconnect.transaction.Transaction;
import org.homo.dbconnect.query.AbstractQuery;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.function.Predicate;

/**
 * @author wujianchuan 2018/12/31
 */

public interface InventoryManager {

    String MYSQL_DB_NAME = "com.mysql.cj.jdbc.Driver";
    String SERIAL_VERSION_UID = "serialVersionUID";
    Predicate<Field> NO_MAPPING_FILTER = field -> !SERIAL_VERSION_UID.equals(field.getName()) && field.getAnnotation(Column.class) != null;

    /**
     * 对应的数据库名称
     *
     * @return 数据库名称
     */
    String getDbName();

    /**
     * 获取事务对象
     *
     * @return 事务对象
     */
    Transaction getTransaction();

    /**
     * 保存
     *
     * @param entity 实体对象
     * @return 实体对象
     * @throws SQLException           语句异常
     * @throws IllegalAccessException 反射异常
     */
    BaseEntity save(BaseEntity entity) throws SQLException, IllegalAccessException;

    /**
     * 更新实体
     *
     * @param entity 实体对象
     * @return 实体对象
     */
    BaseEntity update(BaseEntity entity) throws IllegalAccessException, SQLException, InstantiationException;

    /**
     * 删除实体
     *
     * @param entity 实体对象
     * @return 影响行数
     */
    int delete(BaseEntity entity);

    /**
     * 查询对象
     *
     * @param clazz 类类型
     * @param uuid  数据标识
     * @return 实体对象
     * @throws SQLException           语句异常
     * @throws IllegalAccessException 反射异常
     * @throws InstantiationException 反射异常
     */
    BaseEntity findOne(Class clazz, Long uuid) throws SQLException, IllegalAccessException, InstantiationException;

    /**
     * 获取SQL查询对象
     *
     * @param sql 查询语句
     * @return 查询对象
     */
    AbstractQuery createSQLQuery(String sql);

    /**
     * 获取最大数据标识
     *
     * @param clazz 实体类型
     * @return 最大值
     * @throws SQLException 语句异常
     */
    long getMaxUuid(Class clazz) throws SQLException;
}
