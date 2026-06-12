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
import com.lowagie.text.Font;
import com.lowagie.text.pdf.BaseFont;
import fr.opensagres.poi.xwpf.converter.pdf.PdfConverter;
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    /**
     * 当前系统的字体目录
     */
    private static final String[] SYSTEM_FONT_DIRS;
    static {
        List<String> dirs = new ArrayList<>();
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("win")) {
            String windir = System.getenv("WINDIR");
            dirs.add(windir != null ? windir + "\\Fonts" : "C:\\Windows\\Fonts");
        } else if (os.contains("mac")) {
            dirs.add("/Library/Fonts");
            dirs.add("/System/Library/Fonts");
            dirs.add(System.getProperty("user.home") + "/Library/Fonts");
        } else {
            dirs.add("/usr/share/fonts");
            dirs.add("/usr/local/share/fonts");
            dirs.add(System.getProperty("user.home") + "/.fonts");
            dirs.add(System.getProperty("user.home") + "/.local/share/fonts");
        }
        SYSTEM_FONT_DIRS = dirs.toArray(new String[0]);
    }

    /**
     * 根据字体名称在系统字体目录中查找字库文件，找不到返回 null。
     * 直接用 familyName 匹配文件名（含扩展名 .ttf/.ttc/.otf）。
     */
    private static String findSystemFont(String familyName) {
        String lower = familyName.toLowerCase().replace(" ", "");
        String[] nameCandidates = {
            lower + ".ttf", lower + ".ttc", lower + ".otf",
            familyName + ".ttf", familyName + ".ttc", familyName + ".otf"
        };
        for (String dir : SYSTEM_FONT_DIRS) {
            File found = findFontFile(new File(dir), nameCandidates);
            if (found != null) return found.getAbsolutePath();
        }
        return null;
    }

    /**
     * 在目录中递归查找匹配的字体文件（不区分大小写）
     */
    private static File findFontFile(File dir, String[] candidates) {
        File[] files = dir.listFiles();
        if (files == null) return null;
        for (File f : files) {
            if (f.isDirectory()) {
                File found = findFontFile(f, candidates);
                if (found != null) return found;
            } else {
                String lower = f.getName().toLowerCase();
                for (String c : candidates) {
                    if (lower.equals(c.toLowerCase())) return f;
                }
            }
        }
        return null;
    }

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
                Map<String, BaseFont> fontMap = new HashMap<>();
                // 中文字体处理
                options.fontProvider((familyName, encoding, size, style, color) -> {
                    try {
                        BaseFont bfChinese = fontMap.get(familyName);
                        if(bfChinese==null) {
                            String fontPath = findSystemFont(familyName);
                            if (fontPath == null) {
                                // 回退到内置字体（classpath）
                                String fontKey = familyName.contains("仿") ? "仿" :
                                                 familyName.contains("宋") ? "宋" :
                                                 familyName.contains("楷") ? "楷" : "黑";
                                fontPath = switch (fontKey) {
                                    case "仿" -> "fonts/simfang.ttf";
                                    case "宋" -> "fonts/simsun.ttf";
                                    case "楷" -> "fonts/simkai.ttf";
                                    default  -> "fonts/simhei.ttf";
                                };
                            }
                            bfChinese = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
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
