package com.centit.support.image;

import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class QrCodeConfig {
    /**
     * 塞入二维码的信息
     */
    private String msg;

    /**
     * 二维码顶部文字
     */
    private String topText;

    /**
     * 二维码顶部文字大小
     */
    private Integer topTextFontSize;

    /**
     * 二维码底部文字
     */
    private String downText;

    /**
     * 二维码底部文字大小
     */
    private Integer downTextFontSize;

    /**
     * 顶部部字体类型
     */
    private String topTextFontType;

    /**
     * 底部部字体类型
     */
    private String downTextFontType;

    /**
     * 二维码中间的logo
     */
    private BufferedImage logoImage;
    /**
     * 生成二维码的宽
     */
    private Integer qrWidth;
    /**
     * 生成二维码的高
     */
    private Integer qrHeight;

    /**
     * qrcode message's code, default UTF-8
     */
    private String code;

    /**
     * 二维码白边预留参数
     * 0 - 4
     */
    private int padding;

    /**
     * error level, default H
     */
    private ErrorCorrectionLevel errorCorrection;
    /**
     * 生成二维码的颜色 Params:
     * onColor – pixel on color, specified as an ARGB value as an int offColor – pixel off color, specified as an ARGB value as an int
     */
    private int onColor;
    /**
     * 生成二维码的颜色 Params:
     * onColor – pixel on color, specified as an ARGB value as an int offColor – pixel off color, specified as an ARGB value as an int
     */
    private int offColor;

    /**
     * 生成二维码图片的格式 png, jpg
     */
    private String picType;

    private Color textColor;

    private Color frameColor;

    public QrCodeConfig(){
        this.padding = 0;
        this.downTextFontSize = 7;
        this.topTextFontSize = 8;

        this.topTextFontType = "雅黑";
        this.downTextFontType = "雅黑";
        this.qrWidth = 200;
        this.qrHeight = 200;
        this.code = "UTF-8";
        this.picType = "png";
        this.errorCorrection = ErrorCorrectionLevel.Q;
        this.onColor = MatrixToImageConfig.BLACK;
        this.offColor = MatrixToImageConfig.WHITE;
        this.logoImage = null;
        this.frameColor = Color.white;
        this.textColor = Color.black;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    private void validate() {
        if (msg == null || msg.length() == 0) {
            throw new IllegalArgumentException("二维码内容不能为空!");
        }
    }

    public String getTopText() {
        return topText;
    }

    public void setTopText(String topText) {
        this.topText = topText;
    }

    public Integer getTopTextFontSize() {
        return topTextFontSize;
    }

    public void setTopTextFontSize(Integer topTextFontSize) {
        if(topTextFontSize==null || topTextFontSize<0)
            return;
        this.topTextFontSize = topTextFontSize;
    }

    public String getDownText() {
        return downText;
    }

    public void setDownText(String downText) {
        this.downText = downText;
    }

    public Integer getDownTextFontSize() {
        return downTextFontSize;
    }

    public void setDownTextFontSize(Integer downTextFontSize) {
        if(downTextFontSize==null || downTextFontSize<0)
            return;
        this.downTextFontSize = downTextFontSize;
    }

    public String getTopTextFontType() {
        return topTextFontType;
    }

    public void setTopTextFontType(String topTextFontType) {
        if(StringUtils.isBlank(topTextFontType))
            return;
        this.topTextFontType = topTextFontType;
    }

    public String getDownTextFontType() {
        return downTextFontType;
    }

    public void setDownTextFontType(String downTextFontType) {
        if(StringUtils.isBlank(downTextFontType))
            return;
        this.downTextFontType = downTextFontType;
    }

    public BufferedImage getLogoImage() {
        return logoImage;
    }

    public void setLogoImageUrl(String logoImageUrl) {
        if(StringUtils.isBlank(logoImageUrl))
            return;
        try {
            this.logoImage = ImageOpt.loadImage(logoImageUrl);
        } catch (IOException e) {
            this.logoImage = null;
        }
    }

    public void setLogoImage(BufferedImage logoImage) {
        this.logoImage = logoImage;
    }

    public Integer getQrWidth() {
        return qrWidth;
    }

    public void setQrWidth(Integer qrWidth) {
        if(qrWidth==null || qrWidth<0)
            return;
        this.qrWidth = qrWidth;
    }

    public Integer getQrHeight() {
        return qrHeight;
    }

    public void setQrHeight(Integer qrHeight) {
        if(qrHeight==null || qrHeight<0)
            return;
        this.qrHeight = qrHeight;
    }

    public int getOnColor() {
        return onColor;
    }

    public void setOnColor(Integer onColor) {
        if(onColor!=null)
            this.onColor = onColor;
    }

    public int getOffColor() {
        return offColor;
    }

    public Color getTextColor() {
        return textColor;
    }

    public void setTextColor(Color textColor) {
        this.textColor = textColor;
    }

    public Color getFrameColor() {
        return frameColor;
    }

    public void setFrameColor(Color frameColor) {
        this.frameColor = frameColor;
    }
    public void setOffColor(Integer offColor) {
        if(offColor!=null)
            this.offColor = offColor;
    }

    public String getPicType() {
        return picType;
    }

    public void setPicType(String picType) {
        if(StringUtils.isBlank(picType))
            return;
        this.picType = picType;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        if(StringUtils.isBlank(code))
            return;
        this.code = code;
    }

    public int getPadding() {
        return padding;
    }

    public void setPadding(Integer padding) {
        if(padding==null || padding<0)
            return;
        this.padding = padding;
    }

    public ErrorCorrectionLevel getErrorCorrection() {
        return errorCorrection;
    }

    public void setErrorCorrection(ErrorCorrectionLevel errorCorrection) {
        if(errorCorrection==null)
            return;
        this.errorCorrection = errorCorrection;
    }

    public Map<EncodeHintType, Object> getHints() {
        Map<EncodeHintType, Object> hints = new HashMap<>(3);
        hints.put(EncodeHintType.ERROR_CORRECTION, this.getErrorCorrection());
        hints.put(EncodeHintType.CHARACTER_SET, this.getCode());
        hints.put(EncodeHintType.MARGIN, this.getPadding());
        return hints;
    }
}

