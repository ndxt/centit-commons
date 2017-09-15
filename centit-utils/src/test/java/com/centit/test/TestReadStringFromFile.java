package com.centit.test;

import java.io.IOException;

import com.centit.support.file.FileIOOpt;

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
