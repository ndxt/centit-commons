package com.centit.support.test.security.test;

import com.centit.support.security.SM2Util;
import org.apache.commons.lang3.tuple.Pair;


/**
 * 简单的sm2
 */
public class TestSM2 {



    /*@Test
    // 生成密钥
    public void createKey() throws Exception{
        //String M="encryption standard111111111111111111111111111111";
        SimpSM2Util sm2 = new SimpSM2Util();
        ECGenParameterSpec sm2Spec = new ECGenParameterSpec("sm2p256v1");
        // 获取一个椭圆曲线类型的密钥对生成器
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC", new BouncyCastleProvider());
        // 使用SM2参数初始化生成器
        kpg.initialize(sm2Spec);
        // 获取密钥对
        KeyPair keyPair = kpg.generateKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        BCECPublicKey p=(BCECPublicKey)publicKey;
        System.out.println("publicKey："+Hex.toHexString(p.getQ().getEncoded(false)));

        PrivateKey privateKey = keyPair.getPrivate();
        BCECPrivateKey s=(BCECPrivateKey)privateKey;
        System.out.println("privateKey："+Hex.toHexString(s.getD().toByteArray()));

    }*/
    public static void main(String[] args)  throws Exception {

        Pair<String, String> key = SM2Util.createKey();
        String plainText = "哈哈哈哈哈你好";

        String pubKey = "04F6E0C3345AE42B51E06BF50B98834988D54EBC7460FE135A48171BC0629EAE205EEDE253A530608178A98F1E19BB737302813BA39ED3FA3C51639D7A20C7391A";
        String secretKeyByte = SM2Util.encryptUsePublicKey(pubKey, plainText);
        String priKey = "3690655E33D5EA3D9A4AE1A1ADD766FDEA045CDEAA43A9206FB8C430CEFE0D94";
        plainText = SM2Util.decryptUserPrivateKey(priKey, secretKeyByte);
        System.out.println(plainText);

        secretKeyByte = SM2Util.encryptUsePublicKey(key.getLeft(), plainText);
        plainText = SM2Util.decryptUserPrivateKey(key.getRight(), secretKeyByte);
        System.out.println(plainText);

        secretKeyByte = SM2Util.encryptUsePrivateKey(key.getRight(), plainText);
        plainText = SM2Util.decryptUsePublicKey(key.getLeft(), secretKeyByte);
        System.out.println(plainText);
    }
}


