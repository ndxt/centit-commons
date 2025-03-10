package com.centit.search.test;

import com.centit.search.utils.TikaTextExtractor;

public class TestOFD {
    public static void main(String[] args) {
        try {
            String filePath = "/Users/codefan/projects/RunData/temp/testOFD/f6.1.004.ofd";
            String text = TikaTextExtractor.extractFileText(filePath);//extractFileText(filePath); //
            System.out.println(text);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Done!");
    }
}
