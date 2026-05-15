package com.voting.decrypt;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AES {
    /**
     * GCM_TAG_LENGTH
     */
    public static final int GCM_TAG_LENGTH = 16;

    /**
     * AES_KEY_SIZE
     */
    public static final int AES_KEY_SIZE = 256;

    /**
     * ROOTKEY_ONE
     */
    public static final String ROOTKEY_ONE = "qPVkcLdBOOdy4OXK1F7v6dMtnBh2V+t0k3kQSGgdlS8=";

    static AESKey aesKey = new AESKey("", "");

    private static final Map<Character, Byte> MAP = new HashMap<>();

    private static final char[] HEX_CHAR_TABLE = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };

    static {
        for (int i = 0; i < HEX_CHAR_TABLE.length; i++) {
            char c = HEX_CHAR_TABLE[i];
            MAP.put(c, (byte) i);
        }
    }

    /**
     * aes解密
     *
     * @param cipherText cipherText
     * @param key key
     * @param iv iv
     * @return 解密字符串
     * @throws Exception Exception
     */
    public static String decrypt(byte[] cipherText, SecretKey key, byte[] iv) throws Exception {
        // Get Cipher Instance
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

        // Create SecretKeySpec
        SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), "AES");

        // Create GCMParameterSpec
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);

        // Initialize Cipher for DECRYPT_MODE
        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);

        // Perform Decryption
        byte[] decryptedText = cipher.doFinal(cipherText);

        return new String(decryptedText, StandardCharsets.UTF_8);
    }

    /**
     * 十六进制string转byte数组
     *
     * @param hexString hexString
     * @return byte数组
     */
    public static byte[] toByteArray(String hexString) {
        byte[] result = new byte[hexString.length() / 2];
        for (int i = 0; i < hexString.length() / 2; i++) {
            result[i] = (byte) ((MAP.get(hexString.charAt(i * 2)) << 4) + MAP.get(hexString.charAt(i * 2 + 1)));
        }
        return result;
    }

    /**
     * 将密文解码
     *
     * @param encryptedText encryptedText
     * @return 解码
     */
    public static String getOriginalText(String encryptedText) {
        if (aesKey.isEmpty()) {
            aesKey = getAESKey();
        }

        // 获取工作密钥key
        byte[] decodedWorkKey = Base64.getDecoder().decode(aesKey.getKey());
        SecretKey workOriginalKey = new SecretKeySpec(decodedWorkKey, 0, GCM_TAG_LENGTH * 2, "AES");

        // 获取工作密钥IV并还原明文
        try {
            return decrypt(Base64.getDecoder().decode(encryptedText), workOriginalKey, toByteArray(aesKey.getIv()));
        } catch (Exception e) {
            return "";
        }
    }

    public static AESKey getAESKey() {
        // 获取根密钥key
        String rootKey;
        try {
            rootKey = PBKDF2.getEncryptedPassword(xorReduction(), System.getenv("PBKDF2_SALT"));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            return new AESKey("", "");
        }

        byte[] decodedRootKey = Base64.getDecoder().decode(rootKey);
        SecretKey rootOriginalKey = new SecretKeySpec(decodedRootKey, 0, GCM_TAG_LENGTH * 2, "AES");

        // 获取根密钥IV
        byte[] decodeRootIV = toByteArray(System.getenv("ROOTKEY_IV"));

        String workKey = null;
        String workIV = null;
        try {
            workKey = decrypt(Base64.getDecoder().decode(System.getenv("WORKKEY_KEY")), rootOriginalKey, decodeRootIV);
            workIV = decrypt(Base64.getDecoder().decode(System.getenv("WORKKEY_IV")), rootOriginalKey, decodeRootIV);
            return new AESKey(workKey, workIV);
        } catch (Exception e) {
            return new AESKey("", "");
        }
    }

    /**
     * 对字符串进行异或处理
     *
     * @return 行异或处理
     */
    public static String xorReduction() {
        byte[] decodedKeyOne = Base64.getDecoder().decode(AES.ROOTKEY_ONE);
        byte[] decodedKeyTwo = Base64.getDecoder().decode(System.getenv("ROOTKEY_TWO"));
        byte[] decodedKeyThree = Base64.getDecoder().decode(System.getenv("ROOTKEY_THREE"));
        int len = AES_KEY_SIZE / 8;
        byte[] decodedKeyFour = new byte[len];
        for (int i = 0; i < len; i++) {
            decodedKeyFour[i] = (byte) (decodedKeyOne[i] ^ decodedKeyTwo[i] ^ decodedKeyThree[i]);
        }
        return toHexString(decodedKeyFour);
    }

    /**
     * byte数组转十六进制string
     *
     * @param array array
     * @return 十六进制string
     */
    public static String toHexString(byte[] array) {
        StringBuilder sb = new StringBuilder();
        for (byte b : array) {
            sb.append(HEX_CHAR_TABLE[(b & 0xf0) >> 4]);
            sb.append(HEX_CHAR_TABLE[b & 0x0f]);
        }
        return sb.toString();
    }
}
