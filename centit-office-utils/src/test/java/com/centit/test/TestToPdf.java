package com.centit.test;

import com.centit.support.office.OfficeToPdf;

public class TestToPdf {

    public static void main(String[] args) {

        OfficeToPdf.excel2Pdf("D:\\projects\\RunData\\testToPdf.xlsx",
            "D:\\projects\\RunData\\testToPdf.pdf"
        );
        System.out.println("Done!");
    }
}
