# POCKET
## 多数据源配置
**这里以MySQL数据库为例**

### 配置信息
```json
pocket:
  serverId: 200
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
        ##根据session找到数据库并从数据库对应的连接池中获取数据库链接，故所有session不可重复
        session: order,commodity
  ##redis服务
  cache:
      ##应用层缓存
      logic:
        hostName: 127.0.0.1
        port: 6079
      ##数据库层缓存
      base:
        hostName: 127.0.0.1
        port: 6666
```

## 实体类规范
- 继承`BaseEntity`抽象类
- 类注解`@Entity`，`table` 对应数据库表名；`tableId` 对应数据库表标识，目的是为了在生成数据标识的时候区分表；`uuidGenerator` 对应主键生成策略，默认 `increment`，可通过集成 `AbstractUuidGenerator` 自定义主键生成策。
- 属性注解`@Column`，`name` 对应数据库中对应的列名称
- 属性注解`@OneToMany`， `clazz` 对应子类的类类型，`name` 对应该表数据标识在其子表中的字段名称
- 属性注解`@ManyToOne`，`name` 关联主表数据标识的列名称
### 主类
```java
@Entity(table = "TBL_ORDER", tableId = 200)
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
@Entity(table = "TBL_COMMODITY", tblId = 201)
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

### 数据操作(具体操作请参考接口文档)
#### 获取缓存对象
通过`SessionFactory`的静态方法`Session getSession(String sessionName)`获取对象。
#### 使用 Criteria 根据条件查询数据
```java
private Session session = SessionFactory.getSession("homo");
session.open();
private Transaction transaction = session.getTransaction();
transaction.begin();

Criteria criteria = this.session.creatCriteria(Order.class);
criteria.add(Restrictions.like("code", "%A%"))
        .add(Restrictions.ne("code", "A-002"))
        .add(Restrictions.or(Restrictions.gt("price", 13), Restrictions.lt("price", 12.58)))
        .add(Sort.desc("price"))
        .add(Sort.asc("uuid"));
List orderList = criteria.list();

transaction.commit();
session.close();
```

#### 使用 Criteria 更新数据
```java
// 省略session开关操作
Criteria criteria = this.session.creatCriteria(Order.class);
criteria.add(Modern.set("price", 500.5D))
        .add(Restrictions.equ("code", "C-001"))
        .add(Modern.set("day", new Date()));
System.out.println(criteria.update());
```
#### 使用 Criteria 根据条件删除数据
```java
// 省略session开关操作
Criteria criteria = session.creatCriteria(Order.class);
criteria.add(Restrictions.equ("uuid", 1011011L));
criteria.delete();
```

#### 使用 ProcessQuery 调用存储过程查询数据
```java
// 省略session开关操作
ProcessQuery<Order> processQuery = session.createProcessQuery("{call test(?)}");
processQuery.setParameters(new String[]{"蚂蚁"});
Function<ResultSet, Order> mapperFunction = (resultSet) -> {
    try {
        Order order = new Order();
        order.setCode(resultSet.getString(1));
        return order;
    } catch (SQLException e) {
        e.printStackTrace();
        return null;
    }
};
Order order = processQuery.unique(mapperFunction);
System.out.println(order.getCode());
```

> 笔者不建议在程序中拼 `SQL`, 故未对 `SQLQuery` 做过多的支持，此处就不做赘述。
