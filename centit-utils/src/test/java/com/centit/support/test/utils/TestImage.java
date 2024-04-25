package com.centit.support.test.utils;

import com.centit.support.image.CaptchaImageUtil;
import com.centit.support.image.ImageOpt;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;
import java.util.Random;

public class TestImage {
    public static void main2(String arg[]) throws IOException{
        Random random = new Random();
        int a = random.nextInt(100);
        int b = random.nextInt(100);
        boolean c = (random.nextInt(100) % 2) == 1;
        String code, value;
        if(c){
            if(a<b){
                int d=a;
                a=b;
                b=d;
            }
            code = String.valueOf(a)+"-" + String.valueOf(b);
            value = String.valueOf(a-b);
        } else {
            code = String.valueOf(a)+"+" + String.valueOf(b);
            value = String.valueOf(a+b);
        }

        BufferedImage image = CaptchaImageUtil.generateCaptchaImage(code);
        ImageIO.write(image, "png", new File("/Users/codefan/Documents/temp/captcha.png"));
        //String[] s=decode("d:\\52570486.pdf");
        //System.out.println("发票类型"+s[1]+"发票号码"+s[3]+"发票金额"+s[4]);
        //testCreateIcon();
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

    public static void main(String arg[]) {
        String base="/Users/codefan/projects/centit/centit-commons/centit-office-utils/src/test/resources/template/";
        try (InputStream image =  new FileInputStream(base+"group1.png");
             OutputStream image2 =  new FileOutputStream(base+"group2.png")) {
            //ImageOpt.captureScreen("/Users/codefan/appdata/RunData/screen.jpg");

            // 将证书文件放入指定路径，并读取keystore ，获得私钥和证书链
            ImageOpt.addTextToImage(image, "png",
                image2,
                "hello 雅黑 world",
                "雅黑",
                Color.BLACK,
                28, 50, 50);

            System.out.println("Done!");
        } catch (Exception e) {
            System.out.println("Error!");
            //e.printStackTrace();
        }
    }
}
