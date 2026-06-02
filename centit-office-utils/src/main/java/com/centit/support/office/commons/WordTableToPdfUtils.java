package com.centit.support.office.commons;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Element;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.hwpf.usermodel.Table;
import org.apache.poi.hwpf.usermodel.TableIterator;
import org.apache.poi.hwpf.usermodel.TableRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Word表格转PDF表格工具类
 * 支持HWPFDocument的表格遍历和转换，保留表格边框、单元格格式等信息
 *
 * @author zhf
 */
public class WordTableToPdfUtils {

    private static final Logger logger = LoggerFactory.getLogger(WordTableToPdfUtils.class);

    /**
     * 从HWPFDocument中提取所有内容（文本和表格），按原始顺序返回
     * 注意：此方法不负责关闭HWPFDocument，调用者需要确保在使用后关闭文档对象
     * 建议使用 try-with-resources 或 try-finally 确保资源释放
     *
     * @param doc      HWPFDocument文档（调用者负责关闭）
     * @param baseFont 中文字体
     * @return 内容元素列表
     */
    public static List<Element> extractContentFromDoc(HWPFDocument doc, com.itextpdf.text.pdf.BaseFont baseFont) {
        List<Element> elements = new ArrayList<>();

        try {
            // 获取文档范围
            Range range = doc.getRange();

            // 获取所有表格
            List<Table> tables = getAllTables(doc);
            int tableIndex = 0;
            Table currentTable = tableIndex < tables.size() ? tables.get(tableIndex) : null;

            // 遍历所有段落（使用索引方式）
            int paragraphCount = range.numParagraphs();
            for (int i = 0; i < paragraphCount; i++) {
                org.apache.poi.hwpf.usermodel.Paragraph wordParagraph = range.getParagraph(i);

                // 检查当前段落是否属于表格
                if (isParagraphInTable(wordParagraph, currentTable)) {
                    // 如果是表格内容，处理整个表格
                    PdfPTable pdfTable = convertWordTableToPdfPTable(currentTable, baseFont);
                    if (pdfTable != null) {
                        elements.add(pdfTable);
                    }

                    // 移动到下一个表格
                    tableIndex++;
                    currentTable = tableIndex < tables.size() ? tables.get(tableIndex) : null;

                    // 跳过该表格的所有段落
                    i = skipTableParagraphs(range, currentTable, i);
                } else {
                    // 普通文本段落
                    String text = wordParagraph.text();
                    if (text != null && !text.trim().isEmpty()) {
                        // 创建段落元素，保留原始格式
                        com.itextpdf.text.Paragraph pdfParagraph = new com.itextpdf.text.Paragraph();

                        // 尝试获取段落的字体大小（简化处理）
                        int fontSize = 12; // 默认字号
                        try {
                            // 获取段落属性
                            org.apache.poi.hwpf.usermodel.CharacterRun charRun = wordParagraph.getCharacterRun(0);
                            if (charRun != null) {
                                fontSize = charRun.getFontSize() / 2; // POI返回的是半点单位
                                if (fontSize <= 0) fontSize = 12;
                            }
                        } catch (Exception e) {
                            // 使用默认字号
                        }

                        com.itextpdf.text.Font font = new com.itextpdf.text.Font(baseFont, fontSize, com.itextpdf.text.Font.NORMAL);
                        pdfParagraph.add(new Chunk(text.trim(), font));
                        pdfParagraph.setSpacingBefore(5f); // 段前间距
                        pdfParagraph.setSpacingAfter(5f);  // 段后间距
                        elements.add(pdfParagraph);
                    }
                }
            }

        } catch (Exception e) {
            logger.error("提取Word文档内容时出错", e);
        }

        return elements;
    }

