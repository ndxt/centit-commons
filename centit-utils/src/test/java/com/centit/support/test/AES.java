package com.centit.support.test;

import com.beust.ah.A;
import com.centit.support.security.AESSecurityUtils;
import com.sun.org.apache.xml.internal.security.utils.Base64;
import org.junit.Before;
import org.junit.Test;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author huwenlong
 * @date 2020/9/18 20:22
 */
public class AES {

    private Cipher cipher;

    private SecretKeySpec secretKeySpec;

    public void init() throws Exception {
        String type = "AES";
        String pattern = "AES/CBC/PKCS5Padding";
        cipher = Cipher.getInstance(pattern);
        // AES加密key是16位字符串
        String key = "1234567890ABCDEF";
        secretKeySpec = new SecretKeySpec(key.getBytes(), type);
        //ivParameterSpec = new IvParameterSpec(key.getBytes());
    }


    public void testEncipher() throws Exception {
        System.out.println(encipher("hello")); // jtElayKS51OV98k4g2FX9A==
    }


    public void testDecipher() throws Exception {
        System.out.println(decipher(encipher("hello"))); // hello

    }

    public String encipher(String str) throws Exception {
        // 设置加密规则，如果加密模式是CBC加密则需要设置ivParameterSpec
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        return Base64.encode(cipher.doFinal(str.getBytes()));
    }

    public String decipher(String str) throws Exception {
        // 设置解密规则，如果加密模式是CBC加密则需要设置ivParameterSpec
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        return new String(cipher.doFinal(Base64.decode(str)));
    }

    public static void main(String[] args) throws Exception{
        AES aes = new AES();
        aes.init();
        //L5HnTFIpHBq5LVqrJiSVhA==
        ///Vmb9detUN5/hCvAU15dMg==
        //cwPp5rSIqAyxmfr3uq0Nxw==
        System.out.println(aes.encipher("centit.1"));

        System.out.println(AESSecurityUtils.encryptAndBase64(
            "centit.1", "1234567890ABCDEF"));

        System.out.println(AESSecurityUtils.encryptParameterString("centit.1"));
        System.out.println(AESSecurityUtils.decryptParameterString("cipher:pzS0Vd7iuzB2C+pdhEjVzw=="));
        System.out.println(AESSecurityUtils.decryptParameterString("cipher:U2FsdGVkX1+BymlPjWWWUG1TpTpkinX9pNNs+I4xqBU="));
    }
}
