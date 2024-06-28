package com.centit.test;

import com.centit.support.office.OfficeToPdf;

public class TestToPdf {

    public static void main(String[] args) {

        OfficeToPdf.excel2Pdf("D:\\projects\\RunData\\month6.xlsx",
            "D:\\projects\\RunData\\month6.pdf"
        );
        System.out.println("Done!");
    }
}
