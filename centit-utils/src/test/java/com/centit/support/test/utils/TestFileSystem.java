package com.centit.support.test.utils;

import com.alibaba.fastjson2.JSON;
import com.centit.support.algorithm.CollectionsOpt;
import com.centit.support.file.CsvFileIO;
import com.centit.support.file.FileSystemOpt;
import com.centit.support.file.FileType;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class TestFileSystem {
    public static void main(String[] args) throws IOException {
        CsvFileIO.saveData2OutputStream(
            CollectionsOpt.createList(
                CollectionsOpt.createHashMap("c", 17, "b", "hello", "d", 2.5f),
                CollectionsOpt.createHashMap("d", 12, "b", "world", "g", 100)),
            new FileOutputStream("/Users/codefan/temp/test2.csv"),
            true, null, "UTF-8");

        List<Map<String, Object>> listData = CsvFileIO.readDataFromInputStream(
            new FileInputStream("/Users/codefan/temp/test2.csv"),
            true, null, "UTF-8");

        System.out.println(JSON.toJSONString(listData));
    }

    public static void testGetFileType() {
        System.out.println(FileSystemOpt.transformBlackSlant("C:\\\\feile\\南大先腾.pdf"));
        System.out.println(FileType.truncateFileExtName("C:\\feile南大先腾.pdf"));
        System.out.println(FileType.truncateFileExtName("C:\\feile/南大先腾.pdf"));
        System.out.println(FileType.truncateFileExtName("C:/feile\\南大先腾.pdf"));
        System.out.println(FileType.getFileMimeType("C:\\feile南大先腾.jpg"));
        System.out.println(FileType.getFileMimeType("C:\\feile南大先腾.mp4"));
        System.out.println(FileType.getFileMimeType("C:\\feile南大先腾.mp3"));
        System.out.println(FileType.getFileMimeType("C:\\feile南大先腾.png"));
        System.out.println(FileType.getFileMimeType("C:\\feile南大先腾.mid"));
    }

}
