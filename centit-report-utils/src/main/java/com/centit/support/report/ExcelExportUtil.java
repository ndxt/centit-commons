package com.centit.support.report;

import com.alibaba.fastjson2.JSONObject;
import com.centit.support.algorithm.*;
import com.centit.support.common.JavaBeanMetaData;
import com.centit.support.common.ObjectException;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.*;

/**
 * 生成基本EXCEL工具类
 *
 * @author codefan@sina.com
 * 2013-6-25
 */
@SuppressWarnings("unused")
public abstract class ExcelExportUtil {

    protected static final Logger logger = LoggerFactory.getLogger(ExcelExportUtil.class);

    private ExcelExportUtil() {
        throw new IllegalAccessError("Utility class");
    }

    /**
     * 生成Excel字节流
     *
     * @param outputStream 输出文件流
     * @param sheetName    pageName 页面名称
     * @param objLists     对象集合
     * @param header       Excel页头
     * @param property     需要显示的属性
     * @throws IOException 文件操作异常
     */
    public static void generateExcel(OutputStream outputStream, String sheetName,
                                     List<?> objLists, String[] header, String[] property) throws IOException {
        try(SXSSFWorkbook wb = new SXSSFWorkbook()) {
            Sheet sheet = wb.createSheet(sheetName);
            generateExcelSheet(sheet, objLists, header, property);
            wb.write(outputStream);
        }
    }

    /**
     * 生成Excel字节流
     *
     * @param outputStream 输出文件流
     * @param objLists     对象集合
     * @param header       Excel页头
     * @param property     需要显示的属性
     * @throws IOException 文件操作异常
     */
    public static void generateExcel(OutputStream outputStream, List<?> objLists, String[] header, String[] property) throws IOException {
        try(SXSSFWorkbook wb = new SXSSFWorkbook()) {
            Sheet sheet = wb.createSheet();
            generateExcelSheet(sheet, objLists, header, property);
            wb.write(outputStream);
        }
    }

    /**
     * 生成Excel字节流
     *
     * @param outputStream 输出文件流
     * @param sheetName    pageName 页面名称
     * @param objLists     对象集合
     * @param header       Excel页头
     * @throws IOException 文件操作异常
     */
    public static void generateExcel(OutputStream outputStream, String sheetName,
                                     List<Object[]> objLists, String[] header) throws IOException {
        try(SXSSFWorkbook wb = new SXSSFWorkbook()) {
            Sheet sheet = wb.createSheet(sheetName);
            generateExcelSheet(sheet, objLists, header);
            wb.write(outputStream);
        }
    }

    public static void generateExcel(OutputStream outputStream, List<Object[]> objLists, String[] header) throws IOException {
        try(SXSSFWorkbook wb = new SXSSFWorkbook()) {
            Sheet sheet = wb.createSheet();
            generateExcelSheet(sheet, objLists, header);
            wb.write(outputStream);
        }
    }

    /**
     * 生成Excel字节流
     *
     * @param outputStream 输出文件流
     * @param sheetName    pageName 页面名称
     * @param objLists     对象集合
     * @param objType      对象类型
     * @throws IOException 文件操作异常
     **/
    public static void generateExcel(OutputStream outputStream, String sheetName,
                                     List<?> objLists, Class<?> objType) throws IOException {
        try(SXSSFWorkbook wb = new SXSSFWorkbook()) {
            Sheet sheet = wb.createSheet(sheetName);
            generateExcelSheet(sheet, objLists, objType);
            wb.write(outputStream);
        }
    }

    public static void generateExcel(OutputStream outputStream, List<?> objLists, Class<?> objType) throws IOException {
        try(SXSSFWorkbook wb = new SXSSFWorkbook()) {
            Sheet sheet = wb.createSheet();
            generateExcelSheet(sheet, objLists, objType);
            wb.write(outputStream);
        }
    }

    /**
     * 生成Excel 2003字节流
     *
     * @param outputStream 输出文件流
     * @param sheetName    pageName 页面名称
     * @param objLists     对象集合
     * @param header       Excel页头
     * @param property     需要显示的属性
     * @throws IOException 文件操作异常
     **/
    public static void generateExcel2003(OutputStream outputStream, String sheetName,
                                         List<?> objLists, String[] header, String[] property) throws IOException {
        try(HSSFWorkbook wb = new HSSFWorkbook()) {
            Sheet sheet = wb.createSheet(sheetName);
            generateExcelSheet(sheet, objLists, header, property);
            wb.write(outputStream);
        }
    }

    public static void generateExcel2003(OutputStream outputStream, List<?> objLists,
                                         String[] header, String[] property) throws IOException {
        try(HSSFWorkbook wb = new HSSFWorkbook()) {
            Sheet sheet = wb.createSheet();
            generateExcelSheet(sheet, objLists, header, property);
            wb.write(outputStream);
        }
    }

