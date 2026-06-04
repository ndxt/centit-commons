package com.centit.test;

import com.centit.support.office.OfdUtils;
import com.centit.support.office.OfficeToPdf;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.BaseFont;
import fr.opensagres.poi.xwpf.converter.pdf.PdfConverter;
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;


public class TestToPdf {


    private static final Logger logger = LoggerFactory.getLogger(TestToPdf.class);
    public static boolean word2Pdf(InputStream inWordStream, OutputStream outPdfStram, String suffix) {
        try {
            if ("docx".equalsIgnoreCase(suffix)) {
                XWPFDocument docx = new XWPFDocument(inWordStream);
                PdfOptions options = PdfOptions.create();
                Map<String, BaseFont> fontMap = new HashMap<>();
                // 中文字体处理
                options.fontProvider((familyName, encoding, size, style, color) -> {
                    try {
                        BaseFont bfChinese = fontMap.get(familyName);
                        if(bfChinese==null) {
                            if (familyName.indexOf("仿") >= 0) { //仿宋
                                bfChinese = BaseFont.createFont("simfang.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                            } else if (familyName.indexOf("宋") >= 0) { //宋体
                                bfChinese = BaseFont.createFont("simsun.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                            } else if (familyName.indexOf("楷") >= 0) { //楷体
                                bfChinese = BaseFont.createFont("simkai.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                            } else { // 黑体
                                bfChinese = BaseFont.createFont("simhei.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                            }
                            fontMap.put(familyName, bfChinese);
                        }
                        Font fontChinese = new Font(bfChinese, size, style, color);
                        fontChinese.setFamily(familyName);
                        return fontChinese;
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                        return null;
                    }
                });
                PdfConverter.getInstance().convert(docx, outPdfStram, options);
            } else if ("doc".equalsIgnoreCase(suffix)) {
                // 读取DOC文件
                HWPFDocument doc = new HWPFDocument(inWordStream);
                String text = doc.getDocumentText();
                // 创建PDF
                Document pdf = new Document();
                PdfWriter.getInstance(pdf, outPdfStram);
                pdf.open();
                pdf.add(new Paragraph(text));
                pdf.close();
            }
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return false;
        }
    }

    public static void main(String[] args) throws IOException {

        /*String waterMark = "杨淮生 codefan 2024-9-12";
        System.out.println(waterMark.length());*/
        OfficeToPdf.word2Pdf(new FileInputStream("/Users/codefan/Documents/temp/four-test.docx"),
           new FileOutputStream("/Users/codefan/Documents/temp/four-test.pdf"), "docx");

        System.out.println("Done!");
    }
}
