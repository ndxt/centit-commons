package com.centit.test;

import com.centit.support.office.Watermark4Pdf;

public class TestAddImage {
    public static final char[] PASSWORD = "123456".toCharArray();// keystory密码

    public static void main(String[] args) {
        try {

            String base="/Users/codefan/projects/centit/centit-commons/centit-office-utils/src/test/resources/template/";
            // 将证书文件放入指定路径，并读取keystore ，获得私钥和证书链
            Watermark4Pdf.addImage2Pdf(base+"fff.pdf",
                base+"output2.pdf",
                 base+"group1.png",
                0.4f,
                100, 200);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
