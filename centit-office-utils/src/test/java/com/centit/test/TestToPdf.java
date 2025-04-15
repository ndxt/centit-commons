package com.centit.test;

import com.centit.support.office.OfdUtils;
import com.centit.support.office.OfficeToPdf;

import java.io.IOException;

public class TestToPdf {

    public static void main(String[] args) throws IOException {

        /*String waterMark = "杨淮生 codefan 2024-9-12";
        System.out.println(waterMark.length());*/
        OfdUtils.ofd2Pdf("/Users/codefan/projects/RunData/temp/testOFD/f6.1.004.ofd",
            "/Users/codefan/projects/RunData/temp/f6.1.004.pdf");
        /*Watermark4Pdf.addWatermark4Pdf("d:\\Users\\2024.pdf",
            "d:\\Users\\2.pdf",
            "杨淮生 codefan 2024-9-12",
            0.4f,-45,24, true);*/
        System.out.println("Done!");
    }
}
