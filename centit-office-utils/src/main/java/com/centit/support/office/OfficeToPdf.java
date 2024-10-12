package com.centit.support.office;

import com.centit.support.file.FileType;
import com.centit.support.office.commons.CommonUtils;
import com.centit.support.office.commons.Excel2PdfUtils;
import com.centit.support.office.commons.PDFPageEvent;
import com.centit.support.office.commons.PowerPointUtils;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.BaseFont;
import fr.opensagres.poi.xwpf.converter.pdf.PdfConverter;
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zhf
 */
public abstract class OfficeToPdf {
    private OfficeToPdf() {
        throw new IllegalAccessError("Utility class");
    }

    private static final Logger logger = LoggerFactory.getLogger(OfficeToPdf.class);

    final static String DOC = "doc";
    final static String DOCX = "docx";

    public static boolean ppt2Pdf(String inPptFile, String outPdfFile, String suffix) {
        String inputFile = CommonUtils.mapWidowsPathIfNecessary(inPptFile);
        String pdfFile = CommonUtils.mapWidowsPathIfNecessary(outPdfFile);

        if ("ok".equals(PowerPointUtils.pptToPdfUseImage(inputFile, pdfFile, suffix))) {
            return true;
        }
        return false;
    }

    public static boolean ppt2Pdf(String inPptFile, String outPdfFile) {
        return ppt2Pdf(inPptFile, outPdfFile, FileType.getFileExtName(inPptFile));
    }

    public static boolean word2Pdf(InputStream inWordStream, OutputStream outPdfStram, String suffix) {
        try {
            if (DOCX.equalsIgnoreCase(suffix)) {
                XWPFDocument docx = new XWPFDocument(inWordStream);
                PdfOptions options = PdfOptions.create();
                Map<String, Font> fontMap = new HashMap<>();
                // 中文字体处理
                options.fontProvider((familyName, encoding, size, style, color) -> {
                    try {
                        Font fontChinese = fontMap.get(familyName);
                        if(fontChinese!=null){
                            return fontChinese;
                        }
                        BaseFont bfChinese;
                        if(familyName.indexOf("仿")>=0) { //仿宋
                            bfChinese = BaseFont.createFont("simfang.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                        } else if(familyName.indexOf("宋")>=0) { //宋体
                            bfChinese = BaseFont.createFont("simsun.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                        } else if(familyName.indexOf("楷")>=0) { //楷体
                            bfChinese = BaseFont.createFont("simkai.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                        } else  { // 黑体
                            bfChinese = BaseFont.createFont("simhei.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                        }
                        fontChinese = new Font(bfChinese, size, style, color);
                        fontChinese.setFamily(familyName);
                        fontMap.put(familyName, fontChinese);
                        return fontChinese;
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                        return null;
                    }
                });
                PdfConverter.getInstance().convert(docx, outPdfStram, options);
            } else if (DOC.equalsIgnoreCase(suffix)) {
                //WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(inWordStream);
                /*Source source = new StreamSource(inWordStream);
                Word2003XmlConverter conv = new Word2003XmlConverter(source);
                WordprocessingMLPackage wordMLPackage = conv.getWordprocessingMLPackage();
                Docx4J.toPDF(wordMLPackage, outPdfStram);*/
            }
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return false;
        }
    }

    public static boolean word2Pdf(String inWordFile, String outPdfFile, String suffix) {

        String inputFile = CommonUtils.mapWidowsPathIfNecessary(inWordFile);
        String pdfFile = CommonUtils.mapWidowsPathIfNecessary(outPdfFile);
        try(InputStream inWordStream = Files.newInputStream(new File(inputFile).toPath());
            OutputStream outPdfStream = Files.newOutputStream(new File(pdfFile).toPath())) {
            return word2Pdf(inWordStream, outPdfStream, suffix);
        } catch (IOException e) {
            logger.error(e.getMessage());
            return false;
        }
    }

    public static boolean word2Pdf(String inWordFile, String outPdfFile) {
        return word2Pdf(inWordFile, outPdfFile, FileType.getFileExtName(inWordFile));
    }

    public static boolean excel2Pdf(InputStream inWExcelStream, OutputStream outPdfStram) {
        try {
            com.itextpdf.text.Document document = new com.itextpdf.text.Document();
            document.setPageSize(PageSize.A4.rotate());
            PdfWriter writer = PdfWriter.getInstance(document, outPdfStram);
            writer.setPageEvent(new PDFPageEvent());
            //Open document
            document.open();
            Workbook wb = WorkbookFactory.create(inWExcelStream);
            int nSheetSize = wb.getNumberOfSheets();


            //Single one
            if(nSheetSize > 1){
                Excel2PdfUtils.toCreateContentIndexes(document, nSheetSize);
            }
            for (int i = 0; i < nSheetSize; i++) {
                Sheet sheet = wb.getSheetAt(i);
                PdfPTable table = Excel2PdfUtils.toParseContent(wb, sheet, i);
                table.setKeepTogether(true);
                //      table.setWidthPercentage(new float[]{100} , writer.getPageSize());
                table.getDefaultCell().setBorder(PdfPCell.NO_BORDER);
                document.add(table);
            }
            document.close();
            return true;
        } catch (DocumentException | IOException e) {
            logger.error(e.getMessage());
            return false;
        }
    }

    public static boolean excel2Pdf(String inExcelFile, String outPdfFile) {
        String inputFile = CommonUtils.mapWidowsPathIfNecessary(inExcelFile);
        String pdfFile = CommonUtils.mapWidowsPathIfNecessary(outPdfFile);
        try(InputStream inExcelStream = Files.newInputStream(new File(inputFile).toPath());
            OutputStream outPdfStream = Files.newOutputStream(new File(pdfFile).toPath())) {
            return excel2Pdf(inExcelStream, outPdfStream);
        } catch (IOException e) {
            logger.error(e.getMessage());
            return false;
        }
    }

}
