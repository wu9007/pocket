package org.homo.common.service;

import org.hibernate.Session;
import org.homo.common.annotation.HomoTransaction;
import org.homo.common.evens.ServiceEven;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @author wujianchuan 2018/12/29
 */
public abstract class AbstractService {
    private Map<String, Field> fieldMapper = new HashMap<>(20);
    private Session session;
    private HomoTransaction homoTransactionAnnotation;

    @Autowired
    private ApplicationContext context;

    public Object handle(Function<Map<String, Object>, Object> function, Map<String, Object> parameter) {

        this.before(function);
        Object result = function.apply(parameter);
        this.after(function, result);
        return result;
    }

    /**
     * 前置通知
     *
     * @param function 执行函数
     */
    private void before(Function<Map<String, Object>, Object> function) {
        this.transactionAnnotation(function.toString());

        if (this.homoTransactionAnnotation != null && this.homoTransactionAnnotation.open()) {
            // TODO 通过工厂获取session {this.session = SessionFactory.getInstance.getSession("demo")}
            System.out.println("开启事务-" + this.homoTransactionAnnotation.sessionName());
        }
    }

    /**
     * 后置通知
     *
     * @param function 执行函数
     */
    private void after(Function<Map<String, Object>, Object> function, Object result) {

        if (this.homoTransactionAnnotation != null && this.homoTransactionAnnotation.open()) {
            // TODO 通过工厂获取session {this.session = SessionFactory.getInstance.getSession("demo")}
            System.out.println("关闭事务-" + this.homoTransactionAnnotation.sessionName());
        }

        this.notifyAllListener(function, result);
    }

    private void notifyAllListener(Function<Map<String, Object>, Object> function, Object result) {
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
