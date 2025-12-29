package com.centit.test;

import com.centit.support.image.ImageOpt;
import com.centit.support.office.DocOptUtil;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

public class TestPdf2Images {

    public static void main(String[] args) {
        try {
            // 测试 pdf2Images 功能
            FileInputStream pdfInput = new FileInputStream("/Users/codefan/projects/centit/centit-commons/centit-office-utils/src/test/resources/template/JS25040037.pdf");
            // 设置为每毫米3像素（约72 DPI）
            List<BufferedImage> images = DocOptUtil.fetchPdfImages(pdfInput);
            BufferedImage image = ImageOpt.mergeImages(images, 1, 0);

            System.out.println("PDF转换完");
            ImageOpt.saveBufferedImage("/Users/codefan/projects/centit/centit-commons/centit-office-utils/src/test/resources/template/content_all.png", image, 100);
        } catch (FileNotFoundException e) {
            System.err.println("测试文件未找到，跳过测试: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
        }
    }
}

