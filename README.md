[![Build Status](https://travis-ci.org/leyan95/pocket.svg?branch=master)](https://travis-ci.org/leyan95/pocket) 
[![](https://jitpack.io/v/leyan95/pocket.svg)](https://jitpack.io/#leyan95/pocket)

# Pocket🚀

If you have an improvement, I will be happy to get a pull request from you!  [Github](https://github.com/HunterVillage/pocket) 

---
To get a Git project into your build:<br />**Step 1.** Add the JitPack repository to your build file<br />Add it in your root build.gradle at the end of repositories:
```
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```
**Step 2.** Add the dependency
```
dependencies {
	        implementation 'com.github.leyan95:pocket:Tag'
	}
```
_
<a name="21aeff42"></a>
## 多数据源配置

**这里以MySQL数据库为例**

<a name="09ef8a30"></a>
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
        session: session1,session2
  ##redis服务
  cache:
      ##应用层缓存
      logic:
        hostName: 127.0.0.1
        port: ***
      ##数据库层缓存，启动前开启redis服务，作为数据库层缓存
      base:
        hostName: 127.0.0.1
        port: ***
```

<a name="fd245658"></a>
## 实体类规范

* 继承`BaseEntity`抽象类（数据标识为`String`）
* 类注解`@Entity`，`table` 对应数据库表名；`tableId` 对应数据库表标识，目的是为了在生成数据标识的时候区分表；`uuidGenerator` 对应主键生成策略，默认 `increment`，可通过集继承`AbstractUuidGenerator` 自定义主键生成策。
* 属性注解`@Column`，`name` 对应数据库中对应的列名称
* 属性注解`@OneToMany`， `clazz` 对应子类的类类型，`name` 对应该表数据标识在其子表中的字段名称
* 属性注解`@ManyToOne`，`name` 关联主表数据标识的列名称

<a name="4c4631b0"></a>
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

<a name="4886c9f8"></a>
### 明细类

```java
@Entity(table = "TBL_COMMODITY", tableId = 201, businessName = "订单明细")
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

<a name="153563f4"></a>
## 数据操作(具体操作请参考接口文档)

<a name="c4bbf568"></a>
#### 获取缓存对象

通过`SessionFactory`的静态方法`Session getSession(String sessionName)`获取对象。

<a name="c547517b"></a>
#### 使用 Criteria 根据条件查询数据

```java
private Session session = SessionFactory.getSession("session1");
session.open();
private Transaction transaction = session.getTransaction();
transaction.begin();

Criteria criteria = this.session.createCriteria(Order.class)
				.add(Restrictions.like("code", "%A%"))
        .add(Restrictions.or(Restrictions.gt("price", 13), Restrictions.lt("price", 12.58)))
        .add(Sort.desc("price"))
        .add(Sort.asc("uuid"))
  			.limit(0, 5);
List orderList = criteria.list();

transaction.commit();
session.close();
```

<a name="8d689659"></a>
#### 使用 Criteria 更新数据

```java
// 省略session开关操作
Criteria criteria = this.session.createCriteria(Order.class);
criteria.add(Modern.set("price", 500.5D))
  			.add(Modern.set("day", new Date())
        .add(Restrictions.equ("code", "C-001")));
System.out.println(criteria.update());
```

<a name="bc860109"></a>
#### 使用 Criteria 根据条件删除数据

```java
// 省略session开关操作
Criteria criteria = session.createCriteria(Order.class);
criteria.add(Restrictions.equ("uuid", 1011011L));
criteria.delete();
```

<a name="b3d259ee"></a>
#### 使用 SQLQuery

```java
SQLQuery query = this.session.createSQLQuery("select uuid,code,price from tbl_order", Order.class);
Order order = (Order) query.unique();

SQLQuery query = this.session.createSQLQuery("select uuid,code,price from tbl_order", Order.class);
List<Order> orders = query.limit(0, 5).list();
```


<a name="390c53c9"></a>
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

<a name="32dc34c5"></a>
#### 保存历史数据 `@Track` 
| 参数 | 值 |
| --- | --- |
| data | 存储对哪个实体操作的历史数据 |
| operator | 操作人 |
| operate | 操作类型 `OperateEnum`  |

```java
@Repository
public class UserRepositoryImpl extends AbstractRepository implements UserRepository {
    @Override
    @Track(data = "#user", operator = "#avatar", operate = OperateEnum.SAVE)
    public int save(User user, String avatar) {
      user.setEnable(true);
      return this.getSession().save(user);
    }
}
```

<a name="b46bbc49"></a>
#### 缓存数据 `@Cache` 
| 参数 | 值 |
| --- | --- |
| key | redis中对应的缓存内容的键（支持spel） |
| duration | 缓存持续时长（毫秒）默认10. |
| target | 对应缓存那种业务，类型 `CacheTarget` ，业务层： `DATA_BASE` ，数据库层： `DATA_BASE`  |

<a name="9a339b6d"></a>
##### 数据库层

```java
@Repository
public class OrderRepositoryImpl implements OrderRepository {
    @Override
    @Cache(key = "'order code - ' + #code")
    public List<Order> loadByCode(String code) {
        Criteria criteria = session.createCriteria(Order.class)
                .add(Restrictions.equ("code", "C-001"));
        List<Order> orders = criteria.list();
        return orders;
    }
}
```

<a name="edfaf5b6"></a>
##### 应用层缓存

```java
@Controller(bundleId = "user")
public class UserControllerImpl extends AbstractController{
  	private final UserService userService;
  
  	@Autowired
    public AuthControllerImpl(UserService userService) {
        this.userService = userService;
    }
  
  	@Auth("user_read")
    @Action(actionId = "/users", method = RequestMethod.GET)
    @Cache(key = "'dept - ' + #department", target = CacheTarget.BUSINESS, duration = 20)
    public Body getUsers(@RequestParam String department) {
      return Body.newSuccessInstance("成功", "获取可用服务成功", userService.listByDepartment(department));
  }
}
```

> 笔者不建议在程序中拼写 `SQL`, 故未对 `SQLQuery` `ProcessQuery` 做过多的支持，此处就不做赘述。
> 在接下来的版本中将会支持将 `SQL` 写在 `xml` 文件中的方式。 


