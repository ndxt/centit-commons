package com.centit.support.security;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;

/**
 * MD5 散列算法返回的128bit的编码，HEX编码后的长度为32Byte
 * @author codefan
 *
 */
@SuppressWarnings("unused")
public abstract class Md5Encoder {
	private Md5Encoder() {
		throw new IllegalAccessError("Utility class");
	}
	public static String encode(byte[] data){
		MessageDigest MD5;
		try {
			MD5 = MessageDigest.getInstance("MD5");		
			MD5.update(data, 0, data.length);
			return new String(Hex.encodeHex(MD5.digest()));
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
	}
	
	public static String encode(String data){
		try {
			return encode(data.getBytes("utf8"));
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}
	/**
	 * encoding password for spring security 
	 * 目前框架中的密码都是这样加密的
	 * @param data  密文
	 * @param salt  盐
	 * @return 散列值
	 */
	public static String encodePasswordAsSpringSecurity(String data,String salt){
		return encode(data + "{" + salt + "}");
	}
	
	
	/**
	 * encoding password for spring JA-SIG Cas 
	 * @param data  密文
	 * @param salt  盐
	 * @param iterations 迭代次数
	 * @return 散列值
	 */
	public static String encodePasswordAsJasigCas(String data,String salt, int iterations){
		MessageDigest MD5;
		try {
			MD5 = MessageDigest.getInstance("MD5");
			byte[] saltBytes = salt.getBytes("utf8");
			MD5.update(saltBytes, 0, saltBytes.length);
			byte[] hashedBytes = MD5.digest(data.getBytes("utf8"));
			for(int i=0;i<iterations-1;i++)
				hashedBytes = MD5.digest(hashedBytes);
			return new String(Hex.encodeHex(hashedBytes));
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			return null;
		}
	}
	
	/**
	 * 先腾框架默认的密码算法
	 * @param data  密文
	 * @param salt  盐
	 * @return 散列值
	 */
	public static String encodePassword(String data,String salt){
		return encodePasswordAsSpringSecurity(data , salt);
	}
	
	/**
	 * 先腾框架双重加密算法： 客户端 用md5将密码加密一下传输到后台，后台将密码再用salt加密一下放入数据库中
	 * 		这个算法可以用于后台设置密码时使用，正常验证和以前一样。
	 * @param data  密文
	 * @param salt  盐
	 * @return 散列值
	 */
	public static String encodePasswordWithDoubleMd5(String data,String salt){
		return encodePasswordAsSpringSecurity(
				encode(data) , salt);
	}
}
