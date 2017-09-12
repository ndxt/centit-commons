package com.centit.support.security;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.SecureRandom;

@SuppressWarnings("unused")
public abstract class AESSecurityUtils {

	private AESSecurityUtils() {
		throw new IllegalAccessError("Utility class");
	}

	public static Cipher createEncryptCipher(String keyValue) throws GeneralSecurityException  {
		Key key = getKey(keyValue);
		Cipher encryptCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		encryptCipher.init(Cipher.ENCRYPT_MODE, key);
		return encryptCipher;
	}

	public static Cipher createDencryptCipher(String keyValue) throws GeneralSecurityException {
		Key key = getKey(keyValue);
		Cipher decryptCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		decryptCipher.init(Cipher.DECRYPT_MODE, key);
		return decryptCipher;
	}

	/**   
     * 生成指定字符串的密钥   
     * @param secret        要生成密钥的字符串   
     * @return secretKey    生成后的密钥   
	 * @throws GeneralSecurityException 异常
     */    
    private static Key getKey(String secret) throws GeneralSecurityException {     
        KeyGenerator kgen = KeyGenerator.getInstance("AES");     
        kgen.init(128, new SecureRandom(secret.getBytes()));     
        SecretKey secretKey = kgen.generateKey();     
        return secretKey;     
    }     
    
	/**
	 * 加密字节数组
	 * 
	 * @param arrB 需加密的字节数组
	 * @param keyValue 密码
	 * @return 加密后的字节数组
	 * @throws GeneralSecurityException 父类抛出的异常
	 */
	public static byte[] encrypt(byte[] arrB,String keyValue) throws GeneralSecurityException {
		return createEncryptCipher(keyValue).doFinal(arrB);
	}

	/**
	 * 解密字节数组
	 * 
	 * @param arrB  需解密的字节数组
	 * @param keyValue 密码
	 * @return 解密后的字节数组
	 * @throws Exception  父类抛出的异常
	 */
	public static byte[] decrypt(byte[] arrB,String keyValue) throws Exception {
		return createDencryptCipher(keyValue).doFinal(arrB);
	}
	
	

	 /**
     * 用Des加密再用base64编码
     * @param str  需加密的字节数组
	 * @param keyValue 密码
     * @return 密文
     */

    public static String encryptAndBase64(String str,String keyValue){
    	try {
			return new String(Base64.encodeBase64(encrypt(str.getBytes(),keyValue)));
		} catch (Exception e) {
			return null;
		}
    }
    /**
     * 用base64解码再用Des解密
     * @param str 需解密的字节数组
	 * @param keyValue 密码
     * @return 明文
     */
    public static String decryptBase64String(String str,String keyValue){
    	try {
			return new String(decrypt(Base64.decodeBase64(str.getBytes()),keyValue));
		} catch (Exception e) {
			return null;
		}
    }
}
