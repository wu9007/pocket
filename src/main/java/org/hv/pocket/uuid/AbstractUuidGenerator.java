package org.hv.pocket.uuid;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wujianchuan 2019/2/14
 */
public abstract class AbstractUuidGenerator implements UuidGenerator {
    final static Map<String, Long> POOL = new ConcurrentHashMap<>(60);
    GenerationType generationType;
    Integer serverId;

    @Override
    public void setServerId(Integer serverId) {
        this.serverId = serverId;
    }

    /**
     * 设置数据标识生成方式名称
     */
    @Override
    public abstract void setGeneratorId();

    @Override
    public GenerationType getGenerationType() {
        return this.generationType;
    }
}
