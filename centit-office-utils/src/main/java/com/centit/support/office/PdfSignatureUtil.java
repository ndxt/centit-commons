package com.centit.support.office;

import com.centit.support.office.commons.SignatureInfo;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.security.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;

public abstract class PdfSignatureUtil {

    public static SignatureInfo createSingInfo(){
        return new SignatureInfo();
    }

    public static boolean sign(InputStream srcStream, OutputStream targetStream, SignatureInfo signatureInfo) {
        try{
            ByteArrayOutputStream tempArrayOutputStream = new ByteArrayOutputStream();
            PdfReader reader = new PdfReader(srcStream);
            // 创建签章工具PdfStamper ，最后一个boolean参数是否允许被追加签名
            // false的话，pdf文件只允许被签名一次，多次签名，最后一次有效
            // true的话，pdf可以被追加签名，验签工具可以识别出每次签名之后文档是否被修改
            PdfStamper stamper = PdfStamper.createSignature(reader,
                    tempArrayOutputStream, '\0', null, true);
            // 获取数字签章属性对象
            PdfSignatureAppearance appearance = stamper
                    .getSignatureAppearance();
            appearance.setReason(signatureInfo.getReasonDesc());
            appearance.setLocation(signatureInfo.getLocationDesc());
            // 设置签名的位置，页码，签名域名称，多次追加签名的时候，签名预名称不能一样 图片大小受表单域大小影响（过小导致压缩）
            // 签名的位置，是图章相对于pdf页面的位置坐标，原点为pdf页面左下角
            // 四个参数的分别是，图章左下角x，图章左下角y，图章右上角x，图章右上角y
            appearance.setVisibleSignature(signatureInfo.getSignRect(), signatureInfo.getSignPage(), signatureInfo
                            .getFieldName());
            // 读取图章图片
            appearance.setSignatureGraphic(signatureInfo.getSignImage());
            appearance.setCertificationLevel(signatureInfo
                    .getCertificationLevel());
            // 设置图章的显示方式，如下选择的是只显示图章（还有其他的模式，可以图章和签名描述一同显示）
            appearance.setRenderingMode(signatureInfo.getRenderingMode());
            // 摘要算法
            ExternalDigest digest = new BouncyCastleDigest();
            // 签名算法
            ExternalSignature signature = new PrivateKeySignature(
                    signatureInfo.getPk(), signatureInfo.getDigestAlgorithm(),
                    null);
            // 调用itext签名方法完成pdf签章 //数字签名格式，CMS,CADE
            MakeSignature.signDetached(appearance, digest, signature,
                    signatureInfo.getChain(), null, null, null, 0,
                    MakeSignature.CryptoStandard.CADES);
            targetStream.write(tempArrayOutputStream.toByteArray());
            targetStream.flush();
            return true;
        } catch (DocumentException | IOException | GeneralSecurityException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean sign(String src, String target, SignatureInfo signatureInfo) {
        try(InputStream srcStream = Files.newInputStream(Paths.get(src));
            OutputStream targetStream = Files.newOutputStream(Paths.get(target))) {
            return sign(srcStream, targetStream, signatureInfo);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
