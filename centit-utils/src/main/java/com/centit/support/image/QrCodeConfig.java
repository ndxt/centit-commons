package com.centit.support.image;

import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

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
    private String logo;
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
     * 生成二维码的颜色
     */
    private MatrixToImageConfig matrixToImageConfig;


    /**
     * 生成二维码图片的格式 png, jpg
     */
    private String picType;

    public QrCodeConfig(){
        padding = 0;
        downTextFontSize = 7;
        topTextFontSize = 8;
        topTextFontType = "雅黑";
        downTextFontType = "雅黑";
        qrWidth = 200;
        qrHeight = 200;
        code = "UTF-8";
        picType = "png";
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
        this.downTextFontSize = downTextFontSize;
    }

    public String getTopTextFontType() {
        return topTextFontType;
    }

    public void setTopTextFontType(String topTextFontType) {
        this.topTextFontType = topTextFontType;
    }

    public String getDownTextFontType() {
        return downTextFontType;
    }

    public void setDownTextFontType(String downTextFontType) {
        this.downTextFontType = downTextFontType;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public Integer getQrWidth() {
        return qrWidth;
    }

    public void setQrWidth(Integer qrWidth) {
        this.qrWidth = qrWidth;
    }

    public Integer getQrHeight() {
        return qrHeight;
    }

    public void setQrHeight(Integer qrHeight) {
        this.qrHeight = qrHeight;
    }

    public MatrixToImageConfig getMatrixToImageConfig() {
        return matrixToImageConfig;
    }

    public void setMatrixToImageConfig(MatrixToImageConfig matrixToImageConfig) {
        this.matrixToImageConfig = matrixToImageConfig;
    }

    public String getPicType() {
        return picType;
    }

    public void setPicType(String picType) {
        this.picType = picType;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getPadding() {
        return padding;
    }

    public void setPadding(int padding) {
        this.padding = padding;
    }

    public ErrorCorrectionLevel getErrorCorrection() {
        return errorCorrection;
    }

    public void setErrorCorrection(ErrorCorrectionLevel errorCorrection) {
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

