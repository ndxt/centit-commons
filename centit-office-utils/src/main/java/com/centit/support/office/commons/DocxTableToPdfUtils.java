package com.centit.support.office.commons;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTDecimalNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * DOCX表格转PDF高级工具类
 * 专门处理DOCX格式的表格，提供更好的保真度
 *
 * @author zhf
 */
public class DocxTableToPdfUtils {

    private static final Logger logger = LoggerFactory.getLogger(DocxTableToPdfUtils.class);

    /**
     * 将DOCX表格转换为PDF表格
     *
     * @param table    DOCX表格对象
     * @param baseFont 中文字体
     * @return PDF表格
     */
    public static PdfPTable convertXWPFTableToPdf(XWPFTable table, com.itextpdf.text.pdf.BaseFont baseFont) {
        if (table == null) {
            logger.warn("表格对象为null");
            return null;
        }
        try {
            // 获取行数和列数
            List<XWPFTableRow> rows = table.getRows();
            if (rows.isEmpty()) {
                logger.warn("表格没有行");
                return null;
            }
            int rowCount = rows.size();
            int colCount = getMaxColumnCount(rows);
            logger.debug("转换表格: {} 行, {} 列", rowCount, colCount);
            if (colCount == 0) {
                logger.warn("表格列数为0");
                return null;
            }
            // 计算列宽
            float[] columnWidths = calculateColumnWidths(table, colCount);
            // 创建PDF表格
            PdfPTable pdfTable = new PdfPTable(columnWidths);
            pdfTable.setWidthPercentage(100);
            pdfTable.setKeepTogether(false); // 允许跨页
            // 检测表头（仅用于样式标记，不使用setHeaderRows）
            int headerRows = detectHeaderRows(rows);
            logger.debug("表格信息: 总行数={}, 检测到表头行数={}", rowCount, headerRows);
            // 不使用setHeaderRows()，因为它可能导致表头内容不显示
            // 改为在转换时给表头行添加背景色来区分
            if (headerRows > 0) {
                logger.info("✓ 检测到表头: {} 行（将通过背景色标记）", headerRows);
            } else {
                logger.debug("未检测到表头");
            }
            // 转换每一行
            for (int i = 0; i < rowCount; i++) {
                XWPFTableRow row = rows.get(i);
                logger.debug("正在转换第 {} 行 / 共 {} 行", i + 1, rowCount);

                // 如果是表头行，添加特殊标记
                boolean isHeaderRow = (headerRows > 0 && i < headerRows);
                convertRowToPdf(row, pdfTable, baseFont, colCount, isHeaderRow);
            }
            logger.debug("表格转换完成: 总共添加了 {} 行", rowCount);
            return pdfTable;

        } catch (Exception e) {
            logger.error("转换DOCX表格时出错: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取最大列数（考虑合并单元格）
     */
    private static int getMaxColumnCount(List<XWPFTableRow> rows) {
        int maxCols = 0;
        for (XWPFTableRow row : rows) {
            int cellCount = row.getTableCells().size();
            // 考虑colSpan
            for (XWPFTableCell cell : row.getTableCells()) {
                CTDecimalNumber gridSpan = cell.getCTTc().getTcPr().getGridSpan();
                if (gridSpan != null && gridSpan.getVal() != null) {
                    cellCount += gridSpan.getVal().intValue() - 1;
                }
            }
            maxCols = Math.max(maxCols, cellCount);
        }
        return maxCols;
    }

    /*
     * 计算列宽 - 基于DOCX表格的实际宽度设置
     * 优先级：单元格宽度比例 > 表格总宽度平均分配 > 内容估算 > 默认等宽
     */
    private static float[] calculateColumnWidths(XWPFTable table, int colCount) {
        float[] widths = new float[colCount];
        try {
            List<XWPFTableRow> rows = table.getRows();
            if (rows.isEmpty()) {
                Arrays.fill(widths, 1f);
                return widths;
            }
            // 获取表格总宽度（单位：twips，1 twip = 1/20 point）
            float totalTableWidthPt = 0f;
            try {
                org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblWidth tblW =
                    table.getCTTbl().getTblPr().getTblW();
                if (tblW != null && tblW.isSetW()) {
                    String type = tblW.getType() != null ? tblW.getType().toString() : "dxa";
                    int wVal = (int) tblW.getW();
                    if ("pct".equalsIgnoreCase(type)) {
                        // 百分比模式：wVal 的单位是 1/50%，即 5000 = 100%
                        float pageWidthPt = 525f;
                        totalTableWidthPt = pageWidthPt * wVal / 5000f;
                    } else if ("dxa".equalsIgnoreCase(type)) {
                        totalTableWidthPt = wVal / 20f;
                    }
                }
            } catch (Exception e) {
                logger.debug("获取表格宽度失败: {}", e.getMessage());
            }
            // 从第一行的单元格获取各列宽度
            XWPFTableRow firstRow = rows.get(0);
            List<XWPFTableCell> cells = firstRow.getTableCells();
            float[] cellWidths = new float[colCount];
            boolean hasCellWidths = false;
            for (int i = 0; i < Math.min(cells.size(), colCount); i++) {
                try {
                    XWPFTableCell cell = cells.get(i);
                    org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcPr tcPr =
                        cell.getCTTc().getTcPr();
                    if (tcPr != null) {
                        // 获取单元格宽度
                        Object tcW = tcPr.getTcW();
                        if (tcW != null) {
                            try {
                                java.lang.reflect.Method isSetWMethod = tcW.getClass().getMethod("isSetW");
                                Boolean isSetW = (Boolean) isSetWMethod.invoke(tcW);
                                if (Boolean.TRUE.equals(isSetW)) {
                                    java.lang.reflect.Method getTypeMethod = tcW.getClass().getMethod("getType");
                                    java.lang.reflect.Method getWMethod = tcW.getClass().getMethod("getW");
                                    Object typeObj = getTypeMethod.invoke(tcW);
                                    Object wObj = getWMethod.invoke(tcW);
                                    String wType = typeObj != null ? typeObj.toString() : "dxa";
                                    int wVal = wObj != null ? ((Number) wObj).intValue() : 0;
                                    if ("dxa".equalsIgnoreCase(wType)) {
                                        cellWidths[i] = wVal / 20f;
                                    } else if ("pct".equalsIgnoreCase(wType)) {
                                        cellWidths[i] = wVal / 50f;
                                    }
                                    hasCellWidths = true;
                                }
                            } catch (Exception ex) {
                                logger.debug("解析单元格 {} 宽度失败: {}", i, ex.getMessage());
                            }
                        }
                        // 考虑列合并（gridSpan）：将合并单元格的宽度均分给各逻辑列
                        org.openxmlformats.schemas.wordprocessingml.x2006.main.CTDecimalNumber gridSpan =
                            tcPr.getGridSpan();
                        if (gridSpan != null && gridSpan.getVal() != null && gridSpan.getVal().intValue() > 1) {
                            int span = gridSpan.getVal().intValue();
                            float widthPerCol = cellWidths[i] / span;
                            for (int j = 0; j < span && (i + j) < colCount; j++) {
                                cellWidths[i + j] = widthPerCol;
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.debug("获取单元格 {} 宽度失败: {}", i, e.getMessage());
                }
            }
            for (int i = cells.size(); i < colCount; i++) {
                cellWidths[i] = 0f;
            }
            // 情况1：有单元格宽度 → 按比例分配（不再依赖 totalTableWidthPt）
            if (hasCellWidths) {
                float sumCellWidths = 0f;
                for (float w : cellWidths) {
                    sumCellWidths += w;
                }
                if (sumCellWidths > 0) {
                    for (int i = 0; i < colCount; i++) {
                        widths[i] = cellWidths[i] > 0
                            ? cellWidths[i] / sumCellWidths
                            : 1f / colCount;
                    }
                    logger.debug("使用单元格宽度比例分配, {} 列", colCount);
                    return widths;
                }
            }
            // 情况2：有表格总宽度但无单元格宽度 → 平均分配
            if (totalTableWidthPt > 0) {
                Arrays.fill(widths, totalTableWidthPt / colCount);
                logger.debug("使用表格总宽度平均分配: {}pt / {} 列", totalTableWidthPt, colCount);
                return widths;
            }
            // 情况3：无宽度信息 → 根据内容估算（考虑中文字符宽度）
            for (int i = 0; i < Math.min(cells.size(), colCount); i++) {
                String text = cells.get(i).getText();
                float estimatedWidth = 0f;
                for (int c = 0; c < text.length(); c++) {
                    estimatedWidth += text.charAt(c) > 127 ? 12f : 6f;
                }
                widths[i] = Math.max(50f, Math.min(estimatedWidth, 300f));
            }
            for (int i = cells.size(); i < colCount; i++) {
                widths[i] = 50f;
            }
            logger.debug("使用文本长度估算列宽");
            return widths;
        } catch (Exception e) {
            logger.warn("计算列宽失败，使用默认值", e);
        }
        Arrays.fill(widths, 1f);
        return widths;
    }

    /**
     * 检测表头行数
     */
    private static int detectHeaderRows(List<XWPFTableRow> rows) {
        if (rows.isEmpty()) {
            return 0;
        }
        // 检查第一行是否加粗（检查所有单元格）
        XWPFTableRow firstRow = rows.get(0);
        List<XWPFTableCell> cells = firstRow.getTableCells();
        if (!cells.isEmpty()) {
            int boldCellCount = 0;
            int totalCheckedCells = 0;
            for (XWPFTableCell cell : cells) {
                totalCheckedCells++;
                List<XWPFParagraph> paragraphs = cell.getParagraphs();
                if (!paragraphs.isEmpty()) {
                    XWPFParagraph para = paragraphs.get(0);
                    List<XWPFRun> runs = para.getRuns();
                    // 检查该单元格是否有加粗文本
                    boolean cellIsBold = false;
                    if (runs != null && !runs.isEmpty()) {
                        for (XWPFRun run : runs) {
                            String text = run.getText(0);
                            boolean isBold = run.isBold();
                            logger.debug("单元格 {} Run: 文本='{}', 加粗={}", totalCheckedCells, text, isBold);
                            if (isBold) {
                                cellIsBold = true;
                                break;
                            }
                        }
                    } else {
                        logger.debug("单元格 {} 没有 Runs", totalCheckedCells);
                    }
                    if (cellIsBold) {
                        boldCellCount++;
                        logger.debug("单元格 {} 判定为加粗", totalCheckedCells);
                    }
                } else {
                    logger.debug("单元格 {} 没有段落", totalCheckedCells);
                }
            }
            logger.debug("表头检测结果: {}/{} 单元格加粗", boldCellCount, totalCheckedCells);
            // 改进的检测策略：
            // 1. 如果所有单元格都加粗，肯定是表头
            // 2. 如果超过一半单元格加粗，很可能是表头
            // 3. 如果只有第一个单元格加粗（常见于左侧标题列），也认为是表头
            boolean isHeader = false;
            if (boldCellCount == totalCheckedCells) {
                // 全部加粗 - 肯定是表头
                isHeader = true;
                logger.info("✓ 检测到表头（全部加粗）: {}/{} 单元格", boldCellCount, totalCheckedCells);
            } else if (boldCellCount > totalCheckedCells / 2) {
                // 超过一半加粗 - 很可能是表头
                isHeader = true;
                logger.info("✓ 检测到表头（多数加粗）: {}/{} 单元格", boldCellCount, totalCheckedCells);
            } else if (boldCellCount == 1 && totalCheckedCells >= 2) {
                // 检查是否是第一个单元格加粗（常见的左侧标题格式）
                XWPFTableCell firstCell = cells.get(0);
                boolean firstCellBold = false;
                List<XWPFParagraph> paras = firstCell.getParagraphs();
                if (!paras.isEmpty()) {
                    List<XWPFRun> runs = paras.get(0).getRuns();
                    if (runs != null && !runs.isEmpty()) {
                        for (XWPFRun run : runs) {
                            if (run.isBold()) {
                                firstCellBold = true;
                                break;
                            }
                        }
                    }
                }
                if (firstCellBold) {
                    isHeader = true;
                    logger.info("✓ 检测到表头（首单元格加粗）: {}/{} 单元格", boldCellCount, totalCheckedCells);
                }
            }

            if (isHeader) {
                return 1;
            } else {
                logger.debug("✗ 未满足表头条件: {}/{} 单元格加粗", boldCellCount, totalCheckedCells);
            }
        } else {
            logger.debug("第一行没有单元格");
        }
        return 0;
    }

    /**
     * 转换行到PDF
     */
    private static void convertRowToPdf(XWPFTableRow row, PdfPTable pdfTable, com.itextpdf.text.pdf.BaseFont baseFont, int expectedCols, boolean isHeaderRow) {
        List<XWPFTableCell> cells = row.getTableCells();
        int cellIndex = 0;

        for (int i = 0; i < expectedCols; i++) {
            XWPFTableCell cell = (cellIndex < cells.size()) ? cells.get(cellIndex) : null;
            if (cell == null) {
                // 空单元格
                PdfPCell pdfCell = createEmptyCell();
                pdfTable.addCell(pdfCell);
            } else {
                // 转换单元格
                PdfPCell pdfCell = convertCellToPdf(cell, baseFont, isHeaderRow);
                // 处理列合并
                int gridSpan = getGridSpan(cell);
                if (gridSpan > 1) {
                    pdfCell.setColspan(gridSpan);
                }
                pdfTable.addCell(pdfCell);
                cellIndex++;
            }
        }
        pdfTable.completeRow();
    }

    /**
     * 转换单元格到PDF
     */
    private static PdfPCell convertCellToPdf(XWPFTableCell cell, com.itextpdf.text.pdf.BaseFont baseFont) {
        return convertCellToPdf(cell, baseFont, false);
    }

    /**
     * 转换单元格到PDF
     */
    private static PdfPCell convertCellToPdf(XWPFTableCell cell, com.itextpdf.text.pdf.BaseFont baseFont, boolean isHeaderRow) {
        PdfPCell pdfCell = new PdfPCell();

        try {
            // 获取单元格文本
            String text = cell.getText();
            if (text == null || text.trim().isEmpty()) {
                pdfCell.setPhrase(new Phrase(" "));
            } else {
                // 创建包含样式的短语
                Phrase phrase = createStyledPhrase(cell, baseFont);
                pdfCell.setPhrase(phrase);
            }
            // 设置内边距
            pdfCell.setPadding(5f);
            // 设置边框
            applyBorders(pdfCell, cell);
            // 设置对齐方式
            applyAlignment(pdfCell, cell);
            // 设置背景色
            applyBackgroundColor(pdfCell, cell);
        } catch (Exception e) {
            logger.warn("转换单元格时出错", e);
            pdfCell.setPhrase(new Phrase(" "));
        }
        return pdfCell;
    }

    /**
     * 创建带样式的短语
     */
    private static Phrase createStyledPhrase(XWPFTableCell cell, com.itextpdf.text.pdf.BaseFont defaultBaseFont) {
        Phrase phrase = new Phrase();
        try {
            List<XWPFParagraph> paragraphs = cell.getParagraphs();
            for (int p = 0; p < paragraphs.size(); p++) {
                XWPFParagraph para = paragraphs.get(p);
                List<XWPFRun> runs = para.getRuns();
                if (runs != null && !runs.isEmpty()) {
                    // 遍历每个 run，保留各自的样式
                    for (XWPFRun run : runs) {
                        String runText = run.getText(0);
                        if (runText == null || runText.isEmpty()) {
                            continue;
                        }
                        // 提取字体大小
                        double fontSize = run.getFontSizeAsDouble();
                        if (fontSize <= 0.0) {
                            fontSize = 10.0; // 默认字号
                        }
                        // 检测字体样式
                        int fontStyle = Font.NORMAL;
                        if (run.isBold()) {
                            fontStyle |= Font.BOLD;
                        }
                        if (run.isItalic()) {
                            fontStyle |= Font.ITALIC;
                        }
                        // 获取字体族
                        String runFontFamily = run.getFontFamily();
                        com.itextpdf.text.pdf.BaseFont runBaseFont = defaultBaseFont;
                        if (runFontFamily != null && !runFontFamily.isEmpty()) {
                            runBaseFont = createChineseFontForTable(runFontFamily);
                            if (runBaseFont == null) {
                                runBaseFont = defaultBaseFont;
                            }
                        }
                        // 创建字体
                        Font font = new Font(runBaseFont,(float) fontSize, fontStyle);
                        // 设置字体颜色
                        String colorStr = run.getColor();
                        if (colorStr != null && !colorStr.isEmpty()) {
                            try {
                                int rgb = Integer.parseInt(colorStr, 16);
                                int r = (rgb >> 16) & 0xFF;
                                int g = (rgb >> 8) & 0xFF;
                                int b = rgb & 0xFF;
                                font.setColor(r, g, b);
                                logger.debug("设置字体颜色: {} -> RGB({},{},{})", colorStr, r, g, b);
                            } catch (NumberFormatException e) {
                                logger.debug("颜色解析失败: {}", colorStr);
                            }
                        }
                        // 创建 Chunk 并添加到短语
                        com.itextpdf.text.Chunk chunk = new com.itextpdf.text.Chunk(runText, font);
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
                            logger.warn("转换单元格时出错:" + e.getMessage(), e);
                            // 忽略下划线处理错误
                        }
                        // 处理删除线
                        if (run.isStrikeThrough()) {
                            chunk.setUnderline(0.5f, 3f); // 使用上划线模拟删除线
                        }
                        phrase.add(chunk);
                    }
                } else if (p > 0) {
                    // 在段落之间添加换行
                    phrase.add("\n");
                }
            }

            // 如果没有任何内容，添加空格
            if (phrase.isEmpty()) {
                phrase.add(" ");
            }

        } catch (Exception e) {
            logger.debug("创建带样式短语失败: {}", e.getMessage());
            // 回退到简单文本
            phrase.add(new com.itextpdf.text.Chunk(cell.getText(), new Font(defaultBaseFont, 10, Font.NORMAL)));
        }
        return phrase;
    }

    /**
     * 为表格创建中文字体
     */
    private static com.itextpdf.text.pdf.BaseFont createChineseFontForTable(String fontFamily) {
        try {
            if (fontFamily.contains("宋体") || fontFamily.contains("SimSun")) {
                return com.itextpdf.text.pdf.BaseFont.createFont("simsun.ttf",
                    com.itextpdf.text.pdf.BaseFont.IDENTITY_H,
                    com.itextpdf.text.pdf.BaseFont.NOT_EMBEDDED);
            } else if (fontFamily.contains("黑体") || fontFamily.contains("SimHei")) {
                return com.itextpdf.text.pdf.BaseFont.createFont("simhei.ttf",
                    com.itextpdf.text.pdf.BaseFont.IDENTITY_H,
                    com.itextpdf.text.pdf.BaseFont.NOT_EMBEDDED);
            } else if (fontFamily.contains("楷体") || fontFamily.contains("KaiTi")) {
                return com.itextpdf.text.pdf.BaseFont.createFont("simkai.ttf",
                    com.itextpdf.text.pdf.BaseFont.IDENTITY_H,
                    com.itextpdf.text.pdf.BaseFont.NOT_EMBEDDED);
            } else if (fontFamily.contains("仿宋") || fontFamily.contains("FangSong")) {
                return com.itextpdf.text.pdf.BaseFont.createFont("simfang.ttf",
                    com.itextpdf.text.pdf.BaseFont.IDENTITY_H,
                    com.itextpdf.text.pdf.BaseFont.NOT_EMBEDDED);
            } else {
                return com.itextpdf.text.pdf.BaseFont.createFont("simsun.ttf",
                    com.itextpdf.text.pdf.BaseFont.IDENTITY_H,
                    com.itextpdf.text.pdf.BaseFont.NOT_EMBEDDED);
            }
        } catch (Exception e) {
            logger.debug("创建字体失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 提取字体大小（已废弃，使用 createStyledPhrase 替代）
     */
    @Deprecated
    private static int extractFontSize(XWPFTableCell cell) {
        try {
            List<XWPFParagraph> paragraphs = cell.getParagraphs();
            if (!paragraphs.isEmpty()) {
                XWPFParagraph para = paragraphs.get(0);
                List<XWPFRun> runs = para.getRuns();

                if (!runs.isEmpty()) {
                    XWPFRun run = runs.get(0);
                    int fontSize = run.getFontSize();
                    if (fontSize > 0) {
                        return fontSize;
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("提取字体大小失败: {}", e.getMessage());
            // 忽略
        }
        return 10; // 默认字号
    }

    /**
     * 应用边框
     */
    private static void applyBorders(PdfPCell pdfCell, XWPFTableCell cell) {
        try {
            // 设置统一的细边框
            pdfCell.setBorderWidth(0.5f);
            pdfCell.setBorderColor(BaseColor.GRAY);
            pdfCell.setBorder(PdfPCell.BOX);
        } catch (Exception e) {
            pdfCell.setBorderWidth(0.5f);
            pdfCell.setBorderColor(BaseColor.GRAY);
            pdfCell.setBorder(PdfPCell.BOX);
        }
    }

    /**
     * 应用对齐方式
     */
    private static void applyAlignment(PdfPCell pdfCell, XWPFTableCell cell) {
        try {
            List<XWPFParagraph> paragraphs = cell.getParagraphs();
            if (!paragraphs.isEmpty()) {
                XWPFParagraph para = paragraphs.get(0);
                // 获取段落对齐方式
                ParagraphAlignment alignment = para.getAlignment();
                switch (alignment) {
                    case CENTER:
                        pdfCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        break;
                    case RIGHT:
                        pdfCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        break;
                    case BOTH:
                        pdfCell.setHorizontalAlignment(Element.ALIGN_JUSTIFIED);
                        break;
                    case LEFT:
                    default:
                        pdfCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        break;
                }
            } else {
                pdfCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            }
            pdfCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        } catch (Exception e) {
            pdfCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            pdfCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        }
    }

    /**
     * 应用背景色
     */
    private static void applyBackgroundColor(PdfPCell pdfCell, XWPFTableCell cell) {
        try {
            // 获取单元格的底层XML结构
            org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTc ctTc = cell.getCTTc();
            if (ctTc != null && ctTc.getTcPr() != null) {
                org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcPr tcPr = ctTc.getTcPr();
                logger.debug("检查单元格背景色: isSetShd={}", tcPr.isSetShd());
                // 获取背景色 (shading)
                if (tcPr.isSetShd()) {
                    org.openxmlformats.schemas.wordprocessingml.x2006.main.CTShd shd = tcPr.getShd();
                    String colorVal = null;
                    // CTShd 结构：val=填充模式(solid), color=背景色, fill=前景色
                    // 我们需要获取 color 属性作为单元格背景色
                    Object colorObj = shd.getColor();
                    if (colorObj != null) {
                        // getColor() 可能返回 byte[] 或 String
                        if (colorObj instanceof byte[] bytes) {
                            // 将 byte[] 转换为十六进制字符串
                            StringBuilder hex = new StringBuilder();
                            for (byte b : bytes) {
                                hex.append(String.format("%02X", b));
                            }
                            colorVal = hex.toString();
                            logger.debug("从 shd.getColor() byte[] 转换: {}", colorVal);
                        } else {
                            colorVal = colorObj.toString();
                            logger.debug("从 shd.getColor() 获取: {}", colorVal);
                        }
                    }
                    // 如果 color 为空或 auto，尝试从 XML 直接提取
                    if (colorVal == null || colorVal.equalsIgnoreCase("auto")) {
                        try {
                            // 尝试通过反射或 XML 直接读取
                            String xmlText = shd.toString();
                            // 使用正则表达式提取 w:color="xxxxxx"
                            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("w:color=\"([0-9A-Fa-f]{6})\"");
                            java.util.regex.Matcher matcher = pattern.matcher(xmlText);
                            if (matcher.find()) {
                                colorVal = matcher.group(1);
                                logger.debug("从 XML 中提取到颜色: {}", colorVal);
                            }
                        } catch (Exception ex) {
                            logger.debug("从 XML 提取颜色失败: {}", ex.getMessage());
                        }
                    }
                    // 跳过 "auto" 等无效值，只处理十六进制颜色
                    if (colorVal != null && !colorVal.isEmpty() && !colorVal.equalsIgnoreCase("auto")) {
                        try {
                            // 解析十六进制颜色值
                            int rgb = Integer.parseInt(colorVal, 16);
                            int r = (rgb >> 16) & 0xFF;
                            int g = (rgb >> 8) & 0xFF;
                            int b = rgb & 0xFF;
                            pdfCell.setBackgroundColor(new com.itextpdf.text.BaseColor(r, g, b));
                            logger.debug("✓ 设置单元格背景色: {} -> RGB({},{},{})", colorVal, r, g, b);
                        } catch (NumberFormatException e) {
                            logger.debug("背景色格式不支持: {} (可能是图案模式而非颜色)", colorVal);
                        }
                    } else {
                        logger.debug("跳过无效颜色值: {}", colorVal != null ? colorVal : "null");
                    }
                }
            } else {
                logger.debug("单元格没有 TcPr 属性");
            }
        } catch (Exception e) {
            logger.debug("应用背景色失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 获取列跨度
     */
    private static int getGridSpan(XWPFTableCell cell) {
        try {
            CTDecimalNumber gridSpan = cell.getCTTc().getTcPr().getGridSpan();
            if (gridSpan != null && gridSpan.getVal() != null) {
                return gridSpan.getVal().intValue();
            }
        } catch (Exception e) {
            logger.debug("获取列跨度失败: {}", e.getMessage(), e);
            // 忽略
        }
        return 1;
    }

    /**
     * 创建空单元格
     */
    private static PdfPCell createEmptyCell() {
        PdfPCell cell = new PdfPCell();
        cell.setPhrase(new Phrase(" "));
        cell.setBorderWidth(0.5f);
        cell.setBorderColor(BaseColor.GRAY);
        cell.setBorder(PdfPCell.BOX);
        cell.setPadding(5f);
        return cell;
    }
}
