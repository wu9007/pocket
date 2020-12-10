## 0.1.9.PRE - 2020/06/03
* PERF: 添加 `collectLog` 配置项，默认 `false`， 当设置为 `true` 时收集语句执行数据（包含：sql、执行效率、前后镜像）
* PERF: 在项目中自定义日志监听类，并实现`PersistenceLogObserver`接口的`dealWithPersistenceLog`方法，自定义对日志地处理。

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
* PERF: 启动时检测多数据源配置中的 `nodeName` 不可重复。

## 0.1.11.PRE - 2020/06/26
* PERF: 开关会话和事务打印日志改为debug。

## 0.1.12.PRE - 2020/07/01
* PERF: 删除tableId，修改主键生成策略。
* PERF: 添加数值类型的主键生成策略。

## 0.1.13.PRE - 2020/07/01
* PERF: 可级联查询不清空过滤条件。
* PERF: 将serverId从pocket配置中去除。

## 0.1.15.PRE - 2020/07/08
* FIX BUG.

## 0.1.16.PRE - 2020/07/13
* PERF: SQLQuery支持增删改。

## 0.1.19.PRE - 2020/07/17
* PERF: 修改 Session 类：
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
* PERF: 支持批量语句执行(需要注意是否开启事务)
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
* PERF: 查询当下时间：
`LocalDateTime localDateTime = this.session.createSQLQuery().now()`

#0.1.24.PRE - 2020/07/29
* PERF: Upgrade Criteria.or and Criteria.and
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
- PERF: 语句执行时长大于`pocket.datasource.node.warningLogTimeout`设置的值时则打印警告日志到控制台和文件。
- PERF: 语句执行失败后打印异常语句到控制台和文件。

#0.1.28.PRE - 2020/08/07
- PERF: @Column @ManyToOne 添加属性 ignoreCompare 标识在更新数据比较实体获取脏数据时是否忽略该属性（默认不忽略）

# 0.1.32.PRE - 2020/08/18
- PERF: @Column添加encryptModel参数标注持久化时选择的加密方式以及查询时选择的解密方式。
- PERF: 加密字段禁止使用PoEl表达式进行更新。
- PERF: 使用SQLQuery更新加密字段时需要使用`EncryptUtil`手动进行加密。

# 0.1.34.PRE - 2020/08/18
- PERF: oracle 添加 limit 方言。
- PERF: CriteriaImpl 的 count() 返回值有 long 修改为 Number。

# 0.1.37.PRE - 2020/08/20
- PERF: @Join 以相同方式关联同一张表则在生成的sql中以and链接（解决重复链接同一张表的问题）。

# 0.1.38.PRE - 2020/08/20
- PERF: Criteria 添加方法 specifyField 指定部分查询列。
```java
Criteria criteria = this.session.createCriteria(Order.class);
criteria.add(Restrictions.equ("code", "C-006"))
        .add(Sort.asc("code"))
        .specifyField("code", "price");
List<Order> orderList = criteria.list();
```

# 0.1.39.PRE - 2020/08/21
- PERF: debug日志打印sql中的具体参数。
# 0.1.40.PRE - 2020/08/21
- FIX: Join bug。
# 0.1.41.PRE - 2020/08/21
- FIX: complete prepared statement bug.
# 0.1.42.PRE - 2020/08/26
- PERF: add oneToOne Annotation.
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

# 0.2.3.PRE - 2020/09/14
PERF: `criteria.limit(0, 1).findOne()` 查询单条记录。

# 0.2.3.PRE - 2020/09/15
FIX: 链接oracle数据库使用SQL查询单条记录时last()方法引起报错。

# 0.2.5.PRE - 2020/09/15
FIX: oracle limit 函数报数组下标越界错误。

# 0.2.6.PRE - 2020/10/24
FEAT: 关联查询被标记加密的字段。

# 0.2.9.PRE - 2020/11/09
FIX: SQL语句参数匹配错误 (例：select ‘00:02’ from t_user 语句中会将:02视为参数)，参数不可包含数字以修复此问题。

# 0.2.12.PRE - 2020/11/18
FEAT: 链接池添加保活线程。

# 0.2.13.PRE - 2020/11/24
FIX: 修复一对一查询异常（获取不到父类 Field）。

# 0.2.14.PRE - 2020/11/25
## FEAT：
- availableInterval 维护链接池中链接可用性的时间间隔（每几【默认1800秒】多少秒使用链接池中的链接去执行一个简单的查询操作） 
- miniInterval 维护连接池中的最小链接数的时间间隔（默认36000秒）

# 0.2.17.PRE - 2020/11/26
## PERF
- timeout 默认值设置为 2s，修复获取链接长时间阻塞问题。
## FIX
- 修复查询加密数据问题: Restrictions.or(Restrictions...) 根据加密字段进行查询找不到值。

# 0.2.22.PRE - 2020/11/27
## FIX
- SQLQueryImpl 查询异常
- 保活线程释放连接后将链接从连接池中移除
## PERF
- 连接池中的链接不可用时销毁并重新创建

# 0.2.27.PRE - 2020/11/30
## FIX
- 修复oracle分页查询排序问题。
- 修复oracle查询当前时间问题。

# 0.2.30.PRE - 2020/12/09
## FEAT
- 添加SM4加解密工具，数据库字段可配置 EncryptType.SM4_CEB EncryptType.SM4_CBC 对数据进行加密
- 数据库账户和密码加密
## PERF
- 如果需要使用数据库字段加密和数据库用户名或密码加密，则需要在初始化orm资源前设置 des 和 sm2 的密钥

```java
@Bean
Object getPersistenceConfig() {
    return new Object() {
        @Autowired
        private PocketConfig pocketConfig;

        @PostConstruct
        public void run() throws PocketMapperException {
            // init pocket resource.
            this.pocketConfig.setDesKey("sward007")
                    .setSm4Key("sward18713839007")
                    .init();
        }
    };
}
```
