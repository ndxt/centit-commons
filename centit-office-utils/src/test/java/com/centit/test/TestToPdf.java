package com.centit.test;

import com.centit.support.office.OfficeToPdf;

public class TestToPdf {

    public static void main(String[] args) {

        OfficeToPdf.excel2Pdf("/Users/codefan/Downloads/month6.xlsx",
            "/Users/codefan/Downloads/month6.pdf"
        );
        System.out.println("Done!");
    }
}
