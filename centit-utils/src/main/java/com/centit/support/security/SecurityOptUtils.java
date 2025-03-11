package com.centit.support.security;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

@SuppressWarnings("unused")
public abstract class SecurityOptUtils {

    protected static final Logger logger = LoggerFactory.getLogger(SecurityOptUtils.class);

    public static Pair<byte[], byte[]> makeCbcKey(String password, String algorithm){
        if(StringUtils.isBlank(password)){
            if(StringUtils.equalsIgnoreCase("SM4", algorithm)){
                return new MutablePair<>(SM4Util.SM4_SECRET_KEY_SPEC.getBytes(), SM4Util.SM4_IV_PARAMETER_SPEC.getBytes());
            } else { //AES
                return new ImmutablePair<>(AESSecurityUtils.AES_SECRET_KEY_SPEC.getBytes(), AESSecurityUtils.AES_IV_PARAMETER_SPEC.getBytes());
            }
        }
        int strLen = password.length();
        if(strLen == 64){
            return new ImmutablePair<>(Hex.decode(password.substring(0,32)), Hex.decode(password.substring(32,64)));
        }
        while (strLen < 32){
            password = password + password;
            strLen *= 2;
        }
        return new ImmutablePair<>(password.substring(0,16).getBytes(StandardCharsets.UTF_8),
            password.substring(16,32).getBytes(StandardCharsets.UTF_8));
    }

    public static String decodeSecurityString(String sStr){
        if(StringUtils.isBlank(sStr))
            return "";
        sStr = sStr.trim();
        if (sStr.startsWith("encode:")) {
            return new String(Base64.decodeBase64(sStr.substring(7))).trim();
        } if (sStr.startsWith("cipher:")) {
            return AESSecurityUtils.decryptBase64String(sStr.substring(7), AESSecurityUtils.AES_DEFAULT_KEY);
        } else if (sStr.startsWith("aescbc:")) {
            return AESSecurityUtils.decryptAsCBCType(sStr.substring(7),
                AESSecurityUtils.AES_SECRET_KEY_SPEC, AESSecurityUtils.AES_IV_PARAMETER_SPEC);
        } else if (sStr.startsWith("sm4cbc:")) {
            return SM4Util.decryptAsCBCType(sStr.substring(7),
                SM4Util.SM4_SECRET_KEY_SPEC, SM4Util.SM4_IV_PARAMETER_SPEC);
        } else {
            return sStr;
        }
    }

    public static String encodeSecurityString(String sStr, String encType){
        if(sStr==null)
            return "";
        switch (encType){
            case "cipher":
                return "cipher:" + AESSecurityUtils.encryptAndBase64(
                    sStr, AESSecurityUtils.AES_DEFAULT_KEY);
            case "base64":
                return "encode:" + Base64.encodeBase64String(sStr.getBytes(StandardCharsets.UTF_8));
            case "aescbc":
                return "aescbc:" + AESSecurityUtils.encryptAsCBCType(sStr,
                    AESSecurityUtils.AES_SECRET_KEY_SPEC, AESSecurityUtils.AES_IV_PARAMETER_SPEC);
            case "sm4cbc":
                return "sm4cbc:" + SM4Util.encryptAsCBCType(sStr,
                    SM4Util.SM4_SECRET_KEY_SPEC, SM4Util.SM4_IV_PARAMETER_SPEC);
            default:
                return sStr;
        }
    }
}
