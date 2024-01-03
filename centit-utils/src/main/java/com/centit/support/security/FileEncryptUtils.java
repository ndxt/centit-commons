package com.centit.support.security;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

@SuppressWarnings("unused")
public abstract class FileEncryptUtils {
    private FileEncryptUtils() {
        throw new IllegalAccessError("Utility class");
    }

    /**
     * 文件file进行加密并保存目标文件destFile中
     *
     * @param sourceFileName     要加密的文件 如c:/test/srcFile.txt
     * @param diminationFileName 加密后存放的文件名 如c:/加密后文件.txt
     * @param algorithm          加密算法
     * @param keyValue           密码
     * @throws IOException              异常
     * @throws GeneralSecurityException 异常
     */
    public static void encrypt(String sourceFileName, String diminationFileName, String algorithm, String keyValue)
        throws IOException, GeneralSecurityException {
        encrypt(new File(sourceFileName), new File(diminationFileName), algorithm, keyValue);
    }

    /**
     * 文件file进行加密并保存目标文件destFile中
     *
     * @param sourceFile     要加密的文件 如c:/test/srcFile.txt
     * @param diminationFile 加密后存放的文件名 如c:/加密后文件.txt
     * @param algorithm          加密算法
     * @param keyValue       密码
     * @throws GeneralSecurityException 异常
     * @throws IOException              异常
     */
    public static void encrypt(InputStream sourceFile, OutputStream diminationFile, String algorithm, String keyValue)
        throws IOException, GeneralSecurityException {
        Cipher  cipher ;
        switch (algorithm){
            case "SM4":
                cipher = SM4Util.generateEcbCipher(SM4Util.ALGORITHM_NAME_ECB_PADDING, Cipher.ENCRYPT_MODE,
                    StringUtils.isBlank(keyValue)? SM4Util.SM4_SECRET_KEY_SPEC.getBytes(StandardCharsets.UTF_8)
                        :keyValue.getBytes(StandardCharsets.UTF_8));
                break;
            case "AES_CBC": {
                Pair<String, String> key =  SecurityOptUtils.makeCbcKey(keyValue, "AES");
                cipher = AESSecurityUtils.createCbcEncryptCipher(key.getLeft().getBytes(StandardCharsets.UTF_8),
                    key.getRight().getBytes(StandardCharsets.UTF_8));
            }
            break;
            case "SM4_CBC": {
                Pair<String, String> key =  SecurityOptUtils.makeCbcKey(keyValue, "SM4");
                cipher = SM4Util.generateCbcCipher(SM4Util.ALGORITHM_NAME_CBC_PADDING, Cipher.ENCRYPT_MODE,
                    key.getLeft().getBytes(StandardCharsets.UTF_8), key.getRight().getBytes(StandardCharsets.UTF_8));
            }
            break;
            case "AES":
            default:
                cipher = AESSecurityUtils.createEncryptCipher(
                    StringUtils.isBlank(keyValue)? AESSecurityUtils.AES_SECRET_KEY_SPEC : keyValue);
                break;
        }

        try (CipherInputStream cis = new CipherInputStream(sourceFile,cipher)) {
            byte[] buffer = new byte[1024];
            int r;
            while ((r = cis.read(buffer)) > 0) {
                diminationFile.write(buffer, 0, r);
            }
        }
    }

    public static void encrypt(File sourceFile, File diminationFile, String algorithm, String keyValue)
        throws IOException, GeneralSecurityException {
        try (InputStream is = new FileInputStream(sourceFile);
             OutputStream out = new FileOutputStream(diminationFile)) {
            encrypt(is, out, algorithm, keyValue);
        }
    }

    /**
     * 文件采用AS算法解密文件
     *
     * @param sourceFileName     已加密的文件 如c:/加密后文件.txt * @param destFile 解密后存放的文件名 如c:/
     *                           test/解密后文件.txt
     * @param diminationFileName 加密后存放的文件名 如c:/加密后文件.txt
     * @param algorithm          加密算法
     * @param keyValue           密码
     * @throws GeneralSecurityException 异常
     * @throws IOException              异常
     */
    public static void decrypt(String sourceFileName, String diminationFileName, String algorithm, String keyValue)
        throws IOException, GeneralSecurityException {
        decrypt(new File(sourceFileName), new File(diminationFileName), algorithm, keyValue);
    }

    /**
     * 文件采用AS算法解密文件
     *
     * @param sourceFile     已加密的文件 如c:/加密后文件.txt * @param destFile 解密后存放的文件名 如c:/
     *                       test/解密后文件.txt
     * @param diminationFile 加密后存放的文件名 如c:/加密后文件.txt
     * @param algorithm          加密算法
     * @param keyValue       密码
     * @throws GeneralSecurityException 异常
     * @throws IOException              异常
     */
    public static void decrypt(InputStream sourceFile, OutputStream diminationFile, String algorithm, String keyValue)
        throws IOException, GeneralSecurityException {
        Cipher  cipher ;

        switch (algorithm){
            case "SM4":
                cipher = SM4Util.generateEcbCipher(SM4Util.ALGORITHM_NAME_ECB_PADDING, Cipher.DECRYPT_MODE,
                    StringUtils.isBlank(keyValue)? SM4Util.SM4_SECRET_KEY_SPEC.getBytes(StandardCharsets.UTF_8)
                        :keyValue.getBytes(StandardCharsets.UTF_8));
                break;
            case "AES_CBC": {
                Pair<String, String> key =  SecurityOptUtils.makeCbcKey(keyValue, "AES");
                cipher = AESSecurityUtils.createCbcDencryptCipher(key.getLeft().getBytes(StandardCharsets.UTF_8),
                    key.getRight().getBytes(StandardCharsets.UTF_8));
            }
            break;
            case "SM4_CBC": {
                Pair<String, String> key =  SecurityOptUtils.makeCbcKey(keyValue, "SM4");
                cipher = SM4Util.generateCbcCipher(SM4Util.ALGORITHM_NAME_CBC_PADDING, Cipher.DECRYPT_MODE,
                    key.getLeft().getBytes(StandardCharsets.UTF_8), key.getRight().getBytes(StandardCharsets.UTF_8));
            }
            break;
            case "AES":
            default:
                cipher = AESSecurityUtils.createDencryptCipher(
                    StringUtils.isBlank(keyValue)? AESSecurityUtils.AES_SECRET_KEY_SPEC : keyValue);
                break;
        }

        try (
            CipherOutputStream cos = new CipherOutputStream(diminationFile,cipher)) {
            byte[] buffer = new byte[1024];
            int r;
            while ((r = sourceFile.read(buffer)) >= 0) {
                cos.write(buffer, 0, r);
            }
        }
    }

    /**
     * 文件采用AES算法解密文件
     *
     * @param sourceFile     已加密的文件 如c:/加密后文件.txt * @param destFile 解密后存放的文件名 如c:/
     *                       test/解密后文件.txt
     * @param diminationFile 加密后存放的文件名 如c:/加密后文件.txt
     * @param algorithm          加密算法
     * @param keyValue       密码
     * @throws GeneralSecurityException 异常
     * @throws IOException              异常
     */
    public static void decrypt(File sourceFile, File diminationFile, String algorithm, String keyValue)
        throws IOException, GeneralSecurityException {
        try (InputStream is = new FileInputStream(sourceFile);
             OutputStream out = new FileOutputStream(diminationFile)) {
            decrypt(is, out, algorithm, keyValue);
        }
    }
}
