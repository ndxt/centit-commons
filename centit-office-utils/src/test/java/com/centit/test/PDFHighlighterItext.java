package com.centit.test;

import com.centit.support.algorithm.CollectionsOpt;
import com.centit.support.office.DocOptUtil;

import java.io.IOException;

public class PDFHighlighterItext {
    // /Users/codefan/Documents/temp/DeveloperWorkbook.pdf
    public static void main(String[] args) throws IOException {
        DocOptUtil.pdfHighlightKeywords("/Users/codefan/Documents/temp/test.pdf",
                "/Users/codefan/Documents/temp/HighLigher.pdf",
                CollectionsOpt.createList("运行","控制","指令","恶意","代码"));
        System.out.println("Done!");
    }
}
