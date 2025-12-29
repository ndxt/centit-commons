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
            FileInputStream pdfInput = new FileInputStream("/Users/codefan/projects/centit/centit-commons/centit-office-utils/src/test/resources/template/content.pdf");
            // 设置为每毫米3像素（约72 DPI）
            double ppm = 10.0;
            List<BufferedImage> images = DocOptUtil.pdf2Images(pdfInput, ppm);

            System.out.println("PDF转换完成，共生成 " + images.size() + " 张图片");

            for (int i = 0; i < images.size(); i++) {
                BufferedImage img = images.get(i);
                ImageOpt.saveBufferedImage("/Users/codefan/projects/centit/centit-commons/centit-office-utils/src/test/resources/template/content_" + (i + 1) + ".png", img, 100);
                System.out.println("第 " + (i + 1) + " 页图片尺寸: " + img.getWidth() + "x" + img.getHeight());
            }

        } catch (FileNotFoundException e) {
            System.err.println("测试文件未找到，跳过测试: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
        }
    }
}
