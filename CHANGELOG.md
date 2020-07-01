## 0.1.9 - 2020/06/03
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

## 0.1.10 - 2020/06/12
* 启动时检测多数据源配置中的 `nodeName` 不可重复。

## 0.1.11 - 2020/06/26
* 开关会话和事务打印日志改为debug。

## 0.1.12 - 2020/07/01
* 删除tableId，修改主键生成策略。
* 添加数值类型的主键生成策略。

## 0.1.13 - 2020/07/01
* 可级联查询不清空过滤条件。
* 将serverId从pocket配置中去除。