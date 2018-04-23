package com.centit.test;

import com.centit.support.file.FileIOOpt;

import java.io.IOException;

public class TestReadStringFromFile {
    public static void main(String[] args) {
        try {
            System.out.println(FileIOOpt.readStringFromFile
                    ("D:/temp/static_system_config.json","UTF-8"));
            System.out.println(FileIOOpt.readStringFromFile
                    ("D:/temp/static_system_config.json","GBK"));
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }
}
