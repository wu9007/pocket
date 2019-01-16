# Homo
[![Version](https://img.shields.io/badge/Version-0.0.1-brightgreen.svg)](https://github.com/leyan95/Homo)
[![Build Status](https://travis-ci.org/leyan95/Homo.svg?branch=master)](https://travis-ci.org/leyan95/Homo)

Little scaffold

#### About me
[![github](https://img.shields.io/badge/GitHub-leyan95-blue.svg)](https://github.com/leyan95)

> 一个清新脱俗的小框架，欢迎 request

## 多数据源配置
**这里以MySQL数据库为例**

### 配置信息
```json
pocket:
  datasource:
    node:
      - url: jdbc:mysql://127.0.0.1:3306/homo?serverTimezone=GMT%2B8&useUnicode=true&characterEncoding=UTF-8
        nodeName: mysql-01
        driverName: com.mysql.cj.jdbc.Driver
        showSql: false
        user: root
        password: root
        poolMiniSize: 10
        poolMaxSize: 15
        timeout: 1000
        session: homo,user
      - url: jdbc:mysql://127.0.0.1:3306/homo?serverTimezone=GMT%2B8&useUnicode=true&characterEncoding=UTF-8
        nodeName: mysql-01-02
        driverName: com.mysql.cj.jdbc.Driver
        showSql: true
        user: root
        password: root
        poolMiniSize: 10
        poolMaxSize: 15
        timeout: 1000
        session: order,commodity
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

    // 这里省略 getter setter
}
```

## 持久化类规范
- 类注解`@Repository`，通过`database`配置数据源
- 继承`AbstractRepository<T extend BaseEntity>`抽象类，可在方法内直接使用`this.Session`对象
- 实现三个持久化方法（save, update, delete）
- 调用个方法时需要先通过`getProxy()`获取代理调用
```java
@Repository(database = "mysql")
public class OrderRepositoryImpl extends AbstractRepository<Order> implements OrderRepository {

    @Override
    public Order save(Order entity, User operator) throws Exception {
        return (Order) this.session.save(entity);
    }

    @Override
    public Order update(Order entity, User operator) throws Exception {
        return (Order) this.session.update(entity);
    }

    @Override
    public int delete(Order entity, User operator) throws Exception {
        return this.session.delete(entity);
    }

    @Override
    @Cacheable(value = "homo", key = "#root.method.getReturnType().getName()+#uuid")
    public Order findOne(long uuid) throws Exception {
        return (Order) session.findOne(Order.class, uuid);
    }
}
```

## 服务类规范
- 类注解`@Service`，通过`database`配置数据源（在该类中出现的所有继承自`AbstractRepository`的类均需与该类属于同一个数据源）
- 继承`AbstractService<T extend AbstractRepository<T extend BaseEntity>>`抽象类
- 属性注解`Transaction`，用来开启事务
- 属性注解`Message`，`type`指定信息发送接听类的过滤类型

### 服务类
```java
@Service(database = "mysql")
public class OrderServiceImpl extends AbstractService {

    @Message(type = Order.class)
    public BiFunction<HomoRequest, ApplicationContext, Object> getCode = (request, context) -> "A-001";

    @Transaction
    @Message(type = Order.class)
    public BiFunction<HomoRequest, ApplicationContext, Object> discount = (request, context) -> {
        Order order;
        try {
            OrderRepositoryImpl orderRepository = context.getBean(OrderRepositoryImpl.class);
            long uuid = Long.parseLong(request.getParameter("uuid"));
            order = orderRepository.findOne(uuid);
            order.setPrice(order.getPrice().add(new BigDecimal("1")));
            orderRepository.getProxy().update(order, request.getUser());
            return order.getPrice().toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    };
}
```

### 信息发送监听类
```java
@Component
public class OrderMessageSender extends AbstractSender {
    @Override
    public Class supportsType() {
        return Order.class;
    }

    @Override
    public void send(Object object) {
        System.out.println("发送短息：" + object);
    }
}
```
