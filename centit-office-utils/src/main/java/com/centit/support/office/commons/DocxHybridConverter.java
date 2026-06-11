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
 * 提供多种DOCX到PDF转换模式，满足不同场景需求
 *
 * 支持的转换模式：
 * 1. 自动模式（convert）：使用fr.opensagres转换器，支持所有复杂特性，推荐默认使用
 * 2. 手动模式（convertWithManualMode）：自定义表格转换，表格保真度最高，但不支持复杂特性
 * 3. 智能模式（convertWithSmartMode）：自动检测文档特性并选择最佳转换模式
 *
 * 使用建议：
 * - 一般情况：使用convert()（自动模式）
 * - 纯表格文档：使用convertWithManualMode()（手动模式）
 * - 不确定文档类型：使用convertWithSmartMode()（智能模式）
 *
 * @author zhf
 */
public class DocxHybridConverter {

    private static final Logger logger = LoggerFactory.getLogger(DocxHybridConverter.class);

    /**
     * 转换DOCX到PDF（使用自动模式）
     * 自动模式使用fr.opensagres转换器，支持所有复杂特性（图片、嵌套表格、页眉页脚等）
     *
     * @param docx         DOCX文档
     * @param outputStream PDF输出流
     * @return 是否成功
     */
    public static boolean convert(XWPFDocument docx, OutputStream outputStream) {
        try {
            logger.info("使用自动模式转换DOCX到PDF");
            return convertWithAutoMode(docx, outputStream);
        } catch (Exception e) {
            logger.error("自动模式转换失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 转换DOCX到PDF（使用手动模式）
     * 手动模式使用自定义表格转换，表格保真度最高，但不支持复杂特性
     * 适用于：简单文档、主要包含表格的文档、对表格质量要求高的场景
     *
     * 注意：手动模式不支持以下特性：
     * - 图片
     * - 嵌套表格
     * - 页眉页脚
     * - 多分节
     * - 文本框
     * - 形状/SmartArt
     * - 水印
     * - 脚注/尾注
     * - 目录
     *
     * 如果文档包含以上特性，建议使用convert()（自动模式）
     *
     * @param docx         DOCX文档
     * @param outputStream PDF输出流
     * @return 是否成功
     */
    public static boolean convertWithManualMode(XWPFDocument docx, OutputStream outputStream) {
        try {
            logger.info("使用手动模式转换DOCX到PDF");
            return convertWithManualTables(docx, outputStream);
        } catch (Exception e) {
            logger.error("手动模式转换失败，回退到自动模式: {}", e.getMessage(), e);
            try {
                return convertWithAutoMode(docx, outputStream);
            } catch (Exception ex) {
                logger.error("自动模式转换也失败: {}", ex.getMessage(), ex);
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
        // 从 docx 读取页面尺寸和方向（含横向 landscape 支持），并按 docx 实际页边距构造 PDF 页面
        com.itextpdf.text.Rectangle pageSize = resolvePageSize(docx);
        float[] margins = resolvePageMargins(docx);
        Document pdf = new Document(pageSize, margins[0], margins[1], margins[2], margins[3]);
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
            for (int i = 0; i < bodyElements.size(); i++) {
                org.apache.poi.xwpf.usermodel.IBodyElement element = bodyElements.get(i);

                // 检查下一个元素是否是表格
                boolean nextIsTable = false;
                if (i + 1 < bodyElements.size()) {
                    nextIsTable = bodyElements.get(i + 1) instanceof XWPFTable;
                }

                if (element instanceof org.apache.poi.xwpf.usermodel.XWPFParagraph) {
                    // 处理段落（标题、正文等）
                    XWPFParagraph paragraph = (XWPFParagraph) element;

                    // 创建PDF段落
                    com.itextpdf.text.Paragraph pdfPara = new com.itextpdf.text.Paragraph();

                    // 获取段落文本
                    String text = paragraph.getText();

                    // 修复空行转换问题：不跳过空段落，而是保留空行以保持文档布局
                    // 空段落在Word中用于控制间距和布局，应该被保留
                    if (text != null && !text.trim().isEmpty()) {
                        // 有文本的段落，按原逻辑处理

                        // 检测标题级别和样式
                        String style = paragraph.getStyle();
                        int fontSize = 12; // 默认字号
                        boolean isBold = false;

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
                                // 提取 run 的样式
                                float runFontSize = fontSize; // 继承段落默认字号
                                if (run.getFontSizeAsDouble() != null) {
                                    runFontSize = run.getFontSizeAsDouble().floatValue();
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

                                // 通过反射获取下划线、删除线、上下标等属性
                                boolean hasUnderline = false;
                                boolean hasStrikeThrough = run.isStrikeThrough();
                                String vertAlignStr = null;
                                try {
                                    java.lang.reflect.Method getCTRMethod = run.getClass().getMethod("getCTR");
                                    Object ctr = getCTRMethod.invoke(run);
                                    if (ctr != null) {
                                        java.lang.reflect.Method getRPrMethod = ctr.getClass().getMethod("getRPr");
                                        Object rpr = getRPrMethod.invoke(ctr);
                                        if (rpr != null) {
                                            java.lang.reflect.Method getUMethod = rpr.getClass().getMethod("getU");
                                            Object u = getUMethod.invoke(rpr);
                                            if (u != null) {
                                                java.lang.reflect.Method getValMethod = u.getClass().getMethod("getVal");
                                                Object val = getValMethod.invoke(u);
                                                if (val != null && !val.toString().contains("NONE")) {
                                                    hasUnderline = true;
                                                }
                                            }
                                            java.lang.reflect.Method getVertAlignMethod = rpr.getClass().getMethod("getVertAlign");
                                            Object vertAlign = getVertAlignMethod.invoke(rpr);
                                            if (vertAlign != null) {
                                                vertAlignStr = vertAlign.toString();
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    // 忽略属性解析错误
                                }

                                // 遍历 run 的 XML 子节点，按原始顺序处理文本和换行符
                                try {
                                    org.w3c.dom.Node runNode = run.getCTR().getDomNode();
                                    org.w3c.dom.NodeList children = runNode.getChildNodes();
                                    boolean hasContent = false;

                                    for (int childIdx = 0; childIdx < children.getLength(); childIdx++) {
                                        org.w3c.dom.Node child = children.item(childIdx);
                                        if (child.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
                                            continue;
                                        }
                                        String localName = child.getLocalName();
                                        if ("t".equals(localName) || "delText".equals(localName)) {
                                            // 文本节点
                                            String runText = child.getTextContent();
                                            if (runText == null || runText.isEmpty()) {
                                                continue;
                                            }
                                            hasContent = true;
                                            com.itextpdf.text.Chunk chunk = new com.itextpdf.text.Chunk(runText, runFont);
                                            if (hasUnderline) {
                                                chunk.setUnderline(0.5f, -2f);
                                            }
                                            if (hasStrikeThrough) {
                                                chunk.setUnderline(0.5f, 3f);
                                            }
                                            if (vertAlignStr != null) {
                                                if (vertAlignStr.contains("SUPERSCRIPT")) {
                                                    chunk.setTextRise(6f);
                                                } else if (vertAlignStr.contains("SUBSCRIPT")) {
                                                    chunk.setTextRise(-3f);
                                                }
                                            }
                                            pdfPara.add(chunk);
                                        } else if ("br".equals(localName)) {
                                            // 行内换行符
                                            hasContent = true;
                                            pdfPara.add(new com.itextpdf.text.Chunk("\n", runFont));
                                        }
                                    }

                                    if (!hasContent) {
                                        // 回退：使用 getText(0) 获取文本
                                        String runText = run.getText(0);
                                        if (runText != null && !runText.isEmpty()) {
                                            com.itextpdf.text.Chunk chunk = new com.itextpdf.text.Chunk(runText, runFont);
                                            if (hasUnderline) {
                                                chunk.setUnderline(0.5f, -2f);
                                            }
                                            if (hasStrikeThrough) {
                                                chunk.setUnderline(0.5f, 3f);
                                            }
                                            if (vertAlignStr != null) {
                                                if (vertAlignStr.contains("SUPERSCRIPT")) {
                                                    chunk.setTextRise(6f);
                                                } else if (vertAlignStr.contains("SUBSCRIPT")) {
                                                    chunk.setTextRise(-3f);
                                                }
                                            }
                                            pdfPara.add(chunk);
                                        }
                                    }
                                } catch (Exception e) {
                                    // 回退：使用 getText(0) 获取文本
                                    String runText = run.getText(0);
                                    if (runText != null && !runText.isEmpty()) {
                                        com.itextpdf.text.Chunk chunk = new com.itextpdf.text.Chunk(runText, runFont);
                                        if (hasUnderline) {
                                            chunk.setUnderline(0.5f, -2f);
                                        }
                                        if (hasStrikeThrough) {
                                            chunk.setUnderline(0.5f, 3f);
                                        }
                                        if (vertAlignStr != null) {
                                            if (vertAlignStr.contains("SUPERSCRIPT")) {
                                                chunk.setTextRise(6f);
                                            } else if (vertAlignStr.contains("SUBSCRIPT")) {
                                                chunk.setTextRise(-3f);
                                            }
                                        }
                                        pdfPara.add(chunk);
                                    }
                                }
                            }
                        }
                    } // end of if (text != null && !text.trim().isEmpty())

                    // 修复空段落转换问题：为空段落添加不可见内容以确保有高度
                    boolean isEmptyParagraph = (text == null || text.trim().isEmpty());
                    if (isEmptyParagraph) {
                        // 添加一个不可见的零宽度空格，确保段落有内容
                        com.itextpdf.text.Font invisibleFont = new com.itextpdf.text.Font(defaultFont, 1, com.itextpdf.text.Font.NORMAL);
                        invisibleFont.setColor(new com.itextpdf.text.BaseColor(255, 255, 255));
                        com.itextpdf.text.Chunk invisibleChunk = new com.itextpdf.text.Chunk(" ", invisibleFont);
                        pdfPara.add(invisibleChunk);
                    }

                    // 设置段落对齐方式
                    applyParagraphAlignment(pdfPara, paragraph);

                    // 设置段落间距（从样式中读取）
                    applyParagraphSpacing(pdfPara, paragraph, isEmptyParagraph);

                    // 设置首行缩进（如果有）
                    applyParagraphIndentation(pdfPara, paragraph);

                    // 关键修复：如果段落下一个是表格，确保有足够的间距避免重叠
                    if (nextIsTable) {
                        float currentSpacingAfter = pdfPara.getSpacingAfter();
                        // 确保段后间距至少为10pt，如果当前间距太小则增加
                        if (currentSpacingAfter < 8f) {
                            pdfPara.setSpacingAfter(10f);
                        }
                    }

                    pdf.add(pdfPara);

                } else if (element instanceof XWPFTable) {
                    // 处理表格
                    XWPFTable table = (XWPFTable) element;

                    // 使用我们的工具类转换表格
                    com.itextpdf.text.pdf.PdfPTable pdfTable =
                        DocxTableToPdfUtils.convertXWPFTableToPdf(table, defaultFont);

                    if (pdfTable != null) {
                        // 关键修复：为表格添加前导间距，避免与上面的文本重叠
                        pdfTable.setSpacingBefore(8f);

                        pdf.add(pdfTable);
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
            // 忽略对齐应用失败
        }
    }

    /**
     * 应用段落间距
     */
    private static void applyParagraphSpacing(com.itextpdf.text.Paragraph pdfPara, XWPFParagraph paragraph, boolean isEmptyParagraph) {
        try {
            // 获取段前间距（单位：twips，1 twip = 1/20 point）
            int spacingBefore = paragraph.getSpacingBefore();
            if (spacingBefore > 0) {
                pdfPara.setSpacingBefore(spacingBefore / 20f);
            }

            // 获取段后间距
            int spacingAfter = paragraph.getSpacingAfter();

            // 获取段落的实际字体大小（用于计算行间距）
            float actualFontSize = getParagraphFontSize(paragraph);

            // 检查是否是标题
            String style = paragraph.getStyle();
            boolean isHeading = false;
            if (style != null) {
                isHeading = style.contains("Heading") || style.contains("标题");
            }

            // 对于标题，确保有足够的段后间距避免重叠
            if (isHeading) {
                // 标题默认需要更大的段后间距
                float headingSpacingAfter = spacingAfter > 0 ? spacingAfter / 20f : 0f;
                if (headingSpacingAfter < 12f) {
                    // 如果段后间距太小，设置最小值为12pt
                    headingSpacingAfter = 12f;
                }
                pdfPara.setSpacingAfter(headingSpacingAfter);
            } else {
                // 非标题段落的段后间距
                if (spacingAfter > 0) {
                    pdfPara.setSpacingAfter(spacingAfter / 20f);
                }
            }

            // 获取行间距
            // Word中的行间距有多种类型：单倍、1.5倍、双倍、最小值、固定值等
            // 需要通过底层XML来准确读取
            float leading;
            if (isEmptyParagraph) {
                // 空段落使用固定的行间距，确保有可见高度
                leading = 12f; // 12pt的行间距
            } else {
                leading = calculateLeading(paragraph, actualFontSize, isHeading);
            }
            pdfPara.setLeading(leading);
        } catch (Exception e) {
            // 忽略间距应用失败
        }
    }

    /**
     * 计算段落行间距
     * 读取Word文档的行间距设置并转换为PDF的leading值
     */
    private static float calculateLeading(XWPFParagraph paragraph, float fontSize, boolean isHeading) {
        try {
            // 通过底层XML获取行间距设置
            String xmlText = paragraph.getCTP().toString();

            // 查找 w:spacing 元素
            java.util.regex.Pattern spacingPattern = java.util.regex.Pattern.compile("<w:spacing[^>]*/>");
            java.util.regex.Matcher spacingMatcher = spacingPattern.matcher(xmlText);

            if (spacingMatcher.find()) {
                String spacingTag = spacingMatcher.group();

                // 获取行间距类型 w:lineRule
                String lineRule = "auto"; // 默认自动
                java.util.regex.Pattern lineRulePattern = java.util.regex.Pattern.compile("w:lineRule=\"([^\"]+)\"");
                java.util.regex.Matcher lineRuleMatcher = lineRulePattern.matcher(spacingTag);
                if (lineRuleMatcher.find()) {
                    lineRule = lineRuleMatcher.group(1);
                }

                // 获取行间距值 w:line
                int lineValue = 240; // 默认单倍行距（240 twips）
                java.util.regex.Pattern linePattern = java.util.regex.Pattern.compile("w:line=\"([0-9]+)\"");
                java.util.regex.Matcher lineMatcher = linePattern.matcher(spacingTag);
                if (lineMatcher.find()) {
                    lineValue = Integer.parseInt(lineMatcher.group(1));
                }

                // 根据不同的lineRule计算leading
                if ("auto".equalsIgnoreCase(lineRule) || lineRule.isEmpty()) {
                    // 自动行距：lineValue单位是1/240行，240表示单倍行距
                    // 转换为point：lineValue / 240 * fontSize
                    return (lineValue / 240.0f) * fontSize;
                } else if ("atLeast".equalsIgnoreCase(lineRule)) {
                    // 最小值：lineValue单位是twips（1/20 point）
                    // 取lineValue/20和fontSize的较大值
                    float minLeading = lineValue / 20.0f;
                    return Math.max(fontSize, minLeading);
                } else if ("exact".equalsIgnoreCase(lineRule)) {
                    // 固定值：lineValue单位是twips（1/20 point）
                    return lineValue / 20.0f;
                }
            }

            // 如果没有找到spacing设置，使用默认值
            // Word的默认行间距通常是单倍（1.0）
            return fontSize;
        } catch (Exception e) {
            // 出错时使用默认值
            return fontSize;
        }
    }

    /**
     * 获取段落的实际字体大小
     */
    private static float getParagraphFontSize(XWPFParagraph paragraph) {
        try {
            // 首先检查段落样式
            String style = paragraph.getStyle();
            int styleFontSize = 12; // 默认字号

            if (style != null) {
                if (style.contains("Heading1") || style.contains("标题1")) {
                    styleFontSize = 18;
                } else if (style.contains("Heading2") || style.contains("标题2")) {
                    styleFontSize = 16;
                } else if (style.contains("Heading3") || style.contains("标题3")) {
                    styleFontSize = 14;
                }
            }

            // 然后检查run中的字体大小（优先使用run的设置）
            List<org.apache.poi.xwpf.usermodel.XWPFRun> runs = paragraph.getRuns();
            if (runs != null && !runs.isEmpty()) {
                for (org.apache.poi.xwpf.usermodel.XWPFRun run : runs) {
                    Double runFontSize = run.getFontSizeAsDouble();
                    if (runFontSize != null && runFontSize > 0) {
                        return runFontSize.floatValue();
                    }
                }
            }

            // 使用样式中的字体大小
            return styleFontSize;
        } catch (Exception e) {
            return 12f; // 出错时返回默认值
        }
    }

    /**
     * 应用段落缩进
     */
    private static void applyParagraphIndentation(com.itextpdf.text.Paragraph pdfPara, XWPFParagraph paragraph) {
        try {
            // 获取首行缩进（twips to points）
            // 需要处理负值（悬挂缩进）和正值（首行缩进）
            int firstLineIndent = paragraph.getIndentationFirstLine();
            if (firstLineIndent != 0) {
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
            // 忽略缩进应用失败
        }
    }

    /**
     * 从 docx 读取页面尺寸（含横向 landscape 支持）。
     * 如果 docx 未指定页面尺寸或读取失败，回退到 A4 竖向。
     */
    private static com.itextpdf.text.Rectangle resolvePageSize(XWPFDocument docx) {
        try {
            org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr sectPr =
                docx.getDocument().getBody().getSectPr();
            if (sectPr != null && sectPr.isSetPgSz()) {
                org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageSz pgSz = sectPr.getPgSz();
                float widthPt = pgSz.getW() != null ? ((Number) pgSz.getW()).floatValue() / 20f : 0f;
                float heightPt = pgSz.getH() != null ? ((Number) pgSz.getH()).floatValue() / 20f : 0f;

                // 显式横向：交换宽高（docx 的 w/h 在 landscape 模式下未必已交换）
                boolean isLandscape = false;
                if (pgSz.isSetOrient()) {
                    isLandscape = pgSz.getOrient().toString().equalsIgnoreCase("landscape");
                }

                if (widthPt > 0 && heightPt > 0) {
                    if (isLandscape && widthPt < heightPt) {
                        float tmp = widthPt;
                        widthPt = heightPt;
                        heightPt = tmp;
                    }
                    // 使用docx页面尺寸
                    return new com.itextpdf.text.Rectangle(widthPt, heightPt);
                }
            }
        } catch (Exception e) {
            // 使用默认A4页面
        }
        return PageSize.A4;
    }

    /**
     * 从 docx 读取页边距（top, bottom, left, right），单位 pt。
     * 失败时回退到默认 36pt。
     */
    private static float[] resolvePageMargins(XWPFDocument docx) {
        float left = 36f, right = 36f, top = 36f, bottom = 36f;
        try {
            org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr sectPr =
                docx.getDocument().getBody().getSectPr();
            if (sectPr != null && sectPr.isSetPgMar()) {
                org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageMar pgMar = sectPr.getPgMar();
                if (pgMar.getLeft() != null) left = ((Number) pgMar.getLeft()).floatValue() / 20f;
                if (pgMar.getRight() != null) right = ((Number) pgMar.getRight()).floatValue() / 20f;
                if (pgMar.getTop() != null) top = ((Number) pgMar.getTop()).floatValue() / 20f;
                if (pgMar.getBottom() != null) bottom = ((Number) pgMar.getBottom()).floatValue() / 20f;
            }
        } catch (Exception e) {
            // 使用默认页边距
        }
        // Document 构造参数顺序：left, right, top, bottom
        return new float[]{left, right, top, bottom};
    }

    /**
     * 创建中文字体 - 返回iText的BaseFont
     * 永不返回null，如果创建失败会尝试使用系统字体
     */
    private static com.itextpdf.text.pdf.BaseFont createChineseFont(
        Map<String, com.itextpdf.text.pdf.BaseFont> fontMap, String fontFamily) {
        try {
            com.itextpdf.text.pdf.BaseFont bf = fontMap.get(fontFamily);
            if (bf == null) {
                // 尝试创建字体
                try {
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
                } catch (Exception e) {
                    // 如果字体文件找不到，尝试使用内置字体
                    logger.warn("字体文件 '" + fontFamily + "' 未找到，尝试使用STSong-Light");
                    try {
                        bf = com.itextpdf.text.pdf.BaseFont.createFont("STSong-Light",
                            "UniGB-UCS2-H",
                            com.itextpdf.text.pdf.BaseFont.NOT_EMBEDDED);
                    } catch (Exception e2) {
                        // 最后回退到Helvetica（不支持中文但不会崩溃）
                        logger.error("无法加载中文字体，使用Helvetica代替，中文可能显示为方框");
                        bf = com.itextpdf.text.pdf.BaseFont.createFont(
                            com.itextpdf.text.pdf.BaseFont.HELVETICA,
                            com.itextpdf.text.pdf.BaseFont.WINANSI,
                            com.itextpdf.text.pdf.BaseFont.EMBEDDED);
                    }
                }
                fontMap.put(fontFamily, bf);
            }
            // 确保永不返回null
            if (bf == null) {
                logger.error("字体创建返回null，使用Helvetica作为紧急回退");
                bf = com.itextpdf.text.pdf.BaseFont.createFont(
                    com.itextpdf.text.pdf.BaseFont.HELVETICA,
                    com.itextpdf.text.pdf.BaseFont.WINANSI,
                    com.itextpdf.text.pdf.BaseFont.EMBEDDED);
            }
            return bf;
        } catch (Exception e) {
            logger.error("创建字体失败: {}", e.getMessage(), e);
            // 紧急回退：使用系统默认字体
            try {
                return com.itextpdf.text.pdf.BaseFont.createFont(
                    com.itextpdf.text.pdf.BaseFont.HELVETICA,
                    com.itextpdf.text.pdf.BaseFont.WINANSI,
                    com.itextpdf.text.pdf.BaseFont.EMBEDDED);
            } catch (Exception e2) {
                logger.error("紧急回退也失败，这不应该发生");
                return null; // 只有在极端情况下才会返回null
            }
        }
    }
}
