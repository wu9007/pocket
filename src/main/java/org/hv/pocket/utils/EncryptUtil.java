package org.hv.pocket.utils;

import org.hv.pocket.flib.DecryptFunctionLib;
import org.hv.pocket.flib.EncryptFunctionLib;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * @author wujianchuan 2020/8/18 10:45
 */
public class EncryptUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(EncryptUtil.class);

    /**
     * 判断字段值是否需要加密
     *
     * @param encryptModel 加密方式
     * @param key          密钥
     * @param value        待加密字符
     * @return 密文
     */
    public static String encrypt(String encryptModel, String key, String value) {
        byte[] persistenceKey = key.getBytes(StandardCharsets.UTF_8);
        byte[] targetBytesValue = new byte[0];
        try {
            targetBytesValue = EncryptFunctionLib.getEncryptFunction(encryptModel).apply(value.getBytes(StandardCharsets.UTF_8), persistenceKey);
        } catch (Exception e) {
            LOGGER.warn(e.getMessage());
        }
        return Base64.getEncoder().encodeToString(targetBytesValue);
    }

    public static String decrypt(String encryptModel, String key, String value) {
        byte[] persistenceKey = key.getBytes(StandardCharsets.UTF_8);
        byte[] columnBytesValue = new byte[0];
        try {
            columnBytesValue = DecryptFunctionLib.getDecryptFunction(encryptModel).apply(Base64.getDecoder().decode(value.replaceAll(" +", "+").replace("\r\n", "")), persistenceKey);
        } catch (Exception e) {
            LOGGER.warn(e.getMessage());
        }
        return new String(columnBytesValue, StandardCharsets.UTF_8);
    }
}
