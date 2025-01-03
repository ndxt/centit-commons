package com.centit.support.test.security;

import com.centit.support.file.FileIOOpt;
import com.centit.support.security.SM4Util;

import java.nio.charset.StandardCharsets;

public class TestSM4 {

    public static void main(String[] args) throws Exception {
        byte[] bytes = FileIOOpt.readBytesFromFile("/Users/codefan/Downloads/errorreport.sm4");
        byte[] cipherText = SM4Util.decryptEcbPadding("a123456789012345".getBytes(StandardCharsets.UTF_8), bytes);
        FileIOOpt.writeBytesToFile(cipherText, "/Users/codefan/Downloads/errorreport2.png");
        System.out.println("解密成功");
    }

    public static void main3(String[] args) throws Exception {
        byte[] bytes = FileIOOpt.readBytesFromFile("/Users/codefan/Downloads/7268db2618db3a79068211501aaa637.png");
        byte[] cipherText = SM4Util.encryptEcbPadding("a123456789012345".getBytes(StandardCharsets.UTF_8), bytes);
        FileIOOpt.writeBytesToFile(cipherText, "/Users/codefan/Downloads/errorreport.sm4");
        System.out.println("加密成功");
    }

    public static void main2(String[] args) throws Exception {
        byte[] bytes = FileIOOpt.readBytesFromFile("/Users/codefan/Downloads/7268db2618db3a79068211501aaa637.png");
        byte[] cipherText = SM4Util.encryptEcbPadding("a123456789012345".getBytes(StandardCharsets.UTF_8), bytes);
        System.out.println(cipherText.length);
        byte[] bytes2 = SM4Util.decryptEcbPadding("a123456789012345".getBytes(StandardCharsets.UTF_8), cipherText);
        System.out.println(bytes2.length);
        if(bytes.length != bytes2.length){
            System.out.println("解密失败");
            return;
        }
        for(int i=0; i<bytes.length; i++){
            if(bytes[i] != bytes2[i]){
                System.out.println("解密失败");
                return;
            }
        }
        System.out.println("解密成功");
    }
}
