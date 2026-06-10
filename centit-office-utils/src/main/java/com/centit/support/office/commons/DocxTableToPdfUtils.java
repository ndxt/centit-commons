package com.centit.support.office.commons;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTDecimalNumber;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * DOCX表格转PDF高级工具类
 * 专门处理DOCX格式的表格，提供更好的保真度
 *
 * @author zhf
 */
public class DocxTableToPdfUtils {

    /**
     * 将DOCX表格转换为PDF表格
     *
     * @param table    DOCX表格对象
     * @param baseFont 中文字体
     * @return PDF表格
     */
    public static PdfPTable convertXWPFTableToPdf(XWPFTable table, com.itextpdf.text.pdf.BaseFont baseFont) {
        if (table == null) {
            return null;
        }

        // 确保字体不为null，如果为null则创建紧急回退字体
        if (baseFont == null) {
            try {
                baseFont = com.itextpdf.text.pdf.BaseFont.createFont(
                    com.itextpdf.text.pdf.BaseFont.HELVETICA,
                    com.itextpdf.text.pdf.BaseFont.WINANSI,
                    com.itextpdf.text.pdf.BaseFont.EMBEDDED);
            } catch (Exception e) {
                return null;
            }
        }

        try {
            // 获取行数和列数
            List<XWPFTableRow> rows = table.getRows();
            if (rows.isEmpty()) {
                return null;
            }

            int rowCount = rows.size();
            int colCount = getMaxColumnCount(rows);

            if (colCount == 0) {
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

            // 不使用setHeaderRows()，因为它可能导致表头内容不显示
            // 改为在转换时给表头行添加背景色来区分

            // 转换每一行
            for (int i = 0; i < rowCount; i++) {
                XWPFTableRow row = rows.get(i);

                // 如果是表头行，添加特殊标记
                boolean isHeaderRow = (headerRows > 0 && i < headerRows);
                convertRowToPdf(row, pdfTable, baseFont, colCount, isHeaderRow, i, table);
            }

            return pdfTable;

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取最大列数（考虑合并单元格）
     */
    private static int getMaxColumnCount(List<XWPFTableRow> rows) {
        int maxCols = 0;
        for (XWPFTableRow row : rows) {
            List<XWPFTableCell> cellList = row.getTableCells();
            int cellCount = cellList.size();
            // 考虑colSpan
            for (XWPFTableCell cell : cellList) {
                try {
                    org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcPr tcPr =
                        cell.getCTTc() != null ? cell.getCTTc().getTcPr() : null;
                    if (tcPr != null) {
                        CTDecimalNumber gridSpan = tcPr.getGridSpan();
                        if (gridSpan != null && gridSpan.getVal() != null) {
                            cellCount += gridSpan.getVal().intValue() - 1;
                        }
                    }
                } catch (Exception e) {
                    // 忽略，按默认 gridSpan=1 处理
                }
            }
            maxCols = Math.max(maxCols, cellCount);
        }
        return maxCols;
    }

    /**
     * 计算列宽 - 基于DOCX表格的实际宽度设置
     */
    private static float[] calculateColumnWidths(XWPFTable table, int colCount) {
        float[] widths = new float[colCount];

        try {
            List<XWPFTableRow> rows = table.getRows();
            if (rows.isEmpty()) {
                // 默认等宽
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

                    // getW() 可能返回 BigInteger 或 Integer，需要安全处理
                    Object wObj = tblW.getW();
                    int wVal = 0;
                    if (wObj instanceof java.math.BigInteger) {
                        wVal = ((java.math.BigInteger) wObj).intValue();
                    } else if (wObj instanceof Integer) {
                        wVal = (Integer) wObj;
                    } else if (wObj != null) {
                        wVal = Integer.parseInt(wObj.toString());
                    }

                    if ("pct".equalsIgnoreCase(type)) {
                        // 百分比模式：wVal 的单位是 1/50%，即 5000 = 100%
                        // A4 页面可用宽度约 525pt（842pt - 2*72pt 页边距）
                        float pageWidthPt = 525f;
                        totalTableWidthPt = pageWidthPt * wVal / 5000f;
                    } else if ("dxa".equalsIgnoreCase(type)) {
                        // twips 模式：转换为 point
                        totalTableWidthPt = wVal / 20f;
                    } else if ("auto".equalsIgnoreCase(type) || wVal == 0) {
                        // auto类型或宽度为0，使用页面宽度作为默认值
                        totalTableWidthPt = 525f; // A4页面可用宽度
                    } else {
                        totalTableWidthPt = 525f; // 使用页面宽度作为默认值
                    }
                } else {
                    totalTableWidthPt = 525f; // 使用页面宽度作为默认值
                }
            } catch (Exception e) {
                totalTableWidthPt = 525f; // 使用页面宽度作为默认值
            }

            // 优先尝试从tblGrid获取列宽（这是最精确的方法）
            float[] gridWidths = tryGetColumnWidthsFromGrid(table, colCount, totalTableWidthPt);
            if (gridWidths != null) {
                return gridWidths;
            }

            // 尝试从多行的单元格获取各列宽度（比只用第一行更准确）
            float[] cellWidths = calculateColumnWidthsFromRows(rows, colCount);

            // 检查是否获得了有效的列宽信息
            boolean hasCellWidths = false;
            for (float w : cellWidths) {
                if (w > 0) {
                    hasCellWidths = true;
                    break;
                }
            }

            if (hasCellWidths && totalTableWidthPt > 0) {
                // 情况1：有表格总宽度 + 有单元格宽度 → 按比例分配
                float sumCellWidths = 0f;
                int nonZeroCount = 0;
                for (int i = 0; i < colCount; i++) {
                    sumCellWidths += cellWidths[i];
                    if (cellWidths[i] > 0) {
                        nonZeroCount++;
                    }
                }

                if (sumCellWidths > 0 && nonZeroCount > 0) {
                    for (int i = 0; i < colCount; i++) {
                        if (cellWidths[i] > 0) {
                            widths[i] = totalTableWidthPt * cellWidths[i] / sumCellWidths;
                        } else {
                            // 没有明确宽度的列，平均分配剩余空间
                            widths[i] = totalTableWidthPt / colCount;
                        }
                    }
                    return widths;
                }
            }

            if (totalTableWidthPt > 0) {
                // 情况2：有表格总宽度但无单元格宽度 → 平均分配
                Arrays.fill(widths, totalTableWidthPt / colCount);
                return widths;
            }

            // 情况3：无宽度信息 → 根据内容估算
            // 使用第一行的文本长度估算列宽
            XWPFTableRow firstRow = rows.get(0);
            List<XWPFTableCell> cells = firstRow.getTableCells();
            for (int i = 0; i < Math.min(cells.size(), colCount); i++) {
                String text = cells.get(i).getText();
                widths[i] = Math.max(50f, text.length() * 3f);
            }
            for (int i = cells.size(); i < colCount; i++) {
                widths[i] = 50f;
            }
            return widths;

        } catch (Exception e) {
            // 使用默认值
        }
        // 默认等宽
        Arrays.fill(widths, 1f);
        return widths;
    }

    /**
     * 从多行的单元格计算列宽（比只用第一行更准确）
     */
    private static float[] calculateColumnWidthsFromRows(List<XWPFTableRow> rows, int colCount) {
        float[] maxWidths = new float[colCount];
        boolean hasAnyWidth = false;
        // 遍历所有行，找出每列的最大宽度
        for (XWPFTableRow row : rows) {
            List<XWPFTableCell> cells = row.getTableCells();
            int colPos = 0;
            for (int i = 0; i < cells.size() && colPos < colCount; i++) {
                XWPFTableCell cell = cells.get(i);
                try {
                    org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcPr tcPr =
                        cell.getCTTc() != null ? cell.getCTTc().getTcPr() : null;
                    // 获取gridSpan
                    int gridSpan = 1;
                    if (tcPr != null) {
                        CTDecimalNumber gridSpanObj =
                            tcPr.getGridSpan();
                        if (gridSpanObj != null && gridSpanObj.getVal() != null) {
                            gridSpan = gridSpanObj.getVal().intValue();
                        }
                    }
                    // 获取单元格宽度
                    if (tcPr != null) {
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
                                    int wVal = 0;

                                    // 安全处理不同类型的返回值
                                    if (wObj instanceof java.math.BigInteger) {
                                        wVal = ((java.math.BigInteger) wObj).intValue();
                                    } else if (wObj instanceof Integer) {
                                        wVal = (Integer) wObj;
                                    } else if (wObj != null) {
                                        wVal = Integer.parseInt(wObj.toString());
                                    }

                                    float cellWidth = 0f;
                                    if ("dxa".equalsIgnoreCase(wType)) {
                                        cellWidth = wVal / 20f; // twips -> pt
                                    } else if ("pct".equalsIgnoreCase(wType)) {
                                        cellWidth = wVal / 50f; // 1/50% -> %
                                    }

                                    // 将单元格宽度平均分配到它跨越的各列
                                    if (cellWidth > 0) {
                                        float widthPerCol = cellWidth / gridSpan;
                                        for (int j = 0; j < gridSpan && (colPos + j) < colCount; j++) {
                                            // 更新最大宽度
                                            if (widthPerCol > maxWidths[colPos + j]) {
                                                maxWidths[colPos + j] = widthPerCol;
                                            }
                                        }
                                        hasAnyWidth = true;
                                    }
                                }
                            } catch (Exception ex) {
                                // 忽略解析错误
                            }
                        }
                    }

                    colPos += gridSpan;
                } catch (Exception e) {
                    colPos++;
                }
            }
        }

