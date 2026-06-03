package com.centit.test;

import com.centit.support.algorithm.CollectionsOpt;
import com.centit.support.office.DocOptUtil;

public class TestPdfMerge {
    public static void main(String[] args) {
        ///Users/codefan/Documents/temp/pdf2.pdf
        DocOptUtil.mergePdfFiles("/Users/codefan/Documents/temp/pdfMerge.pdf",
            CollectionsOpt.createList("/Users/codefan/Documents/temp/pdf1.pdf",
                "/Users/codefan/Documents/temp/pdf2.pdf"));
        System.out.println("Done!");
    }
}
