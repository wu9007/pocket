package org.hv.pocket.utils;

import org.hv.pocket.constant.EncryptType;
import org.hv.pocket.flib.DecryptFunctionLib;
import org.hv.pocket.flib.EncryptFunctionLib;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wujianchuan 2020/8/18 10:45
 */
public class EncryptUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(EncryptUtil.class);
    private static final Map<String/* encrypt model*/, String/* secret key */> ENCRYPT_KEY = new ConcurrentHashMap<>(4);

    /**
     * Encrypts the contents {@code value} in the specified model {@code encryptModel}
     *
     * @param encryptModel encrypt model
     * @param key          secret key
     * @param value        Character to be encrypted
     * @return ciphertext
     */
    public static String encrypt(String encryptModel, String key, String value) {
        String targetValue = null;
        try {
            targetValue = EncryptFunctionLib.getEncryptFunction(encryptModel).apply(value, key);
        } catch (Exception e) {
            LOGGER.warn(e.getMessage());
        }
        return targetValue;
    }

    /**
     * Decrypt the contents {@code value} in the specified model {@code encryptModel}
     *
     * @param encryptModel decrypt model
     * @param key          secret key
     * @param value        Character to be decrypt
     * @return plaintext
     */
    public static String decrypt(String encryptModel, String key, String value) {
        String columnValue = null;
        try {
            columnValue = DecryptFunctionLib.getDecryptFunction(encryptModel).apply(value, key);
        } catch (Exception e) {
            LOGGER.warn(e.getMessage());
        }
        return columnValue;
    }

    /**
     * Encrypts the contents {@code value} in the specified model {@code encryptModel}
     *
     * @param encryptModel encrypt model
     * @param value        Character to be encrypted
     * @return ciphertext
     */
    public static String encrypt(String encryptModel, String value) {
        return encrypt(encryptModel, ENCRYPT_KEY.get(encryptModel), value);
    }

    /**
     * Decrypt the contents {@code value} in the specified model {@code encryptModel}
     *
     * @param encryptModel decrypt model
     * @param value        Character to be decrypt
     * @return plaintext
     */
    public static String decrypt(String encryptModel, String value) {
        return decrypt(encryptModel, ENCRYPT_KEY.get(encryptModel), value);
    }

    /**
     * Set the common DES key
     *
     * @param desKey des key
     */
    public static void setDesKey(String desKey) {
        if (StringUtils.isEmpty(desKey)) {
            LOGGER.error("The key must not be empty");
        }
        if (ENCRYPT_KEY.putIfAbsent(EncryptType.DES, desKey) != null) {
            LOGGER.warn("The key setting for DES failed because the key value was set multiple times");
        }
    }

    /**
     * Set the common SM4 key
     *
     * @param sm4Key sm4 key
     */
    public static void setSm4Key(String sm4Key) {
        if (sm4Key == null || sm4Key.length() != 16) {
            LOGGER.error("The key must not be null and must have a length of 16");
        }
        if (ENCRYPT_KEY.putIfAbsent(EncryptType.SM4_CBC, sm4Key) != null) {
            LOGGER.warn("The key setting for SM4_CBC failed because the key value was set multiple times");
        }
        if (ENCRYPT_KEY.putIfAbsent(EncryptType.SM4_CEB, sm4Key) != null) {
            LOGGER.warn("The key setting for SM4_CEB failed because the key value was set multiple times");
        }
    }
}
