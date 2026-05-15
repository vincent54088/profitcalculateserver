/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.voting.decrypt;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * 加解密
 *
 * @author w30022769
 * @since 2024-01-18
 */
public class PBKDF2 {
    /**
     * PBKDF2_ALGORITHM
     */
    public static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA1";

    /**
     * 生成密文的长度
     */
    public static final int HASH_BIT_SIZE = 44 * 4;

    /**
     * 迭代次数
     */
    public static final int PBKDF2_ITERATIONS = 1000;

    /**
     * 根据明文和盐值生成密文
     *
     * @param password password
     * @param salt salt
     * @return 密文
     * @throws NoSuchAlgorithmException NoSuchAlgorithmException
     * @throws InvalidKeySpecException InvalidKeySpecException
     */
    public static String getEncryptedPassword(String password, String salt)
        throws NoSuchAlgorithmException, InvalidKeySpecException {

        KeySpec spec = new PBEKeySpec(password.toCharArray(), fromHex(salt), PBKDF2_ITERATIONS, HASH_BIT_SIZE);
        SecretKeyFactory f = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
        return toHex(f.generateSecret(spec).getEncoded());
    }

    /**
     * 六进制字符串转二进制字符串
     *
     * @param hex hex
     * @return 二进制字符串
     */
    public static byte[] fromHex(String hex) {
        byte[] binary = new byte[hex.length() / 2];
        for (int i = 0; i < binary.length; i++) {
            binary[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return binary;
    }

    /**
     * 二进制字符串转十六进制字符串
     *
     * @param array array
     * @return 十六进制字符串
     */
    public static String toHex(byte[] array) {
        BigInteger bi = new BigInteger(1, array);
        String hex = bi.toString(16);
        int paddingLength = (array.length * 2) - hex.length();
        if (paddingLength > 0) {
            return String.format("%0" + paddingLength + "d", 0) + hex;
        }
        return hex;
    }

}
