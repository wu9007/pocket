package org.hv.pocket.utils.sm4;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Sm4Utils {
    private static final Pattern PATTERN = Pattern.compile("\\s*|\t|\r|\n");

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public void setHexString(boolean hexString) {
        this.hexString = hexString;
    }

    private String secretKey = "";

    public void setIv(String iv) {
        this.iv = iv;
    }

    private String iv = "";

    private boolean hexString = false;

    public Sm4Utils() {
    }

    public String encryptDataEcb(String plainText) {
        try {
            Sm4Context ctx = new Sm4Context();
            ctx.isPadding = true;
            ctx.mode = Sm4.SM4_ENCRYPT;

            byte[] keyBytes;
            if (hexString) {
                keyBytes = Util.hexStringToBytes(secretKey);
            } else {
                keyBytes = secretKey.getBytes();
            }

            Sm4 sm4 = new Sm4();
            sm4.sm4SetKeyEnc(ctx, keyBytes);
            byte[] encrypted = sm4.sm4CryptEcb(ctx, plainText.getBytes("GBK"));
            String cipherText = new BASE64Encoder().encode(encrypted);
            if (cipherText != null && cipherText.trim().length() > 0) {
                Matcher m = PATTERN.matcher(cipherText);
                cipherText = m.replaceAll("");
            }
            return cipherText;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String decryptDataEbc(String cipherText) {
        try {
            Sm4Context ctx = new Sm4Context();
            ctx.isPadding = true;
            ctx.mode = Sm4.SM4_DECRYPT;

            byte[] keyBytes;
            if (hexString) {
                keyBytes = Util.hexStringToBytes(secretKey);
            } else {
                keyBytes = secretKey.getBytes();
            }

            Sm4 sm4 = new Sm4();
            sm4.sm4SetKeyDec(ctx, keyBytes);
            byte[] decrypted = sm4.sm4CryptEcb(ctx, new BASE64Decoder().decodeBuffer(cipherText));
            return new String(decrypted, "GBK");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String encryptDataCbc(String plainText) {
        try {
            Sm4Context ctx = new Sm4Context();
            ctx.isPadding = true;
            ctx.mode = Sm4.SM4_ENCRYPT;

            byte[] keyBytes;
            byte[] ivBytes;
            if (hexString) {
                keyBytes = Util.hexStringToBytes(secretKey);
                ivBytes = Util.hexStringToBytes(iv);
            } else {
                keyBytes = secretKey.getBytes();
                ivBytes = iv.getBytes();
            }

            Sm4 sm4 = new Sm4();
            sm4.sm4SetKeyEnc(ctx, keyBytes);
            byte[] encrypted = sm4.sm4CryptCbc(ctx, ivBytes, plainText.getBytes("GBK"));
            String cipherText = new BASE64Encoder().encode(encrypted);
            if (cipherText != null && cipherText.trim().length() > 0) {
                Matcher m = PATTERN.matcher(cipherText);
                cipherText = m.replaceAll("");
            }
            return cipherText;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String decryptDataCbc(String cipherText) {
        try {
            Sm4Context ctx = new Sm4Context();
            ctx.isPadding = true;
            ctx.mode = Sm4.SM4_DECRYPT;

            byte[] keyBytes;
            byte[] ivBytes;
            if (hexString) {
                keyBytes = Util.hexStringToBytes(secretKey);
                ivBytes = Util.hexStringToBytes(iv);
            } else {
                keyBytes = secretKey.getBytes();
                ivBytes = iv.getBytes();
            }

            Sm4 sm4 = new Sm4();
            sm4.sm4SetKeyDec(ctx, keyBytes);
            byte[] decrypted = sm4.sm4CryptCbc(ctx, ivBytes, new BASE64Decoder().decodeBuffer(cipherText));
            return new String(decrypted, "GBK");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
