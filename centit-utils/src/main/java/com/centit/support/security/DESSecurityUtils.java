package com.centit.support.security;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import java.security.GeneralSecurityException;
import java.security.Key;

@SuppressWarnings("unused")
public abstract class DESSecurityUtils {

	private DESSecurityUtils() {
		throw new IllegalAccessError("Utility class");
	}

	public static Cipher createEncryptCipher(String keyValue)
			 throws GeneralSecurityException {
		Key key = getKey(keyValue.getBytes());
		Cipher encryptCipher = Cipher.getInstance("DES");
		encryptCipher.init(Cipher.ENCRYPT_MODE, key);
		return encryptCipher;
	}

	public static Cipher createDencryptCipher(String keyValue) throws GeneralSecurityException {
		Key key = getKey(keyValue.getBytes());
		Cipher decryptCipher = Cipher.getInstance("DES");
		decryptCipher.init(Cipher.DECRYPT_MODE, key);
		return decryptCipher;
	}

	/**
	 * 从指定字符串生成密钥，密钥所需的字节数组长度为8位 不足8位时后面补0，超出8位只取前8位
	 * 
	 * @param arrBTmp
	 *            构成该字符串的字节数组
	 * @return 生成的密钥
	 */
	private static Key getKey(byte[] arrBTmp)  {
		// 创建一个空的8位字节数组（默认值为0）
		byte[] arrB = new byte[8];
		// 将原始字节数组转换为8位
		for (int i = 0; i < arrBTmp.length && i < arrB.length; i++) {
			arrB[i] = arrBTmp[i];
		}
		// 生成密钥
		Key key = new javax.crypto.spec.SecretKeySpec(arrB, "DES");
		return key;
	}

	/**
	 * 加密字节数组
	 * 
	 * @param arrB
	 *            需加密的字节数组
	 * @param keyValue 密码
	 * @return 加密后的字节数组
	 * @throws GeneralSecurityException 异常
	 */
	public static byte[] encrypt(byte[] arrB,String keyValue) throws GeneralSecurityException {
		return createEncryptCipher(keyValue).doFinal(arrB);
	}

	/**
	 * 解密字节数组
	 * 
	 * @param arrB
	 *            需解密的字节数组
	 * @param keyValue 密码
	 * @return 解密后的字节数组
	 * @throws GeneralSecurityException 异常
	 */
	public static byte[] decrypt(byte[] arrB,String keyValue) throws GeneralSecurityException {
		return createDencryptCipher(keyValue).doFinal(arrB);
	}
	
	

	 /**
     * 用Des加密再用base64编码
     * @param str 需加密的字节数组
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
