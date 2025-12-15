package com.centit.test;

import com.centit.support.algorithm.CollectionsOpt;
import com.centit.support.office.DocOptUtil;

import java.awt.*;
import java.io.IOException;

public class PDFHighlighterItext {
    // /Users/codefan/Documents/temp/DeveloperWorkbook.pdf
    public static void main(String[] args) throws IOException {
        DocOptUtil.pdfHighlightKeywords("/Users/codefan/Documents/temp/DeveloperWorkbook.pdf",
                "/Users/codefan/Documents/temp/HighLigher.pdf",
                CollectionsOpt.createList("运行","控制","指令","恶意","代码"), Color.YELLOW);
        System.out.println("Done!");
    }
}