        return hasAnyWidth ? maxWidths : new float[colCount];
    }

    /**
     * 尝试从tblGrid获取列宽（最精确的方法）
     */
    private static float[] tryGetColumnWidthsFromGrid(XWPFTable table, int colCount, float totalTableWidthPt) {
        try {
            org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTbl ctTbl = table.getCTTbl();
            if (ctTbl != null && ctTbl.getTblGrid() != null) {
                org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblGrid tblGrid = ctTbl.getTblGrid();
                if (tblGrid != null && tblGrid.getGridColList() != null) {
                    List<?> gridColList = tblGrid.getGridColList();
                    float[] widths = new float[colCount];

                    // 第一步：读取原始列宽（单位：twips）
                    float[] rawWidths = new float[Math.min(gridColList.size(), colCount)];
                    float totalGridWidth = 0f;

                    for (int i = 0; i < Math.min(gridColList.size(), colCount); i++) {
                        Object gridCol = gridColList.get(i);
                        try {
                            // 通过反射获取列宽
                            java.lang.reflect.Method getWMethod = gridCol.getClass().getMethod("getW");
                            Object wObj = getWMethod.invoke(gridCol);

                            int wVal = 0;
                            if (wObj instanceof java.math.BigInteger) {
                                wVal = ((java.math.BigInteger) wObj).intValue();
                            } else if (wObj instanceof Integer) {
                                wVal = (Integer) wObj;
                            } else if (wObj != null) {
                                wVal = Integer.parseInt(wObj.toString());
                            }

                            // DOCX中网格列宽度单位是twips (1/20 point)
                            rawWidths[i] = wVal / 20f;
                            totalGridWidth += rawWidths[i];
                        } catch (Exception e) {
                            rawWidths[i] = totalTableWidthPt / colCount; // 使用平均宽度
                            totalGridWidth += rawWidths[i];
                        }
                    }

                    // 检查是否有异常小的列宽（可能是数据错误）
                    boolean hasVerySmallColumn = false;
                    for (float w : rawWidths) {
                        if (w < 20f) { // 小于20pt认为是异常
                            hasVerySmallColumn = true;
                            break;
                        }
                    }
                    // 如果有异常小的列宽，不使用tblGrid数据
                    if (hasVerySmallColumn) {
                        return null;
                    }
                    // 第二步：根据表格实际宽度按比例调整列宽
                    // 如果原始网格总宽度与实际表格宽度差异较大，需要按比例缩放
                    if (totalGridWidth > 0 && Math.abs(totalGridWidth - totalTableWidthPt) > 10) {
                        // 按比例调整每列宽度
                        float scale = totalTableWidthPt / totalGridWidth;
                        for (int i = 0; i < rawWidths.length; i++) {
                            widths[i] = rawWidths[i] * scale;
                        }
                    } else {
                        // 直接使用原始宽度
                        System.arraycopy(rawWidths, 0, widths, 0, rawWidths.length);
                    }
                    // 填充剩余列（如果有）
                    for (int i = rawWidths.length; i < colCount; i++) {
                        widths[i] = totalTableWidthPt / colCount;
                    }
                    return widths;
                }
            }
        } catch (Exception e) {
            // 忽略
        }
        return null;
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
                            if (run.isBold()) {
                                cellIsBold = true;
                                break;
                            }
                        }
                    }

                    if (cellIsBold) {
                        boldCellCount++;
                    }
                }
            }

            // 改进的检测策略：
            // 1. 如果所有单元格都加粗，肯定是表头
            // 2. 如果超过一半单元格加粗，很可能是表头
            // 3. 如果只有第一个单元格加粗（常见于左侧标题列），也认为是表头
            boolean isHeader = false;

            if (boldCellCount == totalCheckedCells) {
                // 全部加粗 - 肯定是表头
                isHeader = true;
            } else if (boldCellCount > totalCheckedCells / 2) {
                // 超过一半加粗 - 很可能是表头
                isHeader = true;
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
                }
            }

            if (isHeader) {
                return 1;
            }
        }

        return 0;
    }

    /**
     * 转换行到PDF
     * 处理垂直合并：被合并的单元格应该完全跳过，iText会自动处理rowspan的列位置
     */
    private static void convertRowToPdf(XWPFTableRow row, PdfPTable pdfTable, com.itextpdf.text.pdf.BaseFont baseFont, int expectedCols, boolean isHeaderRow, int rowIndex, XWPFTable table) {
        List<XWPFTableCell> cells = row.getTableCells();
        // 获取行高
        float rowHeightPt = getRowHeight(row);
        // 按单元格在DOCX中的顺序处理
        for (int cellIndex = 0; cellIndex < cells.size(); cellIndex++) {
            XWPFTableCell cell = cells.get(cellIndex);
            int gridSpan = getGridSpan(cell);
            // 计算当前单元格的逻辑列位置
            int docxColPos = getCellColPosition(row, cellIndex);
            // 计算rowSpan
            int rowSpan = getRowSpan(cell, table, rowIndex, docxColPos);
            // 被垂直合并的单元格（rowSpan=0），完全跳过
            // iText会自动处理rowspan，不需要添加占位单元格
            if (rowSpan == 0) {
                continue;
            }
            // 添加正常单元格到PDF
            PdfPCell pdfCell = convertCellToPdf(cell, baseFont, isHeaderRow, table);
            // 设置行高
            if (rowHeightPt > 0) {
                pdfCell.setMinimumHeight(rowHeightPt);
            }
            if (gridSpan > 1) {
                pdfCell.setColspan(gridSpan);
            }
            if (rowSpan > 1) {
                pdfCell.setRowspan(rowSpan);
            }
            pdfTable.addCell(pdfCell);
        }
        // 完成当前行
        pdfTable.completeRow();
    }

    /**
     * 获取行高度（单位：points）
     * 综合考虑DOCX行高设置、字体大小、段落间距等因素
     * <p>
     * DOCX行高说明：
     * - 单位：twips (1/20 point)，例如 850 twips = 42.5 points
     * - hRule属性：
     * - auto: 自动计算行高（默认）
     * - atLeast: 最小行高，内容多时会自动扩展
     * - exact: 精确行高，内容超出时可能被截断
     */
    private static float getRowHeight(XWPFTableRow row) {
        float rowHeightPt = 0f;
        boolean isExactHeight = false;
        boolean isAtLeastHeight = false;

        try {
            Object ctRow = row.getCtRow();
            if (ctRow != null) {
                // 方法1：优先通过API获取行高（更可靠）
                try {
                    // 获取行属性 CTTblPrBase
                    java.lang.reflect.Method getTrPrMethod = ctRow.getClass().getMethod("getTrPr");
                    Object trPr = getTrPrMethod.invoke(ctRow);

                    if (trPr != null) {
                        // 获取行高对象 CTHeight
                        java.lang.reflect.Method getTrHeightMethod = trPr.getClass().getMethod("getTrHeight");
                        Object trHeight = getTrHeightMethod.invoke(trPr);

                        if (trHeight != null) {
                            // 获取行高值 (BigInteger)
                            java.lang.reflect.Method getValMethod = trHeight.getClass().getMethod("getVal");
                            Object valObj = getValMethod.invoke(trHeight);

                            // 获取行高规则 (STHeightRule)
                            java.lang.reflect.Method getHRuleMethod = trHeight.getClass().getMethod("getHRule");
                            Object hRuleObj = getHRuleMethod.invoke(trHeight);

                            // 解析行高值
                            int hVal = 0;
                            if (valObj instanceof java.math.BigInteger) {
                                hVal = ((java.math.BigInteger) valObj).intValue();
                            } else if (valObj instanceof Integer) {
                                hVal = (Integer) valObj;
                            } else if (valObj != null) {
                                hVal = Integer.parseInt(valObj.toString());
                            }

                            // 解析行高规则
                            String hRule = "auto";
                            if (hRuleObj != null) {
                                hRule = hRuleObj.toString();
                            }

                            // 处理不同的hRule值
                            if (hVal > 0 && !"auto".equalsIgnoreCase(hRule)) {
                                // 转换单位：twips -> points
                                rowHeightPt = hVal / 20.0f;

                                if ("exact".equalsIgnoreCase(hRule)) {
                                    isExactHeight = true;
                                } else if ("atLeast".equalsIgnoreCase(hRule)) {
                                    isAtLeastHeight = true;
                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                    // API方法失败，回退到XML解析
                }

                // 方法2：如果API方法失败，使用XML正则解析（兼容性方案）
                if (rowHeightPt <= 0) {
                    try {
                        String xmlText = ctRow.toString();

                        // 查找 w:trHeight 元素
                        // 格式: <w:trHeight w:val="850" w:hRule="exact"/>
                        java.util.regex.Pattern trHeightPattern =
                            java.util.regex.Pattern.compile("<w:trHeight\\s[^>]*/>");
                        java.util.regex.Matcher trHeightMatcher = trHeightPattern.matcher(xmlText);

                        if (trHeightMatcher.find()) {
                            String trHeightTag = trHeightMatcher.group();

                            // 获取行高值 w:val
                            java.util.regex.Pattern valPattern =
                                java.util.regex.Pattern.compile("w:val=\"([0-9]+)\"");
                            java.util.regex.Matcher valMatcher = valPattern.matcher(trHeightTag);
                            int hVal = 0;
                            if (valMatcher.find()) {
                                hVal = Integer.parseInt(valMatcher.group(1));
                            }

                            // 获取行高规则 w:hRule
                            String hRule = "auto";
                            java.util.regex.Pattern hRulePattern =
                                java.util.regex.Pattern.compile("w:hRule=\"([^\"]+)\"");
                            java.util.regex.Matcher hRuleMatcher = hRulePattern.matcher(trHeightTag);
                            if (hRuleMatcher.find()) {
                                hRule = hRuleMatcher.group(1);
                            }

                            // 根据hRule判断如何处理行高
                            if (hVal > 0 && !"auto".equalsIgnoreCase(hRule)) {
                                // 转换单位：twips -> points
                                rowHeightPt = hVal / 20.0f;

                                if ("exact".equalsIgnoreCase(hRule)) {
                                    isExactHeight = true;
                                } else if ("atLeast".equalsIgnoreCase(hRule)) {
                                    isAtLeastHeight = true;
                                }
                            }
                        }
                    } catch (Exception ex) {
                        // XML解析也失败，使用内容估算
                    }
                }
            }

            // 如果是精确高度，需要确保至少能容纳内容
            if (isExactHeight && rowHeightPt > 0) {
                // 计算内容所需的最小高度
                float contentMinHeight = 0f;
                List<XWPFTableCell> cells = row.getTableCells();
                for (XWPFTableCell cell : cells) {
                    float cellRequiredHeight = calculateCellRequiredHeight(cell);
                    if (cellRequiredHeight > contentMinHeight) {
                        contentMinHeight = cellRequiredHeight;
                    }
                }
                // 如果精确高度小于内容所需高度，使用内容高度
                // 这样可以避免文字被裁剪
                if (contentMinHeight > rowHeightPt) {
                    return contentMinHeight;
                }
                return rowHeightPt;
            }

            // 如果没有明确行高或是最小高度，根据单元格内容估算
            if (rowHeightPt <= 0 || isAtLeastHeight) {
                float maxRequiredHeight = 0f;
                List<XWPFTableCell> cells = row.getTableCells();

                for (XWPFTableCell cell : cells) {
                    float cellRequiredHeight = calculateCellRequiredHeight(cell);
                    if (cellRequiredHeight > maxRequiredHeight) {
                        maxRequiredHeight = cellRequiredHeight;
                    }
                }

                // 使用估算的最大高度，加上内边距
                float calculatedHeight = maxRequiredHeight + 4f; // 内边距2pt*2

                // 对于atLeast，取设置值和计算值的较大者
                if (isAtLeastHeight && rowHeightPt > 0) {
                    rowHeightPt = Math.max(rowHeightPt, calculatedHeight);
                } else if (calculatedHeight > 0) {
                    rowHeightPt = calculatedHeight;
                }
            }

            // 设置合理的最小行高（避免行高过小）
            if (rowHeightPt < 12f) {
                rowHeightPt = 12f;
            }

            // 设置合理的最大行高（避免异常大的行高）
            if (rowHeightPt > 200f) {
                rowHeightPt = 200f;
            }

        } catch (Exception e) {
            // 出错时使用默认行高
            rowHeightPt = 15f;
        }

        return rowHeightPt;
    }

    /**
     * 计算单元格所需的高度（基于字体大小和段落数量）
     */
    private static float calculateCellRequiredHeight(XWPFTableCell cell) {
        try {
            float maxFontSize = 12f; // 默认字体大小
            int paragraphCount = 0;
            List<XWPFParagraph> paragraphs = cell.getParagraphs();
            for (XWPFParagraph para : paragraphs) {
                List<XWPFRun> runs = para.getRuns();
                for (XWPFRun run : runs) {
                    Double fontSize = run.getFontSizeAsDouble();
                    if (fontSize != null) {
                        if (fontSize.floatValue() > maxFontSize) {
                            maxFontSize = fontSize.floatValue();
                        }
                    }
                }
                // 只计算非空段落
                if (!runs.isEmpty()) {
                    boolean hasContent = false;
                    for (XWPFRun run : runs) {
                        String text = run.getText(0);
                        if (text != null && !text.trim().isEmpty()) {
                            hasContent = true;
                            break;
                        }
                    }
                    if (hasContent) {
                        paragraphCount++;
                    }
                }
            }
            // 计算所需高度：字体大小 * 段落数量 * 行间距系数(1.15) + 内边距(2pt)
            return maxFontSize * paragraphCount * 1.15f + 2f;
        } catch (Exception e) {
            return 15f; // 进一步减小默认高度为15f
        }
    }

    /**
     * 转换单元格到PDF
     */
    private static PdfPCell convertCellToPdf(XWPFTableCell cell, com.itextpdf.text.pdf.BaseFont baseFont, boolean isHeaderRow, XWPFTable table) {
        PdfPCell pdfCell = new PdfPCell();
        try {
            // 获取单元格文本
            String text = cell.getText();
            if (text == null || text.trim().isEmpty()) {
                pdfCell.setPhrase(new Phrase(" "));
            } else {
                // 检查单元格是否需要段落间距支持
                boolean needsParagraphSpacing = cellNeedsParagraphSpacing(cell);

                if (needsParagraphSpacing) {
                    // 使用Paragraph来支持完整的间距设置
                    // 清空单元格，使用addElement添加Paragraph
                    pdfCell.setPhrase(new Phrase("")); // 设置空phrase避免null

                    List<XWPFParagraph> paragraphs = cell.getParagraphs();
                    for (int i = 0; i < paragraphs.size(); i++) {
                        XWPFParagraph wordPara = paragraphs.get(i);
                        com.itextpdf.text.Paragraph pdfPara = convertWordParagraphToPdfParagraph(wordPara, baseFont);
                        pdfCell.addElement(pdfPara);
                    }
                } else {
                    // 使用Phrase（原有逻辑）
                    Phrase phrase = createStyledPhrase(cell, baseFont);
                    pdfCell.setPhrase(phrase);
                }

                // 修复换行问题：默认允许自动换行，保持原文档的换行行为
                // 只有在单元格内容非常短且确实是单个单词/数字时，才考虑禁用换行
                String cellText = cell.getText();
                if (cellText != null && !cellText.isEmpty()) {
                    // 移除所有空白字符后检查长度
                    String trimmedText = cellText.replaceAll("\\s+", "");
                    // 只有当内容长度小于8个字符且不包含空格时，才禁用换行
                    // 这样可以保持短文本（如数字、代码）在一行，但允许其他文本正常换行
                    if (trimmedText.length() < 8 && !cellText.contains(" ") && !cellText.contains("\n")) {
                        try {
                            pdfCell.setNoWrap(true);
                        } catch (Exception e) {
                            // 忽略
                        }
                    } else {
                        // 对于较长的文本或包含空格的文本，确保允许自动换行
                        pdfCell.setNoWrap(false);
                    }
                }
            }
            // 设置内边距
            pdfCell.setPadding(3f);
            // 设置边框
            applyBorders(pdfCell, cell, isHeaderRow, table);
            // 设置对齐方式
            applyAlignment(pdfCell, cell);
            // 设置背景色
            applyBackgroundColor(pdfCell, cell);

        } catch (Exception e) {
            pdfCell.setPhrase(new Phrase(" "));
        }
        return pdfCell;
    }

    /**
     * 创建带样式的短语
     * 对于表格单元格，处理所有段落，段落之间用换行符分隔
     */
    private static Phrase createStyledPhrase(XWPFTableCell cell, com.itextpdf.text.pdf.BaseFont defaultBaseFont) {
        // 获取单元格内段落的行间距设置
        float cellLeading = calculateCellLeading(cell, defaultBaseFont);

        Phrase phrase = new Phrase();
        // 设置单元格的行间距（使用直接值）
        // iText中setLeading(fixedLeading, multipliedLeading)的公式是：
        // actualLeading = fixedLeading + multipliedLeading * maxFontSize
        // 如果要设置固定的leading值，可以使用setLeading(fixedLeading, 0)
        phrase.setLeading(cellLeading, 0f);
        try {
            List<XWPFParagraph> paragraphs = cell.getParagraphs();
            boolean hasAnyContent = false;
            boolean isFirstParagraph = true; // 标记是否是第一个段落
            // 处理所有段落，段落之间添加换行符
            for (XWPFParagraph para : paragraphs) {
                // 获取段落的首行缩进（用于模拟缩进）
                int firstLineIndent = para.getIndentationFirstLine();

                List<XWPFRun> runs = para.getRuns();

                if (runs != null && !runs.isEmpty()) {
                    // 检查段落是否有内容
                    boolean paraHasContent = false;
                    for (XWPFRun run : runs) {
                        String text = run.getText(0);
                        if (text != null && !text.isEmpty()) {
                            paraHasContent = true;
                            break;
                        }
                    }
                    // 如果段落有内容，处理它
                    if (paraHasContent) {
                        // 获取段落的段前间距（单位：twips，转换为point）
                        int spacingBefore = para.getSpacingBefore();
                        float spacingBeforePt = spacingBefore / 20.0f;

                        // 获取段落的字体大小
                        float paraFontSize = 12f; // 默认字号
                        try {
                            List<XWPFRun> paraRuns = para.getRuns();
                            if (paraRuns != null && !paraRuns.isEmpty()) {
                                XWPFRun firstRun = paraRuns.get(0);
                                Double runFontSize = firstRun.getFontSizeAsDouble();
                                if (runFontSize != null && runFontSize > 0) {
                                    paraFontSize = runFontSize.floatValue();
                                }
                            }
                        } catch (Exception e) {
                            // 使用默认值
                        }

                        // 使用正确的行间距计算方法（支持lineRule类型）
                        float leading = calculateParagraphLeading(para, paraFontSize);
                        // 需要额外增加的间距（行间距超出单倍的部分）
                        float extraLineSpacing = leading - paraFontSize;

                        // 段前间距 + 行间距额外部分
                        float totalSpacing = spacingBeforePt + extraLineSpacing;

                        // 如果不是第一个段落，先添加换行符
                        if (hasAnyContent) {
                            // 创建换行符
                            com.itextpdf.text.Chunk newline = new com.itextpdf.text.Chunk("\n", new Font(defaultBaseFont, paraFontSize));
                            phrase.add(newline);

                            // 使用不可见Chunk来模拟段前间距和行间距
                            if (totalSpacing > 2f) {
                                Font spacingFont = new Font(defaultBaseFont, totalSpacing, Font.NORMAL);
                                spacingFont.setColor(new BaseColor(255, 255, 255)); // 白色（不可见）
                                com.itextpdf.text.Chunk spacingChunk = new com.itextpdf.text.Chunk(" ", spacingFont);
                                phrase.add(spacingChunk);
                            }
                        } else if (isFirstParagraph && spacingBeforePt > 0) {
                            // 第一个段落也有段前间距，使用不可见Chunk来模拟
                            if (spacingBeforePt > 1f) {
                                Font spacingFont = new Font(defaultBaseFont, spacingBeforePt, Font.NORMAL);
                                spacingFont.setColor(new BaseColor(255, 255, 255)); // 白色（不可见）
                                com.itextpdf.text.Chunk spacingChunk = new com.itextpdf.text.Chunk(" ", spacingFont);
                                phrase.add(spacingChunk);
                            }
                        }

                        hasAnyContent = true;
                        isFirstParagraph = false;

                        // 处理首行缩进：添加空格来模拟缩进
                        // Word中首行缩进单位是twips（1/20 point），需要转换为字符数
                        // 假设1个中文字符宽度约为12-14pt，空格约为6pt
                        if (firstLineIndent > 0) {
                            float indentPt = firstLineIndent / 20.0f; // 转换为point
                            // paraFontSize已经在上面定义并获取了，直接使用
                            // 计算需要添加的空格数
                            // 中文字符宽度约为字体大小，空格约为字体大小的0.5倍
                            float charWidth = paraFontSize * 0.5f; // 空格宽度
                            int spaceCount = Math.round(indentPt / charWidth);
                            if (spaceCount > 0) {
                                String indentSpaces = String.format("%" + spaceCount + "s", "");
                                com.itextpdf.text.Chunk indentChunk = new com.itextpdf.text.Chunk(indentSpaces, new Font(defaultBaseFont, paraFontSize, Font.NORMAL));
                                phrase.add(indentChunk);
                            }
                        }

                        // 处理当前段落的每个 run
                        for (XWPFRun run : runs) {
                            String runText = run.getText(0);
                            if (runText == null || runText.isEmpty()) {
                                continue;
                            }

                            // 提取字体大小
                            Double fontSize = run.getFontSizeAsDouble();
                            if (fontSize == null) {
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
                            // 双重保护：确保基础字体不为null
                            if (runBaseFont == null) {
                                runBaseFont = defaultBaseFont;
                            }

                            // 创建字体
                            Font font = new Font(runBaseFont, fontSize.floatValue(), fontStyle);

                            // 设置字体颜色
                            String colorStr = run.getColor();
                            if (colorStr != null && !colorStr.isEmpty()) {
                                try {
                                    int rgb = Integer.parseInt(colorStr, 16);
                                    int r = (rgb >> 16) & 0xFF;
                                    int g = (rgb >> 8) & 0xFF;
                                    int b = rgb & 0xFF;
                                    font.setColor(r, g, b);
                                } catch (NumberFormatException e) {
                                    // 忽略颜色解析失败
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
                                // 忽略下划线处理错误
                            }

                            // 处理删除线
                            if (run.isStrikeThrough()) {
                                chunk.setUnderline(0.5f, 3f); // 使用上划线模拟删除线
                            }

                            phrase.add(chunk);
                        }
                    } else {
                        // 段落有run但没有内容（空段落）
                        // 仍然需要添加换行符以保持段落间距
                        if (hasAnyContent) {
                            com.itextpdf.text.Chunk newline = new com.itextpdf.text.Chunk("\n", new Font(defaultBaseFont, 10));
                            phrase.add(newline);
                        }
                        hasAnyContent = true;
                    }
                } else {
                    // 重要修复：处理段落有 0 个 run 的情况
                    // 这种情况下，cell.getText() 能获取到文本，但没有 run 对象
                    // 文本可能直接存储在段落的底层 XML 中
                    String paraText = para.getText();
                    if (paraText != null && !paraText.trim().isEmpty()) {
                        // 如果不是第一个段落，先添加换行符
                        if (hasAnyContent) {
                            com.itextpdf.text.Chunk newline = new com.itextpdf.text.Chunk("\n", new Font(defaultBaseFont, 10.0f, Font.NORMAL));
                            phrase.add(newline);
                        }
                        hasAnyContent = true;
                        // 使用默认字体添加文本
                        com.itextpdf.text.Chunk chunk = new com.itextpdf.text.Chunk(paraText, new Font(defaultBaseFont, 10.5f, Font.NORMAL));
                        phrase.add(chunk);
                    } else {
                        // 完全空段落（没有run且没有文本）
                        // 仍然需要添加换行符以保持段落间距
                        if (hasAnyContent) {
                            com.itextpdf.text.Chunk newline = new com.itextpdf.text.Chunk("\n", new Font(defaultBaseFont, 10));
                            phrase.add(newline);
                        }
                        hasAnyContent = true;
                    }
                }
            } // 结束段落循环

            // 重要修复：检查phrase是否为空或内容很少
            // 有时候cell.getText()有文本，但所有段落都没有run或run为空
            // 这种情况下，使用cell.getText()作为回退
            String fullCellText = cell.getText();
            if (fullCellText != null && !fullCellText.trim().isEmpty()) {
                // 检查phrase的内容长度
                String phraseContent = phrase.getContent();
                int phraseLength = (phraseContent != null) ? phraseContent.length() : 0;
                int fullTextLength = fullCellText.trim().length();

                // 如果phrase内容明显少于完整文本（少于80%），说明有文本丢失
                // 使用完整文本替换
                if (phraseLength < fullTextLength * 0.8) {
                    // 清空phrase并使用完整文本
                    phrase = new Phrase();
                    com.itextpdf.text.Chunk chunk = new com.itextpdf.text.Chunk(fullCellText, new Font(defaultBaseFont, 10.5f, Font.NORMAL));
                    phrase.add(chunk);
                }
            }

            // 如果没有任何内容，添加空格
            if (phrase.isEmpty()) {
                phrase.add(" ");
            }
        } catch (Exception e) {
            // 回退到简单文本
            try {
                String cellText = cell.getText();
                phrase = new Phrase();
                phrase.add(new com.itextpdf.text.Chunk(cellText, new Font(defaultBaseFont, 10, Font.NORMAL)));
            } catch (Exception e2) {
                phrase = new Phrase();
                phrase.add(" ");
            }
        }

        return phrase;
    }

    /**
     * 计算单元格的行间距
     * 基于单元格内段落的行间距设置，支持auto/atLeast/exact三种类型
     */
    private static float calculateCellLeading(XWPFTableCell cell, com.itextpdf.text.pdf.BaseFont defaultBaseFont) {
        try {
            List<XWPFParagraph> paragraphs = cell.getParagraphs();
            float defaultLeading = 18f; // 默认行间距

            // 统计不同行间距出现的次数
            Map<Float, Integer> leadingCount = new java.util.HashMap<>();

            for (XWPFParagraph para : paragraphs) {
                // 使用正确的方法计算行间距，支持不同lineRule类型
                float fontSize = 12f; // 默认字号
                try {
                    List<XWPFRun> runs = para.getRuns();
                    if (runs != null && !runs.isEmpty()) {
                        for (XWPFRun run : runs) {
                            Double runFontSize = run.getFontSizeAsDouble();
                            if (runFontSize != null && runFontSize > 0) {
                                fontSize = runFontSize.floatValue();
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    // 使用默认值
                }

                // 计算段落行间距（支持lineRule类型）
                float leading = calculateParagraphLeading(para, fontSize);

                // 统计出现次数（四舍五入到最近的整数）
                float roundedLeading = Math.round(leading);
                leadingCount.put(roundedLeading, leadingCount.getOrDefault(roundedLeading, 0) + 1);
            }

            // 取出现次数最多的行间距
            float mostCommonLeading = defaultLeading;
            int maxCount = 0;
            for (java.util.Map.Entry<Float, Integer> entry : leadingCount.entrySet()) {
                if (entry.getValue() > maxCount) {
                    maxCount = entry.getValue();
                    mostCommonLeading = entry.getKey();
                }
            }

            return mostCommonLeading;
        } catch (Exception e) {
            return 18f; // 出错时返回默认值
        }
    }

    /**
     * 计算段落的行间距
     * 支持auto/atLeast/exact三种lineRule类型
     * 与DocxHybridConverter.calculateLeading逻辑保持一致
     */
    private static float calculateParagraphLeading(XWPFParagraph paragraph, float fontSize) {
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
                    // 这就是"间距2.0磅"对应的设置！
                    return lineValue / 20.0f;
                }
            }

            // 如果没有找到spacing设置，使用默认值
            return fontSize;
        } catch (Exception e) {
            // 出错时使用默认值
            return fontSize;
        }
    }

    /**
     * 为表格创建中文字体
     * 永不返回null，如果创建失败会尝试使用系统字体
     */
    private static com.itextpdf.text.pdf.BaseFont createChineseFontForTable(String fontFamily) {
        try {
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
                // 字体文件找不到，尝试内置字体
                try {
                    return com.itextpdf.text.pdf.BaseFont.createFont("STSong-Light",
                        "UniGB-UCS2-H",
                        com.itextpdf.text.pdf.BaseFont.NOT_EMBEDDED);
                } catch (Exception e2) {
                    // 最后回退到Helvetica（不支持中文但不会崩溃）
                    return com.itextpdf.text.pdf.BaseFont.createFont(
                        com.itextpdf.text.pdf.BaseFont.HELVETICA,
                        com.itextpdf.text.pdf.BaseFont.WINANSI,
                        com.itextpdf.text.pdf.BaseFont.EMBEDDED);
                }
            }
        } catch (Exception e) {
            // 紧急回退
            try {
                return com.itextpdf.text.pdf.BaseFont.createFont(
                    com.itextpdf.text.pdf.BaseFont.HELVETICA,
                    com.itextpdf.text.pdf.BaseFont.WINANSI,
                    com.itextpdf.text.pdf.BaseFont.EMBEDDED);
            } catch (Exception e2) {
                return null; // 只有在极端情况下才会返回null
            }
        }
    }

    /**
     * 应用边框
     * 检查Word文档中的边框设置，正确处理隐藏边框
     */
    private static void applyBorders(PdfPCell pdfCell, XWPFTableCell cell, boolean isHeaderRow, XWPFTable table) {
        try {
            boolean hasCellBorderSettings = false;

            // 获取单元格的边框设置
            Object tcPr = cell.getCTTc() != null ? cell.getCTTc().getTcPr() : null;

            if (tcPr != null) {
                // 使用反射获取边框信息，避免类名问题
                try {
                    java.lang.reflect.Method getBordersMethod = tcPr.getClass().getMethod("getTcBorders");
                    Object borders = getBordersMethod.invoke(tcPr);
                    if (borders != null) {
                        hasCellBorderSettings = true;
                        // 检查每个边框的设置
                        Object topBorder = getBorderFromBorders(borders, "getTop");
                        Object bottomBorder = getBorderFromBorders(borders, "getBottom");
                        Object leftBorder = getBorderFromBorders(borders, "getLeft");
                        Object rightBorder = getBorderFromBorders(borders, "getRight");

                        applyBorderSide(pdfCell, topBorder, PdfPCell.TOP, isHeaderRow);
                        applyBorderSide(pdfCell, bottomBorder, PdfPCell.BOTTOM, isHeaderRow);
                        applyBorderSide(pdfCell, leftBorder, PdfPCell.LEFT, isHeaderRow);
                        applyBorderSide(pdfCell, rightBorder, PdfPCell.RIGHT, isHeaderRow);
                    }
                } catch (Exception e) {
                    // 反射失败，继续检查表格级别边框
                }
            }

            // 如果单元格没有明确的边框设置，检查表格级别的边框设置
            if (!hasCellBorderSettings && table != null) {
                try {
                    org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblPr tblPr = table.getCTTbl().getTblPr();
                    if (tblPr != null) {
                        Object tblBorders = tblPr.getTblBorders();
                        if (tblBorders != null) {
                            // 使用表格级别的边框设置
                            Object topBorder = getBorderFromBorders(tblBorders, "getTop");
                            Object bottomBorder = getBorderFromBorders(tblBorders, "getBottom");
                            Object leftBorder = getBorderFromBorders(tblBorders, "getLeft");
                            Object rightBorder = getBorderFromBorders(tblBorders, "getRight");

                            applyBorderSide(pdfCell, topBorder, PdfPCell.TOP, isHeaderRow);
                            applyBorderSide(pdfCell, bottomBorder, PdfPCell.BOTTOM, isHeaderRow);
                            applyBorderSide(pdfCell, leftBorder, PdfPCell.LEFT, isHeaderRow);
                            applyBorderSide(pdfCell, rightBorder, PdfPCell.RIGHT, isHeaderRow);
                            return;
                        }
                    }
                } catch (Exception e) {
                    // 获取表格边框失败
                }
            }

            // 如果单元格和表格都没有明确的边框设置，默认不显示边框
            // 只有在明确设置了边框时才显示
            if (!hasCellBorderSettings) {
                pdfCell.setBorder(PdfPCell.NO_BORDER);
            }

        } catch (Exception e) {
            // 出错时默认不显示边框
            pdfCell.setBorder(PdfPCell.NO_BORDER);
        }
    }

    /**
     * 从边框对象中获取单个边框
     */
    private static Object getBorderFromBorders(Object borders, String methodName) {
        try {
            java.lang.reflect.Method method = borders.getClass().getMethod(methodName);
            return method.invoke(borders);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 应用单个边框的设置
     */
    private static void applyBorderSide(PdfPCell pdfCell, Object border, int side, boolean isHeaderRow) {
        try {
            if (border == null) {
                // 没有设置该边框，使用默认边框
                pdfCell.enableBorderSide(side);
                return;
            }

            // 使用反射获取边框的val属性
            String val = "single";
            try {
                java.lang.reflect.Method getValMethod = border.getClass().getMethod("getVal");
                Object valObj = getValMethod.invoke(border);
                if (valObj != null) {
                    val = valObj.toString();
                }
            } catch (Exception e) {
                // 使用默认值
            }

            // 检查是否是nil或none（无边框/隐藏）
            if ("nil".equalsIgnoreCase(val) || "none".equalsIgnoreCase(val)) {
                // 隐藏该边框
                pdfCell.disableBorderSide(side);
                return;
            }

            // 有边框，应用设置
            pdfCell.enableBorderSide(side);

        } catch (Exception e) {
            // 出错时使用默认边框
            pdfCell.enableBorderSide(side);
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

                // 获取背景色 (shading)
                if (tcPr.isSetShd()) {
                    org.openxmlformats.schemas.wordprocessingml.x2006.main.CTShd shd = tcPr.getShd();
                    String colorVal = null;

                    // CTShd 结构：val=填充模式(solid), color=背景色, fill=前景色
                    // 我们需要获取 color 属性作为单元格背景色
                    Object colorObj = shd.getColor();
                    if (colorObj != null) {
                        // getColor() 可能返回 byte[] 或 String
                        if (colorObj instanceof byte[]) {
                            // 将 byte[] 转换为十六进制字符串
                            byte[] bytes = (byte[]) colorObj;
                            StringBuilder hex = new StringBuilder();
                            for (byte b : bytes) {
                                hex.append(String.format("%02X", b));
                            }
                            colorVal = hex.toString();
                        } else {
                            colorVal = colorObj.toString();
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
                            }
                        } catch (Exception ex) {
                            // 忽略
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
                        } catch (NumberFormatException e) {
                            // 背景色格式不支持
                        }
                    }
                }
            }
        } catch (Exception e) {
            // 忽略
        }
    }

    /**
     * 获取列跨度（水平合并）
     */
    private static int getGridSpan(XWPFTableCell cell) {
        try {
            org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcPr tcPr =
                cell.getCTTc() != null ? cell.getCTTc().getTcPr() : null;
            if (tcPr != null) {
                CTDecimalNumber gridSpan = tcPr.getGridSpan();
                if (gridSpan != null && gridSpan.getVal() != null) {
                    return gridSpan.getVal().intValue();
                }
            }
        } catch (Exception e) {
            // 忽略
        }
        return 1;
    }

    /**
     * 获取单元格的垂直合并状态
     *
     * @param cell 单元格
     * @return 0=无合并, 1=合并起始(restart), -1=被合并(continue)
     */
    private static int getVMergeStatus(XWPFTableCell cell) {
        try {
            org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcPr tcPr =
                cell.getCTTc() != null ? cell.getCTTc().getTcPr() : null;
            if (tcPr == null) {
                return 0;
            }

            org.openxmlformats.schemas.wordprocessingml.x2006.main.CTVMerge vMerge = tcPr.getVMerge();
            if (vMerge == null) {
                return 0; // 无合并
            }

            String val = vMerge.getVal() != null ? vMerge.getVal().toString() : "";
            if ("restart".equalsIgnoreCase(val)) {
                return 1; // 合并起始
            } else if (val.isEmpty() || "continue".equalsIgnoreCase(val)) {
                return -1; // 被合并（需要跳过）
            }
            return 0;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 计算某一行中指定单元格的列起始位置
     * 考虑前面单元格的gridSpan
     */
    private static int getCellColPosition(XWPFTableRow row, int cellIndex) {
        List<XWPFTableCell> cells = row.getTableCells();
        int colPos = 0;
        for (int i = 0; i < cellIndex && i < cells.size(); i++) {
            colPos += getGridSpan(cells.get(i));
        }
        return colPos;
    }

    /**
     * 在指定行中找到列位置为targetColPos的单元格
     *
     * @return 单元格索引，如果没找到返回-1
     */
    private static int findCellAtColPosition(XWPFTableRow row, int targetColPos) {
        List<XWPFTableCell> cells = row.getTableCells();
        int colPos = 0;
        for (int i = 0; i < cells.size(); i++) {
            if (colPos == targetColPos) {
                return i;
            }
            colPos += getGridSpan(cells.get(i));
        }
        return -1;
    }

    /**
     * 获取行跨度（垂直合并）
     * DOCX中垂直合并：vMerge val="restart" 表示合并起始单元格，vMerge val="continue" 或无val表示被合并单元格
     *
     * @param cell     当前单元格
     * @param table    表格对象
     * @param rowIndex 当前行索引
     * @param colPos   当前单元格的列起始位置
     * @return 行跨度，0=被合并需要跳过，>0=合并起始的行跨度
     */
    private static int getRowSpan(XWPFTableCell cell, XWPFTable table, int rowIndex, int colPos) {
        int vMergeStatus = getVMergeStatus(cell);

        // 被合并的单元格（continue），返回0表示需要跳过
        if (vMergeStatus == -1) {
            return 0;
        }

        // 无合并或合并起始（restart），计算行跨度
        int span = 1;
        List<XWPFTableRow> rows = table.getRows();

        for (int r = rowIndex + 1; r < rows.size(); r++) {
            XWPFTableRow nextRow = rows.get(r);
            // 在下一行中找到相同列位置的单元格
            int nextCellIdx = findCellAtColPosition(nextRow, colPos);
            if (nextCellIdx < 0) {
                break; // 找不到对应列位置的单元格
            }

            XWPFTableCell nextCell = nextRow.getTableCells().get(nextCellIdx);
            int nextStatus = getVMergeStatus(nextCell);

            if (nextStatus == -1) {
                // 被合并的单元格，行跨度+1
                span++;
            } else {
                // 遇到新的起始或无合并，停止
                break;
            }
        }

        return span;
    }

    /**
     * 检查单元格是否需要段落间距支持
     * 放宽检测条件，只要有间距设置就返回true
     */
    private static boolean cellNeedsParagraphSpacing(XWPFTableCell cell) {
        try {
            List<XWPFParagraph> paragraphs = cell.getParagraphs();
            for (XWPFParagraph para : paragraphs) {
                // 检查段前间距
                if (para.getSpacingBefore() != 0) {
                    return true;
                }
                // 检查段后间距
                if (para.getSpacingAfter() != 0) {
                    return true;
                }
                // 检查是否有任何间距相关的XML设置
                try {
                    String xml = para.getCTP().toString();
                    // 只要包含w:spacing元素，就认为需要段落间距支持
                    // 这样可以捕获所有行距设置，包括倍数行距
                    if (xml.contains("w:spacing")) {
                        return true;
                    }
                } catch (Exception e) {
                    // 忽略
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 将Word段落转换为PDF段落
     * 支持完整的段落间距设置
     */
    private static com.itextpdf.text.Paragraph convertWordParagraphToPdfParagraph(
        XWPFParagraph wordPara, com.itextpdf.text.pdf.BaseFont baseFont) {

        com.itextpdf.text.Paragraph pdfPara = new com.itextpdf.text.Paragraph();
        float fontSize = 12f; // 在try块外定义，确保后面可以访问

        try {
            // 1. 设置段前间距
            int spacingBefore = wordPara.getSpacingBefore();
            if (spacingBefore > 0) {
                pdfPara.setSpacingBefore(spacingBefore / 20.0f);
            }

            // 2. 设置段后间距
            int spacingAfter = wordPara.getSpacingAfter();
            if (spacingAfter > 0) {
                pdfPara.setSpacingAfter(spacingAfter / 20.0f);
            }

            // 3. 设置行距（支持lineRule类型）
            List<XWPFRun> runs = wordPara.getRuns();
            if (runs != null && !runs.isEmpty()) {
                for (XWPFRun run : runs) {
                    Double runFontSize = run.getFontSizeAsDouble();
                    if (runFontSize != null && runFontSize > 0) {
                        fontSize = runFontSize.floatValue();
                        break;
                    }
                }
            }
            float leading = calculateParagraphLeading(wordPara, fontSize);
            pdfPara.setLeading(leading);

            // 4. 添加文本内容
            for (XWPFRun run : runs) {
                if (run == null) continue;

                String runText = run.getText(0);
                if (runText == null || runText.isEmpty()) {
                    continue;
                }

                Double runFontSize = run.getFontSizeAsDouble();
                if (runFontSize == null) {
                    runFontSize = 10.0;
                }

                int fontStyle = com.itextpdf.text.Font.NORMAL;
                if (run.isBold()) {
                    fontStyle |= com.itextpdf.text.Font.BOLD;
                }
                if (run.isItalic()) {
                    fontStyle |= com.itextpdf.text.Font.ITALIC;
                }

                com.itextpdf.text.pdf.BaseFont runBaseFont = baseFont;
                String runFontFamily = run.getFontFamily();
                if (runFontFamily != null && !runFontFamily.isEmpty()) {
                    runBaseFont = createChineseFontForTable(runFontFamily);
                    if (runBaseFont == null) {
                        runBaseFont = baseFont;
                    }
                }

                com.itextpdf.text.Font font = new com.itextpdf.text.Font(
                    runBaseFont != null ? runBaseFont : baseFont,
                    runFontSize.floatValue(), fontStyle);

                // 设置颜色
                String colorStr = run.getColor();
                if (colorStr != null && !colorStr.isEmpty()) {
                    try {
                        int rgb = Integer.parseInt(colorStr, 16);
                        int r = (rgb >> 16) & 0xFF;
                        int g = (rgb >> 8) & 0xFF;
                        int b = rgb & 0xFF;
                        font.setColor(new com.itextpdf.text.BaseColor(r, g, b));
                    } catch (NumberFormatException e) {
                        // 忽略
                    }
                }

                com.itextpdf.text.Chunk chunk = new com.itextpdf.text.Chunk(runText, font);

                // 处理下划线 - 通过反射获取底层XML
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
                    chunk.setUnderline(0.5f, 3f);
                }

                pdfPara.add(chunk);
            }

            // 5. 设置对齐方式
            ParagraphAlignment alignment = wordPara.getAlignment();
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

            // 6. 设置首行缩进
            int firstLineIndent = wordPara.getIndentationFirstLine();
            if (firstLineIndent != 0) {
                pdfPara.setFirstLineIndent(firstLineIndent / 20.0f);
            }

        } catch (Exception e) {
            // 出错时添加空段落
            pdfPara.add(new com.itextpdf.text.Chunk(" ", new com.itextpdf.text.Font(baseFont, 10)));
        }

        // 重要：检查段落是否为空或内容丢失
        // 有时候para.getText()有文本，但所有runs都没有或为空
        // 这种情况下，使用para.getText()作为回退
        String fullParaText = wordPara.getText();
        if (fullParaText != null && !fullParaText.trim().isEmpty()) {
            // 检查pdfPara的内容长度
            String pdfContent = pdfPara.getContent();
            int pdfLength = (pdfContent != null) ? pdfContent.length() : 0;
            int fullTextLength = fullParaText.trim().length();

            // 如果pdf内容明显少于完整文本（少于80%），说明有文本丢失
            // 使用完整文本替换
            if (pdfLength < fullTextLength * 0.8) {
                // 清空pdfPara并使用完整文本
                pdfPara = new com.itextpdf.text.Paragraph();

                // 重新设置间距
                int spacingBefore = wordPara.getSpacingBefore();
                if (spacingBefore > 0) {
                    pdfPara.setSpacingBefore(spacingBefore / 20.0f);
                }
                int spacingAfter = wordPara.getSpacingAfter();
                if (spacingAfter > 0) {
                    pdfPara.setSpacingAfter(spacingAfter / 20.0f);
                }
                float leading = calculateParagraphLeading(wordPara, fontSize);
                pdfPara.setLeading(leading);

                // 重新设置对齐方式（重要！）
                ParagraphAlignment alignment = wordPara.getAlignment();
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

                // 添加完整文本
                com.itextpdf.text.Chunk chunk = new com.itextpdf.text.Chunk(
                    fullParaText, new com.itextpdf.text.Font(baseFont, fontSize, com.itextpdf.text.Font.NORMAL));
                pdfPara.add(chunk);
            }
        }

        // 最后检查：如果段落还是完全空的，添加空格
        if (pdfPara.getChunks().isEmpty()) {
            com.itextpdf.text.Chunk emptyChunk = new com.itextpdf.text.Chunk(" ", new com.itextpdf.text.Font(baseFont, fontSize));
            pdfPara.add(emptyChunk);
        }

        return pdfPara;
    }
}
