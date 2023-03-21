package com.centit.test;

import com.centit.support.office.OfficeToPdf;
import com.centit.support.office.PdfBaseOpt;
import com.itextpdf.text.DocumentException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class TestToPdf {
    /**
     * @param args
     */
    public static void main(String[] args) {
        try{
            fetchPdfImage();
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        OfficeToPdf.excel2Pdf("/D/Projects/RunData/file_home/六上课程表.xlsx",
            "/D/Projects/RunData/file_home/六上课程表.pdf");
        // OfficeToPdf.excel2Pdf("/D/Projects/RunData/demo_home/temp/case3.xlsx",
        //    "/D/Projects/RunData/demo_home/temp/case3.pdf");

        //OfficeToPdf.office2Pdf("D:/temp/复星集团.doc", "D:/temp/复星集团.pdf");
        //OfficeToPdf.office2Pdf("D:/temp/复星集团.docx", "D:/temp/复星集团x.pdf");

        //OfficeToPdf.word2Pdf("D:/CA签名步骤.doc", "D:/CA签名步骤.pdf","doc");
        //OfficeToPdf.office2Pdf("D:/temp/财务报表.xlsx", "D:/temp/财务报表.pdf");
        //OfficeToPdf.office2Pdf("D:/temp/财务报表.xlsm", "D:/temp/财务报表m.pdf");

        //OfficeToPdf.office2Pdf("D:/temp/经营数据管理系统需求.pptx", "D:/temp/经营数据管理系统需求.pdf");
        //OfficeToPdf.office2Pdf("D:/temp/自来水公司整体业务介绍.ppt", "D:/temp/自来水公司整体业务介绍.pdf");

    }

    public static void fetchPdfImage() throws IOException, DocumentException {
        List<BufferedImage> images =  PdfBaseOpt.fetchPdfImages("/Users/codefan/Documents/temp/testPdfImage.pdf");
        int i=0;
        for(BufferedImage image : images){
            ImageIO.write(image, "jpg", new File("/Users/codefan/Documents/temp/images/image"+i+".jpg"));
            i++;
        }
        //stp.close();
    }

}
