package com.centit.test;

import com.centit.support.file.FileIOOpt;
import com.centit.support.file.FileType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class TestFileSystem {

    public static void main(String[] args) {

        try (FileInputStream file = new FileInputStream(new File(
                "/home/codefan/java_error_in_IDEA_14478.log"))) {
            byte[] buf = FileIOOpt.readBytesFromInputStream(file, 1024*1024*64);
            System.out.println(buf.length);
            System.out.println(new String(buf));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void testGetFileType() {
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
