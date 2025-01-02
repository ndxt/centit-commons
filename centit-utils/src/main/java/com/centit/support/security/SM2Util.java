package com.centit.support.security;

import com.centit.support.common.ObjectException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.tuple.Pair;
import org.bouncycastle.asn1.gm.GMNamedCurves;
import org.bouncycastle.asn1.gm.GMObjectIdentifiers;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.params.*;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jcajce.provider.asymmetric.x509.CertificateFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPrivateKeySpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.Hex;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.X509Certificate;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Locale;

public abstract class SM2Util {
    /**
     * 数字签名参数
     */
    public static final String CRYPTO_NAME_SM2 = "sm2p256v1";

    /**
     * 从证书中获取公钥，证书为base64编码字符串
     * @param certStr 证书为base64编码字符串
     * @return 公钥 04开头
     */
    public static String obtainPublicKey(String certStr){
        try {
            CertificateFactory factory = new CertificateFactory();
            X509Certificate certificate = (X509Certificate) factory
                .engineGenerateCertificate(new ByteArrayInputStream(Base64.decodeBase64(certStr)));
            String publicKeyStr = Hex.toHexString(certificate.getPublicKey().getEncoded());
            return publicKeyStr.length()>130?
                publicKeyStr.substring(publicKeyStr.length() - 130) : publicKeyStr;
        }catch (Exception e){
            throw new ObjectException(ObjectException.DATA_VALIDATE_ERROR, "证书解析错误，请检查证书格式！", e);
        }
    }

