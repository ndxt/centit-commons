package com.centit.test;

import com.centit.support.file.FileType;

public class TestFileSystem {

    public static void main(String[] args) {
        System.out.println(FileType.truncateFileExtName("C:\\feile那大先腾.pdf"));

        System.out.println(FileType.truncateFileExtName("C:\\feile/那大先腾.pdf"));

        System.out.println(FileType.truncateFileExtName("C:/feile\\那大先腾.pdf"));


        System.out.println(FileType.getFileMimeType("C:\\feile那大先腾.jpg"));
        System.out.println(FileType.getFileMimeType("C:\\feile那大先腾.mp4"));
        System.out.println(FileType.getFileMimeType("C:\\feile那大先腾.mp3"));
        System.out.println(FileType.getFileMimeType("C:\\feile那大先腾.png"));
        System.out.println(FileType.getFileMimeType("C:\\feile那大先腾.mid"));
    }

}
