package com.centit.support.office;

import com.centit.support.file.FileType;
import com.centit.support.office.commons.*;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.lowagie.text.pdf.BaseFont;
import org.apache.poi.hwpf.HWPFDocument;
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
import java.util.List;

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
                try (XWPFDocument docx = new XWPFDocument(inWordStream)) {
                    // 使用混合转换器，获得更好的表格保真度
                    return DocxHybridConverter.convert(docx, outPdfStram);
                }
            } else if (DOC.equalsIgnoreCase(suffix)) {
                // 读取DOC文件
                try (HWPFDocument doc = new HWPFDocument(inWordStream)) {
                    // 创建PDF文档
                    Document pdf = new Document(PageSize.A4, 36, 36, 36, 36); // 设置页边距
                    PdfWriter writer = PdfWriter.getInstance(pdf, outPdfStram);
                    writer.setPageEvent(new PDFPageEvent());

                    // 设置中文字体支持
                    com.itextpdf.text.pdf.BaseFont bfChinese = com.itextpdf.text.pdf.BaseFont.createFont(
                        "STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);

                    pdf.open();

                    // 使用工具类提取内容（包括文本和表格），按原始顺序合并
                    List<Element> elements = WordTableToPdfUtils.extractContentFromDoc(doc, bfChinese);

                    for (Element element : elements) {
                        if (element != null) {
                            pdf.add(element);
                        }
                    }

                    pdf.close();
                }
            }
            return true;
        } catch (Exception e) {
            logger.error("Word转PDF失败: {}", e.getMessage(), e);
            return false;
        }
    }

    public static boolean word2Pdf(String inWordFile, String outPdfFile, String suffix) {

        String inputFile = CommonUtils.mapWidowsPathIfNecessary(inWordFile);
        String pdfFile = CommonUtils.mapWidowsPathIfNecessary(outPdfFile);
        try (InputStream inWordStream = Files.newInputStream(new File(inputFile).toPath());
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

    /**
     * Word转PDF（使用手动模式）
     * 手动模式使用自定义表格转换，表格保真度最高
     * 适用于：纯表格文档、主要包含表格的文档、对表格质量要求高的场景
     *
     * 注意：此函数仅支持DOCX格式，不支持DOC格式
     * 手动模式不支持图片、嵌套表格、页眉页脚等复杂特性
     * 如果文档包含这些特性，建议使用word2Pdf()
     *
     * @param inWordStream  Word文档输入流（仅支持DOCX）
     * @param outPdfStream  PDF文档输出流
     * @return 是否成功
     */
    public static boolean word2PdfManualMode(InputStream inWordStream, OutputStream outPdfStream) {
        try {
            try (XWPFDocument docx = new XWPFDocument(inWordStream)) {
                // 使用手动模式转换，获得最高的表格保真度
                return DocxHybridConverter.convertWithManualMode(docx, outPdfStream);
            }
        } catch (Exception e) {
            logger.error("Word转PDF（手动模式）失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Word转PDF（使用手动模式）
     * 手动模式使用自定义表格转换，表格保真度最高
     * 适用于：纯表格文档、主要包含表格的文档、对表格质量要求高的场景
     *
     * 注意：此函数仅支持DOCX格式，不支持DOC格式
     * 手动模式不支持图片、嵌套表格、页眉页脚等复杂特性
     * 如果文档包含这些特性，建议使用word2Pdf()
     *
     * @param inWordFile  Word文档路径（仅支持DOCX）
     * @param outPdfFile  PDF文档路径
     * @return 是否成功
     */
    public static boolean word2PdfManualMode(String inWordFile, String outPdfFile) {
        String inputFile = CommonUtils.mapWidowsPathIfNecessary(inWordFile);
        String pdfFile = CommonUtils.mapWidowsPathIfNecessary(outPdfFile);

        // 检查文件格式
        String suffix = FileType.getFileExtName(inWordFile);
        if (!DOCX.equalsIgnoreCase(suffix)) {
            logger.error("word2PdfManualMode仅支持DOCX格式，不支持{}格式", suffix);
            return false;
        }

        try (InputStream inWordStream = Files.newInputStream(new File(inputFile).toPath());
             OutputStream outPdfStream = Files.newOutputStream(new File(pdfFile).toPath())) {
            return word2PdfManualMode(inWordStream, outPdfStream);
        } catch (Exception e) {
            logger.error("Word转PDF（手动模式）失败: {}", e.getMessage(), e);
            return false;
        }
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
            if (nSheetSize > 1) {
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
        try (InputStream inExcelStream = Files.newInputStream(new File(inputFile).toPath());
             OutputStream outPdfStream = Files.newOutputStream(new File(pdfFile).toPath())) {
            return excel2Pdf(inExcelStream, outPdfStream);
        } catch (IOException e) {
            logger.error(e.getMessage());
            return false;
        }
    }

}
