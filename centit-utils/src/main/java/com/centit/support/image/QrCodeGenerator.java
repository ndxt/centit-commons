package com.centit.support.image;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.encoder.ByteMatrix;
import com.google.zxing.qrcode.encoder.Encoder;
import com.google.zxing.qrcode.encoder.QRCode;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;

public abstract class QrCodeGenerator {
    private static final int QUIET_ZONE_SIZE = 4;
    /**
     * 在图片中间,插入圆角的logo
     *
     * @param qrCode 原图
     * @param logImage  image
     */
    public static void insertLogo(BufferedImage qrCode, BufferedImage logImage) {
        if(logImage==null)
            return;
        int qrcodeWidth = qrCode.getWidth();
        int qrcodeHeight = qrCode.getHeight();

        // 获取logo图片

        int size = logImage.getWidth() > qrcodeWidth / 5 ? qrcodeWidth / 25 : logImage.getWidth() / 5;
        logImage = ImageOpt.makeRoundBorder(logImage, 60, size, Color.WHITE); // 边距为二维码图片的1/10

        // logo的宽高
        int w = logImage.getWidth() > qrcodeWidth  / 5 ? qrcodeWidth / 5 : logImage.getWidth();
        int h = logImage.getHeight() > qrcodeHeight / 5 ? qrcodeHeight / 5 : logImage.getHeight();

        // 插入LOGO
        Graphics2D graph = qrCode.createGraphics();

        int x = (qrcodeWidth - w) / 2;
        int y = (qrcodeHeight - h) / 2;

        graph.drawImage(logImage, x, y, w, h, null);
        graph.dispose();
        qrCode.flush();
    }

