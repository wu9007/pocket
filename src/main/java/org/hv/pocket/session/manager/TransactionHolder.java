package org.hv.pocket.session.manager;

import org.hv.pocket.session.Session;
import org.hv.pocket.session.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 负责数据库会话中事务的创建、开启、关闭以及异常处理
 *
 * @author wujianchuan 2020/9/10 10:28
 */
public class TransactionHolder {
    private final static Logger LOGGER = LoggerFactory.getLogger(TransactionHolder.class);
    /**
     * 每个线程开启的数据库事务
     */
    private static final ThreadLocal<Transaction> TRANSACTION_THREAD_LOCAL = new ThreadLocal<>();
    /**
     * 每个线程当前所在方法（以带由@Affair注解且on为true）的方法为起始节点，当线程所在方法节点序号再次变为0，则表示关闭数据库事务
     */
    private static final ThreadLocal<Integer> METHOD_NODE_NUMBER_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 在使用ActiveSession注册后执行事务前置处理
     *
     * @param session 数据库绘画
     * @param enable  是否开启事务
     */
    protected static void preProcess(Session session, boolean enable) {
        if (enable) {
            Transaction transaction = TRANSACTION_THREAD_LOCAL.get();
            if (transaction == null) {
                LOGGER.info("开启事务");
                transaction = session.getTransaction();
                transaction.begin();
                TRANSACTION_THREAD_LOCAL.set(transaction);
                METHOD_NODE_NUMBER_THREAD_LOCAL.set(0);
            } else {
                int methodNodeNumber = METHOD_NODE_NUMBER_THREAD_LOCAL.get();
                METHOD_NODE_NUMBER_THREAD_LOCAL.set(methodNodeNumber + 1);
                LOGGER.info("事务 Thread Local + 1 当前值：{}", METHOD_NODE_NUMBER_THREAD_LOCAL.get());
            }
        }
    }

    /**
     * 在使用ActiveSession取消注册后执行事务的后置处理
     */
    protected static void proProcess(boolean enable) {
        if (enable) {
            Transaction transaction = TRANSACTION_THREAD_LOCAL.get();
            if (transaction != null) {
                Integer methodNodeNumber = METHOD_NODE_NUMBER_THREAD_LOCAL.get();
                if (methodNodeNumber == null) {
                    // FIX: 暂时规避事务异常
                    LOGGER.warn("方法栈已被清空，但事务还存在");
                } else {
                    if (methodNodeNumber == 0) {
                        LOGGER.info("提交事务");
                        transaction.commit();
                        LOGGER.info("清除事务 Thread Local 数据");
                        remove();
                    } else {
                        METHOD_NODE_NUMBER_THREAD_LOCAL.set(methodNodeNumber - 1);
                        LOGGER.info("事务 Thread Local - 1 当前值：{}", METHOD_NODE_NUMBER_THREAD_LOCAL.get());
                    }
                }
            }
        }
    }

    /**
     * 在使用ActiveSession处理异常时执行事务的异常处理
     */
    protected static void exceptionProcess(boolean enable) {
        if (enable) {
            Transaction transaction = TRANSACTION_THREAD_LOCAL.get();
            if (transaction != null) {
                Integer methodNodeNumber = METHOD_NODE_NUMBER_THREAD_LOCAL.get();
                if (methodNodeNumber == null) {
                    // FIX: 暂时规避事务异常
                    LOGGER.warn("方法栈已被清空，但事务还存在");
                } else {
                    if (methodNodeNumber == 0) {
                        LOGGER.info("回滚事务");
                        transaction.rollBack();
                        LOGGER.info("清除事务 Thread Local 数据");
                        remove();
                    } else {
                        METHOD_NODE_NUMBER_THREAD_LOCAL.set(methodNodeNumber - 1);
                        LOGGER.info("事务 Thread Local - 1 当前值：{}", METHOD_NODE_NUMBER_THREAD_LOCAL.get());
                    }
                }
            }
        }
    }

    /**
     * 清理资源
     */
    private static void remove() {
        METHOD_NODE_NUMBER_THREAD_LOCAL.remove();
        TRANSACTION_THREAD_LOCAL.remove();
    }
}