    /**
     *  测试通过
     * @param publicKey 公钥
     * @param data 明文数据
     * @return 秘文
     */
    public static  byte[] encryptUsePublicKey(byte[] data, String publicKey) {
        if (publicKey.length() == 128) {
            publicKey = "04" + publicKey;
        } else if (publicKey.length() > 500) { //这个应该是传成了base64证书了
            publicKey = obtainPublicKey(publicKey);
        }
        // 获取一条SM2曲线参数
        X9ECParameters sm2ECParameters = GMNamedCurves.getByName(CRYPTO_NAME_SM2);
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
     *  测试通过
     * @param privateKey 私钥
     * @param cipherDataByte 秘文数据
     * @return 明文
     */
    public static byte[] decryptUserPrivateKey(byte[] cipherDataByte, String privateKey) {
        BigInteger privateKeyD = new BigInteger(privateKey, 16);
        //获取一条SM2曲线参数
        X9ECParameters sm2ECParameters = GMNamedCurves.getByName(CRYPTO_NAME_SM2);
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

    public static Pair<String, String> generateKey(boolean compressed)  {
        try {
            ECGenParameterSpec sm2Spec = new ECGenParameterSpec(CRYPTO_NAME_SM2);
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
            return Pair.of(Hex.toHexString(p.getQ().getEncoded(compressed)),
                Hex.toHexString(s.getD().toByteArray()));
        } catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException e) {
            return Pair.of("error", e.getMessage());
        }
    }

    public static Pair<String, String> generateSm2Keys(boolean compressed) {
        // 获取一条SM2曲线参数
        X9ECParameters sm2ECParameters = GMNamedCurves.getByName(CRYPTO_NAME_SM2);
        // 构造domain参数
        ECDomainParameters domainParameters = new ECDomainParameters(sm2ECParameters.getCurve(),
            sm2ECParameters.getG(), sm2ECParameters.getN());
        // 创建秘钥对生成器
        ECKeyPairGenerator keyPairGenerator = new ECKeyPairGenerator();
        // 初始化生成器,带上随机数
        keyPairGenerator.init(new ECKeyGenerationParameters(domainParameters, new SecureRandom()));
        // 生成秘钥对
        AsymmetricCipherKeyPair asymmetricCipherKeyPair = keyPairGenerator.generateKeyPair();
        // 把公钥转换为椭圆点
        ECPublicKeyParameters publicKeyParameters = (ECPublicKeyParameters) asymmetricCipherKeyPair.getPublic();
        ECPoint ecPoint = publicKeyParameters.getQ();
        // 把公钥转换为HEX
        // 公钥前面的02或者03表示是压缩公钥,04表示未压缩公钥,04的时候,可以去掉前面的04,默认压缩公钥
        String publicKey = Hex.toHexString(ecPoint.getEncoded(compressed)).toUpperCase(Locale.ROOT);
        // 把私钥转换为HEX
        ECPrivateKeyParameters privateKeyParameters = (ECPrivateKeyParameters) asymmetricCipherKeyPair.getPrivate();
        BigInteger intPrivateKey = privateKeyParameters.getD();
        String privateKey = intPrivateKey.toString(16).toUpperCase(Locale.ROOT);
        // 构造HEX秘钥对，并返回
        return Pair.of(publicKey, privateKey);
    }

    /**
     * 签名
     *
     * @param priKey    私钥
     * @param data  待签名数据
     * @return 签名
     */
    public static byte[] sign(byte[] data, String priKey) {
        try {
            // 构造提供器
            BouncyCastleProvider provider = new BouncyCastleProvider();
            // 获取一条SM2曲线参数
            X9ECParameters sm2ECParameters = GMNamedCurves.getByName(CRYPTO_NAME_SM2);
            // 构造椭圆参数规格
            ECParameterSpec ecParameterSpec = new ECParameterSpec(sm2ECParameters.getCurve(),
                sm2ECParameters.getG(), sm2ECParameters.getN(), sm2ECParameters.getH());
            // 创建Key工厂
            KeyFactory keyFactory = KeyFactory.getInstance("EC", provider);
            // 创建签名对象
            Signature signature = Signature.getInstance(GMObjectIdentifiers.sm2sign_with_sm3.toString(), provider);
            // 将私钥HEX字符串转换为X值
            BigInteger bigInteger = new BigInteger(priKey, 16);
            // 生成SM2私钥
            BCECPrivateKey bcecPrivateKey = (BCECPrivateKey) keyFactory.generatePrivate(new ECPrivateKeySpec(bigInteger, ecParameterSpec));
            // 初始化为签名状态
            signature.initSign(bcecPrivateKey);
            // 传入签名字节
            signature.update(data);
            // 签名
            return signature.sign();
        } catch (Exception e) {
            throw new ObjectException(e);
        }
    }

    /**
     * 验签
     *
     * @param pubKey         公钥
     * @param data      签名的数据
     * @param signatureValue 签名
     * @return 验签结果
     */
    public static boolean verify(byte[] data, String pubKey, byte[] signatureValue) {
        // 非压缩模式公钥对接放是128位HEX秘钥，需要为BC库加上“04”标记
        if (pubKey.length() == 128) {
            pubKey = "04" + pubKey;
        }
        try {
            // 构造提供器
            BouncyCastleProvider provider = new BouncyCastleProvider();
            // 获取一条SM2曲线参数
            X9ECParameters sm2ECParameters = GMNamedCurves.getByName(CRYPTO_NAME_SM2);
            // 构造椭圆参数规格
            ECParameterSpec ecParameterSpec = new ECParameterSpec(sm2ECParameters.getCurve(),
                sm2ECParameters.getG(), sm2ECParameters.getN(), sm2ECParameters.getH());
            // 创建Key工厂
            KeyFactory keyFactory = KeyFactory.getInstance("EC", provider);
            // 创建签名对象
            Signature signature = Signature.getInstance(GMObjectIdentifiers.sm2sign_with_sm3.toString(), provider);
            // 将公钥HEX字符串转换为椭圆曲线对应的点
            ECPoint ecPoint = sm2ECParameters.getCurve().decodePoint(Hex.decode(pubKey));
            BCECPublicKey bcecPublicKey = (BCECPublicKey) keyFactory.generatePublic(new ECPublicKeySpec(ecPoint, ecParameterSpec));
            // 初始化为验签状态
            signature.initVerify(bcecPublicKey);
            signature.update(data);
            return signature.verify(signatureValue);
        } catch (Exception e) {
            throw new ObjectException(e);
        }
    }

    /**
     * 证书验签
     * @param certStr      证书串
     * @param data         签名原文
     * @param signValueStr 签名产生签名值 此处的签名值实际上就是 R和S的sequence
     * @return 证书验证结果
     */
    public static boolean certVerify(byte[] data, String certStr, String signValueStr) {
        try {
            // 构造提供器
            BouncyCastleProvider provider = new BouncyCastleProvider();
            // 解析证书
            byte[] signValue = Hex.decode(signValueStr);
            CertificateFactory factory = new CertificateFactory();
            X509Certificate certificate = (X509Certificate) factory
                .engineGenerateCertificate(new ByteArrayInputStream(Hex.decode(certStr)));
            // 验证签名
            Signature signature = Signature.getInstance(certificate.getSigAlgName(), provider);
            signature.initVerify(certificate);
            signature.update(data);
            return signature.verify(signValue);
        } catch (Exception e) {
            throw new ObjectException(e);
        }
    }

}
