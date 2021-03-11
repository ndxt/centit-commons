package com.centit.test;

import com.centit.support.image.ImageOpt;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import org.apache.commons.logging.Log;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import sun.misc.BASE64Decoder;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class TestImage {
    public static void main(String arg[]) {
        String[] s=decode("d:\\52570486.pdf");
        System.out.println("发票类型"+s[1]+"发票号码"+s[3]+"发票金额"+s[4]);
//        testCreateIcon();
        /*System.out.println(CaptchaImageUtil.checkcodeMatch("",null));
        System.out.println(CaptchaImageUtil.checkcodeMatch("hello","hello "));
        System.out.println(CaptchaImageUtil.checkcodeMatch("hello","HeL1o"));
        System.out.println(CaptchaImageUtil.checkcodeMatch("he1lo","HeLLo"));
        System.out.println(CaptchaImageUtil.checkcodeMatch("he110","Heilo"));
        System.out.println(CaptchaImageUtil.checkcodeMatch("heIL0","Heil2"));
        System.out.println(CaptchaImageUtil.checkcodeMatch("heI20","Heilo"));
*/    }
    private static List<BufferedImage>  extractImage(File pdfFile) throws Exception {
        List<BufferedImage> imageList = new ArrayList<BufferedImage>();

        PDDocument document = PDDocument.load(pdfFile);
        PDPage page = document.getPage(0); //电子发票只有一页
        PDResources resources = page.getResources();

        for (COSName name : resources.getXObjectNames()) {
            if (resources.isImageXObject(name)) {
                PDImageXObject obj = (PDImageXObject)resources.getXObject(name);
                imageList.add(obj.getImage());
            }
        }
        return imageList;
    }
    public static String[] decode(String input) {
        try {
            List<BufferedImage> imageList = extractImage(new File(input));
            LuminanceSource source = new BufferedImageLuminanceSource(imageList.get(1));
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            Map<DecodeHintType,Object> hints = new LinkedHashMap<DecodeHintType,Object>();
            // 解码设置编码方式为：utf-8，
            hints.put(DecodeHintType.CHARACTER_SET, "utf-8");
            //优化精度
            hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
            //复杂模式，开启PURE_BARCODE模式
            hints.put(DecodeHintType.PURE_BARCODE, Boolean.TRUE);
            Result result = new MultiFormatReader().decode(bitmap, hints);
            String[] infos = result.getText().split(",");
            return infos;
        } catch (Exception e) {
            return new String[]{"解码失败，请确认的你二维码是否正确，或者图片有多个二维码！"};
        }
    }
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
