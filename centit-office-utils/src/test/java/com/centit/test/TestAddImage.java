package com.centit.test;

import com.centit.support.office.Watermark4Pdf;
import com.itextpdf.text.Image;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TestAddImage {
    private static final Logger logger = LogManager.getLogger(TestAddImage.class);

    public static final char[] PASSWORD = "123456".toCharArray();// keystory密码
    public static void main(String[] args) {
        try {
            String base="/Users/codefan/projects/centit/centit-commons/centit-office-utils/src/test/resources/template/";
            // 将证书文件放入指定路径，并读取keystore ，获得私钥和证书链
            Watermark4Pdf.addImage2Pdf(base+"286.pdf",
                base+"output2.pdf",
                -1,
                base+"group2.png",
                1f,
                0.5f, 0.9f, 0.3f, 0.1f);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
