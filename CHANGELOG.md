## 0.1.9.PRE - 2020/06/03
* 添加 `collectLog` 配置项，默认 `false`， 当设置为 `true` 时收集语句执行数据（包含：sql、执行效率、前后镜像）
* 在项目中自定义日志监听类，并实现`PersistenceLogObserver`接口的`dealWithPersistenceLog`方法，自定义对日志地处理。

例：
```java
@Component
public class PersistenceLogObserverImpl implements PersistenceLogObserver {
    public PersistenceLogObserverImpl() {
        PersistenceLogSubject.getInstance().registerObserver(this);
    }

    @Override
    public void dealWithPersistenceLog(String s) {
        System.out.println(s);
    }
}
```

## 0.1.10.PRE - 2020/06/12
* 启动时检测多数据源配置中的 `nodeName` 不可重复。

## 0.1.11.PRE - 2020/06/26
* 开关会话和事务打印日志改为debug。

## 0.1.12.PRE - 2020/07/01
* 删除tableId，修改主键生成策略。
* 添加数值类型的主键生成策略。

## 0.1.13.PRE - 2020/07/01
* 可级联查询不清空过滤条件。
* 将serverId从pocket配置中去除。

## 0.1.15.PRE - 2020/07/08
* FIX BUG.

## 0.1.16.PRE - 2020/07/13
* SQLQuery支持增删改。

## 0.1.19.PRE - 2020/07/17
* 修改 Session 类：
```java
    /**
     * 级联查询对象
     *
     * @param clazz    类类型
     * @param identify 数据标识
     * @return 实体对象
     * @throws SQLException e
     */
    <T extends AbstractEntity> T findOne(Class<T> clazz, Serializable identify) throws SQLException;


    /**
     * 查询对象
     *
     * @param clazz    类类型
     * @param identify 数据标识
     * @param cascade  是否进行级联保存操作
     * @return 实体对象
     * @throws SQLException e
     */
    <T extends AbstractEntity> T findOne(Class<T> clazz, Serializable identify, boolean cascade) throws SQLException;

    /**
     * 级联删除
     *
     * @param entity 实体对象
     * @return 影响行数
     * @throws SQLException           语句异常
     * @throws IllegalAccessException e
     */
    int delete(AbstractEntity entity) throws SQLException, IllegalAccessException;

    /**
     * 删除实体
     *
     * @param entity  实体对象
     * @param cascade 是否进行级联更新操作
     * @return 影响行数
     * @throws SQLException           语句异常
     * @throws IllegalAccessException e
     */
    int delete(AbstractEntity entity, boolean cascade) throws SQLException, IllegalAccessException;
```

## 0.1.21.PRE - 2020/07/20
* Fix Transaction Bug.

## 0.1.22.PRE - 2020/07/22
* 支持批量语句执行(需要注意是否开启事务)
```java
SQLQuery queryInsert = this.session.createSQLQuery("insert into tbl_order(uuid,code,price) values(:UUID, :CODE, :PRICE)");
for (int index = 0; index < 10; index++) {
    queryInsert.setParameter("UUID", "2020" + index)
            .setParameter("CODE", "C-00" + index)
            .setParameter("PRICE", index)
            .addBatch();
}
int[] rowInserts = queryInsert.executeBatch();
```

# 0.1.23.PRE - 2020/07/28
* 查询当下时间：
`LocalDateTime localDateTime = this.session.createSQLQuery().now()`

#0.1.24.PRE - 2020/07/29
* Upgrade Criteria.or and Criteria.and
```java
@Test
public void test28() {
    Criteria criteria = this.session.createCriteria(Order.class);
    criteria.add(Restrictions.and(
            Restrictions.or(
                    Restrictions.gt("price", 13),
                    Restrictions.lt("price", 12.58),
                    Restrictions.lt("type", "001")),
            Restrictions.like("code", "%A%")
    ))
            .add(Sort.asc("code"));
    List<Order> orderList = criteria.list();
    orderList.forEach(order -> System.out.println(order.getPrice()));
}
```

#0.1.26.PRE - 2020/07/31
- 语句执行时长大于`pocket.datasource.node.warningLogTimeout`设置的值时则打印警告日志到控制台和文件。
- 语句执行失败后打印异常语句到控制台和文件。

#0.1.28.PRE - 2020/08/07
- @Column @ManyToOne 添加属性 ignoreCompare 标识在更新数据比较实体获取脏数据时是否忽略该属性（默认不忽略）

# 0.1.32.PRE - 2020/08/18
- @Column添加encryptModel参数标注持久化时选择的加密方式以及查询时选择的解密方式。
- 加密字段禁止使用PoEl表达式进行更新。
- 使用SQLQuery更新加密字段时需要使用`EncryptUtil`手动进行加密。

# 0.1.34.PRE - 2020/08/18
- oracle 添加 limit 方言。
- CriteriaImpl 的 count() 返回值有 long 修改为 Number。

# 0.1.37.PRE - 2020/08/20
- @Join 以相同方式关联同一张表则在生成的sql中以and链接（解决重复链接同一张表的问题）。

# 0.1.38.PRE - 2020/08/20
- Criteria 添加方法 specifyField 指定部分查询列。
```java
Criteria criteria = this.session.createCriteria(Order.class);
criteria.add(Restrictions.equ("code", "C-006"))
        .add(Sort.asc("code"))
        .specifyField("code", "price");
List<Order> orderList = criteria.list();
```

# 0.1.39.PRE - 2020/08/21
- debug日志打印sql中的具体参数。
# 0.1.40.PRE - 2020/08/21
- FIX Join bug。
# 0.1.41.PRE - 2020/08/21
- FIX complete prepared statement bug.
# 0.1.42.PRE - 2020/08/26
- PERF add oneToOne Annotation.
```java
public class Order {

    @Column
    private String typeUuid;
    @OneToOne(ownField = "typeUuid", relatedField = "uuid")
    private OrderType orderType;
}

public class OrderType {

    @Identify
    @Column
    private String uuid;
    @Column
    private String name;
}
```

# 0.2.1.PRE - 2020/09/10
FIX: 修复主键生成策略。
