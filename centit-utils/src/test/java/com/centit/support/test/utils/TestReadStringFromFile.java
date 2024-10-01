package com.centit.support.test.utils;

import com.centit.support.file.FileIOOpt;

import java.io.IOException;

public class TestReadStringFromFile {
    public static void main(String[] args) {
        try {
            System.out.println(FileIOOpt.readStringFromFile
                ("D:/temp/static_system_config.json", "UTF-8"));
            System.out.println(FileIOOpt.readStringFromFile
                ("D:/temp/static_system_config.json", "GBK"));
        } catch (IOException e) {
            //logger.error(e.getMessage(), e);
        }
    }
}
