package org.homo.pocket.utils;

import org.homo.pocket.session.Session;

/**
 * @author wujianchuan 2019/1/2
 */
public interface UuidGenerator {

    /**
     * 获取数据标识
     *
     * @param clazz   实体类型
     * @param session 缓存管理类
     * @return 数据标识
     * @throws Exception sql语句异常
     */
    long getUuid(Class clazz, Session session) throws Exception;
}
