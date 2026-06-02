package com.centit.support.office.commons;

import com.itextpdf.text.Document;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.BaseFont;
import fr.opensagres.poi.xwpf.converter.pdf.PdfConverter;
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DOCX混合转换器
 * 结合fr.opensagres自动转换和手动表格转换，获得最佳的表格保真度
 *
 * @author zhf
 */
public class DocxHybridConverter {

    private static final Logger logger = LoggerFactory.getLogger(DocxHybridConverter.class);

    /**
     * 混合转换DOCX到PDF
     * - 文本、图片等：使用fr.opensagres自动转换
     * - 表格：使用DocxTableToPdfUtils手动转换
     *
     * @param docx         DOCX文档
     * @param outputStream PDF输出流
     * @return 是否成功
     */
    public static boolean convert(XWPFDocument docx, OutputStream outputStream) {
        try {
            // 方案A：先尝试完全手动控制（推荐，表格质量最高）
            return convertWithManualTables(docx, outputStream);

        } catch (Exception e) {
            logger.error("混合转换失败，回退到自动转换: {}", e.getMessage(), e);
            try {
                // 方案B：回退到完全自动转换
                return convertWithAutoMode(docx, outputStream);
            } catch (Exception ex) {
                logger.error("自动转换也失败: {}", ex.getMessage(), ex);
                return false;
            }
        }
    }

