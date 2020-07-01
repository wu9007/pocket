package org.hv.pocket.identify;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author wujianchuan 2019/2/14
 */
public abstract class AbstractIdentifyGenerator implements IdentifyGenerator {
    final static Map<String, AtomicLong> POOL = new ConcurrentHashMap<>(60);
    String generationType;

    /**
     * 设置数据标识生成方式名称
     */
    @Override
    public abstract void setGeneratorId();

    @Override
    public String getGenerationType() {
        return this.generationType;
    }
}
