package com.centit.support.office.commons;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTDecimalNumber;

import java.util.List;

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
                for (int i = 0; i < colCount; i++) {
                    widths[i] = 1f;
                }
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
                for (int i = 0; i < colCount; i++) {
                    widths[i] = totalTableWidthPt / colCount;
                }
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
        for (int i = 0; i < colCount; i++) {
            widths[i] = 1f;
        }

        return widths;
    }

    /**
     * 从多行的单元格计算列宽（比只用第一行更准确）
     */
    private static float[] calculateColumnWidthsFromRows(List<XWPFTableRow> rows, int colCount) {
        float[] maxWidths = new float[colCount];
        boolean hasAnyWidth = false;

        // 遍历所有行，找出每列的最大宽度
        for (int rowIdx = 0; rowIdx < rows.size(); rowIdx++) {
            XWPFTableRow row = rows.get(rowIdx);
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
                        org.openxmlformats.schemas.wordprocessingml.x2006.main.CTDecimalNumber gridSpanObj =
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
                        for (int i = 0; i < rawWidths.length; i++) {
                            widths[i] = rawWidths[i];
                        }
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
            PdfPCell pdfCell = convertCellToPdf(cell, baseFont, isHeaderRow);

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
     */
    private static float getRowHeight(XWPFTableRow row) {
        float rowHeightPt = 0f;

        try {
            // 1. 尝试从DOCX行属性中获取行高
            Object ctRow = row.getCtRow();
            if (ctRow != null) {
                java.lang.reflect.Method getTrPrMethod = ctRow.getClass().getMethod("getTrPr");
                Object trPr = getTrPrMethod.invoke(ctRow);
                if (trPr != null) {
                    // 尝试获取高度属性
                    try {
                        java.lang.reflect.Method getHMethod = trPr.getClass().getMethod("getH");
                        Object hObj = getHMethod.invoke(trPr);
                        if (hObj != null) {
                            int hVal;
                            if (hObj instanceof java.math.BigInteger) {
                                hVal = ((java.math.BigInteger) hObj).intValue();
                            } else if (hObj instanceof Integer) {
                                hVal = (Integer) hObj;
                            } else {
                                hVal = Integer.parseInt(hObj.toString());
                            }
                            // DOCX中行高单位是twips (1/20 point)，需要转换为points
                            if (hVal > 0) {
                                rowHeightPt = hVal / 20.0f;
                            }
                        }
                    } catch (Exception e) {
                        // 没有设置行高，继续尝试其他方法
                    }
                }
            }

            // 2. 如果没有明确的行高设置，根据单元格内容估算
            if (rowHeightPt <= 0) {
                float maxRequiredHeight = 0f;
                List<XWPFTableCell> cells = row.getTableCells();

                for (XWPFTableCell cell : cells) {
                    float cellRequiredHeight = calculateCellRequiredHeight(cell);
                    if (cellRequiredHeight > maxRequiredHeight) {
                        maxRequiredHeight = cellRequiredHeight;
                    }
                }

                // 使用估算的最大高度，加上一些内边距
                if (maxRequiredHeight > 0) {
                    rowHeightPt = maxRequiredHeight + 2f; // 减小额外间距为2pt
                }
            }

            // 3. 设置最小行高，确保不会太小
            if (rowHeightPt < 10f) {
                rowHeightPt = 10f; // 进一步减小默认最小行高为10pt
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
                    int fontSize = run.getFontSize();
                    if (fontSize > 0) {
                        if ((float) fontSize > maxFontSize) {
                            maxFontSize = (float) fontSize;
                        }
                    }
                }
                // 只计算非空段落
                if (runs != null && !runs.isEmpty()) {
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

                // 修复换行问题：默认允许自动换行，保持原文档的换行行为
                // 只有在单元格内容非常短且确实是单个单词/数字时，才考虑禁用换行
                String cellText = cell.getText();
                if (cellText != null && cellText.length() > 0) {
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
            applyBorders(pdfCell, cell, isHeaderRow);

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
        Phrase phrase = new Phrase();

        try {
            List<XWPFParagraph> paragraphs = cell.getParagraphs();

            // 首先检查第一个段落的内容，用于后续过滤
            String firstParaText = "";
            if (!paragraphs.isEmpty()) {
                List<XWPFRun> firstRuns = paragraphs.get(0).getRuns();
                if (firstRuns != null) {
                    for (XWPFRun run : firstRuns) {
                        String text = run.getText(0);
                        if (text != null) {
                            firstParaText += text;
                        }
                    }
                }
            }

            boolean hasAnyContent = false;

            // 处理所有段落，段落之间添加换行符
            for (int p = 0; p < paragraphs.size(); p++) {
                XWPFParagraph para = paragraphs.get(p);
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
                        // 如果不是第一个段落，先添加换行符
                        if (hasAnyContent) {
                            // 使用 Chunk 创建换行符
                            com.itextpdf.text.Chunk newline = new com.itextpdf.text.Chunk("\n", new Font(defaultBaseFont, 10));
                            phrase.add(newline);
                        }

                        hasAnyContent = true;

                        // 处理当前段落的每个 run
                        for (int runIdx = 0; runIdx < runs.size(); runIdx++) {
                            XWPFRun run = runs.get(runIdx);
                            String runText = run.getText(0);
                            if (runText == null || runText.isEmpty()) {
                                continue;
                            }

                            // 提取字体大小
                            int fontSize = run.getFontSize();
                            if (fontSize <= 0) {
                                fontSize = 10; // 默认字号
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
                            Font font = new Font(runBaseFont, fontSize, fontStyle);

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
                    }
                }
            } // 结束段落循环

            // 如果没有任何内容，添加空格
            if (phrase.size() == 0) {
                phrase.add(" ");
            }

        } catch (Exception e) {
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
            // 忽略
        }

        return 10; // 默认字号
    }

    /**
     * 应用边框
     */
    private static void applyBorders(PdfPCell pdfCell, XWPFTableCell cell, boolean isHeaderRow) {
        try {
            // 设置统一的细边框
            pdfCell.setBorderWidth(0.5f);
            pdfCell.setBorderColor(BaseColor.GRAY);
            pdfCell.setBorder(PdfPCell.BOX);

            // 如果是表头行，增强底部边框以更好地区分标题行
            if (isHeaderRow) {
                // 为表头行设置更明显的底部边框
                try {
                    pdfCell.setBorderWidthBottom(1.5f);
                    // 增加表头行的底部间距，避免与表格线重合
                    pdfCell.setPaddingBottom(8f);
                } catch (Exception e) {
                    // 忽略设置失败
                }
            }

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
}
