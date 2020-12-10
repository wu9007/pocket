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
final public class DecryptFunctionLib {
    private static final Map<String, PocketBiFunction<String, String, String>> DECRYPT_STRATEGY_POOL = new ConcurrentHashMap<>();

    static {
        DECRYPT_STRATEGY_POOL.put(EncryptType.DES, DesEncryption::decryptDes);
        DECRYPT_STRATEGY_POOL.put(EncryptType.SM4_CEB, Sm4CebEncryption::decryptSm4Ceb);
        DECRYPT_STRATEGY_POOL.put(EncryptType.SM4_CBC, Sm4CbcEncryption::decryptSm4Cbc);
    }

    public static PocketBiFunction<String, String, String> getDecryptFunction(String encryptModel) {
        return DECRYPT_STRATEGY_POOL.get(encryptModel);
    }
}