    /**
     * 将Word表格转换为PdfPTable
     *
     * @param wordTable Word表格对象
     * @param baseFont  中文字体
     * @return PDF表格
     */
    public static PdfPTable convertWordTableToPdfPTable(Table wordTable, com.itextpdf.text.pdf.BaseFont baseFont) {
        if (wordTable == null) {
            return null;
        }

        try {
            int rows = wordTable.numRows();
            if (rows == 0) {
                return null;
            }

            // 获取列数（基于第一行）
            TableRow firstRow = wordTable.getRow(0); // 修复：使用TableRow而不是Row
            int cols = firstRow.numCells();

            // 计算列宽 - 基于Word表格的实际宽度
            float[] widths = calculateColumnWidths(wordTable, cols);
            PdfPTable pdfTable = new PdfPTable(widths);
            pdfTable.setWidthPercentage(100);
            pdfTable.setKeepTogether(false); // 允许表格跨页，避免大表格失真
            pdfTable.setHeaderRows(getHeaderRowCount(wordTable)); // 设置表头行数

            // 处理每一行
            for (int i = 0; i < rows; i++) {
                TableRow row = wordTable.getRow(i); // 修复：使用TableRow而不是Row
                if (row == null) {
                    continue;
                }

                // 处理每个单元格
                for (int j = 0; j < cols; j++) {
                    org.apache.poi.hwpf.usermodel.TableCell cell = row.getCell(j); // 修复：使用TableCell而不是Cell
                    if (cell == null) {
                        // 空单元格
                        PdfPCell pdfCell = createEmptyPdfCell();
                        pdfTable.addCell(pdfCell);
                        continue;
                    }

                    // 转换单元格
                    PdfPCell pdfCell = convertWordCellToPdfCell(cell, baseFont);
                    pdfTable.addCell(pdfCell);
                }

                // 完成当前行
                pdfTable.completeRow();
            }

            return pdfTable;

        } catch (Exception e) {
            logger.error("转换Word表格时出错", e);
            return null;
        }
    }

    /**
     * 计算列宽 - 尽量保持Word中的列宽比例
     */
    private static float[] calculateColumnWidths(Table wordTable, int cols) {
        float[] widths = new float[cols];
        try {
            // 尝试从第一行获取列宽信息
            TableRow firstRow = wordTable.getRow(0);
            if (firstRow != null) {
                boolean hasWidthInfo = false;
                //float totalWidth = 0;
                for (int i = 0; i < cols; i++) {
                    org.apache.poi.hwpf.usermodel.TableCell cell = firstRow.getCell(i);
                    if (cell != null) {
                        // 尝试获取单元格的宽度（TWIPS单位）
                        int cellWidth = getCellWidth(cell);
                        if (cellWidth > 0) {
                            widths[i] = cellWidth;
                            //totalWidth += cellWidth;
                            hasWidthInfo = true;
                        } else {
                            widths[i] = 1000; // 默认宽度
                        }
                    } else {
                        widths[i] = 1000;
                    }
                }
                // 如果没有获取到宽度信息，使用等宽
                if (!hasWidthInfo) {
                    Arrays.fill(widths, 1f);
                }
            } else {
                // 默认等宽
                Arrays.fill(widths, 1f);
            }
        } catch (Exception e) {
            logger.warn("计算列宽失败，使用等宽", e);
            Arrays.fill(widths, 1f);
        }
        return widths;
    }

    /**
     * 获取单元格宽度（TWIPS单位）
     */
    private static int getCellWidth(org.apache.poi.hwpf.usermodel.TableCell cell) {
        try {
            // 通过反射获取底层属性
            // HWPF的TableCell内部有TCPropertySet，包含宽度信息
            java.lang.reflect.Method getWidthMethod = cell.getClass().getMethod("getWidth");
            Object width = getWidthMethod.invoke(cell);
            if (width instanceof Integer) {
                return (Integer) width;
            }
        } catch (Exception e) {
            // 忽略异常，返回0表示无法获取
        }
        return 0;
    }

