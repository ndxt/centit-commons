package com.centit.support.security;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.net.util.Base64;
import sun.security.rsa.RSAPrivateCrtKeyImpl;
import sun.security.rsa.RSAPublicKeyImpl;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public abstract class RSASecurityUtils {

    private RSASecurityUtils() {
        throw new IllegalAccessError("Utility class");
    }

    /** 指定加密算法为RSA */
    private static final String ALGORITHM = "RSA";
    /** 密钥长度，用来初始化
     * 加密字符串长度受秘钥长度的限制
     * 当KEYSIZE为1024时最多加密117个字节，长度为2048时可以加密245个字节*/
    private static final int KEYSIZE = 1024;

    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        return generateKeyPair(KEYSIZE);
    }
    /**
     * 生成密钥对
     * @param  keysize 密码大小
     * @return  密钥对
     * @throws Exception 父类抛出的异常
     */
    public static KeyPair generateKeyPair(int keysize) throws NoSuchAlgorithmException {

        // /** RSA算法要求有一个可信任的随机数源 */
        // SecureRandom secureRandom = new SecureRandom();

        /** 为RSA算法创建一个KeyPairGenerator对象 */
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);

        /** 利用上面的随机数据源初始化这个KeyPairGenerator对象 */
        // keyPairGenerator.initialize(KEYSIZE, secureRandom);
        keyPairGenerator.initialize(keysize);

        /** 生成密匙对 */
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        return keyPair;
    }

    /**
     * 加密方法
     * @param source
     *            源数据  加密字符串长度受秘钥长度的限制，最多加密245个字节
     * @param key 密码
     * @return 密文
     * @throws Exception 父类抛出的异常
     */
    public static String encrypt(String source,Key key) throws
            BadPaddingException, IllegalBlockSizeException,
            NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        generateKeyPair();
        Key publicKey = key;

        /** 得到Cipher对象来实现对源数据的RSA加密 */
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] b = source.getBytes();
        /** 执行加密操作 */
        byte[] b1 = cipher.doFinal(b);
        //BASE64Encoder encoder = new BASE64Encoder();
        return Base64.encodeBase64String(b1);
    }

    /**
     * 解密算法
     *
     * @param cryptograph
     *            密文
     * @param key 密码
     * @return 密文
     * @throws Exception 父类抛出的异常
     */
    public static String decrypt(String cryptograph,Key key) throws NoSuchPaddingException,
            NoSuchAlgorithmException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {
        Key privateKey = key;
        /** 得到Cipher对象对已用公钥加密的数据进行RSA解密 */
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        //BASE64Decoder decoder = new BASE64Decoder();
        byte[] b1 = Base64.decodeBase64(cryptograph);

        /** 执行解密操作 */
        byte[] b = cipher.doFinal(b1);
        return new String(b);
    }

    public static String keyPairToJson(KeyPair keyPair) {
        Map<String,String> keyJson = new HashMap<>();
        keyJson.put("private", Base64.encodeBase64String(keyPair.getPrivate().getEncoded()));
        keyJson.put("public", Base64.encodeBase64String(keyPair.getPublic().getEncoded()));
        return JSON.toJSONString(keyJson);
    }

    public static KeyPair keyPairFromJson(String keyJsonString) throws InvalidKeyException{
        JSONObject keyJson =  JSON.parseObject(keyJsonString);
        return new KeyPair(
                new RSAPublicKeyImpl(Base64.decodeBase64(keyJson.getString("public"))),
                RSAPrivateCrtKeyImpl.newKey(
                        Base64.decodeBase64(keyJson.getString("private"))));
    }

}