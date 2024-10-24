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
     *  测试通过
     * @param publicKey 公钥
     * @param data 明文数据
     * @return 秘文
     */
    public static  byte[] encryptUsePublicKey(String publicKey, byte[] data) {
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
        sm2Engine.init(true, new ParametersWithRandom(publicKeyParameters, new SecureRandom()));

        try {
            return sm2Engine.processBlock(data, 0, data.length);
        } catch (Exception e) {
            System.out.println("SM2公钥加密时出现异常:"+e.getMessage());
        }
        return null;

    }

    /**
     *  测试没有通过
     * @param publicKey 公钥
     * @param cipherData 秘文数据
     * @return 明文
     */
    public static byte[]  decryptUsePublicKey(String publicKey, byte[] cipherData) {

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
        sm2Engine.init(false, publicKeyParameters);
        try {
            return sm2Engine.processBlock(cipherData, 0, cipherData.length);
        } catch (Exception e) {
            System.out.println("SM2公钥解密时出现异常:"+e.getMessage());
        }
        return null;

    }

    /**
     *  测试没有通过
     * @param privateKey 私钥
     * @param data 明文数据
     * @return 秘文
     */
    public static byte[]  encryptUsePrivateKey(String privateKey, byte[] data) {

        // 初始化SM2算法需要的参数
        BigInteger privateKeyD = new BigInteger(privateKey, 16);
        //获取一条SM2曲线参数
        X9ECParameters sm2ECParameters = GMNamedCurves.getByName("sm2p256v1");
        //构造domain参数
        ECDomainParameters domainParameters = new ECDomainParameters(sm2ECParameters.getCurve(), sm2ECParameters.getG(), sm2ECParameters.getN());
        ECPrivateKeyParameters privateKeyParameters = new ECPrivateKeyParameters(privateKeyD, domainParameters);
        try {
            // 加密私钥
            SM2Engine engine = new SM2Engine();
            engine.init(true, new ParametersWithRandom(privateKeyParameters, new SecureRandom()));
            return engine.processBlock(data, 0, data.length);
        } catch (Exception e) {
            System.out.println("SM2私钥加密时出现异常:"+e.getMessage());
        }
        return null;
    }

    /**
     *  测试通过
     * @param privateKey 私钥
     * @param cipherDataByte 秘文数据
     * @return 明文
     */
    public static byte[] decryptUserPrivateKey(String privateKey, byte[] cipherDataByte) {
        BigInteger privateKeyD = new BigInteger(privateKey, 16);
        //获取一条SM2曲线参数
        X9ECParameters sm2ECParameters = GMNamedCurves.getByName("sm2p256v1");
        //构造domain参数
        ECDomainParameters domainParameters = new ECDomainParameters(sm2ECParameters.getCurve(), sm2ECParameters.getG(), sm2ECParameters.getN());
        ECPrivateKeyParameters privateKeyParameters = new ECPrivateKeyParameters(privateKeyD, domainParameters);

        SM2Engine sm2Engine = new SM2Engine(SM2Engine.Mode.C1C3C2);
        // 设置sm2为解密模式
        sm2Engine.init(false, privateKeyParameters);

        try {
            return sm2Engine.processBlock(cipherDataByte, 0, cipherDataByte.length);
        } catch (Exception e) {
            System.out.println("SM2私钥解密时出现异常:"+e.getMessage());
        }
        return null;
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
