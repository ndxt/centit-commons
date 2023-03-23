package com.centit.support.office.commons;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.security.DigestAlgorithms;
import lombok.Data;

import java.awt.*;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.cert.Certificate;

@Data
public class SignatureInfo {

    private String reasonDesc; //签名的原因，显示在pdf签名属性中
    private String locationDesc;//签名的地点，显示在pdf签名属性中
    private String digestAlgorithm;//摘要算法名称，例如SHA-1
    private Image signImage;//图章路径
    private String fieldName;//表单域名称
    private Certificate[] chain;//证书链
    private PrivateKey pk;//签名私钥
    /**
     * @see com.itextpdf.text.pdf.PdfSignatureAppearance
     *    int NOT_CERTIFIED = 0;  Approval signature; default value
     *    int CERTIFIED_NO_CHANGES_ALLOWED = 1; Author signature, no changes allowed
     *    int CERTIFIED_FORM_FILLING = 2; Author signature, form filling allowed
     *    int CERTIFIED_FORM_FILLING_AND_ANNOTATIONS = 3; Author signature, form filling and annotations allowed
     */
    private int certificationLevel; //批准签章
    //表现形式：仅描述，仅图片，图片和描述，签章者和描述
    private PdfSignatureAppearance.RenderingMode renderingMode;
    //图章属性
    private Rectangle signRect;//图章左下角x
    private int signPage; //批准签章

    public SignatureInfo(){
        certificationLevel = 0;
        digestAlgorithm = DigestAlgorithms.SHA1;
        renderingMode = PdfSignatureAppearance.RenderingMode.GRAPHIC;
    }

    public SignatureInfo reason(String reasonString){
        this.reasonDesc = reasonString;
        return this;
    }

    public SignatureInfo location(String locationDesc){
        this.locationDesc = locationDesc;
        return this;
    }

    public SignatureInfo algorithm(String digestAlgorithm){
        this.digestAlgorithm = digestAlgorithm;
        return this;
    }

    public SignatureInfo field(String fieldName){
        this.fieldName = fieldName;
        return this;
    }

    public SignatureInfo certificate(Certificate[] chain){
        this.chain = chain;
        return this;
    }

    public SignatureInfo privateKey(PrivateKey pk){
        this.pk = pk;
        return this;
    }

    public SignatureInfo certificateLevel(int certificationLevel){
        this.certificationLevel = certificationLevel;
        return this;
    }

    public SignatureInfo renderingMode(PdfSignatureAppearance.RenderingMode renderingMode){
        this.renderingMode = renderingMode;
        return this;
    }

    public SignatureInfo image(Image image){
        this.signImage = image;
        return this;
    }

    public SignatureInfo image(String imagePath){
        try {
            this.signImage = Image.getInstance(imagePath);
        } catch (BadElementException | IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    public SignatureInfo image(final java.awt.Image image){
        try {
            this.signImage = Image.getInstance(image, new Color(255,255,255));
        } catch (BadElementException | IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    public SignatureInfo page(int signPage){
        this.signPage = signPage;
        return this;
    }
    public SignatureInfo rect(Rectangle rectangle){
        this.signRect = rectangle;
        return this;
    }

    public SignatureInfo rect(final float llx, final float lly, final float urx, final float ury) {
        this.signRect = new Rectangle(llx, lly, urx, ury);
        return this;
    }
}
