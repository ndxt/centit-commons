package com.centit.support.test.utils;

import com.centit.support.algorithm.ZipCompressor;

public class TestUnzip {
    public static void main(String[] args) {
        ZipCompressor.release("/Users/codefan/Documents/temp/OA.zip",
            "/Users/codefan/Documents/temp/oafile");
        System.out.println("done！");
    }
}
