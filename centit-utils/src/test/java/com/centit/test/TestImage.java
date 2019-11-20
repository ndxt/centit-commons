package com.centit.test;

import com.centit.support.image.ImageOpt;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class TestImage {
     public static void main(String arg[]) {
         try {
             BufferedImage image = ImageOpt.createIdIcon("codefan@sina.com", 64, 8);
             ImageIO.write(image, "png", new File("/D/Projects/RunData/demo_home/images/codefan2.png"));

             image = ImageOpt.createIdIcon("codefan@sina.com", 32, 8);
             ImageIO.write(image, "png", new File("/D/Projects/RunData/demo_home/images/codefan3.png"));

             image = ImageOpt.createIdIcon("codefan@sina.com", 80, 8);
             ImageIO.write(image, "png", new File("/D/Projects/RunData/demo_home/images/codefan4.png"));

         } catch (IOException e) {
             //e.printStackTrace();
         }
     }

     public static void testImageOpt(){
         try {
            BufferedImage image = ImageIO.read(new File("D:/Projects/RunData/demo_home/images/word9.png"));
            List<BufferedImage> subimages = ImageOpt.splitImage(image, 6);
            int i=0;
            for(BufferedImage subimage: subimages)
                ImageIO.write(subimage, "png", new File("D:/Projects/RunData/demo_home/images/word9_"+ i++ +".png"));
        } catch (IOException e) {
            //e.printStackTrace();
        }
     }

     public static void testImageOpt2(){
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
