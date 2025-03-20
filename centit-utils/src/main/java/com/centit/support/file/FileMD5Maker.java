package com.centit.support.file;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author 朱晓文
 */
@SuppressWarnings("unused")
public abstract class FileMD5Maker {
    protected static final Logger logger = LoggerFactory.getLogger(FileMD5Maker.class);

    private FileMD5Maker() {
        throw new IllegalAccessError("Utility class");
    }

    public static String makeFileMD5(File file) throws IOException {

        try (FileInputStream fis = new FileInputStream(file)) {
            return makeFileMD5(fis);
        }
    }

    public static String makeFileMD5(InputStream is) throws IOException {
        try {
            MessageDigest MD5 = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];
            int length;
            while ((length = is.read(buffer)) != -1) {
                MD5.update(buffer, 0, length);
            }
            return new String(Hex.encodeHex(MD5.digest()));
        } catch (NoSuchAlgorithmException e) {
            logger.error(e.getMessage(), e);//logger.error(e.getMessage(), e);
            return null;
        }
    }
}
