package com.centit.support.test.security;

import com.centit.support.security.AESSecurityUtils;
import org.apache.commons.codec.binary.Base64;

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
    private IvParameterSpec ivParameterSpec;
    public void init() throws Exception {
        String type = "AES";
        String pattern = "AES/CBC/PKCS5Padding";
        cipher = Cipher.getInstance(pattern);
        // AES加密key是16位字符串
        String key = "1234567890abcdef";
        secretKeySpec = new SecretKeySpec(key.getBytes(), type);
        ivParameterSpec = new IvParameterSpec(key.getBytes());
    }


    public void testEncipher() throws Exception {
        System.out.println(encipher("hello")); // jtElayKS51OV98k4g2FX9A==
    }


    public void testDecipher() throws Exception {
        System.out.println(decipher(encipher("hello"))); // hello

    }

    public String encipher(String str) throws Exception {
        // 设置加密规则，如果加密模式是CBC加密则需要设置ivParameterSpec
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
        return Base64.encodeBase64String(cipher.doFinal(str.getBytes()));
    }

    public String decipher(String str) throws Exception {
        // 设置解密规则，如果加密模式是CBC加密则需要设置ivParameterSpec
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
        return new String(cipher.doFinal(Base64.decodeBase64(str)));
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

        System.out.println(AESSecurityUtils.encryptAsCBCTypeAsBase64("centit.1",
            "1234567890abcdef", "1234567890abcdef"));
        System.out.println(AESSecurityUtils.encryptAsCBCTypeAsBase64("centit.1",
            AESSecurityUtils.AES_SECRET_KEY_SPEC, AESSecurityUtils.AES_IV_PARAMETER_SPEC));

        System.out.println(AESSecurityUtils.decryptBase64AsCBCType("k556Ug617iPVULItZldkOQ==",
            AESSecurityUtils.AES_SECRET_KEY_SPEC, AESSecurityUtils.AES_IV_PARAMETER_SPEC));

    }
}
