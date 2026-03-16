package com.centit.support.test.security;

import com.centit.support.algorithm.UuidOpt;
import com.centit.support.file.FileIOOpt;
import com.centit.support.security.SM4Util;
import com.centit.support.security.SecurityOptUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.nio.charset.StandardCharsets;

public class TestSM4 {

    public static void main(String[] args) throws Exception {
        String password = "SGVsbG8gV29ybGQ0SGVsbG8gV29ybGQ0SGVsbG8gV28=";
        Pair<byte[], byte[]> key = SecurityOptUtils.makeCbcKey(password, "AES");
        System.out.println(new String(key.getLeft()));
        System.out.println(new String(key.getRight()));
        System.out.println("解密成功");

        key = SecurityOptUtils.makeCbcKey(
            "41414141414141414141414141414141" +
                      "000000000000000000000000000000000", "AES");
        System.out.println(key.getLeft());
        System.out.println(key.getRight());
        System.out.println("解密成功");
    }

    public static void main4(String[] args) throws Exception {
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

    public static void main12(String[] args) throws Exception {  String helloWorld = "Hello World！";
        byte[] bytes = helloWorld.getBytes();
        byte[] ivBytes = UuidOpt.randomString(16).getBytes();
        byte[] cipherText = SM4Util.encryptCbcPadding("a123456789012345".getBytes(StandardCharsets.UTF_8),
            ivBytes, bytes);
        bytes = SM4Util.decryptCbcPadding("a123456789012345".getBytes(StandardCharsets.UTF_8),
            ivBytes, cipherText);
        System.out.println(new String(bytes));
    }
}
