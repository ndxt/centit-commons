package com.centit.support.test.security;

import com.centit.support.file.FileIOOpt;
import com.centit.support.security.SM2Util;
import org.apache.commons.lang3.tuple.Pair;

import org.apache.commons.codec.binary.Base64;

import java.io.IOException;


/**
 * 简单的sm2
 */
public class TestSM2 {
    final static String pubKey = "04F6E0C3345AE42B51E06BF50B98834988D54EBC7460FE135A48171BC0629EAE205EEDE253A530608178A98F1E19BB737302813BA39ED3FA3C51639D7A20C7391A";
    final static String priKey = "3690655E33D5EA3D9A4AE1A1ADD766FDEA045CDEAA43A9206FB8C430CEFE0D94";
    public static void main2(String[] args)  throws Exception {
        byte[] data = FileIOOpt.readBytesFromFile("/Users/codefan/projects/RunData/temp/fepCorp.zip");
        byte[] sign = SM2Util.sign(data, priKey);
        System.out.println(Base64.encodeBase64String(sign));
        System.out.println(SM2Util.verify(data, pubKey, sign));
    }

    public static void main5(String[] args) throws IOException {
        String password = "a123456789012345";
        String publicKey="04F6E0C3345AE42B51E06BF50B98834988D54EBC7460FE135A48171BC0629EAE205EEDE253A530608178A98F1E19BB737302813BA39ED3FA3C51639D7A20C7391A";
        System.out.println(Base64.encodeBase64String(SM2Util.encryptUsePublicKey( password.getBytes(), publicKey)));
    }


    public static void main(String[] args) throws IOException {
        String dataBase64 ="MHgCIGmOeERO+YT/Ld5DWv31QqCMCFn3p7UJM4RfKjXIX1J3AiBJdiIqSUVSN7E0m6hTVR6BtgewdtQBj0tYdA/AIO/dZAQgaKGllqH2OEkayagjUUE8/PQdlQPG8Oo6Iwz+KSYzAnUEEOuTCiHFycxmmXzHp1qjV0A=";
        byte[] plainText2 = SM2Util.decryptUserPrivateKey(Base64.decodeBase64(dataBase64), priKey);
        System.out.println(new  String(plainText2));

    }

    public static void main3(String[] args)  throws Exception {

        Pair<String, String> key = SM2Util.generateSm2Keys(false);
        String plainText = "哈哈哈哈哈你好";

        byte[] secretKeyByte = SM2Util.encryptUsePublicKey(plainText.getBytes(), pubKey);
        byte[] plainText2 = SM2Util.decryptUserPrivateKey(secretKeyByte, priKey);
        System.out.println(new  String(plainText2));

        //System.out.println(key.getLeft());
        //System.out.println(key.getRight());

        secretKeyByte = SM2Util.encryptUsePublicKey(plainText.getBytes(), key.getLeft());
        plainText2 = SM2Util.decryptUserPrivateKey(secretKeyByte, key.getRight());
        System.out.println(new  String(plainText2));


    }
}


