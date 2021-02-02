package org.hv.pocket.session.manager;

import org.hv.pocket.session.Session;
import org.hv.pocket.session.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 负责各线程的数据库创建、开启、关闭、销毁以及异常处理
 *
 * @author wujianchuan 2020/9/10 09:02
 */
public class ActiveSessionCenter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ActiveSessionCenter.class);
    /**
     * 每个线程持有的活跃的数据库会话
     */
    private static final ThreadLocal<Session> SESSION_THREAD_LOCAL = new ThreadLocal<>();
    /**
     * 每个线程当前所在的方法（这里指带有@Affair注解的方法）节点序号，
     * 当前线程所在方法节点序号再次变为0时，则表示可以关闭数据库会话。
     */
    private static final ThreadLocal<Integer> METHOD_NODE_NUMBER_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 获取当前数据库会话
     *
     * @return 当前数据库会话
     */
    public static Session getCurrentSession() {
        return SESSION_THREAD_LOCAL.get();
    }

    /**
     * 在当前线程每个方法（这里指带有@Affair注解的方法）进入的时候都需要进行登记，
     * 如果是首节点则根据{@code sessionName}创建并开启当前线程与数据库的会话，
     * 如果{@code enableTransaction}为{@code true}则开启事务。
     *
     * @param sessionName       数据库会话名
     * @param enableTransaction 是否开启事务
     */
    public static void register(String sessionName, boolean enableTransaction) {
        Integer methodNodeSerialNum = METHOD_NODE_NUMBER_THREAD_LOCAL.get();
        if (methodNodeSerialNum == null) {
            LOGGER.info("开启数据库会话");
            createAndOpenSession(sessionName);
            METHOD_NODE_NUMBER_THREAD_LOCAL.set(0);
        } else {
            METHOD_NODE_NUMBER_THREAD_LOCAL.set(methodNodeSerialNum + 1);
            LOGGER.info("会话 Thread Local + 1 当前值：{}", METHOD_NODE_NUMBER_THREAD_LOCAL.get());
        }
        TransactionHolder.preProcess(SESSION_THREAD_LOCAL.get(), enableTransaction);
    }

    /**
     * 在当前线程执行完每个方法的时候需要注销登记，
     * 如果是首节点则关闭当前线程与数据库的当前的会话
     *
     * @param enableTransaction 是否开启事务
     */
    public static void cancelTheRegistration(boolean enableTransaction) {
        TransactionHolder.proProcess(enableTransaction);
        Integer methodNodeSerialNum = METHOD_NODE_NUMBER_THREAD_LOCAL.get();
        if (methodNodeSerialNum == 0) {
            LOGGER.info("关闭会话\n清除会话 Thread Local 数据");
            closeAndRemove();
        } else {
            METHOD_NODE_NUMBER_THREAD_LOCAL.set(methodNodeSerialNum - 1);
            LOGGER.info("会话 Thread Local - 1 当前值：{}", METHOD_NODE_NUMBER_THREAD_LOCAL.get());
        }
    }

    /**
     * 在当前线程在开启数据库会话并遇到异常时进行处理
     *
     * @param enableTransaction 是否开启事务
     * @param throwable         异常
     */
    public static void handleException(Throwable throwable, boolean enableTransaction) {
        TransactionHolder.exceptionProcess(enableTransaction);
        Integer methodNodeSerialNum = METHOD_NODE_NUMBER_THREAD_LOCAL.get();
        // NOTE: 在最外层方法捕捉到异常后才进行会话及事务的关闭（在Affairs嵌套前提下，切面多次捕捉异常后多次关闭数据库会话异常问题。）
        if (methodNodeSerialNum == 0) {
            LOGGER.error(throwable.getMessage());
            LOGGER.info("关闭会话\n清除会话 Thread Local 数据");
            closeAndRemove();
        } else {
            METHOD_NODE_NUMBER_THREAD_LOCAL.set(methodNodeSerialNum - 1);
            LOGGER.info("会话 Thread Local - 1 当前值：{}", METHOD_NODE_NUMBER_THREAD_LOCAL.get());
        }
    }

    /**
     * 创建并开启数据库会话
     *
     * @param sessionName 数据库会话名
     */
    private static void createAndOpenSession(String sessionName) {
        Session session = SessionFactory.getSession(sessionName);
        session.open();
        SESSION_THREAD_LOCAL.set(session);
    }

    /**
     * 关闭并移除活跃的数据库会话
     */
    private static void closeAndRemove() {
        Session session = SESSION_THREAD_LOCAL.get();
        session.close();
        SESSION_THREAD_LOCAL.remove();
        METHOD_NODE_NUMBER_THREAD_LOCAL.remove();
    }
}
