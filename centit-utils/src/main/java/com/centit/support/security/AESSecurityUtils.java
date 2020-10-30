package com.centit.support.security;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.SecureRandom;

@SuppressWarnings("unused")
public abstract class AESSecurityUtils {

    protected static final Logger logger = LoggerFactory.getLogger(AESSecurityUtils.class);
    private static final String AES_DEFAULT_KEY="0123456789abcdefghijklmnopqrstuvwxyzABCDEF";

    private AESSecurityUtils() {
        throw new IllegalAccessError("Utility class");
    }

    public static Cipher createEncryptCipher(String keyValue) throws GeneralSecurityException {
        Key key = getKey(keyValue);
        Cipher encryptCipher = Cipher.getInstance("AES");//"AES/ECB/PKCS5Padding"
        encryptCipher.init(Cipher.ENCRYPT_MODE, key);
        return encryptCipher;
    }

    public static Cipher createDencryptCipher(String keyValue) throws GeneralSecurityException {
        Key key = getKey(keyValue);
        Cipher decryptCipher = Cipher.getInstance("AES");//"AES/ECB/PKCS5Padding"
        decryptCipher.init(Cipher.DECRYPT_MODE, key);
        return decryptCipher;
    }

    /**
     * 生成指定字符串的密钥
     * kgen.init(128, new SecureRandom(secret.getBytes())); 在windows下能够正常工作，在linux下有问题，
     * SecureRandom 实现完全隨操作系统本身的內部狀態，
     * 除非調用方在調用 getInstance 方法之後又調用了 setSeed 方法；
     * 该实现在 windows 上每次生成的 key 都相同，但是在 solaris 或部分 linux 系统上则不同。
     *
     * @param secret 要生成密钥的字符串
     * @return secretKey    生成后的密钥
     * @throws GeneralSecurityException 异常
     */
    private static SecretKey getKey(String secret) throws GeneralSecurityException {
        //1.构造密钥生成器，指定为AES算法,不区分大小写
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
        secureRandom.setSeed(secret.getBytes());
        //2.根据ecnodeRules规则初始化密钥生成器
        //生成一个128位的随机源,根据传入的字节数组
        kgen.init(128, secureRandom);
        //3.产生原始对称密钥
        return kgen.generateKey();
    }

    /**
     * 加密字节数组
     *
     * @param arrB     需加密的字节数组
     * @param keyValue 密码
     * @return 加密后的字节数组
     * @throws GeneralSecurityException 父类抛出的异常
     */
    public static byte[] encrypt(byte[] arrB, String keyValue) throws GeneralSecurityException {
        return createEncryptCipher(keyValue).doFinal(arrB);
    }

    /**
     * 解密字节数组
     *
     * @param arrB     需解密的字节数组
     * @param keyValue 密码
     * @return 解密后的字节数组
     * @throws GeneralSecurityException 父类抛出的异常
     */
    public static byte[] decrypt(byte[] arrB, String keyValue) throws GeneralSecurityException {
        return createDencryptCipher(keyValue).doFinal(arrB);
    }


    /**
     * 用Aes加密再用base64编码
     *
     * @param str      需加密的字节数组
     * @param keyValue 密码
     * @return 密文
     */

    public static String encryptAndBase64(String str, String keyValue) {
        try {
            return new String(Base64.encodeBase64(encrypt(str.getBytes(), keyValue)));
        } catch (GeneralSecurityException e) {
            logger.error(e.getMessage(), e);//e.printStackTrace();
            return null;
        }
    }


    /**
     * 用base64解码再用Aes解密
     *
     * @param str      需解密的字节数组
     * @param keyValue 密码
     * @return 明文
     */
    public static String decryptBase64String(String str, String keyValue) {
        try {
            return new String(decrypt(Base64.decodeBase64(str.getBytes()), keyValue));
        } catch (GeneralSecurityException e) {
            logger.error(e.getMessage(), e);//e.printStackTrace();
            return null;
        }
    }

    public static String encryptParameterString(String param){
        if(param.startsWith("cipher:")) {
            return param;
        }else {
            return "cipher:" + encryptAndBase64(
                param, AESSecurityUtils.AES_DEFAULT_KEY);
        }
    }

    public static String decryptParameterString(String securityParam){
        if(securityParam.startsWith("cipher:")) {
            return decryptBase64String(
                securityParam.substring(7), AESSecurityUtils.AES_DEFAULT_KEY);
        }else {
            return securityParam;
        }
    }
}
