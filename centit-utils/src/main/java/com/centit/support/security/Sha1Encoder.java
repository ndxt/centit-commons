package com.centit.support.security;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang3.StringUtils;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Secure Hash Algorithm  安全散列算法
 * 算法比MD5效率低（算法复杂度高）所以更为安全
 * SHA-1 散列算法返回的160bit的编码，HEX编码后的长度为40Byte
 *
 * @author codefan
 */
@SuppressWarnings("unused")
public abstract class Sha1Encoder {

    protected static final Logger logger = LoggerFactory.getLogger(Sha1Encoder.class);

    private Sha1Encoder() {
        throw new IllegalAccessError("Utility class");
    }

    public static byte[] rawEncode(byte[] data) {
        if(data == null){
            return null;
        }
        MessageDigest SHA1;
        try {
            SHA1 = MessageDigest.getInstance("SHA-1");
            SHA1.update(data, 0, data.length);
            return SHA1.digest();
        } catch (NoSuchAlgorithmException e) {
            logger.error(e.getMessage(), e);//e.printStackTrace();
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
        try {
            return encode(data.getBytes("utf8"));
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);//e.printStackTrace();
            return null;
        }
    }

    public static String encodeBase64(byte[] data, boolean urlSafe) {
        byte[] md5Code = rawEncode(data);
        if (md5Code != null) {
            return new String(
                urlSafe ? Base64.encodeBase64URLSafe(md5Code) : Base64.encodeBase64(md5Code));
        } else {
            return null;
        }
    }

    public static String encodeBase64(String data, boolean urlSafe) {
        if(StringUtils.isBlank(data)){
            return null;
        }
        try {
            return encodeBase64(data.getBytes("utf8"), urlSafe);
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);//e.printStackTrace();
            return null;
        }
    }
}
