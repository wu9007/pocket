package org.hv.pocket.flib;

import org.hv.pocket.constant.EncryptType;
import org.hv.pocket.encryption.DesEncryption;
import org.hv.pocket.function.PocketBiFunction;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wujianchuan 2020/8/17 10:18
 */
final public class EncryptFunctionLib {
    private static final Map<String, PocketBiFunction<byte[], byte[], byte[]>> ENCRYPT_STRATEGY_POOL = new ConcurrentHashMap<>();

    static {
        ENCRYPT_STRATEGY_POOL.put(EncryptType.DES, DesEncryption::encryptDes);
    }

    public static PocketBiFunction<byte[], byte[], byte[]> getEncryptFunction(String encryptModel) {
        return ENCRYPT_STRATEGY_POOL.get(encryptModel);
    }
}
