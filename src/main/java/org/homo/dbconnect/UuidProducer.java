package org.homo.dbconnect;

/**
 * @author wujianchuan 2019/1/2
 */
public interface UuidProducer {

    /**
     * 获取数据标识
     *
     * @return 数据标识
     */
    String getUuid(Class clazz);
}
