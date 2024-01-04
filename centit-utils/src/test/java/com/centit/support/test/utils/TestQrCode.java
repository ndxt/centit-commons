package com.centit.support.test.utils;

import com.centit.support.algorithm.CollectionsOpt;
import com.centit.support.image.ImageOpt;
import com.centit.support.image.QrCodeConfig;
import com.centit.support.image.QrCodeGenerator;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class TestQrCode {
    public static void main(String[] args) {
        String context = "{\"deviceId\":\"#0001\",\"deviceCode\":\"898687\",\"deviceLabel\":\"23433\",\"deviceName\":\"笔记本电脑\",\"deviceType\":\"电脑\"}";
        // 生成的二维码的路径及名称
        String destPath = "/Users/codefan/Documents/temp/qrcode.jpg";
        // 嵌入二维码的图片路径
        String logImgPath = "/Users/codefan/Documents/temp/jlwaterlogo.png";
        // 简单的生成
        QrCodeConfig qrCodeConfig = new QrCodeConfig();
        qrCodeConfig.setMsg(context);
        qrCodeConfig.setQrHeight(300);
        qrCodeConfig.setQrWidth(300);
        //白边预留值  取值 0-4  0最小
        //.setPadding(0)
        //.setTopText("测试二维码")
        //.setTopTextFontSize(12)
        //.setTopTextFontType("雅黑")
        //.setDownText("江苏省-南京市-雨花台区")
        //.setDownTextFontSize(10)
        //.setDownTextFontType("宋体")
        //二维码中心logo图片
        qrCodeConfig.setLogoImageUrl(logImgPath);
        try {
            BufferedImage bufferedImage = QrCodeGenerator.createQRImage(qrCodeConfig);
            //QrCodeGenWrapper.asFile(qrCodeConfig,destPath);
            //ImageIO.write(bufferedImage, "JPG", new File(destPath));
            //System.out.println(QrCodeReaderWrapper.decode(logImgPath));
            BufferedImage mergeImage = ImageOpt.mergeImages(
                CollectionsOpt.createList(bufferedImage,bufferedImage,bufferedImage,bufferedImage,bufferedImage,bufferedImage,bufferedImage),
                3, 15);
            ImageIO.write(mergeImage, "JPG", new File(destPath));
        } catch (Exception e) {
            System.out.println("create qrcode error! e: " + e);
        }

    }
}
