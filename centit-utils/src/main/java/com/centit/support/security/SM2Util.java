package com.centit.support.security;

import org.apache.commons.lang3.tuple.Pair;
import org.bouncycastle.asn1.gm.GMNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.security.*;
import java.security.spec.ECGenParameterSpec;

public abstract class SM2Util {

    /**
     *
     * @param publicKey 公钥
     * @param data 明文数据
     * @param forEncryption true 加密， false 解密
     * @return
     */
    private static String usePublicKey(String publicKey, String data, boolean forEncryption){
        byte[] in;// = data.getBytes();
        if(forEncryption){
            in = data.getBytes();
        } else {
            if (!data.startsWith("04")){
                data = "04" + data;
            }
            in = Hex.decode(data);
        }
        // 获取一条SM2曲线参数
        X9ECParameters sm2ECParameters = GMNamedCurves.getByName("sm2p256v1");
        // 构造ECC算法参数，曲线方程、椭圆曲线G点、大整数N
        ECDomainParameters domainParameters = new ECDomainParameters(sm2ECParameters.getCurve(), sm2ECParameters.getG(), sm2ECParameters.getN());
        //提取公钥点
        ECPoint pukPoint = sm2ECParameters.getCurve().decodePoint(Hex.decode(publicKey));
        // 公钥前面的02或者03表示是压缩公钥，04表示未压缩公钥, 04的时候，可以去掉前面的04
        ECPublicKeyParameters publicKeyParameters = new ECPublicKeyParameters(pukPoint, domainParameters);

        SM2Engine sm2Engine = new SM2Engine(SM2Engine.Mode.C1C3C2);
        // 设置sm2为加密模式
        sm2Engine.init(forEncryption, new ParametersWithRandom(publicKeyParameters, new SecureRandom()));

        byte[] arrayOfBytes = null;
        try {
            arrayOfBytes = sm2Engine.processBlock(in, 0, in.length);
        } catch (Exception e) {
            System.out.println("SM2加密时出现异常:"+e.getMessage());
        }
        return Hex.toHexString(arrayOfBytes);
    }

    private static String usePrivateKey(String privateKey, String data, boolean forEncryption){
        byte[] cipherDataByte;// = data.getBytes();
        if(forEncryption){
            cipherDataByte = data.getBytes();
        } else {
            if (!data.startsWith("04")){
                data = "04" + data;
            }
            cipherDataByte = Hex.decode(data);
        }

        BigInteger privateKeyD = new BigInteger(privateKey, 16);
        //获取一条SM2曲线参数
        X9ECParameters sm2ECParameters = GMNamedCurves.getByName("sm2p256v1");
        //构造domain参数
        ECDomainParameters domainParameters = new ECDomainParameters(sm2ECParameters.getCurve(), sm2ECParameters.getG(), sm2ECParameters.getN());
        ECPrivateKeyParameters privateKeyParameters = new ECPrivateKeyParameters(privateKeyD, domainParameters);

        SM2Engine sm2Engine = new SM2Engine(SM2Engine.Mode.C1C3C2);
        // 设置sm2为解密模式
        sm2Engine.init(forEncryption, privateKeyParameters);

        String result = "";
        try {
            byte[] arrayOfBytes = sm2Engine.processBlock(cipherDataByte, 0, cipherDataByte.length);
            return new String(arrayOfBytes);
        } catch (Exception e) {
            System.out.println("SM2解密时出现异常:"+e.getMessage());
        }
        return result;
    }

    public static String encryptUsePublicKey(String publicKey, String data) {
        return usePublicKey(publicKey, data, true);

    }
    public static String decryptUsePublicKey(String publicKey, String cipherData) {
        return usePublicKey(publicKey, cipherData, false);

    }

    public static String encryptUsePrivateKey(String privateKey, String data) {
        return usePrivateKey(privateKey, data, true);

    }

    public static String decryptUserPrivateKey(String privateKey, String cipherData) {
        return usePrivateKey(privateKey, cipherData, false);
    }

    public static Pair<String, String> createKey()  {
        try {
            ECGenParameterSpec sm2Spec = new ECGenParameterSpec("sm2p256v1");
            // 获取一个椭圆曲线类型的密钥对生成器
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC", new BouncyCastleProvider());
            // 使用SM2参数初始化生成器
            kpg.initialize(sm2Spec);
            // 获取密钥对
            KeyPair keyPair = kpg.generateKeyPair();
            PublicKey publicKey = keyPair.getPublic();
            BCECPublicKey p = (BCECPublicKey) publicKey;
            //System.out.println("publicKey：" + Hex.toHexString(p.getQ().getEncoded(false)));
            PrivateKey privateKey = keyPair.getPrivate();
            BCECPrivateKey s = (BCECPrivateKey) privateKey;
            //System.out.println("privateKey：" + Hex.toHexString(s.getD().toByteArray()));
            return Pair.of(Hex.toHexString(p.getQ().getEncoded(false)),
                Hex.toHexString(s.getD().toByteArray()));
        } catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException e) {
            return Pair.of("error", e.getMessage());
        }
    }
}
