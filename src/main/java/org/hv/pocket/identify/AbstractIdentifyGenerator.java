package org.hv.pocket.identify;

import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author wujianchuan 2019/2/14
 */
public abstract class AbstractIdentifyGenerator implements IdentifyGenerator {
    protected final static Map<String, AtomicReference<BigInteger>> POOL = new ConcurrentHashMap<>(60);
    protected String generationType;

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
