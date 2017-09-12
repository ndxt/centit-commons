package com.centit.support.security;

import org.apache.commons.codec.binary.Hex;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
/**
 * Secure Hash Algorithm  安全散列算法 
 * 算法比MD5效率低（算法复杂度高）所以更为安全
 * SHA-1 散列算法返回的160bit的编码，HEX编码后的长度为40Byte
 * @author codefan
 *
 */
@SuppressWarnings("unused")
public abstract class Sha1Encoder {

	private Sha1Encoder() {
		throw new IllegalAccessError("Utility class");
	}

	public static String encode(byte[] data){
		MessageDigest SHA1;
		try {
			SHA1 = MessageDigest.getInstance("SHA-1");		
			SHA1.update(data, 0, data.length);
			return new String(Hex.encodeHex(SHA1.digest()));
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

}