    /**
     * 方案A：手动控制表格转换（推荐）
     * 优点：表格保真度最高，保持原文档顺序
     * 缺点：代码复杂，需要处理所有元素类型
     */
    private static boolean convertWithManualTables(XWPFDocument docx, OutputStream outputStream) throws Exception {
        // 创建PDF文档
        Document pdf = new Document(PageSize.A4, 36, 36, 36, 36);
        PdfWriter writer = PdfWriter.getInstance(pdf, outputStream);

        // 设置中文字体 - 使用iText的BaseFont
        Map<String, com.itextpdf.text.pdf.BaseFont> fontMap = new HashMap<>();
        com.itextpdf.text.pdf.BaseFont defaultFont = createChineseFont(fontMap, "宋体");

        pdf.open();

        try {
            // 关键改进：使用 getBodyElements() 获取所有元素的原始顺序
            List<org.apache.poi.xwpf.usermodel.IBodyElement> bodyElements = docx.getBodyElements();

            if (bodyElements.isEmpty()) {
                pdf.close();
                return convertWithAutoMode(docx, outputStream);
            }

            // 按文档原始顺序遍历所有元素
            for (org.apache.poi.xwpf.usermodel.IBodyElement element : bodyElements) {

                if (element instanceof org.apache.poi.xwpf.usermodel.XWPFParagraph) {
                    // 处理段落（标题、正文等）
                    XWPFParagraph paragraph = (XWPFParagraph) element;

                    // 跳过空段落（原文中的空行不需要转换）
                    String text = paragraph.getText();
                    if (text == null || text.trim().isEmpty()) {
                        continue;
                    }

                    // 创建PDF段落
                    com.itextpdf.text.Paragraph pdfPara = new com.itextpdf.text.Paragraph();

                    // 检测标题级别和样式
                    String style = paragraph.getStyle();
                    int fontSize = 12; // 默认字号
                    boolean isBold = false;
                    String fontFamily = null; // 字体族

                    if (style != null) {
                        if (style.contains("Heading1") || style.contains("标题1")) {
                            fontSize = 18;
                            isBold = true;
                        } else if (style.contains("Heading2") || style.contains("标题2")) {
                            fontSize = 16;
                            isBold = true;
                        } else if (style.contains("Heading3") || style.contains("标题3")) {
                            fontSize = 14;
                            isBold = true;
                        }
                    }

                    // 关键改进：逐个处理 Run，保留完整样式
                    List<org.apache.poi.xwpf.usermodel.XWPFRun> runs = paragraph.getRuns();

                    if (runs == null || runs.isEmpty()) {
                        // 有文本但没有 run：使用默认字体添加文本
                        com.itextpdf.text.Font defaultFontStyle = new com.itextpdf.text.Font(defaultFont, fontSize, com.itextpdf.text.Font.NORMAL);
                        pdfPara.add(new com.itextpdf.text.Chunk(text.trim(), defaultFontStyle));
                    } else {
                        // 遍历每个 run，保留各自的样式
                        for (org.apache.poi.xwpf.usermodel.XWPFRun run : runs) {
                            String runText = run.getText(0);
                            if (runText == null || runText.isEmpty()) {
                                continue;
                            }

                            // 提取 run 的样式
                            int runFontSize = fontSize; // 继承段落默认字号
                            if (run.getFontSize() > 0) {
                                runFontSize = run.getFontSize();
                            }

                            // 检测字体样式
                            int fontStyle = com.itextpdf.text.Font.NORMAL;
                            if (run.isBold() || isBold) {
                                fontStyle |= com.itextpdf.text.Font.BOLD;
                            }
                            if (run.isItalic()) {
                                fontStyle |= com.itextpdf.text.Font.ITALIC;
                            }

                            // 获取字体族
                            String runFontFamily = run.getFontFamily();
                            com.itextpdf.text.pdf.BaseFont runBaseFont = defaultFont;
                            if (runFontFamily != null && !runFontFamily.isEmpty()) {
                                runBaseFont = createChineseFont(fontMap, runFontFamily);
                                if (runBaseFont == null) {
                                    runBaseFont = defaultFont;
                                }
                            }

                            // 创建字体
                            com.itextpdf.text.Font runFont = new com.itextpdf.text.Font(runBaseFont, runFontSize, fontStyle);

                            // 设置字体颜色
                            String colorStr = run.getColor();
                            if (colorStr != null && !colorStr.isEmpty()) {
                                try {
                                    int rgb = Integer.parseInt(colorStr, 16);
                                    int r = (rgb >> 16) & 0xFF;
                                    int g = (rgb >> 8) & 0xFF;
                                    int b = rgb & 0xFF;
                                    runFont.setColor(r, g, b);
                                } catch (NumberFormatException e) {
                                    // 忽略颜色解析错误
                                }
                            }

                            // 创建 Chunk 并添加到段落
                            com.itextpdf.text.Chunk chunk = new com.itextpdf.text.Chunk(runText, runFont);

                            // 处理下划线 - 需要更严格的检查
                            try {
                                // 通过反射获取底层XML属性来准确判断是否有下划线
                                java.lang.reflect.Method getCTRMethod = run.getClass().getMethod("getCTR");
                                Object ctr = getCTRMethod.invoke(run);
                                if (ctr != null) {
                                    java.lang.reflect.Method getRPrMethod = ctr.getClass().getMethod("getRPr");
                                    Object rpr = getRPrMethod.invoke(ctr);
                                    if (rpr != null) {
                                        java.lang.reflect.Method getUMethod = rpr.getClass().getMethod("getU");
                                        Object u = getUMethod.invoke(rpr);
                                        // 只有当u不为null且val不为NONE时才添加下划线
                                        if (u != null) {
                                            java.lang.reflect.Method getValMethod = u.getClass().getMethod("getVal");
                                            Object val = getValMethod.invoke(u);
                                            if (val != null && !val.toString().contains("NONE")) {
                                                chunk.setUnderline(0.5f, -2f);
                                            }
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                // 忽略下划线处理错误
                            }

                            // 处理删除线
                            if (run.isStrikeThrough()) {
                                chunk.setUnderline(0.5f, 3f); // 使用上划线模拟删除线
                            }

                            // 处理上标/下标
                            try {
                                // 通过反射获取垂直对齐信息
                                java.lang.reflect.Method getCTRMethod = run.getClass().getMethod("getCTR");
                                Object ctr = getCTRMethod.invoke(run);
                                if (ctr != null) {
                                    java.lang.reflect.Method getRPrMethod = ctr.getClass().getMethod("getRPr");
                                    Object rpr = getRPrMethod.invoke(ctr);
                                    if (rpr != null) {
                                        java.lang.reflect.Method getVertAlignMethod = rpr.getClass().getMethod("getVertAlign");
                                        Object vertAlign = getVertAlignMethod.invoke(rpr);
                                        if (vertAlign != null) {
                                            String vertAlignStr = vertAlign.toString();
                                            if (vertAlignStr.contains("SUPERSCRIPT")) {
                                                chunk.setTextRise(6f); // 上标
                                            } else if (vertAlignStr.contains("SUBSCRIPT")) {
                                                chunk.setTextRise(-3f); // 下标
                                            }
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                // 忽略上标/下标处理错误
                            }

                            pdfPara.add(chunk);
                        }
                    }

                    // 设置段落对齐方式
                    applyParagraphAlignment(pdfPara, paragraph);

                    // 设置段落间距（从样式中读取）
                    applyParagraphSpacing(pdfPara, paragraph);

                    // 设置首行缩进（如果有）
                    applyParagraphIndentation(pdfPara, paragraph);

                    pdf.add(pdfPara);

                } else if (element instanceof XWPFTable) {
                    // 处理表格
                    XWPFTable table = (XWPFTable) element;

                    logger.debug("检测到表格元素");

                    // 使用我们的工具类转换表格
                    com.itextpdf.text.pdf.PdfPTable pdfTable =
                        DocxTableToPdfUtils.convertXWPFTableToPdf(table, defaultFont);

                    if (pdfTable != null) {
                        logger.debug("表格转换成功，添加到PDF");
                        pdf.add(pdfTable);
                    } else {
                        logger.warn("表格转换失败，返回null");
                    }
                }
            }

            pdf.close();
            return true;

        } catch (Exception e) {
            if (pdf.isOpen()) {
                pdf.close();
            }
            throw e;
        }
    }

    /**
     * 方案B：完全自动转换（回退方案）
     * 优点：简单，保留所有格式
     * 缺点：表格可能失真
     */
    private static boolean convertWithAutoMode(XWPFDocument docx, OutputStream outputStream) throws Exception {
        PdfOptions options = PdfOptions.create();

        Map<String, BaseFont> fontMap = new HashMap<>();

        // 配置中文字体
        options.fontProvider((familyName, encoding, size, style, color) -> {
            try {
                BaseFont bfChinese = fontMap.get(familyName);
                if (bfChinese == null) {
                    String lowerFamily = familyName != null ? familyName.toLowerCase() : "";

                    if (lowerFamily.contains("fangsong") || lowerFamily.contains("仿宋")) {
                        bfChinese = BaseFont.createFont("simfang.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                    } else if (lowerFamily.contains("simsun") || lowerFamily.contains("宋体") || lowerFamily.contains("serif")) {
                        bfChinese = BaseFont.createFont("simsun.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                    } else if (lowerFamily.contains("kaiti") || lowerFamily.contains("楷体")) {
                        bfChinese = BaseFont.createFont("simkai.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                    } else if (lowerFamily.contains("hei") || lowerFamily.contains("黑体") || lowerFamily.contains("sans")) {
                        bfChinese = BaseFont.createFont("simhei.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                    } else {
                        bfChinese = BaseFont.createFont("simsun.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                    }
                    fontMap.put(familyName != null ? familyName : "default", bfChinese);
                }

                Font fontChinese = new Font(bfChinese, size, style, color);
                if (familyName != null) {
                    fontChinese.setFamily(familyName);
                }
                return fontChinese;
            } catch (Exception e) {
                logger.error("字体加载失败: {}", e.getMessage(), e);
                return null;
            }
        });

        PdfConverter.getInstance().convert(docx, outputStream, options);
        return true;
    }

    /**
     * 应用段落对齐方式
     */
    private static void applyParagraphAlignment(com.itextpdf.text.Paragraph pdfPara, XWPFParagraph paragraph) {
        try {
            org.apache.poi.xwpf.usermodel.ParagraphAlignment alignment = paragraph.getAlignment();

            if (alignment != null) {
                switch (alignment) {
                    case CENTER:
                        pdfPara.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                        break;
                    case RIGHT:
                        pdfPara.setAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
                        break;
                    case BOTH:
                        pdfPara.setAlignment(com.itextpdf.text.Element.ALIGN_JUSTIFIED);
                        break;
                    case LEFT:
                    default:
                        pdfPara.setAlignment(com.itextpdf.text.Element.ALIGN_LEFT);
                        break;
                }
            }
        } catch (Exception e) {
            logger.debug("应用段落对齐失败: {}", e.getMessage());
        }
    }

    /**
     * 应用段落间距
     */
    private static void applyParagraphSpacing(com.itextpdf.text.Paragraph pdfPara, XWPFParagraph paragraph) {
        try {
            // 获取段前间距（单位：twips，1 twip = 1/20 point）
            int spacingBefore = paragraph.getSpacingBefore();
            if (spacingBefore > 0) {
                pdfPara.setSpacingBefore(spacingBefore / 20f);
            }

            // 获取段后间距
            int spacingAfter = paragraph.getSpacingAfter();
            if (spacingAfter > 0) {
                pdfPara.setSpacingAfter(spacingAfter / 20f);
            }

            // 获取行间距（返回 double 类型）
            double lineSpacing = paragraph.getSpacingBetween();
            if (lineSpacing > 0) {
                pdfPara.setLeading((float)(lineSpacing * 1.5)); // 1.5倍行距
            }
        } catch (Exception e) {
            logger.debug("应用段落间距失败: {}", e.getMessage());
        }
    }

    /**
     * 应用段落缩进
     */
    private static void applyParagraphIndentation(com.itextpdf.text.Paragraph pdfPara, XWPFParagraph paragraph) {
        try {
            // 获取首行缩进
            int firstLineIndent = paragraph.getIndentationFirstLine();
            if (firstLineIndent > 0) {
                // 转换为点（twips to points）
                pdfPara.setFirstLineIndent(firstLineIndent / 20f);
            }

            // 获取左缩进
            int leftIndent = paragraph.getIndentationLeft();
            if (leftIndent > 0) {
                pdfPara.setIndentationLeft(leftIndent / 20f);
            }

            // 获取右缩进
            int rightIndent = paragraph.getIndentationRight();
            if (rightIndent > 0) {
                pdfPara.setIndentationRight(rightIndent / 20f);
            }
        } catch (Exception e) {
            logger.debug("应用段落缩进失败: {}", e.getMessage());
        }
    }

    /**
     * 创建中文字体 - 返回iText的BaseFont
     */
    private static com.itextpdf.text.pdf.BaseFont createChineseFont(
        Map<String, com.itextpdf.text.pdf.BaseFont> fontMap, String fontFamily) {
        try {
            com.itextpdf.text.pdf.BaseFont bf = fontMap.get(fontFamily);
            if (bf == null) {
                if ("宋体".equals(fontFamily)) {
                    bf = com.itextpdf.text.pdf.BaseFont.createFont("simsun.ttf",
                        com.itextpdf.text.pdf.BaseFont.IDENTITY_H,
                        com.itextpdf.text.pdf.BaseFont.NOT_EMBEDDED);
                } else if ("黑体".equals(fontFamily)) {
                    bf = com.itextpdf.text.pdf.BaseFont.createFont("simhei.ttf",
                        com.itextpdf.text.pdf.BaseFont.IDENTITY_H,
                        com.itextpdf.text.pdf.BaseFont.NOT_EMBEDDED);
                } else if ("楷体".equals(fontFamily)) {
                    bf = com.itextpdf.text.pdf.BaseFont.createFont("simkai.ttf",
                        com.itextpdf.text.pdf.BaseFont.IDENTITY_H,
                        com.itextpdf.text.pdf.BaseFont.NOT_EMBEDDED);
                } else if ("仿宋".equals(fontFamily)) {
                    bf = com.itextpdf.text.pdf.BaseFont.createFont("simfang.ttf",
                        com.itextpdf.text.pdf.BaseFont.IDENTITY_H,
                        com.itextpdf.text.pdf.BaseFont.NOT_EMBEDDED);
                } else {
                    bf = com.itextpdf.text.pdf.BaseFont.createFont("simsun.ttf",
                        com.itextpdf.text.pdf.BaseFont.IDENTITY_H,
                        com.itextpdf.text.pdf.BaseFont.NOT_EMBEDDED);
                }
                fontMap.put(fontFamily, bf);
            }
            return bf;
        } catch (Exception e) {
            logger.error("创建字体失败: {}", e.getMessage(), e);
            return null;
        }
    }
}
