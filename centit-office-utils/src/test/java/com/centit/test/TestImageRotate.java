package com.centit.test;

import com.centit.support.image.ImageOpt;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class TestImageRotate {

    public static void main(String[] args) {
        try {
            // 创建一个测试图片 - 带有文字标识的矩形图片便于观察旋转效果
            BufferedImage testImage = createTestImage();

            // 保存原始图片
            ImageIO.write(testImage, "png", new File("/Users/codefan/projects/centit/centit-commons/centit-office-utils/src/test/resources/template/test_original.png"));
            System.out.println("原始图片已保存");

            // 测试90度旋转
            BufferedImage rotated90 = ImageOpt.rotateImage(testImage, 90);
            ImageIO.write(rotated90, "png", new File("/Users/codefan/projects/centit/centit-commons/centit-office-utils/src/test/resources/template/test_rotated_90.png"));
            System.out.println("90度旋转图片已保存，尺寸: " + rotated90.getWidth() + "x" + rotated90.getHeight());

            // 测试180度旋转
            BufferedImage rotated180 = ImageOpt.rotateImage(testImage, 180);
            ImageIO.write(rotated180, "png", new File("/Users/codefan/projects/centit/centit-commons/centit-office-utils/src/test/resources/template/test_rotated_180.png"));
            System.out.println("180度旋转图片已保存，尺寸: " + rotated180.getWidth() + "x" + rotated180.getHeight());

            // 测试270度旋转
            BufferedImage rotated270 = ImageOpt.rotateImage(testImage, 270);
            ImageIO.write(rotated270, "png", new File("/Users/codefan/projects/centit/centit-commons/centit-office-utils/src/test/resources/template/test_rotated_270.png"));
            System.out.println("270度旋转图片已保存，尺寸: " + rotated270.getWidth() + "x" + rotated270.getHeight());

            // 测试便利方法
            BufferedImage rotateLeft = ImageOpt.rotateImage(testImage, 90);
            BufferedImage rotateRight = ImageOpt.rotateImage(testImage, 270);
            BufferedImage rotate180Method = ImageOpt.rotateImage(testImage, 180);

            System.out.println("便利方法测试完成:");
            System.out.println("- 左旋90度尺寸: " + rotateLeft.getWidth() + "x" + rotateLeft.getHeight());
            System.out.println("- 右旋90度尺寸: " + rotateRight.getWidth() + "x" + rotateRight.getHeight());
            System.out.println("- 180度旋转尺寸: " + rotate180Method.getWidth() + "x" + rotate180Method.getHeight());

            // 测试异常情况
            try {
                ImageOpt.rotateImage(testImage, 45);
            } catch (IllegalArgumentException e) {
                System.out.println("正确捕获异常: " + e.getMessage());
            }

        } catch (IOException e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("意外错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 创建一个测试图片，带有方向标识便于观察旋转效果
     */
    private static BufferedImage createTestImage() {
        int width = 300;
        int height = 200;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // 设置背景色
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);

        // 绘制边框
        g2d.setColor(Color.BLACK);
        g2d.drawRect(0, 0, width-1, height-1);

        // 绘制文字标识
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        g2d.setColor(Color.BLUE);
        g2d.drawString("TOP", width/2 - 20, 30);

        g2d.setColor(Color.RED);
        g2d.drawString("BOTTOM", width/2 - 40, height - 20);

        g2d.setColor(Color.GREEN);
        g2d.drawString("LEFT", 10, height/2);

        g2d.setColor(Color.ORANGE);
        g2d.drawString("RIGHT", width - 60, height/2);

        // 绘制中心点
        g2d.setColor(Color.BLACK);
        g2d.fillOval(width/2 - 5, height/2 - 5, 10, 10);

        g2d.dispose();
        return image;
    }
}
