package org.homo.core.service;

import org.homo.core.annotation.HomoTransaction;
import org.homo.core.evens.ServiceEven;
import org.homo.core.repository.AbstractRepository;
import org.homo.core.executor.HomoRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * @author wujianchuan 2018/12/29
 */
public abstract class AbstractService<T extends AbstractRepository> {
    private Map<String, Field> fieldMapper = new HashMap<>(20);
    private HomoTransaction homoTransactionAnnotation;
    private T repository;

    public AbstractService(T repository) {
        this.repository = repository;
    }

    @Autowired
    private ApplicationContext context;

    public Object handle(BiFunction<HomoRequest, T, Object> function, HomoRequest request) {

        this.before(function);
        Object result = function.apply(request, repository);
        this.after(function, result);
        return result;
    }

    /**
     * 前置通知
     *
     * @param function 执行函数
     */
    private void before(BiFunction<HomoRequest, T, Object> function) {
        this.transactionAnnotation(function.toString());

        if (this.homoTransactionAnnotation != null && this.homoTransactionAnnotation.open()) {
            // TODO: 通过JDBC与数据库连接、映射、开启关闭事务，通过Guava进行缓存查询出的数据
            System.out.println("开启事务-" + this.homoTransactionAnnotation.sessionName());
        }
    }

    /**
     * 后置通知
     *
     * @param function 执行函数
     */
    private void after(BiFunction<HomoRequest, T, Object> function, Object result) {

        if (this.homoTransactionAnnotation != null && this.homoTransactionAnnotation.open()) {
            System.out.println("关闭事务-" + this.homoTransactionAnnotation.sessionName());
        }

        this.notifyAllListener(function, result);
    }

    private void notifyAllListener(BiFunction<HomoRequest, T, Object> function, Object result) {
        Map<String, Object> source = new HashMap<>(2);
        source.put("field", fieldMapper.get(function.toString()));
        source.put("result", result);
        context.publishEvent(new ServiceEven(source));
    }

    /**
     * 事务服务是否开启
     *
     * @param name 属性在池中对应的键
     */
    private void transactionAnnotation(String name) {
        Field field;
        if (fieldMapper.size() == 0) {
            this.pushField();
        }
        field = fieldMapper.get(name);
        this.homoTransactionAnnotation = field.getAnnotation(HomoTransaction.class);
    }

    /**
     * 将该单例对应的类属性放入属性池
     */
    private void pushField() {
        for (Field field : this.getClass().getFields()) {
            try {
                fieldMapper.put(field.get(this).toString(), field);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
