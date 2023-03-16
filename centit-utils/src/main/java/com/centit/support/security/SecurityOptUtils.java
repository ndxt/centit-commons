package com.centit.support.security;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

@SuppressWarnings("unused")
public abstract class SecurityOptUtils {

    protected static final Logger logger = LoggerFactory.getLogger(SecurityOptUtils.class);

    public static String decodeSecurityString(String sStr){
        if(sStr==null)
            return "";
        sStr = sStr.trim();
        if (sStr.startsWith("encode:")) {
            return new String(Base64.decodeBase64(sStr.substring(7))).trim();
        } if (sStr.startsWith("cipher:")) {
            return AESSecurityUtils.decryptBase64String(sStr.substring(7), AESSecurityUtils.AES_DEFAULT_KEY);
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
            default:
                return sStr;
        }
    }
}
