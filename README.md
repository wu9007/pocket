[![Build Status](https://travis-ci.org/leyan95/pocket.svg?branch=master)](https://travis-ci.org/leyan95/pocket) 
[![](https://jitpack.io/v/leyan95/pocket.svg)](https://jitpack.io/#leyan95/pocket)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/3e6b780e5ae1409497f0f7ec957fda96)](https://www.codacy.com/app/leyan95/pocket?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=leyan95/pocket&amp;utm_campaign=Badge_Grade)

# Pocket🚀

If you have an improvement, I will be happy to get a pull request from you!  [Github](https://github.com/HunterVillage/pocket) 

---

To get a Git project into your build:
**Step 1.** Add the JitPack repository to your build file
Add it in your root build.gradle at the end of repositories:
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
        nodeName: mysql-02
        driverName: com.mysql.cj.jdbc.Driver
        showSql: true
        user: root
        password: root
        poolMiniSize: 10
        poolMaxSize: 15
        timeout: 1000
        ##根据session找到数据库并从数据库对应的连接池中获取数据库链接，故所有session不可重复
        session: session1,session2
```

## 实体类规范

### 主类

```java
@Entity(table = "TBL_ORDER", tableId = 200)
public class Order extends BaseEntity {
    private static final long serialVersionUID = 2560385391551524826L;

    @Column(name = "CODE")
    private String code;
    @Column(name = "PRICE")
    private BigDecimal price;
    @Column(name = "DAY")
    private Date day;
    @Column(name = "TIME")
    private Date time;
    @Column(name = "STATE")
    private Boolean state;
    @Join(columnName = "TYPE", businessName = "订单支付方式", 
          joinTable = "TBL_ORDER_TYPE", joinMethod = JoinMethod.LEFT, 
          bridgeColumn = "UUID", destinationColumn = "NAME")
    private String type;

    @OneToMany(clazz = Commodity.class, bridgeField = "order")
    private List<Commodity> commodities;

   // 这里省略 getter setter
}
```

### 明细类

```java
@Entity(table = "TBL_COMMODITY", tableId = 201, businessName = "订单明细")
public class Commodity extends BaseEntity {
   private static final long serialVersionUID = -6711578420837877371L;

   @Column(name = "NAME")
   private String name;
   @Column(name = "PRICE")
   private BigDecimal price;

   @ManyToOne(columnName = "ORDER_UUID", clazz = Order.class, upBridgeField = "uuid")
   private Long order;

   // getter setter
}
```

> - 继承`BaseEntity`抽象类（数据标识为`String`）
> - 类注解`@Entity`，`table` 对应数据库表名；`tableId` 对应数据库表标识，目的是为了在生成数据标识的时候区分表；`uuidGenerator` 对应主键生成策略，默认 `increment`，可通过集继承`AbstractUuidGenerator` 自定义主键生成策。
> - 属性注解`@Column`，`name` 对应数据库中对应的列名称
> - 属性注解`@OneToMany`， `clazz` 对应子类的类类型，`name` 对应该表数据标识在其子表中的字段名称
> - 属性注解`@ManyToOne`，`name` 关联主表数据标识的列名称


## 数据操作(具体操作请参考接口文档)

#### 获取缓存对象

通过`SessionFactory`的静态方法`Session getSession(String sessionName)`获取对象。

#### 使用 Session 进行数据操作

```java
// 开启事务
this.session = SessionFactory.getSession("homo");
this.session.open();
this.transaction = session.getTransaction();
this.transaction.begin();

// 查询
RelevantBill order = (RelevantBill) this.session.findOne(RelevantBill.class, "10130");
System.out.println(order.getCode());
order.setCode("Hello-001");
// 更新
this.session.update(order);
System.out.println(order.getCode());
// 删除
this.session.delete(order);

// 关闭事务
this.transaction.commit();
this.session.close();
```

#### 使用 Criteria 根据条件查询数据

```java
// 此处省略session开关操作

Criteria criteria = this.session.createCriteria(Order.class);
criteria.add(Restrictions.like("code", "%A%"))
        .add(Restrictions.or(
            Restrictions.gt("price", 13), 
            Restrictions.lt("price", 12.58)
         ))
        .add(Sort.desc("price"))
        .add(Sort.asc("uuid"))
        .limit(0, 5);
List orderList = criteria.list();
```

#### 使用 Criteria 更新数据

```java
// 此处省略session开关操作

Criteria criteria = this.session.createCriteria(Order.class);
criteria.add(Modern.set("price", 500.5D))
  			.add(Modern.set("day", new Date())
        .add(Restrictions.equ("code", "C-001")));
System.out.println(criteria.update());

// 为保持数据一致性，已支持表达式更新，
// # 后面跟对应对象中的属性名，
// : 后对应参数（后面不要忘了调用setParameter）
session.createCriteria(Order.class)
    	// 在原数据基础上进行拼接
        .add(Modern.setWithPoEl("#code  = CONCAT_WS('', #code, :STR_VALUE)")) 
        // 在原数据的基础上进行加操作
        .add(Modern.setWithPoEl("#price  = #price + :ADD_PRICE")) 
        //条件过滤
        .add(Restrictions.equ("uuid", "10")) 
        // 给 :STR_VALUE 参数赋值
        .setParameter("STR_VALUE", " - A") 
        // 给 :ADD_PRICE 参数赋值
        .setParameter("ADD_PRICE", 100) 
        // 执行更新操作
        .update(); 
```

#### 使用 Criteria 根据条件删除数据

```java
// 此处省略session开关操作

Criteria criteria = session.createCriteria(Order.class);
criteria.add(Restrictions.equ("uuid", 1011011L));
criteria.delete();
```

#### 使用 SQLQuery

```java
SQLQuery query = this.session.createSQLQuery("select uuid,code,price from tbl_order",
                                             Order.class);
Order order = (Order) query.unique();

SQLQuery query = this.session.createSQLQuery("select uuid,code,price from tbl_order",
                                             Order.class);
List<Order> orders = query.limit(0, 5).list();
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

> 笔者不建议在程序中拼写 `SQL`, 故未对 `SQLQuery` `ProcessQuery` 做过多的支持，此处就不做赘述。
> 在接下来的版本中将会支持将 `SQL` 写在 `xml` 文件中的方式。 


## License
[MIT](https://choosealicense.com/licenses/mit/)