package org.hv.pocket.flib;

import org.hv.pocket.constant.EncryptType;
import org.hv.pocket.encryption.DesEncryption;
import org.hv.pocket.encryption.Sm4CbcEncryption;
import org.hv.pocket.encryption.Sm4CebEncryption;
import org.hv.pocket.function.PocketBiFunction;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wujianchuan 2020/8/17 10:18
 */
final public class EncryptFunctionLib {
    private static final Map<String, PocketBiFunction<String, String, String>> ENCRYPT_STRATEGY_POOL = new ConcurrentHashMap<>();

    static {
        ENCRYPT_STRATEGY_POOL.put(EncryptType.DES, DesEncryption::encryptDes);
        ENCRYPT_STRATEGY_POOL.put(EncryptType.SM4_CEB, Sm4CebEncryption::encryptSm4Ceb);
        ENCRYPT_STRATEGY_POOL.put(EncryptType.SM4_CBC, Sm4CbcEncryption::encryptSm4Cbc);
    }

    public static PocketBiFunction<String, String, String> getEncryptFunction(String encryptModel) {
        return ENCRYPT_STRATEGY_POOL.get(encryptModel);
    }
}
