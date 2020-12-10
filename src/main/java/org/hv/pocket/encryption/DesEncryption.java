package org.hv.pocket.encryption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * @author leyan95
 */
public class DesEncryption {
    private static final Logger LOGGER = LoggerFactory.getLogger(DesEncryption.class);

    public static String encryptDes(String data, String key) {
        if (StringUtils.isEmpty(key)) {
            LOGGER.error("The key must not be empty");
            return null;
        }
        try {
            DESKeySpec dks = new DESKeySpec(key.getBytes(StandardCharsets.UTF_8));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey secretKey = keyFactory.generateSecret(dks);
            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new SecureRandom());
            byte[] targetBytesValue = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(targetBytesValue);
        } catch (InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException e) {
            LOGGER.error(e.getMessage());
            return null;
        }
    }

    public static String decryptDes(String data, String key) {
        if (StringUtils.isEmpty(key)) {
            LOGGER.error("The key must not be empty");
            return null;
        }
        try {
            DESKeySpec dks = new DESKeySpec(key.getBytes(StandardCharsets.UTF_8));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey secretKey = keyFactory.generateSecret(dks);
            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new SecureRandom());
            byte[] columnBytesValue = cipher.doFinal(Base64.getDecoder().decode(data.replaceAll(" +", "+").replace("\r\n", "")));
            return new String(columnBytesValue, StandardCharsets.UTF_8);
        } catch (InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException e) {
            LOGGER.error(e.getMessage());
            return null;
        }
    }
}
