package org.hv.pocket.uuid;

import org.hv.pocket.session.Session;

import java.sql.SQLException;

/**
 * @author wujianchuan 2019/1/2
 */
public interface UuidGenerator {

    /**
     * 保存服务ID
     *
     * @param serverId server id.
     */
    void setServerId(Integer serverId);

    /**
     * 获取生成策略标识
     *
     * @return 策略类型
     */
    GenerationType getGenerationType();

    /**
     * 生成策略标识
     */
    void setGeneratorId();

    /**
     * 获取数据标识
     *
     * @param clazz   实体类型
     * @param session 缓存管理类
     * @return 数据标识
     */
    String getIdentify(Class clazz, Session session) throws SQLException;
}
