package com.centit.support.office;

import org.ofdrw.converter.ConvertHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public abstract class OfdUtils {

    private static final Logger logger = LoggerFactory.getLogger(OfdUtils.class);
    public static boolean ofd2Pdf(String ofdPath, String pdfPath) {
        try {
            ConvertHelper.toPdf(Files.newInputStream(Paths.get(ofdPath)), Files.newOutputStream(Paths.get(pdfPath)));
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return false;
        }
    }

    public static boolean ofd2Pdf(InputStream inWordFile, OutputStream outPdfFile) {
        try {
            ConvertHelper.toPdf(inWordFile, outPdfFile);
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return false;
        }
    }
}
