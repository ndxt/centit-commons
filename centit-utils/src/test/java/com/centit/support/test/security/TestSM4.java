package com.centit.support.test.security;

import com.centit.support.file.FileIOOpt;
import com.centit.support.security.SM4Util;

import java.nio.charset.StandardCharsets;

public class TestSM4 {
    public static void main(String[] args) throws Exception {
        byte[] bytes = FileIOOpt.readBytesFromFile("/Users/codefan/Downloads/certify_2024-10-28.pdf");
        byte[] cipherText = SM4Util.encryptEcbPadding("a123456789012345".getBytes(StandardCharsets.UTF_8), bytes);
        System.out.println(cipherText.length);
    }
}
