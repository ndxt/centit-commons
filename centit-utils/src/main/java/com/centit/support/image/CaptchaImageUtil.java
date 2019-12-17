package com.centit.support.image;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

/*
    <bean id="captchaProducer" class="com.google.code.kaptcha.impl.DefaultKaptcha">
        <property name="config">
            <bean class="com.google.code.kaptcha.util.Config">
                <constructor-arg>
                    <props>
                        <prop key="kaptcha.border">no</prop>
                        <prop key="kaptcha.border.color">105,179,90</prop>
                        <prop key="kaptcha.textproducer.font.color">red</prop>
                        <prop key="kaptcha.image.width">250</prop>
                        <prop key="kaptcha.textproducer.font.size">90</prop>
                        <prop key="kaptcha.image.height">90</prop>
                        <prop key="kaptcha.session.key">code</prop>
                        <prop key="kaptcha.textproducer.char.length">4</prop>
                        <prop key="kaptcha.textproducer.font.names">宋体,楷体,微软雅黑</prop>
                    </props>
                </constructor-arg>
            </bean>
        </property>
    </bean>
 */

/**
 * 对辨析难度要求高的可以用 google的kaptcha ，
 * 这个类设计就是为了辨析容易，哈哈虽然有点搞笑，但是有的用户就是有这个要求
 * google的kaptcha 是用方法 ，先配置bean
 * 在类中注入这个bean
 * 然后调用 BufferedImage bi = captchaProducer.createImage(String captchaKey);方法
 * 着用用法就和CaptchaImageUtil的generateCaptchaImage 方法一致了
 *
 * @author codefan
 */
@SuppressWarnings("unused")
public abstract class CaptchaImageUtil {

    public static final String SESSIONCHECKCODE = "session_checkcode";
    public static final String REQUESTCHECKCODE = "j_checkcode";
    private static final String range = "0123456789abcdefghjkmnpqrstuvwxyzABCDEFGHJKMNPQRSRUVWXYZ";
    private CaptchaImageUtil() {
        throw new IllegalAccessError("Utility class");
    }

    public static String getRandomString(int len) {
        Random random = new Random();
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < len; i++) {
            result.append(range.charAt(random.nextInt(range.length())));
        }
        return result.toString();
    }

    public static String getRandomString() {
        return getRandomString(4);
    }

    public static boolean checkcodeMatch(String session_checkcode, String request_checkcode) {
        if (request_checkcode == null || session_checkcode == null
            || "".equals(request_checkcode))
            return false;
        return session_checkcode.equalsIgnoreCase(
            request_checkcode.replaceAll("O", "0").replaceAll("o", "0")
                .replaceAll("I", "1").replaceAll("i", "1")
                .replaceAll("L", "1").replaceAll("l", "1"));
    }
    /*    public static BufferedImage generateCaptchaImage(String captchaKey){
        DefaultKaptcha producer = new DefaultKaptcha();
        producer.setConfig(new Config(new Properties()));
        BufferedImage image = producer.createImage(captchaKey);
        return image;

    }*/

    public static BufferedImage generateCaptchaImage(String captchaKey, Font font) {
        // 设置图片的长宽
        int width = 10 + 13 * captchaKey.length();
        int height = 22;
        // ////// 创建内存图像
        BufferedImage image = new BufferedImage(width, height,
            BufferedImage.TYPE_INT_RGB);
        // 获取图形上下文
        Graphics g = image.createGraphics();
        // 设定图像背景色(因为是做背景，所以偏淡)
        g.setColor(getRandColor(180, 250));
        g.fillRect(0, 0, width, height);
        // 设置字体
        g.setFont(font);
        g.setColor(new Color(0, 0, 0));
        Random rand = new Random();
        for (int i = 0; i < captchaKey.length(); i++) {
            g.drawString(captchaKey.substring(i, i + 1), 13 * i + 6 + rand.nextInt(5), 14 + rand.nextInt(6));
        }
        // 图象生效
        g.dispose();
        return image;
    }

    public static BufferedImage generateCaptchaImage(String captchaKey, String fontName) {
        return generateCaptchaImage(captchaKey, new Font(fontName, Font.PLAIN, 21));
    }

    public static BufferedImage generateCaptchaImage(String captchaKey) {
        return generateCaptchaImage(captchaKey, new Font("Times New Roman", Font.PLAIN, 21));
    }

    public static Color getRandColor(int fc, int bc) {
        Random random = new Random();
        if (fc > 255)
            fc = 255;
        if (bc > 255)
            bc = 255;
        int r = fc + random.nextInt(bc - fc);
        int g = fc + random.nextInt(bc - fc);
        int b = fc + random.nextInt(bc - fc);
        return new Color(r, g, b);
    }

}