    /**
     * 生成Excel 2003字节流
     *
     * @param outputStream 输出文件流
     * @param sheetName    pageName 页面名称
     * @param objLists     对象集合
     * @param header       Excel页头
     * @throws IOException 文件操作异常
     **/
    public static void generateExcel2003(OutputStream outputStream, String sheetName,
                                         List<Object[]> objLists, String[] header) throws IOException {
        try(HSSFWorkbook wb = new HSSFWorkbook()) {
            Sheet sheet = wb.createSheet(sheetName);
            generateExcelSheet(sheet, objLists, header);
            wb.write(outputStream);
        }
    }

    public static void generateExcel2003(OutputStream outputStream, List<Object[]> objLists, String[] header) throws IOException {
        try(HSSFWorkbook wb = new HSSFWorkbook()) {
            Sheet sheet = wb.createSheet();
            generateExcelSheet(sheet, objLists, header);
            wb.write(outputStream);
        }
    }

    /**
     * 生成Excel 2003 字节流
     *
     * @param outputStream 输出文件流
     * @param sheetName    pageName 页面名称
     * @param objLists     对象集合
     * @param objType      对象类型
     * @throws IOException 文件操作异常
     **/
    public static void generateExcel2003(OutputStream outputStream, String sheetName,
                                         List<?> objLists, Class<?> objType) throws IOException {
        try(HSSFWorkbook wb = new HSSFWorkbook()) {
            Sheet sheet = wb.createSheet(sheetName);
            generateExcelSheet(sheet, objLists, objType);
            wb.write(outputStream);
        }
    }

    public static void generateExcel2003(OutputStream outputStream, List<?> objLists, Class<?> objType) throws IOException {
        try(HSSFWorkbook wb = new HSSFWorkbook()) {
            Sheet sheet = wb.createSheet();
            generateExcelSheet(sheet, objLists, objType);
            wb.write(outputStream);
        }
    }
//--------------------------------------------------------------------

