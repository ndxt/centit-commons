package com.centit.test;

import com.centit.support.image.ImageOpt;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class TestImage {
    public static void main(String arg[]) {
        //String[] s=decode("d:\\52570486.pdf");
        //System.out.println("发票类型"+s[1]+"发票号码"+s[3]+"发票金额"+s[4]);
        testCreateIcon();
        /*System.out.println(CaptchaImageUtil.checkcodeMatch("",null));
        System.out.println(CaptchaImageUtil.checkcodeMatch("hello","hello "));
        System.out.println(CaptchaImageUtil.checkcodeMatch("hello","HeL1o"));
        System.out.println(CaptchaImageUtil.checkcodeMatch("he1lo","HeLLo"));
        System.out.println(CaptchaImageUtil.checkcodeMatch("he110","Heilo"));
        System.out.println(CaptchaImageUtil.checkcodeMatch("heIL0","Heil2"));
        System.out.println(CaptchaImageUtil.checkcodeMatch("heI20","Heilo"));
*/    }

    public static void testCreateIcon() {
        try {
            BufferedImage image = ImageOpt.createNameIcon("杨淮生印", 64, new Color(0,0,255), true);
            ImageIO.write(image, "png", new File("/D/Projects/RunData/demo_home/images/杨.png"));

            image = ImageOpt.createNameIcon("杨淮生", 64, new Color(0,0,255), true);
            ImageIO.write(image, "png", new File("/D/Projects/RunData/demo_home/images/杨2.png"));

            image = ImageOpt.createNameIcon("杨淮生印", 32, new Color(0,0,255), true);
            ImageIO.write(image, "png", new File("/D/Projects/RunData/demo_home/images/杨3.png"));

            image = ImageOpt.createNameIcon("杨淮生", 32, new Color(0,0,255), true);
            ImageIO.write(image, "png", new File("/D/Projects/RunData/demo_home/images/杨4.png"));

        } catch (IOException e) {
            //e.printStackTrace();
        }
    }

    public static void testImageOpt() {
        try {
            BufferedImage image = ImageIO.read(new File("D:/Projects/RunData/demo_home/images/word9.png"));
            List<BufferedImage> subimages = ImageOpt.splitImage(image, 6);
            int i = 0;
            for (BufferedImage subimage : subimages)
                ImageIO.write(subimage, "png", new File("D:/Projects/RunData/demo_home/images/word9_" + i++ + ".png"));
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }

    public static void testImageOpt2() {
        try {
            ImageOpt.captureScreen("D:/Projects/RunData/demo_home/images/screen.jpg");
            ImageOpt.createThumbnail("D:/Projects/RunData/demo_home/images/screen.jpg",
                800, 600, 50, "D:/Projects/RunData/demo_home/images/screen2.jpg");
            System.out.println("Done!");
        } catch (Exception e) {
            System.out.println("Error!");
            //e.printStackTrace();
        }

    }
}
