package com.centit.test;

import com.centit.support.algorithm.CollectionsOpt;
import com.centit.support.office.DocOptUtil;

import java.awt.*;
import java.io.IOException;

public class PDFHighlighterItext {
    // /Users/codefan/Documents/temp/DeveloperWorkbook.pdf
    public static void main(String[] args) throws IOException {
        DocOptUtil.pdfHighlightKeywords("/Users/codefan/Documents/temp/developerTest.pdf",
                "/Users/codefan/Documents/temp/HighLigher.pdf",
                CollectionsOpt.createList("道路","管养","市长"), Color.YELLOW);
        System.out.println("Done!");
    }
}
