package com.centit.support.security;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.engines.SM4Engine;
import org.bouncycastle.crypto.macs.CBCBlockCipherMac;
import org.bouncycastle.crypto.macs.GMac;
import org.bouncycastle.crypto.modes.GCMBlockCipher;
import org.bouncycastle.crypto.paddings.BlockCipherPadding;
import org.bouncycastle.crypto.paddings.PKCS7Padding;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;

public abstract class SM4Util extends GMBaseUtil {

    protected static final Logger logger = LoggerFactory.getLogger(SM4Util.class);
    public static final String ALGORITHM_NAME = "SM4";
    public static final String ALGORITHM_NAME_ECB_PADDING = "SM4/ECB/PKCS5Padding";
    public static final String ALGORITHM_NAME_ECB_NOPADDING = "SM4/ECB/NoPadding";
    public static final String ALGORITHM_NAME_CBC_PADDING = "SM4/CBC/PKCS5Padding";
    public static final String ALGORITHM_NAME_CBC_NOPADDING = "SM4/CBC/NoPadding";

    public static final String SM4_SECRET_KEY_SPEC="qbh07dTZ$sO7_wC1";
    public static final String SM4_IV_PARAMETER_SPEC = "slEAYAe2@dh3otTO";
    public static final int DEFAULT_KEY_SIZE = 128;

    public static byte[] generateKey() throws NoSuchAlgorithmException, NoSuchProviderException {
        return generateKey(DEFAULT_KEY_SIZE);
    }

    public static byte[] generateKey(int keySize) throws NoSuchAlgorithmException, NoSuchProviderException {
        KeyGenerator kg = KeyGenerator.getInstance(ALGORITHM_NAME, BouncyCastleProvider.PROVIDER_NAME);
        kg.init(keySize, new SecureRandom());
        return kg.generateKey().getEncoded();
    }

