# POCKET
## 多数据源配置
**这里以MySQL数据库为例**

### 配置信息
```json
pocket:
  datasource:
    node:
      - url: jdbc:mysql://127.0.0.1:3306/pocket1
        nodeName: mysql-01
        driverName: com.mysql.cj.jdbc.Driver
        showSql: false
        user: root
        password: root
        poolMiniSize: 10
        poolMaxSize: 15
        timeout: 1000
        session: homo,user
      - url: jdbc:mysql://127.0.0.1:3306/pocket2
        nodeName: mysql-01-02
        driverName: com.mysql.cj.jdbc.Driver
        showSql: true
        user: root
        password: root
        poolMiniSize: 10
        poolMaxSize: 15
        timeout: 1000
        session: order,commodity
  cache:
      logic:
        hostName: 127.0.0.1
        port: 6079
      base:
        hostName: 127.0.0.1
        port: 6666
```
### 数据操作
#### 获取数据库链接
调用`ConnectionManager`的`getConnection(DatabaseNodeConfig databaseNodeConfig)`API即可从连接池中拿到一个闲置状态的数据库链接对象。
看栗子：
```java
ConnectionManager.getInstance().getConnection(databaseNodeConfig);
```
#### 获取缓存对象
通过`SessionFactory`的静态方法`Session getSession(String sessionName)`获取对象。
#### 查询数据
```java
private Session session = SessionFactory.getSession("homo");
session.open();
private Transaction transaction = session.getTransaction();
transaction.begin();

Criteria criteria = this.session.creatCriteria(Order.class);
criteria.add(Restrictions.like("code", "%A%"))
        .add(Restrictions.ne("code", "A-002"))
        .add(Restrictions.or(Restrictions.gt("price", 13), Restrictions.lt("price", 12.58)));
List orderList = criteria.list();

transaction.commit();
session.close();
```

## 实体类规范
- 继承`BaseEntity`抽象类
- 类注解`@Entity`，`history` 属性控制历史保存功能是否开启，`table` 对应代表对应数据库表
- 属性注解`@column`，`name` 对应数据库中对应的列名称
- 属性注解`@OneToMany`， `clazz` 对应子类的类类型，`name` 对应该表数据标识在其子表中的字段名称
- 属性注解`@ManyToOne`，`name` 关联主表数据标识的列名称
### 主类
```java
@Entity(table = "TBL_ORDER")
public class Order extends BaseEntity {
    private static final long serialVersionUID = 2560385391551524826L;

    @Column(name = "CODE")
    private String code;
    @Column(name = "PRICE")
    private BigDecimal price;

    @OneToMany(clazz = Commodity.class, name = "ORDER_UUID")
    private List<Commodity> commodities;

   // 这里省略 getter setter
}
```
### 子类
```java
@Entity(table = "TBL_COMMODITY")
public class Commodity extends BaseEntity {
    private static final long serialVersionUID = -6711578420837877371L;

    @Column(name = "NAME")
    private String name;
    @Column(name = "PRICE")
    private BigDecimal price;

    @ManyToOne(name = "ORDER_UUID")
    private Long order;

    // getter setter
}
```