    /**
     * 根据二维码配置 二维码矩阵生成二维码图片
     *
     * @param qrCodeConfig 二维码配置
     * @param bitMatrix 二维码图片
     * @return BufferedImage 二维码图片
     * @throws IOException 异常
     */
    private static BufferedImage toBufferedImage(QrCodeConfig qrCodeConfig, BitMatrix bitMatrix) {
        int qrCodeWidth = bitMatrix.getWidth();
        int qrCodeHeight = bitMatrix.getHeight();
        BufferedImage qrCode = new BufferedImage(qrCodeWidth, qrCodeHeight, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < qrCodeWidth; x++) {
            for (int y = 0; y < qrCodeHeight; y++) {
                qrCode.setRGB(x, y,
                    bitMatrix.get(x, y) ?
                        qrCodeConfig.getOnColor() :
                        qrCodeConfig.getOffColor());
            }
        }
        // 插入logo
        if (qrCodeConfig.getLogoImage() != null) {
            insertLogo(qrCode, qrCodeConfig.getLogoImage());
        }
        //插入二维码头文字信息
        if(StringUtils.isNotBlank(qrCodeConfig.getTopText()) || StringUtils.isNotBlank(qrCodeConfig.getDownText())){
            qrCode = addTextInfo(qrCode, qrCodeConfig);
        }
        return qrCode;
    }
    /**
     * 增加底部的说明文字
     * @param source 二维码
     * @param qrCodeConfig 二维码配置信息
     * @return BufferedImage
     */
    private static BufferedImage addTextInfo(BufferedImage source, QrCodeConfig qrCodeConfig) {
        Graphics2D graphics = (Graphics2D)source.getGraphics();
        String topText = qrCodeConfig.getTopText();
        String downText = qrCodeConfig.getDownText();

        double topTextHeight = 0.0;
        double topTextWidth = 0.0;

        double downTextWidth = 0.0;
        double downTextHeight = 0.0;

        Font topTextFont = null;
        Font downTextFont = null;

        if(StringUtils.isNotBlank(topText)) {
            topTextFont = new Font(qrCodeConfig.getTopTextFontType(), Font.BOLD, qrCodeConfig.getTopTextFontSize());
            graphics.setFont(topTextFont);
            Rectangle2D stringBounds = topTextFont.getStringBounds(topText, graphics.getFontRenderContext());
            topTextHeight = stringBounds.getHeight();
            topTextWidth = stringBounds.getWidth();
        }

        if(StringUtils.isNotBlank(downText)) {
            downTextFont = new Font(qrCodeConfig.getDownTextFontType(), Font.BOLD, qrCodeConfig.getDownTextFontSize());
            graphics.setFont(downTextFont);
            Rectangle2D downTextStringBounds = downTextFont.getStringBounds(downText, graphics.getFontRenderContext());
            downTextWidth = downTextStringBounds.getWidth();
            downTextHeight = downTextStringBounds.getHeight();
        }
        double offSetHeight  = topTextHeight > 0 && downTextHeight > 0
            ? (topTextHeight + downTextHeight) * 2
            : (topTextHeight + downTextHeight) * 4;
        int newHeight = (int) (source.getHeight() + offSetHeight);
        int newQrWidth = (int)(source.getWidth() + offSetHeight);

        //在内存创建图片缓冲区  这里设置画板的宽高和类型
        BufferedImage outImage = new BufferedImage(newQrWidth,newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graph = outImage.createGraphics();
        //开启文字抗锯齿
        graph.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        // 画文字到新的面板
        graph.setBackground(qrCodeConfig.getFrameColor());
        graph.setColor(qrCodeConfig.getTextColor());

        graph.clearRect(0, 0, newQrWidth,newHeight);

        if(StringUtils.isNotBlank(topText)){
            graph.setFont(topTextFont);
            //drawString(文字信息、x轴、y轴)方法根据参数设置文字的坐标轴 ，根据需要来进行调整
            float x = (float) (newQrWidth - topTextWidth) / 2;
            float y = (float) topTextHeight;
            graph.drawString(topText,x,y);
        }

        if(StringUtils.isNotBlank(downText)){
            graph.setFont(downTextFont);
            int x = (int) (newQrWidth - downTextWidth) / 2;
            int y = (int) (newHeight -  downTextHeight);
            graph.drawString(downText,x,y);
        }
        // 在画布上画上二维码  X轴Y轴，宽度高度
        graph.drawImage(source, (newQrWidth - source.getWidth()) / 2, (newHeight - source.getHeight()) / 2, source.getWidth(), source.getHeight(), null);
        graph.dispose();
        outImage.flush();
        return outImage;
    }

    /**
     * 生成二维码流
     * @param qrCodeConfig 二维码配置信息
     * @return BufferedImage 二维码流
     * @throws IOException 异常
     * @throws WriterException 异常
     * */
    public static BufferedImage createQRImage(QrCodeConfig qrCodeConfig) throws IOException, WriterException {
        BitMatrix bitMatrix = encode(qrCodeConfig);
        return toBufferedImage(qrCodeConfig, bitMatrix);
    }

    /**
     * 生成二维码流
     * @param message 二维码配置信息
     * @return BufferedImage 二维码流
     * @throws IOException 异常
     * @throws WriterException 异常
     * */
    public static BufferedImage createQRImage(String message) throws IOException, WriterException {
        QrCodeConfig config = new QrCodeConfig();
        config.setMsg(message);
        return createQRImage(config);
    }

    /**
     * 对 zxing 的 QRCodeWriter 进行扩展, 解决白边过多的问题
     * <p/>
     * 源码参考 {@link com.google.zxing.qrcode.QRCodeWriter#encode(String, BarcodeFormat, int, int, Map)}
     */
    private static BitMatrix encode(QrCodeConfig qrCodeConfig) throws WriterException {
        ErrorCorrectionLevel errorCorrectionLevel = qrCodeConfig.getErrorCorrection();
        int quietZone = qrCodeConfig.getPadding();
        if (quietZone > QUIET_ZONE_SIZE) {
            quietZone = QUIET_ZONE_SIZE;
        } else if (quietZone < 0) {
            quietZone = 0;
        }
        QRCode code = Encoder.encode(qrCodeConfig.getMsg(), errorCorrectionLevel, qrCodeConfig.getHints());
        return renderResult(code, qrCodeConfig.getQrWidth(), qrCodeConfig.getQrHeight(), quietZone);
    }


    /**
     * 对 zxing 的 QRCodeWriter 进行扩展, 解决白边过多的问题
     * <p/>
     * 源码参考 {@link com.google.zxing.qrcode.QRCodeWriter #renderResult(QRCode, int, int, int)}
     *
     * @param code
     * @param width
     * @param height
     * @param quietZone 取值 [0, 4]
     * @return
     */
    private static BitMatrix renderResult(QRCode code, int width, int height, int quietZone) {
        ByteMatrix input = code.getMatrix();
        if (input == null) {
            throw new IllegalStateException();
        }
        // xxx 二维码宽高相等, 即 qrWidth == qrHeight
        int inputWidth = input.getWidth();
        int inputHeight = input.getHeight();
        int qrWidth = inputWidth + (quietZone * 2);
        int qrHeight = inputHeight + (quietZone * 2);
        // 白边过多时, 缩放
        int minSize = Math.min(width, height);
        int scale = calculateScale(qrWidth, minSize);
        if (scale > 0) {
            /*if (log.isDebugEnabled()) {
                log.debug("qrCode scale enable! scale: {}, qrSize:{}, expectSize:{}x{}", scale, qrWidth, width, height);
            }*/
            int padding, tmpValue;
            // 计算边框留白
            padding = (minSize - qrWidth * scale) / QUIET_ZONE_SIZE * quietZone;
            tmpValue = qrWidth * scale + padding;
            if (width == height) {
                width = tmpValue;
                height = tmpValue;
            } else if (width > height) {
                width = width * tmpValue / height;
                height = tmpValue;
            } else {
                height = height * tmpValue / width;
                width = tmpValue;
            }
        }
        int outputWidth = Math.max(width, qrWidth);
        int outputHeight = Math.max(height, qrHeight);

        int multiple = Math.min(outputWidth / qrWidth, outputHeight / qrHeight);
        int leftPadding = (outputWidth - (inputWidth * multiple)) / 2;
        int topPadding = (outputHeight - (inputHeight * multiple)) / 2;

        BitMatrix output = new BitMatrix(outputWidth, outputHeight);

        for (int inputY = 0, outputY = topPadding; inputY < inputHeight; inputY++, outputY += multiple) {
            // Write the contents of this row of the barcode
            for (int inputX = 0, outputX = leftPadding; inputX < inputWidth; inputX++, outputX += multiple) {
                if (input.get(inputX, inputY) == 1) {
                    output.setRegion(outputX, outputY, multiple, multiple);
                }
            }
        }

        return output;
    }


    /**
     * 如果留白超过15% , 则需要缩放
     * (15% 可以根据实际需要进行修改)
     *
     * @param qrCodeSize 二维码大小
     * @param expectSize 期望输出大小
     * @return 返回缩放比例, <= 0 则表示不缩放, 否则指定缩放参数
     */
    private static int calculateScale(int qrCodeSize, int expectSize) {
        if (qrCodeSize >= expectSize) {
            return 0;
        }
        int scale = expectSize / qrCodeSize;
        int abs = expectSize - scale * qrCodeSize;
        if (abs < expectSize * 0.15) {
            return 0;
        }
        return scale;
    }

}