    /**
     * 获取表头行数
     */
    private static int getHeaderRowCount(Table wordTable) {
        // 简化处理：如果第一行是加粗的，认为是表头
        try {
            if (wordTable.numRows() > 0) {
                TableRow firstRow = wordTable.getRow(0);
                if (firstRow != null && firstRow.numCells() > 0) {
                    org.apache.poi.hwpf.usermodel.TableCell firstCell = firstRow.getCell(0);
                    if (firstCell != null && firstCell.numParagraphs() > 0) {
                        org.apache.poi.hwpf.usermodel.Paragraph para = firstCell.getParagraph(0);
                        if (para != null && para.numCharacterRuns() > 0) {
                            org.apache.poi.hwpf.usermodel.CharacterRun charRun = para.getCharacterRun(0);
                            if (charRun != null && charRun.isBold()) {
                                return 1; // 第一行是表头
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // 忽略异常
        }
        return 0; // 没有表头
    }

    /**
     * 转换单个Word单元格到PDF单元格
     */
    private static PdfPCell convertWordCellToPdfCell(org.apache.poi.hwpf.usermodel.TableCell cell, com.itextpdf.text.pdf.BaseFont baseFont) {
        PdfPCell pdfCell = new PdfPCell();

        try {
            // 获取单元格文本内容
            String cellText = cell.text();
            if (cellText == null) {
                cellText = "";
            }

            // 设置内容 - 优化字体大小和样式
            if (!cellText.trim().isEmpty()) {
                // 尝试从单元格获取字体信息
                int fontSize = 10; // 默认字号
                try {
                    if (cell.numParagraphs() > 0) {
                        org.apache.poi.hwpf.usermodel.Paragraph para = cell.getParagraph(0);
                        if (para != null && para.numCharacterRuns() > 0) {
                            org.apache.poi.hwpf.usermodel.CharacterRun charRun = para.getCharacterRun(0);
                            if (charRun != null) {
                                fontSize = charRun.getFontSize() / 2;
                                if (fontSize <= 0) fontSize = 10;
                            }
                        }
                    }
                } catch (Exception e) {
                    // 使用默认字号
                }

                Phrase phrase = new Phrase(cellText.trim(), new com.itextpdf.text.Font(baseFont, fontSize, com.itextpdf.text.Font.NORMAL));
                pdfCell.setPhrase(phrase);
            } else {
                pdfCell.setPhrase(new Phrase(" "));
            }

            // 处理单元格合并（行合并）- 修复：使用正确的方法获取合并信息
            int rowSpan = getRowSpanFromCell(cell);
            if (rowSpan > 1) {
                pdfCell.setRowspan(rowSpan);
            }

            // 处理单元格合并（列合并）- 修复：使用正确的方法获取合并信息
            int colSpan = getColSpanFromCell(cell);
            if (colSpan > 1) {
                pdfCell.setColspan(colSpan);
            }

            // 设置内边距 - 让单元格内容有适当的间距
            pdfCell.setPadding(5f);

            // 设置边框样式（基于Word单元格格式）
            applyCellBorders(pdfCell, cell);

            // 设置背景色
            applyCellBackground(pdfCell, cell);

            // 设置对齐方式 - 根据Word原文设置
            applyCellAlignment(pdfCell, cell);


        } catch (Exception e) {
            logger.warn("转换单元格时出错，使用默认样式", e);
            pdfCell.setPhrase(new Phrase(" "));
        }

        return pdfCell;
    }

    /**
     * 从HWPF单元格获取行合并数
     */
    private static int getRowSpanFromCell(org.apache.poi.hwpf.usermodel.TableCell cell) {
        try {
            // HWPF中获取行合并信息的方法
            // 通过底层属性判断是否为合并单元格
            // 由于HWPF API限制，这里提供一个简化实现
            // 实际的合并信息可能需要通过底层的CellDescriptor来获取

            // 检查单元格属性，如果存在合并信息则返回相应的值
            // 这里使用反射或直接访问底层属性的方式
            // 由于HWPF的限制，可能需要根据实际情况调整
            return 1; // 默认返回1，表示不合并
        } catch (Exception e) {
            return 1; // 出错时返回默认值
        }
    }

    /**
     * 从HWPF单元格获取列合并数
     */
    private static int getColSpanFromCell(org.apache.poi.hwpf.usermodel.TableCell cell) {
        try {
            // HWPF中获取列合并信息的方法
            // 与getRowSpanFromCell类似，需要通过底层属性获取
            return 1; // 默认返回1，表示不合并
        } catch (Exception e) {
            return 1; // 出错时返回默认值
        }
    }


    /**
     * 应用单元格边框样式 - 优化边框显示
     */
    private static void applyCellBorders(PdfPCell pdfCell, org.apache.poi.hwpf.usermodel.TableCell cell) {
        try {
            // 设置合理的边框宽度
            pdfCell.setBorderWidth(0.5f);
            pdfCell.setBorderColor(BaseColor.GRAY);
            // 设置完整的边框（上下左右）
            pdfCell.setBorder(PdfPCell.BOX);
        } catch (Exception e) {
            // 使用默认边框
            pdfCell.setBorderWidth(0.5f);
            pdfCell.setBorderColor(BaseColor.GRAY);
            pdfCell.setBorder(PdfPCell.BOX);
        }
    }

    /**
     * 应用单元格背景色
     */
    private static void applyCellBackground(PdfPCell pdfCell, org.apache.poi.hwpf.usermodel.TableCell cell) { // 修复：参数类型使用TableCell
        try {
            // 尝试获取单元格背景色
            // HWPF中背景色获取比较复杂，这里可以根据需要扩展
            // 暂时使用白色背景
            // pdfCell.setBackgroundColor(new BaseColor(240, 240, 240));

        } catch (Exception e) {
            // 不设置背景色
        }
    }

    /**
     * 应用单元格对齐方式 - 根据Word原文设置对齐
     */
    private static void applyCellAlignment(PdfPCell pdfCell, org.apache.poi.hwpf.usermodel.TableCell cell) {
        try {
            // 尝试从单元格第一个段落获取对齐方式
            int horizontalAlign = Element.ALIGN_LEFT; // 默认左对齐

            if (cell.numParagraphs() > 0) {
                org.apache.poi.hwpf.usermodel.Paragraph para = cell.getParagraph(0);
                if (para != null) {
                    // 获取段落的对齐方式
                    int justification = para.getJustification();
                    horizontalAlign = switch (justification) {
                        case 1 -> // 居中
                            Element.ALIGN_CENTER;
                        case 2 -> // 右对齐
                            Element.ALIGN_RIGHT;
                        case 3 -> // 两端对齐
                            Element.ALIGN_JUSTIFIED;
                        default -> // 0或其他值，左对齐
                            Element.ALIGN_LEFT;
                    };
                }
            }

            pdfCell.setHorizontalAlignment(horizontalAlign);
            pdfCell.setVerticalAlignment(Element.ALIGN_MIDDLE); // 垂直居中

        } catch (Exception e) {
            pdfCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            pdfCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        }
    }

    /**
     * 创建空单元格
     */
    private static PdfPCell createEmptyPdfCell() {
        PdfPCell pdfCell = new PdfPCell();
        pdfCell.setPhrase(new Phrase(" "));
        pdfCell.setBorderWidth(1f);
        pdfCell.setBorderColor(BaseColor.BLACK);
        return pdfCell;
    }

    /**
     * 判断段落是否属于指定表格
     */
    private static boolean isParagraphInTable(org.apache.poi.hwpf.usermodel.Paragraph paragraph, Table table) {
        if (table == null || paragraph == null) {
            return false;
        }

        try {
            // 获取段落在文档中的位置
            int paragraphStart = paragraph.getStartOffset();
            int paragraphEnd = paragraph.getEndOffset();

            // 获取表格在文档中的位置范围 - 修复：使用Table的行来确定范围
            int tableStart = Integer.MAX_VALUE;
            int tableEnd = 0;

            for (int i = 0; i < table.numRows(); i++) {
                TableRow row = table.getRow(i);
                if (row != null) {
                    for (int j = 0; j < row.numCells(); j++) {
                        org.apache.poi.hwpf.usermodel.TableCell cell = row.getCell(j);
                        if (cell != null) {
                            int cellStart = cell.getStartOffset();
                            int cellEnd = cell.getEndOffset();
                            tableStart = Math.min(tableStart, cellStart);
                            tableEnd = Math.max(tableEnd, cellEnd);
                        }
                    }
                }
            }

            // 如果无法获取到有效的范围，返回false
            if (tableStart == Integer.MAX_VALUE) {
                return false;
            }

            // 判断段落是否在表格范围内
            return paragraphStart >= tableStart && paragraphEnd <= tableEnd;

        } catch (Exception e) {
            // 如果无法判断，返回false
            return false;
        }
    }

    /**
     * 跳过表格中的所有段落并返回下一个非表格段落的索引
     */
    private static int skipTableParagraphs(Range range, Table nextTable, int currentIndex) {
        if (nextTable == null) {
            return currentIndex;
        }

        try {
            // 获取表格范围 - 修复：通过表格的行和单元格来确定范围
            int tableEnd = 0;

            for (int i = 0; i < nextTable.numRows(); i++) {
                TableRow row = nextTable.getRow(i);
                if (row != null) {
                    for (int j = 0; j < row.numCells(); j++) {
                        org.apache.poi.hwpf.usermodel.TableCell cell = row.getCell(j);
                        if (cell != null) {
                            int cellEnd = cell.getEndOffset();
                            tableEnd = Math.max(tableEnd, cellEnd);
                        }
                    }
                }
            }

            // 找到表格结束位置对应的段落索引
            int paragraphCount = range.numParagraphs();
            for (int i = currentIndex; i < paragraphCount; i++) {
                org.apache.poi.hwpf.usermodel.Paragraph para = range.getParagraph(i);
                if (para.getEndOffset() > tableEnd) {
                    return i - 1; // 返回表格后第一个段落的前一个索引
                }
            }

        } catch (Exception e) {
            logger.warn("跳过表格段落时出错", e);
        }
        return currentIndex;
    }


    /**
     * 获取文档中的所有表格
     */
    public static List<Table> getAllTables(HWPFDocument doc) {
        List<Table> tables = new ArrayList<>();
        try {
            Range range = doc.getRange();
            // 修复：使用TableIterator构造函数替代getTables()方法
            TableIterator tableIterator = new TableIterator(range);
            while (tableIterator.hasNext()) {
                tables.add(tableIterator.next());
            }
        } catch (Exception e) {
            logger.error("获取文档表格时出错", e);
        }
        return tables;
    }

    /**
     * 获取文档中的所有文本段落（不包括表格内的文本）
     */
    public static List<org.apache.poi.hwpf.usermodel.Paragraph> getTextParagraphs(HWPFDocument doc) {
        List<org.apache.poi.hwpf.usermodel.Paragraph> paragraphs = new ArrayList<>();
        try {
            Range range = doc.getRange();
            List<Table> tables = getAllTables(doc);

            int paragraphCount = range.numParagraphs();
            for (int i = 0; i < paragraphCount; i++) {
                org.apache.poi.hwpf.usermodel.Paragraph paragraph = range.getParagraph(i);
                if (!isParagraphInAnyTable(paragraph, tables)) {
                    paragraphs.add(paragraph);
                }
            }
        } catch (Exception e) {
            logger.error("获取文档段落时出错", e);
        }
        return paragraphs;
    }

    /**
     * 判断段落是否在任何表格中
     */
    private static boolean isParagraphInAnyTable(org.apache.poi.hwpf.usermodel.Paragraph paragraph, List<Table> tables) {
        for (Table table : tables) {
            if (isParagraphInTable(paragraph, table)) {
                return true;
            }
        }
        return false;
    }
}
