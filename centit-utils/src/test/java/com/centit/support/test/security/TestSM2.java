package com.centit.support.test.security;

import com.centit.support.security.SM2Util;
import org.apache.commons.lang3.tuple.Pair;


/**
 * 简单的sm2
 */
public class TestSM2 {



    public static void main(String[] args)  throws Exception {

        Pair<String, String> key = SM2Util.generateKey(false);
        String plainText = "哈哈哈哈哈你好";

        String pubKey = "04F6E0C3345AE42B51E06BF50B98834988D54EBC7460FE135A48171BC0629EAE205EEDE253A530608178A98F1E19BB737302813BA39ED3FA3C51639D7A20C7391A";
        byte[] secretKeyByte = SM2Util.encryptUsePublicKey(pubKey, plainText.getBytes());
        String priKey = "3690655E33D5EA3D9A4AE1A1ADD766FDEA045CDEAA43A9206FB8C430CEFE0D94";
        byte[] plainText2 = SM2Util.decryptUserPrivateKey(priKey, secretKeyByte);
        System.out.println(new  String(plainText2));

        //System.out.println(key.getLeft());
        //System.out.println(key.getRight());

        secretKeyByte = SM2Util.encryptUsePublicKey(key.getLeft(), plainText.getBytes());
        plainText2 = SM2Util.decryptUserPrivateKey(key.getRight(), secretKeyByte);
        System.out.println(new  String(plainText2));


    }
}


