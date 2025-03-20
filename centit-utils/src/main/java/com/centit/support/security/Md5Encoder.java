package com.centit.support.security;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5 散列算法返回的128bit的编码，HEX编码后的长度为32Byte
 *
 * @author codefan
 */
@SuppressWarnings("unused")
public abstract class Md5Encoder {
    protected static final Logger logger = LoggerFactory.getLogger(Md5Encoder.class);

    private Md5Encoder() {
        throw new IllegalAccessError("Utility class");
    }

    public static byte[] rawEncode(byte[] data) {
        if(data == null){
            return null;
        }
        MessageDigest MD5;
        try {
            MD5 = MessageDigest.getInstance("MD5");
            MD5.update(data, 0, data.length);
            return MD5.digest();
        } catch (NoSuchAlgorithmException e) {
            logger.error(e.getMessage(), e);//logger.error(e.getMessage(), e);
            return null;
        }
    }

    public static String encode(byte[] data) {
        byte[] md5Code = rawEncode(data);
        if (md5Code != null) {
            return new String(Hex.encodeHex(md5Code));
        } else {
            return null;
        }
    }

    public static String encode(String data) {
        if(StringUtils.isBlank(data)){
            return null;
        }
        return encode(data.getBytes());
    }

    /**
     * 将md5 编码进行base64编码
     *
     * @param data    需要编码的 数据
     * @param urlSafe 返回url合法字符
     * @return 将md5 编码进行base64编码
     */
    public static String encodeBase64(byte[] data, boolean urlSafe) {
        byte[] md5Code = rawEncode(data);
        if (md5Code != null) {
            return new String(
                urlSafe ? Base64.encodeBase64URLSafe(md5Code) : Base64.encodeBase64(md5Code));
        } else {
            return null;
        }
    }

    public static String encodeBase64(byte[] data) {
        return encodeBase64(data, false);
    }

    public static String encodeBase64(String data, boolean urlSafe) {
        if(StringUtils.isBlank(data)){
            return null;
        }
        return encodeBase64(data.getBytes(), urlSafe);
    }

    public static String encodeBase64(String data) {
        return encodeBase64(data, false);
    }

    /**
     * encoding password for spring security
     * 目前框架中的密码都是这样加密的
     *
     * @param data 密文
     * @param salt 盐
     * @return 散列值
     */
    public static String encodePasswordAsSpringSecurity(String data, String salt) {
        return encode(data + "{" + salt + "}");
    }


    /**
     * encoding password for spring JA-SIG Cas
     *
     * @param data       密文
     * @param salt       盐
     * @param iterations 迭代次数
     * @return 散列值
     */
    public static String encodePasswordAsJasigCas(String data, String salt, int iterations) {
        MessageDigest MD5;
        try {
            MD5 = MessageDigest.getInstance("MD5");
            byte[] saltBytes = salt.getBytes();
            MD5.update(saltBytes, 0, saltBytes.length);
            byte[] hashedBytes = MD5.digest(data.getBytes());
            for (int i = 0; i < iterations - 1; i++)
                hashedBytes = MD5.digest(hashedBytes);
            return new String(Hex.encodeHex(hashedBytes));
        } catch (NoSuchAlgorithmException e) {
            logger.error(e.getMessage(), e);//logger.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * 先腾框架默认的密码算法
     *
     * @param data 密文
     * @param salt 盐
     * @return 散列值
     */
    public static String encodePassword(String data, String salt) {
        return encodePasswordAsSpringSecurity(data, salt);
    }

    /**
     * 先腾框架双重加密算法： 客户端 用md5将密码加密一下传输到后台，后台将密码再用salt加密一下放入数据库中
     * 这个算法可以用于后台设置密码时使用，正常验证和以前一样。
     *
     * @param data 密文
     * @param salt 盐
     * @return 散列值
     */
    public static String encodePasswordWithDoubleMd5(String data, String salt) {
        return encodePasswordAsSpringSecurity(
            encode(data), salt);
    }
}
