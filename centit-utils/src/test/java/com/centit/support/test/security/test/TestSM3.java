package com.centit.support.test.security.test;

import com.centit.support.security.SM3Util;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

public class TestSM3 {
    public static void main(String[] args) {
        System.out.println(
            Hex.encodeHex(SM3Util.hash(
                Base64.encodeBase64("codefan@sina.com".getBytes()))));
    }
}

