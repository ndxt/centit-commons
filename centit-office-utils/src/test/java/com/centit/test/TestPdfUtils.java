package com.centit.test;

import com.centit.support.office.DocOptUtil;

public class TestPdfUtils {
    public static void main(String[] args) {
        boolean b = DocOptUtil.pdfContainsJSAction("/Users/codefan/Downloads/test1.pdf");
        System.out.println(b);
        b = DocOptUtil.pdfContainsJSAction("/Users/codefan/Downloads/202504105_1427.pdf");
        System.out.println(b);
    }
}