    public static byte[] encryptEcbPadding(byte[] key, byte[] data)
        throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException,
        NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = generateEcbCipher(ALGORITHM_NAME_ECB_PADDING, Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data);
    }

    public static byte[] decryptEcbPadding(byte[] key, byte[] cipherText)
        throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException,
        NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException {
        Cipher cipher = generateEcbCipher(ALGORITHM_NAME_ECB_PADDING, Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(cipherText);
    }

    public static byte[] encryptEcbNoPadding(byte[] key, byte[] data)
        throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException,
        NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = generateEcbCipher(ALGORITHM_NAME_ECB_NOPADDING, Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data);
    }

    public static byte[] decryptEcbNoPadding(byte[] key, byte[] cipherText)
        throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException,
        NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException {
        Cipher cipher = generateEcbCipher(ALGORITHM_NAME_ECB_NOPADDING, Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(cipherText);
    }

    public static byte[] encryptCbcPadding(byte[] key, byte[] iv, byte[] data)
        throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException,
        NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException,
        InvalidAlgorithmParameterException {
        Cipher cipher = generateCbcCipher(ALGORITHM_NAME_CBC_PADDING, Cipher.ENCRYPT_MODE, key, iv);
        return cipher.doFinal(data);
    }

    public static byte[] decryptCbcPadding(byte[] key, byte[] iv, byte[] cipherText)
        throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException,
        NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException,
        InvalidAlgorithmParameterException {
        Cipher cipher = generateCbcCipher(ALGORITHM_NAME_CBC_PADDING, Cipher.DECRYPT_MODE, key, iv);
        return cipher.doFinal(cipherText);
    }

    public static byte[] encryptCbcNoPadding(byte[] key, byte[] iv, byte[] data)
        throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException,
        NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException,
        InvalidAlgorithmParameterException {
        Cipher cipher = generateCbcCipher(ALGORITHM_NAME_CBC_NOPADDING, Cipher.ENCRYPT_MODE, key, iv);
        return cipher.doFinal(data);
    }

    public static byte[] decryptCbcNoPadding(byte[] key, byte[] iv, byte[] cipherText)
        throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException,
        NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException,
        InvalidAlgorithmParameterException {
        Cipher cipher = generateCbcCipher(ALGORITHM_NAME_CBC_NOPADDING, Cipher.DECRYPT_MODE, key, iv);
        return cipher.doFinal(cipherText);
    }

    public static byte[] doCMac(byte[] key, byte[] data) throws NoSuchProviderException, NoSuchAlgorithmException,
        InvalidKeyException {
        Key keyObj = new SecretKeySpec(key, ALGORITHM_NAME);
        return doMac("SM4-CMAC", keyObj, data);
    }

    public static byte[] doGMac(byte[] key, byte[] iv, int tagLength, byte[] data) {
        org.bouncycastle.crypto.Mac mac = new GMac(new GCMBlockCipher(new SM4Engine()), tagLength * 8);
        return doMac(mac, key, iv, data);
    }

    /**
     * 默认使用PKCS7Padding/PKCS5Padding填充的CBCMAC
     *
     * @param key 密码
     * @param iv iv
     * @param data 待加密数据
     * @return byte[]
     */
    public static byte[] doCBCMac(byte[] key, byte[] iv, byte[] data) {
        SM4Engine engine = new SM4Engine();
        org.bouncycastle.crypto.Mac mac = new CBCBlockCipherMac(engine, engine.getBlockSize() * 8, new PKCS7Padding());
        return doMac(mac, key, iv, data);
    }

    /**
     *
     * @param key 密码
     * @param iv iv
     * @param padding 可以传null，传null表示NoPadding，由调用方保证数据必须是BlockSize的整数倍
     * @param data 待加密数据
     * @return byte[]
     * @throws Exception 异常
     */
    public static byte[] doCBCMac(byte[] key, byte[] iv, BlockCipherPadding padding, byte[] data) throws Exception {
        SM4Engine engine = new SM4Engine();
        if (padding == null) {
            if (data.length % engine.getBlockSize() != 0) {
                throw new Exception("if no padding, data length must be multiple of SM4 BlockSize");
            }
        }
        org.bouncycastle.crypto.Mac mac = new CBCBlockCipherMac(engine, engine.getBlockSize() * 8, padding);
        return doMac(mac, key, iv, data);
    }


    private static byte[] doMac(org.bouncycastle.crypto.Mac mac, byte[] key, byte[] iv, byte[] data) {
        CipherParameters cipherParameters = new KeyParameter(key);
        mac.init(new ParametersWithIV(cipherParameters, iv));
        mac.update(data, 0, data.length);
        byte[] result = new byte[mac.getMacSize()];
        mac.doFinal(result, 0);
        return result;
    }

    private static byte[] doMac(String algorithmName, Key key, byte[] data) throws NoSuchProviderException,
        NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance(algorithmName, BouncyCastleProvider.PROVIDER_NAME);
        mac.init(key);
        mac.update(data);
        return mac.doFinal();
    }

    public static Cipher generateEcbCipher(String algorithmName, int mode, byte[] key)
        throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException,
        InvalidKeyException {
        Cipher cipher = Cipher.getInstance(algorithmName, BouncyCastleProvider.PROVIDER_NAME);
        Key sm4Key = new SecretKeySpec(key, ALGORITHM_NAME);
        cipher.init(mode, sm4Key);
        return cipher;
    }

    public static Cipher generateCbcCipher(String algorithmName, int mode, byte[] key, byte[] iv)
        throws InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException,
        NoSuchProviderException, NoSuchPaddingException {
        Cipher cipher = Cipher.getInstance(algorithmName, BouncyCastleProvider.PROVIDER_NAME);
        Key sm4Key = new SecretKeySpec(key, ALGORITHM_NAME);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        cipher.init(mode, sm4Key, ivParameterSpec);
        return cipher;
    }

    public static String encryptAsCBCTypeAsBase64(String str, byte[] keyValue, byte[] ivParameter) {
        try {
            SecretKeySpec sm4Key = new SecretKeySpec(keyValue, ALGORITHM_NAME);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(ivParameter);
            Cipher cipher = Cipher.getInstance(ALGORITHM_NAME_CBC_PADDING, BouncyCastleProvider.PROVIDER_NAME);
            cipher.init(Cipher.ENCRYPT_MODE, sm4Key, ivParameterSpec);
            return new String(Base64.encodeBase64(cipher.doFinal(str.getBytes())));
        } catch (GeneralSecurityException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    public static String encryptAsCBCTypeAsBase64(String str, String keyValue, String ivParameter) {
        return encryptAsCBCTypeAsBase64(str, keyValue.getBytes(StandardCharsets.UTF_8),
            ivParameter.getBytes(StandardCharsets.UTF_8));
    }

    public static String decryptBase64AsCBCType(String str, byte[]  keyValue, byte[]  ivParameter) {
        try {
            SecretKeySpec sm4Key = new SecretKeySpec(keyValue, ALGORITHM_NAME);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(ivParameter);
            Cipher cipher = Cipher.getInstance(ALGORITHM_NAME_CBC_PADDING, BouncyCastleProvider.PROVIDER_NAME);
            cipher.init(Cipher.DECRYPT_MODE, sm4Key, ivParameterSpec);
            return new String(cipher.doFinal(Base64.decodeBase64(str)));
        } catch (GeneralSecurityException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    public static String decryptBase64AsCBCType(String str, String keyValue, String ivParameter) {
        return decryptBase64AsCBCType(str, keyValue.getBytes(StandardCharsets.UTF_8),
            ivParameter.getBytes(StandardCharsets.UTF_8));
    }

    public static byte[] encryptAsCBCType(byte[] bytes, byte[] keyValue, byte[] ivParameter) {
        try {
            SecretKeySpec sm4Key = new SecretKeySpec(keyValue, ALGORITHM_NAME);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(ivParameter);
            Cipher cipher = Cipher.getInstance(ALGORITHM_NAME_CBC_PADDING, BouncyCastleProvider.PROVIDER_NAME);
            cipher.init(Cipher.ENCRYPT_MODE, sm4Key, ivParameterSpec);
            return cipher.doFinal(bytes);
        } catch (GeneralSecurityException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    public static byte[] encryptAsCBCType(byte[] bytes, String keyValue, String ivParameter) {
        return encryptAsCBCType(bytes, keyValue.getBytes(StandardCharsets.UTF_8),
            ivParameter.getBytes(StandardCharsets.UTF_8));
    }

    public static byte[] decryptAsCBCType(byte[] bytes, byte[] keyValue, byte[] ivParameter) {
        try {
            SecretKeySpec sm4Key = new SecretKeySpec(keyValue, ALGORITHM_NAME);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(ivParameter);
            Cipher cipher = Cipher.getInstance(ALGORITHM_NAME_CBC_PADDING, BouncyCastleProvider.PROVIDER_NAME);
            cipher.init(Cipher.DECRYPT_MODE, sm4Key, ivParameterSpec);
            return cipher.doFinal(bytes);
        } catch (GeneralSecurityException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    public static byte[] decryptAsCBCType(byte[] bytes, String keyValue, String ivParameter) {
        return decryptAsCBCType(bytes, keyValue.getBytes(StandardCharsets.UTF_8),
            ivParameter.getBytes(StandardCharsets.UTF_8));
    }
}
