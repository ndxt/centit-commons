package com.centit.support.security;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Secure Hash Algorithm  安全散列算法
 * 算法比MD5效率低（算法复杂度高）所以更为安全
 * SHA-1 散列算法返回的160bit的编码，HEX编码后的长度为40Byte
 *
 * @author codefan
 */
@SuppressWarnings("unused")
public abstract class HmacSha1Encoder {

    protected static final Logger logger = LoggerFactory.getLogger(HmacSha1Encoder.class);

    private HmacSha1Encoder() {
        throw new IllegalAccessError("Utility class");
    }

    public static byte[] rawEncode(byte[] data, String secret) {
        try {
            SecretKey secretKey = new SecretKeySpec(secret.getBytes("US-ASCII"), "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(secretKey);
            return mac.doFinal(data);
        } catch (NoSuchAlgorithmException | InvalidKeyException | UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);//logger.error(e.getMessage(), e);
            return null;
        }
    }

    public static String encode(byte[] data, String secret) {
        byte[] finalText = rawEncode(data, secret);
        if(finalText == null){
            return null;
        }
        return new String(Hex.encodeHex(finalText));
    }

    public static String encode(String data, String secret) {
        try {
            return encode(data.getBytes("utf8"), secret);
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);//logger.error(e.getMessage(), e);
            return null;
        }
    }

    public static String encodeBase64(byte[] data, String secret, boolean urlSafe) {
        byte[] md5Code = rawEncode(data, secret);
        if (md5Code != null) {
            return new String(
                urlSafe ? Base64.encodeBase64URLSafe(md5Code) : Base64.encodeBase64(md5Code));
        } else {
            return null;
        }
    }

    public static String encodeBase64(String data, String secret, boolean urlSafe) {
        try {
            return encodeBase64(data.getBytes("utf8"), secret, urlSafe);
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);//logger.error(e.getMessage(), e);
            return null;
        }
    }

}
