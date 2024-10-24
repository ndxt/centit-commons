package com.centit.support.test.security.test;

import org.bouncycastle.asn1.gm.GMNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.math.BigInteger;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;

public class TestSM2Private {
    public static void main(String[] args) throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        // 生成SM2的密钥对
        String priKey = "3690655E33D5EA3D9A4AE1A1ADD766FDEA045CDEAA43A9206FB8C430CEFE0D94";
        BigInteger privateKeyD = new BigInteger(priKey, 16);
        X9ECParameters sm2ECParameters = GMNamedCurves.getByName("sm2p256v1");
        ECDomainParameters domainParameters = new ECDomainParameters(sm2ECParameters.getCurve(), sm2ECParameters.getG(), sm2ECParameters.getN());
        ECPrivateKeyParameters privateKeyParameters = new ECPrivateKeyParameters(privateKeyD, domainParameters);
        // 将ECPrivateKeyParameters转换为Java的PrivateKey

        // 使用SM2的私钥进行加密
        SM2Engine engine = new SM2Engine();
        engine.init(true, new ParametersWithRandom(privateKeyParameters, new SecureRandom()));

        byte[] data = "需要加密的数据".getBytes();
        byte[] encryptedData = engine.processBlock(data, 0, data.length);

        // 输出加密结果
        System.out.println("加密后的数据: " + bytesToHexString(encryptedData));
    }

    private static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        for (byte aSrc : src) {
            final String hex = Integer.toHexString(aSrc & 0xFF);
            if (hex.length() == 1) {
                stringBuilder.append('0');
            }
            stringBuilder.append(hex);
        }
        return stringBuilder.toString();
    }

}
