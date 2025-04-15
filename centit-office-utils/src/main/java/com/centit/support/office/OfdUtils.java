package com.centit.support.office;

import org.ofdrw.converter.ImageMaker;
import org.ofdrw.reader.OFDReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public abstract class OfdUtils {

    private static final Logger logger = LoggerFactory.getLogger(OfdUtils.class);

    public static List<BufferedImage> ofd2Images(InputStream inOfdFile, double ppm) {
        try {
            ImageMaker maker = new ImageMaker(new OFDReader(inOfdFile), ppm);
            int ps = maker.pageSize();
            List<BufferedImage> images = new ArrayList<>(ps+1);
            for(int i=0; i<ps; i++) {
                BufferedImage image = maker.makePage(i);
                if(image!=null)
                    images.add(image);
            }
            return images;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    public static List<BufferedImage> ofd2Images(String ofdPath, double ppm) {
        try {
            return ofd2Images(Files.newInputStream(Paths.get(ofdPath)), ppm);
        } catch (IOException e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    public static List<BufferedImage> ofd2Images(String ofdPath) {
        return ofd2Images(ofdPath, 23.62);
    }

    public static List<BufferedImage> ofd2Images(InputStream inputStream) {
        return ofd2Images(inputStream, 23.62);
    }

    public static boolean ofd2Pdf(String ofdPath, String pdfPath) {
        try {
            return ofd2Pdf(Files.newInputStream(Paths.get(ofdPath)), Files.newOutputStream(Paths.get(pdfPath)));
        } catch (Exception e) {
            logger.error(e.getMessage());
            return false;
        }
    }

    public static boolean ofd2Pdf(InputStream inOfdFile, OutputStream outPdfFile) {
        List<BufferedImage> images = ofd2Images(inOfdFile);
        return ImagesToPdf.imagesToPdf(images, outPdfFile);
    }
}
