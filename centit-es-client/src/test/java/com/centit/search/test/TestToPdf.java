package com.centit.search.test;

import com.centit.search.utils.ImagePdfTextExtractor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class TestToPdf {
    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            System.out.println(ImagePdfTextExtractor.imagePdfToText(
                new FileInputStream("/Users/codefan/Documents/temp/testPdfImage.pdf"), ImagePdfTextExtractor.fetchDefaultOrrServer() ));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
