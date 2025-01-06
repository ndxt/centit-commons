package com.centit.support.test.security;

import com.centit.support.file.FileIOOpt;
import com.centit.support.security.SM2Util;
import org.apache.commons.lang3.tuple.Pair;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.asn1.*;
import org.bouncycastle.math.ec.ECPoint;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
        System.out.println(Base64.encodeBase64String(SM2Util.encryptUsePublicKey(password.getBytes(), publicKey)));
    }


    public static void main7(String[] args) throws IOException {
        String dataBase64 = "MIGCAiEA7KsfoMvZojLQgdT5NtQCut/th85uohTJRt7hdl4hBhECICdX1/Cfrvo1FjAtVjJWaOGPQkQpqRKPWOqNYLPMJqsDBCCw1kixD4b2pVYavePAvq9wJ+PpRiHMProcGguQ47UC0QQZo+TY7lYRmFxCZKraarAKSiCUmjV39oKk9Q==";
        //String dataBase64 = "MG4CIBQHONOC7dpsKIu2hmxZk6zwCWlLAfUkwvqcSGTQHodFAiA6ROjC+3bdLHqgDsRyraPp5ZtzTGaEbLT0ZwQVxtyuZwQg4uxurG2CtEs5OIrfMOn9+ntJMNO/Uwn4xQ6sUwyCn44EBv5oIV5bqw==";
        //String dataBase64 ="MH4CIBnq72QkaTobS+TIP58MbnFVptb6F4HSlGNAGQlm/Z44AiAlbUKF5m2L0XZtO9bizHtBF7bBKpAkb7bn5IKpl/NjLgQgd3Jogb1EWrFCGZYoZN6NRBexLpMq+Ih1fl7FXPgrInUEFqxSn77S60vntcDHoxqJQE8EWStckm0=";
        byte [] ansDatas = Base64.decodeBase64(dataBase64);
        System.out.println(ansDatas.length);
        byte [] datas = SM2Util.obtainAniBytes(ansDatas);
        System.out.println(datas.length);
        byte[] plainText2 = SM2Util.decryptUserPrivateKey(datas, priKey);
        System.out.println(new  String(plainText2));
// SM2私钥解密时出现异常:Invalid point encoding 0x69
// SM2私钥解密时出现异常:Invalid point encoding 0x30
    }

    public static void main(String[] args)  throws Exception {
        String plainText = "你好哈哈发生是不是又可以了";
        byte [] datas =plainText.getBytes();

        byte[] secretKeyByte = SM2Util.encryptUsePublicKey(datas, pubKey);

        byte[] mwAsn1 = SM2Util.toAniBytes(secretKeyByte);
        System.out.println(Base64.encodeBase64String(mwAsn1));

        byte[] plainText2 = SM2Util.decryptUserPrivateKey(SM2Util.obtainAniBytes(mwAsn1), priKey);
        System.out.println(new String(plainText2));

    }
}


