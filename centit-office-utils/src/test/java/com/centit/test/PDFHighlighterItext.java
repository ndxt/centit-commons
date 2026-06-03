package com.centit.test;

import com.centit.support.algorithm.CollectionsOpt;
import com.centit.support.office.PdfUtil;

import java.awt.*;
import java.io.IOException;

public class PDFHighlighterItext {
    // /Users/codefan/Documents/temp/DeveloperWorkbook.pdf
    public static void main(String[] args) throws IOException {
        PdfUtil.pdfHighlightKeywords("/Users/codefan/Documents/temp/fileInfo.pdf",
                "/Users/codefan/Documents/temp/HighLigher.pdf",
                CollectionsOpt.createList("排水","考评"), Color.YELLOW);
        System.out.println("Done!");
    }
}
