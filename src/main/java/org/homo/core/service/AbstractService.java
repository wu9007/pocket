package org.homo.core.service;

import org.homo.core.annotation.Service;
import org.homo.core.annotation.Transaction;
import org.homo.core.evens.ServiceEven;
import org.homo.core.executor.HomoRequest;
import org.homo.dbconnect.inventory.SessionFactory;
import org.homo.dbconnect.inventory.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * @author wujianchuan 2018/12/29
 */
public abstract class AbstractService {

    @Autowired
    private ApplicationContext context;

    private Map<String, Field> fieldMapper = new HashMap<>(20);
    private Transaction homoTransactionAnnotation;
    private org.homo.dbconnect.transaction.Transaction transaction;

    public AbstractService() {
        Service service = this.getClass().getAnnotation(Service.class);
        Session inventoryManager = SessionFactory.getSession(service.database(), service.session());
        this.transaction = inventoryManager.getTransaction();
    }

    public Object handle(BiFunction<HomoRequest, ApplicationContext, Object> function, HomoRequest request) throws SQLException {

        this.before(function);
        Object result = function.apply(request, this.context);
        try {
            this.after(function, result);
        } catch (SQLException e) {
            this.transaction.rollBack();
        }
        return result;
    }

    /**
     * 前置通知
     *
     * @param function 执行函数
     */
    private void before(BiFunction<HomoRequest, ApplicationContext, Object> function) throws SQLException {
        this.transactionAnnotation(function.toString());
        this.transaction.connect();
        if (this.homoTransactionAnnotation != null && this.homoTransactionAnnotation.open()) {
            this.transaction.transactionOn();
        }
    }

    /**
     * 后置通知
     *
     * @param function 执行函数
     */
    private void after(BiFunction<HomoRequest, ApplicationContext, Object> function, Object result) throws SQLException {

        if (this.homoTransactionAnnotation != null && this.homoTransactionAnnotation.open()) {
            this.transaction.commit();
        }
        this.transaction.closeConnection();
        this.notifyAllListener(function, result);
    }

    private void notifyAllListener(BiFunction<HomoRequest, ApplicationContext, Object> function, Object result) {
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
        this.homoTransactionAnnotation = field.getAnnotation(Transaction.class);
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
