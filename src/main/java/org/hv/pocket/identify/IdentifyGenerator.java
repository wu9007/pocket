package org.hv.pocket.identify;

import org.hv.pocket.model.AbstractEntity;
import org.hv.pocket.session.Session;

import java.sql.SQLException;

/**
 * @author wujianchuan 2019/1/2
 */
public interface IdentifyGenerator {

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
     * @throws SQLException e
     */
    String getIdentify(Class<? extends AbstractEntity> clazz, Session session) throws SQLException;
}