    /**
     * 生成Excel字节流
     *
     * @param sheetName pageName 页面名称
     * @param objLists  对象集合
     * @param header    Excel页头
     * @param property  需要显示的属性
     * @return InputStream 输出文件流
     * @throws IOException 文件操作异常
     */
    public static InputStream generateExcelStream(String sheetName,
                                                  List<?> objLists, String[] header, String[] property) throws IOException {
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream()) {
            generateExcel(bout, sheetName, objLists, header, property);
            return new ByteArrayInputStream(bout.toByteArray());
        }
    }

    public static InputStream generateExcelStream(List<?> objLists, String[] header, String[] property) throws IOException {
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream()) {
            generateExcel(bout, objLists, header, property);
            return new ByteArrayInputStream(bout.toByteArray());
        }
    }

    /**
     * 生成Excel字节流
     *
     * @param sheetName pageName 页面名称
     * @param objLists  对象集合
     * @param header    Excel页头
     * @return InputStream 输出文件流
     * @throws IOException 文件操作异常
     */
    public static InputStream generateExcelStream(String sheetName,
                                                  List<Object[]> objLists, String[] header) throws IOException {
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream()) {
            generateExcel(bout, sheetName, objLists, header);
            return new ByteArrayInputStream(bout.toByteArray());
        }
    }

    public static InputStream generateExcelStream(List<Object[]> objLists, String[] header) throws IOException {
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream()) {
            generateExcel(bout, objLists, header);
            return new ByteArrayInputStream(bout.toByteArray());
        }
    }

    /**
     * 生成Excel字节流
     *
     * @param sheetName pageName 页面名称
     * @param objLists  对象集合
     * @param objType   对象类型
     * @return InputStream  输出文件流
     * @throws IOException 文件操作异常
     **/
    public static InputStream generateExcelStream(String sheetName,
                                                  List<?> objLists, Class<?> objType) throws IOException {
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream()) {
            generateExcel(bout, sheetName, objLists, objType);
            return new ByteArrayInputStream(bout.toByteArray());
        }
    }

    public static InputStream generateExcelStream(List<?> objLists, Class<?> objType) throws IOException {
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream()) {
            generateExcel(bout, objLists, objType);
            return new ByteArrayInputStream(bout.toByteArray());
        }
    }

    /**
     * 生成Excel 2003字节流
     *
     * @param sheetName pageName 页面名称
     * @param objLists  对象集合
     * @param header    Excel页头
     * @param property  需要显示的属性
     * @return InputStream  输出文件流
     * @throws IOException 文件操作异常
     **/
    public static InputStream generateExcel2003Stream(String sheetName, List<?> objLists, String[] header, String[] property) throws IOException {
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream()) {
            generateExcel2003(bout, sheetName, objLists, header, property);
            return new ByteArrayInputStream(bout.toByteArray());
        }
    }

    public static InputStream generateExcel2003Stream(List<?> objLists,
                                                      String[] header, String[] property) throws IOException {
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream()) {
            generateExcel2003(bout, objLists, header, property);
            return new ByteArrayInputStream(bout.toByteArray());
        }
    }

    /**
     * 生成Excel 2003字节流
     *
     * @param sheetName pageName 页面名称
     * @param objLists  对象集合
     * @param header    Excel页头
     * @return InputStream  输出文件流
     * @throws IOException 文件操作异常
     **/
    public static InputStream generateExcel2003Stream(String sheetName,
                                                      List<Object[]> objLists, String[] header) throws IOException {
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream()) {
            generateExcel2003(bout, sheetName, objLists, header);
            return new ByteArrayInputStream(bout.toByteArray());
        }
    }

    public static InputStream generateExcel2003Stream(List<Object[]> objLists, String[] header) throws IOException {
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream()) {
            generateExcel2003(bout, objLists, header);
            return new ByteArrayInputStream(bout.toByteArray());
        }
    }

    /**
     * 生成Excel 2003 字节流
     *
     * @param sheetName pageName 页面名称
     * @param objLists  对象集合
     * @param objType   对象类型
     * @return InputStream  输出文件流
     * @throws IOException 文件操作异常
     **/
    public static InputStream generateExcel2003Stream(String sheetName,
                                                      List<?> objLists, Class<?> objType) throws IOException {
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream()) {
            generateExcel2003(bout, sheetName, objLists, objType);
            return new ByteArrayInputStream(bout.toByteArray());
        }
    }

    public static InputStream generateExcel2003Stream(List<?> objLists, Class<?> objType) throws IOException {
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream()) {
            generateExcel2003(bout, objLists, objType);
            return new ByteArrayInputStream(bout.toByteArray());
        }
    }

    //---------------------------------------------------------------------
    public static void generateExcelSheet(Sheet sheet, List<?> objLists, Class<?> objType) {
        JavaBeanMetaData metaData = JavaBeanMetaData.createBeanMetaDataFromType(objType);

        Row headerRow = sheet.createRow(0);
        CellStyle cellStyle = getDefaultCellStyle(sheet.getWorkbook());
        List<String> header = new ArrayList<>(metaData.getFileds().keySet());
        int i = 0;
        for (String headStr : header) {
            Cell cell = headerRow.createCell(i);
            setCellStyle(cell, cellStyle);
            cell.setCellValue(headStr);
            i++;
        }

        int row = 1;
        for (Object obj : objLists) {
            Row objRow = sheet.createRow(row++);
            i = 0;
            for (String headStr : header) {
                Cell cell = objRow.createCell(i++);
                setCellStyle(cell, cellStyle);
                cell.setCellValue(StringBaseOpt.objectToString(metaData.getFiled(headStr).getObjectFieldValue(obj)));
            }
        }
    }

    /**
     * 生成Excel字节流
     *
     * @param sheet    excel页面
     * @param objLists 对象集合
     * @param header   Excel页头
     * @param property 需要显示的属性
     */
    public static void generateExcelSheet(Sheet sheet, List<?> objLists,
                                          String[] header, String[] property) {

        int beginRow = 0;
        if (header != null && header.length > 0) {
            generateExcelHeader(sheet, header);
            beginRow++;
        }

        try {
            if (property != null && property.length > 0) {
                generateExcelText(sheet, objLists, property, beginRow);
            }
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException | NoSuchFieldException e) {
            throw new ObjectException(e);
        }
    }


    /**
     * 生成Excel字节流
     *
     * @param sheet    excel页面
     * @param objLists 对象数组集合
     * @param header   Excel页头
     */
    public static void generateExcelSheet(Sheet sheet, List<Object[]> objLists, String[] header) {
        int beginRow = 0;
        if (header != null && header.length > 0) {
            generateExcelHeader(sheet, header);
            beginRow++;
        }
        generateExcelText(sheet, objLists, beginRow);
    }


    private static void generateExcelHeader(Sheet sheet, String[] header) {
        Row headerRow = sheet.createRow(0);
        CellStyle cellStyle = getDefaultCellStyle(sheet.getWorkbook());
        for (int i = 0; i < header.length; i++) {
            Cell cell = headerRow.createCell(i);
            setCellStyle(cell, cellStyle);
            cell.setCellValue(header[i]);
        }
    }

    //@SuppressWarnings("unchecked")
    private static void generateExcelText(Sheet sheet, List<?> objLists, String[] property, int beginRow) throws NoSuchFieldException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        CellStyle cellStyle = getDefaultCellStyle(sheet.getWorkbook());
        for (int i = 0; i < objLists.size(); i++) {
            Row textRow = sheet.createRow(i + beginRow);
            JSONObject obj = JSONObject.from(objLists.get(i));
            for (int j = 0; j < property.length; j++) {
                if (StringUtils.isNotBlank(property[j])) {
//                    CellStyle cellStyle = getDefaultCellStyle(sheet.getWorkbook());
                    Cell cell = textRow.createCell(j);
//                  setCellStyle(cell, cellStyle);
//                  cell.setCellValue( StringBaseOpt.objectToString(
//                  ReflectionOpt.attainExpressionValue(objLists.get(i), property[j])));

                    Object objectValue = obj.get(property[j]);
                    String type = objectValue != null ? objectValue.getClass().getSimpleName() : "String";
                    setCellStyle(sheet, cell, cellStyle, type);
                    setCellValue(cell, objectValue, type);
                }
            }
        }
    }


    private static void generateExcelText(Sheet sheet, List<Object[]> objLists, int beginRow) {
        for (int i = 0; i < objLists.size(); i++) {
            Row textRow = sheet.createRow(i + beginRow);
            CellStyle cellStyle = getDefaultCellStyle(sheet.getWorkbook());
            for (int j = 0; j < objLists.get(i).length; j++) {
                Cell cell = textRow.createCell(j);
                setCellStyle(cell, cellStyle);
                cell.setCellValue(null == objLists.get(i)[j] ? "" : StringBaseOpt.objectToString(objLists.get(i)[j]));
            }
        }
    }

    private static void setCellStyle(Cell cell, CellStyle cellStyle) {
        cell.setCellType(CellType.STRING);
        cell.setCellStyle(cellStyle);
    }

    private static void setCellStyle(Sheet sheet, Cell cell, CellStyle cellStyle, String type) {
        switch (type) {
            case "int":
            case "Integer":
            case "long":
            case "Long":
            case "float":
            case "Float":
            case "double":
            case "Double":
            case "BigDecimal":
                cell.setCellType(CellType.NUMERIC);
                break;
            case "String":
                cell.setCellType(CellType.STRING);
                break;
            case "boolean":
            case "Boolean":
                cell.setCellType(CellType.BOOLEAN);
                break;
            case "Date":
            case "Timestamp":
                cell.setCellType(CellType.NUMERIC);
                DataFormat format = sheet.getWorkbook().createDataFormat();
                cellStyle.setDataFormat(format.getFormat("yyyy-MM-dd"));
                break;
            default:
                cell.setCellType(CellType.STRING);
                break;
        }
        cell.setCellStyle(cellStyle);
    }

    private static void setCellValue(Cell cell, Object obj, String type) {
        switch (type) {
            case "int":
            case "Integer":
                cell.setCellValue(NumberBaseOpt.castObjectToInteger(obj));
                break;
            case "long":
            case "Long":
                cell.setCellValue(NumberBaseOpt.castObjectToLong(obj));
                break;
            case "float":
            case "Float":
            case "double":
            case "Double":
                cell.setCellValue(NumberBaseOpt.castObjectToDouble(obj));
                break;
            case "String":
                cell.setCellValue(StringBaseOpt.objectToString(obj));
                break;
            case "Date":
            case "Timestamp":
                cell.setCellValue(DatetimeOpt.castObjectToDate(obj));
                break;
            default:
                cell.setCellValue(StringBaseOpt.objectToString(obj));
                break;
        }
    }

    /*
     * 设置单元格默认样式
     *
     */
    private static CellStyle getDefaultCellStyle(Workbook wb) {
        CellStyle cellStyle = wb.createCellStyle();
        // 指定单元格居中对齐
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        // 指定单元格垂直居中对齐
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
//        cellStyle.setWrapText(true);// 指定单元格自动换行
        // 设置单元格字体
        Font font = wb.createFont();
//        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        font.setFontName("宋体");
//        font.setFontHeight((short) 300);
        cellStyle.setFont(font);

        return cellStyle;
    }
    private static void createNewRowsForSaveData(Sheet sheet, int beginRow, int nRowCount){
        Row excelRow = sheet.getRow(beginRow);
        if(excelRow==null){
            for(int i=0; i<nRowCount; i++ ){
                Row toRow = sheet.getRow(beginRow + i);
                if(toRow==null){
                    sheet.createRow(beginRow + i);
                }
            }
            return;
        }

        int nColCount = excelRow.getLastCellNum() + 1;
        int lastRow = sheet.getLastRowNum();
        if(beginRow < lastRow && nRowCount>1) {
            sheet.shiftRows(beginRow+1, lastRow, nRowCount-1, true, false);
        }

        for(int i=1; i<nRowCount; i++)
            copyRow(sheet.getWorkbook(), sheet, beginRow, beginRow+i, 1,   nColCount);
    }

    public static Set<Integer> praiseColsRangeDesc(String mergeColCellDesc){
        if(StringUtils.isBlank(mergeColCellDesc) || "0".equals(mergeColCellDesc)) return null;
        Set<Integer> mergeCols = new HashSet<>();
        if(StringRegularOpt.isDigit(mergeColCellDesc)){
            for(int i=0; i < Integer.parseInt(mergeColCellDesc); i++){
                mergeCols.add(i);
            }
        }
        String [] mergeCol= mergeColCellDesc.split(",");
        for(String mergeColCell : mergeCol){
            if(StringUtils.isBlank(mergeColCell)) continue;
            if(mergeColCell.contains("-")){
                String [] colRang = mergeColCell.split("-");
                if(colRang.length==1){
                    mergeCols.add(ExcelImportUtil.mapColumnIndex(colRang[0].trim()));
                } else if(colRang.length>1){
                    int beginCol = ExcelImportUtil.mapColumnIndex(colRang[0].trim());
                    int endCol = ExcelImportUtil.mapColumnIndex(colRang[1].trim());
                    if(beginCol<=endCol) {
                        for (int i = beginCol; i <= endCol; i++) {
                            mergeCols.add(i);
                        }
                    }
                }
            }else{
                mergeCols.add(ExcelImportUtil.mapColumnIndex(mergeColCell.trim()));
            }
        }

        return mergeCols;
    }
    private static void mergeColCell(Sheet sheet, String mergeColCellDesc, int beginRow, int endRow){
        Set<Integer> mergeCols  = praiseColsRangeDesc(mergeColCellDesc);
        if(mergeCols==null || mergeCols.isEmpty()) return;
        for(int i : mergeCols){
            String preCellText = "";// ExcelImportUtil.getCellString( )
            int preBeginRow = beginRow;
            for(int j=beginRow; j<endRow; j++){

                String cellText = ExcelImportUtil.getCellString(ExcelImportUtil.getCell(sheet, j, i));
                if(!StringUtils.equals(cellText, preCellText)){
                    int mergeEndRow = j-1;
                    if(mergeEndRow>preBeginRow) {
                        Cell currentCell = ExcelImportUtil.getCell(sheet, preBeginRow, i);
                        CellStyle style = currentCell.getCellStyle();
                        //CellStyle newStyle = sheet.getWorkbook().createCellStyle();
                        //copyCellStyle(style, newStyle, sheet.getWorkbook());

                        sheet.addMergedRegion(new CellRangeAddress(preBeginRow, mergeEndRow, i, i));
                        Cell newCell = ExcelImportUtil.getCell(sheet, preBeginRow, i);
                        newCell.setCellStyle(style);
                        newCell.setCellValue(preCellText);
                    }
                    preCellText = cellText;
                    preBeginRow = j;
                }
            }
            if(endRow-1 > preBeginRow) {
                sheet.addMergedRegion(new CellRangeAddress(preBeginRow, endRow-1, i, i));
                Cell newCell = ExcelImportUtil.getCell(sheet, preBeginRow, i);
                newCell.setCellValue(preCellText);
            }
            //CellRangeAddress
        }
    }
    public static void setCellValue(Cell cell, Object value){
        if(value==null)
            return;

        if(value instanceof Date){
            cell.setCellValue((Date) value);
        } else if(value instanceof Number){
            cell.setCellValue(((Number) value).doubleValue());
        } else if(value instanceof Boolean){
            cell.setCellValue((Boolean) value);
        } else {
            cell.setCellValue(StringBaseOpt.castObjectToString(value));
        }
    }
    public static void saveObjectsToExcelSheet(Sheet sheet, List<?> objects, Map<Integer, String> fieldDesc, int beginRow,
                                               boolean createRow, String mergeColCellDesc) {
        int nRowCount = objects.size();
        //CellStyle cellStyle = getDefaultCellStyle(sheet.getWorkbook());
        if(createRow)
            createNewRowsForSaveData(sheet, beginRow, nRowCount);

        for (int i = 0; i < nRowCount; i++) {
            Row excelRow = /*createRow ? sheet.createRow(beginRow + i) : */sheet.getRow(beginRow + i);
            Object rowObj = objects.get(i);
            if (rowObj != null && excelRow != null) {

                for (Map.Entry<Integer, String> ent : fieldDesc.entrySet()) {
                    Cell cell = excelRow.getCell(ent.getKey());
                    if (cell == null) {
                        cell = excelRow.createCell(ent.getKey());
                    }
                    setCellValue(cell, ReflectionOpt.attainExpressionValue(rowObj, ent.getValue()));
                }
            }
        }
        //mergeColCell;
        mergeColCell(sheet, mergeColCellDesc, beginRow, beginRow+nRowCount);
    }

    public static void saveObjectsToExcelSheet(Sheet sheet, List<Object[]> objects, int beginCol, int beginRow, boolean createRow, String mergeColCellDesc) {
        int nRowCount = objects.size();
        //CellStyle cellStyle = getDefaultCellStyle(sheet.getWorkbook());
        if(createRow)
            createNewRowsForSaveData(sheet, beginRow, nRowCount);

        for (int i = 0; i < nRowCount; i++) {
            Row excelRow = sheet.getRow(beginRow + i);
            Object[] rowObj = objects.get(i);

            if (rowObj != null && excelRow != null) {
                for (int j = 0; j < rowObj.length; j++) {
                    Cell cell = null;
                    if (!createRow) {
                        cell = excelRow.getCell(beginCol + j);
                    }

                    if (cell == null) {
                        cell = excelRow.createCell(beginCol + j);
                        //setCellStyle(cell, cellStyle);
                    }
                    setCellValue(cell, rowObj[j]);
                }
            }
        }
        mergeColCell(sheet, mergeColCellDesc, beginRow, beginRow + nRowCount);
    }

    /**
     * 保存对象到 Excel 文件
     *
     * @param excelTemplateFilePath 文件
     * @param excelFilePath         文件
     * @param sheetName             sheet名称
     * @param objects               对象数组
     * @param fieldDesc             列和字段对应关系
     * @param beginRow              写入起始行
     * @param createRow             是否 创建（插入）行 还是直接覆盖
     * @throws IOException 文件存储异常
     */
    public static void generateExcelByTemplate(String excelTemplateFilePath, String excelFilePath, String sheetName,
                                               List<?> objects, Map<Integer, String> fieldDesc, int beginRow, boolean createRow) throws IOException {

        ExcelTypeEnum excelType = ExcelTypeEnum.checkFileExcelType(excelTemplateFilePath);
        Workbook wb;

        try (InputStream excelFile = Files.newInputStream(new File(excelTemplateFilePath).toPath())) {
            wb = excelType == ExcelTypeEnum.HSSF ? new HSSFWorkbook(excelFile) : new XSSFWorkbook(excelFile);
            Sheet sheet = (StringUtils.isBlank(sheetName)) ? wb.getSheetAt(0) : wb.getSheet(sheetName);
            saveObjectsToExcelSheet(sheet, objects, fieldDesc, beginRow, createRow, "");
        }

        try (OutputStream newExcelFile = Files.newOutputStream(new File(excelFilePath).toPath())) {
            wb.write(newExcelFile);
        }
    }


    /**
     * 保存对象到 Excel 文件
     *
     * @param excelTemplateFilePath 文件
     * @param excelFilePath         文件
     * @param sheetIndex            sheet 索引
     * @param objects               对象数组
     * @param fieldDesc             列和字段对应关系
     * @param beginRow              写入起始行
     * @param createRow             是否 创建（插入）行 还是直接覆盖
     * @throws IOException 文件存储异常
     */
    public static void generateExcelByTemplate(String excelTemplateFilePath, String excelFilePath, int sheetIndex,
                                               List<?> objects, Map<Integer, String> fieldDesc, int beginRow, boolean createRow) throws IOException {

        ExcelTypeEnum excelType = ExcelTypeEnum.checkFileExcelType(excelTemplateFilePath);
        Workbook wb;

        try (InputStream excelFile = Files.newInputStream(new File(excelTemplateFilePath).toPath())) {
            wb = excelType == ExcelTypeEnum.HSSF ? new HSSFWorkbook(excelFile) : new XSSFWorkbook(excelFile);
            Sheet sheet = wb.getSheetAt(sheetIndex);
            saveObjectsToExcelSheet(sheet, objects, fieldDesc, beginRow, createRow, "");
        }

        try (OutputStream newExcelFile = Files.newOutputStream(new File(excelFilePath).toPath())) {
            wb.write(newExcelFile);
        }
    }


    /**
     * 保存二维数组到 Excel 文件
     *
     * @param excelTemplateFilePath 文件
     * @param excelFilePath         文件
     * @param sheetName             sheet 名称
     * @param objects               二维数组
     * @param beginCol              写入起始列
     * @param beginRow              写入起始行
     * @param createRow             是否 创建（插入）行 还是直接覆盖
     * @throws IOException 文件存储异常
     */
    public static void generateExcelByTemplate(String excelTemplateFilePath, String excelFilePath, String sheetName, List<Object[]> objects, int beginCol, int beginRow, boolean createRow) throws IOException {

        ExcelTypeEnum excelType = ExcelTypeEnum.checkFileExcelType(excelTemplateFilePath);
        Workbook wb;

        try (InputStream excelFile = Files.newInputStream(new File(excelTemplateFilePath).toPath())) {
            wb = excelType == ExcelTypeEnum.HSSF ? new HSSFWorkbook(excelFile) : new XSSFWorkbook(excelFile);
            Sheet sheet = (StringUtils.isBlank(sheetName)) ? wb.getSheetAt(0) : wb.getSheet(sheetName);
            saveObjectsToExcelSheet(sheet, objects, beginCol, beginRow, createRow, "");
        }

        try (OutputStream newExcelFile = Files.newOutputStream(new File(excelFilePath).toPath())) {
            wb.write(newExcelFile);
        }
    }

    /**
     * 保存二维数组到 Excel 文件
     *
     * @param excelTemplateFilePath 文件
     * @param excelFilePath         文件
     * @param sheetIndex            sheet 索引
     * @param objects               二维数组
     * @param beginCol              写入起始列
     * @param beginRow              写入起始行
     * @param createRow             是否 创建（插入）行 还是直接覆盖
     * @throws IOException 文件存储异常
     */
    public static void generateExcelByTemplate(String excelTemplateFilePath, String excelFilePath, int sheetIndex, List<Object[]> objects, int beginCol, int beginRow, boolean createRow) throws IOException {

        ExcelTypeEnum excelType = ExcelTypeEnum.checkFileExcelType(excelTemplateFilePath);
        Workbook wb;

        try (InputStream excelFile = Files.newInputStream(new File(excelTemplateFilePath).toPath())) {
            wb = excelType == ExcelTypeEnum.HSSF ? new HSSFWorkbook(excelFile) : new XSSFWorkbook(excelFile);
            Sheet sheet = wb.getSheetAt(sheetIndex);
            saveObjectsToExcelSheet(sheet, objects, beginCol, beginRow, createRow, "");
        }

        try (OutputStream newExcelFile = Files.newOutputStream(new File(excelFilePath).toPath())) {
            wb.write(newExcelFile);
        }
    }

    public static void appendDataToExcelSheet(String excelFilePath, String sheetName,
                                              List<?> objLists, String[] header, String[] property) throws IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, NoSuchFieldException {

        ExcelTypeEnum excelType = ExcelTypeEnum.checkFileExcelType(excelFilePath);
        Workbook wb;
        Sheet sheet;
        File file = new File(excelFilePath);
        if (file.exists()) {
            InputStream excelFile = Files.newInputStream(file.toPath());
            wb = excelType == ExcelTypeEnum.HSSF ? new HSSFWorkbook(excelFile) : new XSSFWorkbook(excelFile);
            sheet = wb.getSheet(sheetName);
            if(sheet==null){
                sheet=wb.createSheet(sheetName);
            }
        } else {
            wb = excelType == ExcelTypeEnum.HSSF ? new HSSFWorkbook() : new XSSFWorkbook();
            sheet = wb.createSheet(sheetName);
        }
        if (sheet.getLastRowNum() == 0) {
            generateExcelSheet(sheet, objLists, header, property);
        } else {
            int beginRow = sheet.getLastRowNum() + 1;
            generateExcelText(sheet, objLists, property, beginRow);
        }
        try (OutputStream newExcelFile = Files.newOutputStream(file.toPath())) {
            wb.write(newExcelFile);
        }
    }

    public static void appendDataToExcelSheet(String excelFilePath, int sheetIndex,
                                              List<?> objLists, String[] header, String[] property) throws IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, NoSuchFieldException {
        ExcelTypeEnum excelType = ExcelTypeEnum.checkFileExcelType(excelFilePath);
        Workbook wb;
        Sheet sheet;
        File file = new File(excelFilePath);
        if (file.exists()) {
            InputStream excelFile = Files.newInputStream(file.toPath());
            wb = excelType == ExcelTypeEnum.HSSF ? new HSSFWorkbook(excelFile) : new XSSFWorkbook(excelFile);
            sheet = wb.getSheetAt(sheetIndex);
            if(sheet==null){
                sheet=wb.createSheet(String.valueOf(sheetIndex));
            }
        } else {
            wb = excelType == ExcelTypeEnum.HSSF ? new HSSFWorkbook() : new XSSFWorkbook();
            sheet = wb.createSheet();
        }
        if (sheet.getLastRowNum() == 0) {
            generateExcelSheet(sheet, objLists, header, property);
        } else {
            int beginRow = sheet.getLastRowNum() + 1;
            generateExcelText(sheet, objLists, property, beginRow);
        }
        try (OutputStream newExcelFile = Files.newOutputStream(file.toPath())) {
            wb.write(newExcelFile);
        }
    }

    public static void appendDataToExcelSheet(String excelFilePath, String sheetName, List<Object[]> objLists, String[] header) throws IOException {
        ExcelTypeEnum excelType = ExcelTypeEnum.checkFileExcelType(excelFilePath);
        Workbook wb;
        Sheet sheet;
        File file = new File(excelFilePath);
        if (file.exists()) {
            InputStream excelFile = Files.newInputStream(file.toPath());
            wb = excelType == ExcelTypeEnum.HSSF ? new HSSFWorkbook(excelFile) : new XSSFWorkbook(excelFile);
            sheet = wb.getSheet(sheetName);
            if(sheet==null){
                sheet=wb.createSheet(sheetName);
            }
        } else {
            wb = excelType == ExcelTypeEnum.HSSF ? new HSSFWorkbook() : new SXSSFWorkbook();
            sheet = wb.createSheet(sheetName);
        }
        if (sheet.getLastRowNum() == 0) {
            generateExcelSheet(sheet, objLists, header);
        } else {
            int beginRow = sheet.getLastRowNum() + 1;
            generateExcelText(sheet, objLists, beginRow);
        }
        try (OutputStream newExcelFile = Files.newOutputStream(file.toPath())) {
            wb.write(newExcelFile);
        }
    }

    public static void appendDataToExcelSheet(String excelFilePath, int sheetIndex, List<Object[]> objLists, String[] header) throws IOException {
        ExcelTypeEnum excelType = ExcelTypeEnum.checkFileExcelType(excelFilePath);
        Workbook wb;
        Sheet sheet;
        File file = new File(excelFilePath);
        if (file.exists()) {
            InputStream excelFile = Files.newInputStream(file.toPath());
            wb = excelType == ExcelTypeEnum.HSSF ? new HSSFWorkbook(excelFile) : new XSSFWorkbook(excelFile);
            sheet = wb.getSheetAt(sheetIndex);
        } else {
            wb = excelType == ExcelTypeEnum.HSSF ? new HSSFWorkbook() : new SXSSFWorkbook();
            sheet = wb.createSheet();
        }
        if (sheet.getLastRowNum() == 0) {
            generateExcelSheet(sheet, objLists, header);
        } else {
            int beginRow = sheet.getLastRowNum() + 1;
            generateExcelText(sheet, objLists, beginRow);
        }
        try (OutputStream newExcelFile = Files.newOutputStream(file.toPath())) {
            wb.write(newExcelFile);
        }
    }

    // 复制于csdn
    public static void copyRow(Workbook workbook, Sheet sheet, int fromRowIndex, int toRowIndex, int copyRowNum, Integer colNum) {
        for (int i = 0; i < copyRowNum; i++) {
            Row fromRow = sheet.getRow(fromRowIndex + i);
            Row toRow = sheet.getRow(toRowIndex + i);
            if(toRow==null){
                toRow = sheet.createRow(toRowIndex + i);
            }
            for (int colIndex = 0; colIndex < (colNum != null ? colNum : fromRow.getLastCellNum()+1); colIndex++) {
                Cell tmpCell = fromRow.getCell(colIndex);
                if (tmpCell == null) {
                    tmpCell = fromRow.createCell(colIndex);
                    copyCell(workbook, fromRow.createCell(colIndex), tmpCell);
                }
                Cell newCell = toRow.createCell(colIndex);
                copyCell(workbook, tmpCell, newCell);
            }
        }
    }

    /**
     * 复制单元格
     * @param workbook excel工作簿
     * @param srcCell 源单元格
     * @param distCell 目标单元格
     */
    public static void copyCell(Workbook workbook, Cell srcCell, Cell distCell) {
        //CellStyle newStyle = workbook.createCellStyle();
        //copyCellStyle(srcCell.getCellStyle(), newStyle, workbook);
        //样式
        distCell.setCellStyle(srcCell.getCellStyle());//newStyle
        //设置内容
        CellType srcCellType = srcCell.getCellType();//.getCellTypeEnum();
        distCell.setCellType(srcCellType);
        if (srcCellType == CellType.NUMERIC) {
            if (DateUtil.isCellDateFormatted(srcCell)) {
                distCell.setCellValue(srcCell.getDateCellValue());
            } else {
                distCell.setCellValue(srcCell.getNumericCellValue());
            }
        } else if (srcCellType == CellType.STRING) {
            distCell.setCellValue(srcCell.getRichStringCellValue());
        } else if (srcCellType == CellType.BOOLEAN) {
            distCell.setCellValue(srcCell.getBooleanCellValue());
        } else if (srcCellType == CellType.ERROR) {
            distCell.setCellErrorValue(srcCell.getErrorCellValue());
        } else if (srcCellType == CellType.FORMULA) {
            distCell.setCellFormula(srcCell.getCellFormula());
        }
    }

    /*
     * 复制一个单元格样式到目的单元格样式
     * @param fromStyle
     * @param toStyle
     */
    public static void copyCellStyle(CellStyle fromStyle, CellStyle toStyle, Workbook workbook) {
        //水平垂直对齐方式
        toStyle.setAlignment(fromStyle.getAlignment());
        toStyle.setVerticalAlignment(fromStyle.getVerticalAlignment());
        //边框和边框颜色
        toStyle.setBorderBottom(fromStyle.getBorderBottom());
        toStyle.setBorderLeft(fromStyle.getBorderLeft());
        toStyle.setBorderRight(fromStyle.getBorderRight());
        toStyle.setBorderTop(fromStyle.getBorderTop());
        toStyle.setTopBorderColor(fromStyle.getTopBorderColor());
        toStyle.setBottomBorderColor(fromStyle.getBottomBorderColor());
        toStyle.setRightBorderColor(fromStyle.getRightBorderColor());
        toStyle.setLeftBorderColor(fromStyle.getLeftBorderColor());
        //背景和前景
        if (fromStyle instanceof XSSFCellStyle) {
            XSSFCellStyle xssfToStyle = (XSSFCellStyle) toStyle;
            xssfToStyle.setFillBackgroundColor(((XSSFCellStyle) fromStyle).getFillBackgroundColorColor());
            xssfToStyle.setFillForegroundColor(((XSSFCellStyle) fromStyle).getFillForegroundColorColor());
        } else {
            toStyle.setFillBackgroundColor(fromStyle.getFillBackgroundColor());
            toStyle.setFillForegroundColor(fromStyle.getFillForegroundColor());
        }
        toStyle.setDataFormat(fromStyle.getDataFormat());
        toStyle.setFillPattern(fromStyle.getFillPattern());
        if (fromStyle instanceof XSSFCellStyle) {
            toStyle.setFont(((XSSFCellStyle) fromStyle).getFont());
        } else if (fromStyle instanceof HSSFCellStyle) {
            toStyle.setFont(((HSSFCellStyle) fromStyle).getFont(workbook));
        }
        toStyle.setHidden(fromStyle.getHidden());
        //首行缩进
        toStyle.setIndention(fromStyle.getIndention());
        toStyle.setLocked(fromStyle.getLocked());
        //旋转
        toStyle.setRotation(fromStyle.getRotation());
        toStyle.setWrapText(fromStyle.getWrapText());
    }
}
