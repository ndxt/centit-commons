package com.centit.support.security;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

@SuppressWarnings("unused")
public abstract class FileEncryptWithSM4 {
    private FileEncryptWithSM4() {
        throw new IllegalAccessError("Utility class");
    }

    /**
     * 文件file进行加密并保存目标文件destFile中
     *
     * @param sourceFileName     要加密的文件 如c:/test/srcFile.txt
     * @param diminationFileName 加密后存放的文件名 如c:/加密后文件.txt
     * @param keyValue           密码
     * @throws IOException              异常
     * @throws GeneralSecurityException 异常
     */
    public static void encrypt(String sourceFileName, String diminationFileName, String keyValue)
        throws IOException, GeneralSecurityException {
        encrypt(new File(sourceFileName), new File(diminationFileName), keyValue);
    }

    /**
     * 文件file进行加密并保存目标文件destFile中
     *
     * @param sourceFile     要加密的文件 如c:/test/srcFile.txt
     * @param diminationFile 加密后存放的文件名 如c:/加密后文件.txt
     * @param keyValue       密码
     * @throws GeneralSecurityException 异常
     * @throws IOException              异常
     */
    public static void encrypt(InputStream sourceFile, OutputStream diminationFile, String keyValue)
        throws IOException, GeneralSecurityException {
        try (CipherInputStream cis = new CipherInputStream(sourceFile,
                SM4Util.generateEcbCipher(SM4Util.ALGORITHM_NAME_ECB_PADDING, Cipher.ENCRYPT_MODE, keyValue.getBytes(StandardCharsets.UTF_8)))) {
            byte[] buffer = new byte[1024];
            int r;
            while ((r = cis.read(buffer)) > 0) {
                diminationFile.write(buffer, 0, r);
            }
            //cis.close();
            //is.close();
            //out.close();
        }
    }

    public static void encrypt(File sourceFile, File diminationFile, String keyValue)
        throws IOException, GeneralSecurityException {
        try (InputStream is = new FileInputStream(sourceFile);
             OutputStream out = new FileOutputStream(diminationFile)) {
            encrypt(is, out, keyValue);
        }
    }

    /**
     * 文件采用AS算法解密文件
     *
     * @param sourceFileName     已加密的文件 如c:/加密后文件.txt * @param destFile 解密后存放的文件名 如c:/
     *                           test/解密后文件.txt
     * @param diminationFileName 加密后存放的文件名 如c:/加密后文件.txt
     * @param keyValue           密码
     * @throws GeneralSecurityException 异常
     * @throws IOException              异常
     */
    public static void decrypt(String sourceFileName, String diminationFileName, String keyValue)
        throws IOException, GeneralSecurityException {
        decrypt(new File(sourceFileName), new File(diminationFileName), keyValue);
    }

    /**
     * 文件采用AS算法解密文件
     *
     * @param sourceFile     已加密的文件 如c:/加密后文件.txt * @param destFile 解密后存放的文件名 如c:/
     *                       test/解密后文件.txt
     * @param diminationFile 加密后存放的文件名 如c:/加密后文件.txt
     * @param keyValue       密码
     * @throws GeneralSecurityException 异常
     * @throws IOException              异常
     */
    public static void decrypt(InputStream sourceFile, OutputStream diminationFile, String keyValue)
        throws IOException, GeneralSecurityException {
        try (
            CipherOutputStream cos = new CipherOutputStream(diminationFile,
                SM4Util.generateEcbCipher(SM4Util.ALGORITHM_NAME_ECB_PADDING, Cipher.DECRYPT_MODE, keyValue.getBytes(StandardCharsets.UTF_8)))) {
            byte[] buffer = new byte[1024];
            int r;
            while ((r = sourceFile.read(buffer)) >= 0) {
                cos.write(buffer, 0, r);
            }
            //cos.close();
            //out.close();
            //is.close();
        }
    }

    /**
     * 文件采用AES算法解密文件
     *
     * @param sourceFile     已加密的文件 如c:/加密后文件.txt * @param destFile 解密后存放的文件名 如c:/
     *                       test/解密后文件.txt
     * @param diminationFile 加密后存放的文件名 如c:/加密后文件.txt
     * @param keyValue       密码
     * @throws GeneralSecurityException 异常
     * @throws IOException              异常
     */
    public static void decrypt(File sourceFile, File diminationFile, String keyValue)
        throws IOException, GeneralSecurityException {
        try (InputStream is = new FileInputStream(sourceFile);
             OutputStream out = new FileOutputStream(diminationFile)) {
            decrypt(is, out, keyValue);
        }
    }
}
