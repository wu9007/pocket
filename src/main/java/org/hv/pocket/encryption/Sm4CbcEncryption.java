package org.hv.pocket.encryption;

import org.hv.pocket.utils.sm4.Sm4Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * @author leyan95
 */
public class Sm4CbcEncryption {
    private static final Logger LOGGER = LoggerFactory.getLogger(Sm4CbcEncryption.class);

    public static String encryptSm4Cbc(String data, String key) {
        if (StringUtils.isEmpty(key) || key.length() != 16) {
            LOGGER.error("The key must not be null and must have a length of 16");
            return null;
        }
        Sm4Utils sm4 = new Sm4Utils();
        sm4.setSecretKey(key);
        sm4.setIv(key);
        sm4.setHexString(false);
        return sm4.encryptDataCbc(data);
    }

    public static String decryptSm4Cbc(String data, String key) {
        if (StringUtils.isEmpty(key) || key.length() != 16) {
            LOGGER.error("The key must not be null and must have a length of 16");
            return null;
        }
        Sm4Utils sm4 = new Sm4Utils();
        sm4.setSecretKey(key);
        sm4.setIv(key);
        sm4.setHexString(false);
        return sm4.decryptDataCbc(data);
    }
}
