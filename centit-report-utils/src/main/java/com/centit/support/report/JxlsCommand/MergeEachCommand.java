package com.centit.support.report.JxlsCommand;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import com.alibaba.fastjson2.JSONArray;
import com.centit.support.algorithm.ReflectionOpt;
import org.jxls.area.Area;
import org.jxls.command.AbstractCommand;
import org.jxls.common.CellRef;
import org.jxls.common.Context;
import org.jxls.common.Size;
import org.jxls.transform.Transformer;
import org.jxls.transform.poi.PoiTransformer;

/**
 * 支持合并单元格的 Each 命令
 * 使用方法：jx:mergeEach(items="list" var="item" cols="0,1,2")
 * cols 参数指定需要合并的列索引（从0开始），多个列用逗号分隔
 */
public class MergeEachCommand extends AbstractCommand {

    private String items;
    private String var;
    private String cols; // 需要合并的列，如 "0,1,2"

    @Override
    public String getName() {
        return "mergeEach";
    }

    @Override
    public Size applyAt(CellRef cellRef, Context context) {
        if (items == null || var == null) {
            throw new IllegalArgumentException("items and var attributes are required");
        }

        Object collection = resolveItems(context, items);
        if (!(collection instanceof Iterable) && !(collection instanceof JSONArray)) {
            throw new IllegalArgumentException("items must be an iterable collection or JSONArray");
        }

        Transformer transformer = getTransformer();
        if (!(transformer instanceof PoiTransformer)) {
            throw new IllegalStateException("Only PoiTransformer is supported");
        }

        PoiTransformer poiTransformer = (PoiTransformer) transformer;
        Sheet sheet = poiTransformer.getWorkbook().getSheet(cellRef.getSheetName());

        int startRow = cellRef.getRow();
        int startCol = cellRef.getCol();

        // 解析需要合并的列
        int[] mergeCols = parseMergeCols(cols);

        int rowIndex = startRow;
        Size size = null;

        if (collection instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) collection;
            for (int i = 0; i < jsonArray.size(); i++) {
                context.putVar(var, jsonArray.get(i));
                Area area = getAreaList().get(0);
                size = area.applyAt(new CellRef(cellRef.getSheetName(), rowIndex, startCol), context);
                if (mergeCols != null && mergeCols.length > 0 && rowIndex > startRow) {
                    mergeCellsIfNeeded(sheet, rowIndex, startRow, startCol, mergeCols, size.getWidth());
                }
                rowIndex += size.getHeight();
            }
            return size != null ? new Size(size.getWidth(), rowIndex - startRow) : new Size(0, 0);
        }

        for (Object item : (Iterable<?>) collection) {
            context.putVar(var, item);

            // 应用每个项的模板
            Area area = getAreaList().get(0);
            size = area.applyAt(new CellRef(cellRef.getSheetName(), rowIndex, startCol), context);

            // 如果需要合并单元格，处理合并逻辑
            if (mergeCols != null && mergeCols.length > 0 && rowIndex > startRow) {
                mergeCellsIfNeeded(sheet, rowIndex, startRow, startCol, mergeCols, size.getWidth());
            }

            rowIndex += size.getHeight();
        }

        return size != null ? new Size(size.getWidth(), rowIndex - startRow) : new Size(0, 0);
    }

    /**
     * 解析 items 属性，支持点号分隔的嵌套路径（如 "list.data"）
     */
    private Object resolveItems(Context context, String itemsExpr) {
        if (itemsExpr == null) return null;
        int dotIndex = itemsExpr.indexOf('.');
        if (dotIndex < 0) {
            return context.getVar(itemsExpr);
        }
        String rootVar = itemsExpr.substring(0, dotIndex);
        String path = itemsExpr.substring(dotIndex + 1);
        Object root = context.getVar(rootVar);
        if (root == null) return null;
        return ReflectionOpt.attainExpressionValue(root, path);
    }

    /**
     * 解析合并列配置
     */
    private int[] parseMergeCols(String colsStr) {
        if (colsStr == null || colsStr.trim().isEmpty()) {
            return null;
        }

        String[] parts = colsStr.split(",");
        int[] cols = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try {
                cols[i] = Integer.parseInt(parts[i].trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid column index: " + parts[i]);
            }
        }
        return cols;
    }

    /**
     * 获取单元格值的字符串表示
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }

    /**
     * 检查并合并单元格
     */
    private void mergeCellsIfNeeded(Sheet sheet, int currentRow, int startRow,
                                    int startCol, int[] mergeCols, int width) {
        for (int colIndex : mergeCols) {
            int absoluteCol = startCol + colIndex;
            if (absoluteCol >= startCol + width) {
                continue; // 超出范围，跳过
            }

            // 查找包含上一行的合并区域
            CellRangeAddress existingRegion = findMergedRegion(sheet, currentRow - 1, absoluteCol);

            // 检查当前单元格和上一行单元格的值是否相同
            Row prevRow = sheet.getRow(currentRow - 1);
            Row currRow = sheet.getRow(currentRow);

            if (prevRow == null || currRow == null) continue;

            Cell prevCell = prevRow.getCell(absoluteCol);
            Cell currCell = currRow.getCell(absoluteCol);

            if (prevCell == null || currCell == null) continue;

            String prevValue = getCellValueAsString(prevCell);
            String currValue = getCellValueAsString(currCell);

            if (prevValue == null || !prevValue.equals(currValue)) continue;

            if (existingRegion != null) {
                expandMergedRegion(sheet, existingRegion, currentRow);
            } else {
                sheet.addMergedRegion(new CellRangeAddress(
                        currentRow - 1, currentRow, absoluteCol, absoluteCol));
            }
        }
    }

    /**
     * 查找包含指定单元格的合并区域
     */
    private CellRangeAddress findMergedRegion(Sheet sheet, int row, int col) {
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress region = sheet.getMergedRegion(i);
            if (region.isInRange(row, col)) {
                return region;
            }
        }
        return null;
    }

    /**
     * 扩展现有的合并区域
     */
    private void expandMergedRegion(Sheet sheet, CellRangeAddress region, int newRow) {
        if (newRow > region.getLastRow()) {
            int firstRow = region.getFirstRow();
            int lastRow = newRow;
            int firstCol = region.getFirstColumn();
            int lastCol = region.getLastColumn();

            // 移除旧的合并区域
            for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
                if (sheet.getMergedRegion(i).equals(region)) {
                    sheet.removeMergedRegion(i);
                    break;
                }
            }

            // 添加新的合并区域
            sheet.addMergedRegion(new CellRangeAddress(firstRow, lastRow, firstCol, lastCol));
        }
    }

    public String getItems() {
        return items;
    }

    public void setItems(String items) {
        this.items = items;
    }

    public String getVar() {
        return var;
    }

    public void setVar(String var) {
        this.var = var;
    }

    public String getCols() {
        return cols;
    }

    public void setCols(String cols) {
        this.cols = cols;
    }
}
