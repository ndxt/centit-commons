package com.centit.support.report;

import com.alibaba.fastjson2.JSONObject;
import com.centit.support.algorithm.DatetimeOpt;
import com.centit.support.algorithm.NumberBaseOpt;
import com.centit.support.algorithm.ReflectionOpt;
import com.centit.support.algorithm.StringBaseOpt;
import com.centit.support.common.JavaBeanMetaData;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.usermodel.*;
import org.openxmlformats.schemas.drawingml.x2006.main.*;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTConnector;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTShape;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.apache.poi.ss.usermodel.CellType.NUMERIC;

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
                                     List<? extends Object> objLists, String[] header, String[] property) throws IOException {
        XSSFWorkbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet(sheetName);

        generateExcelSheet(sheet, objLists, header, property);
        wb.write(outputStream);
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
    public static void generateExcel(OutputStream outputStream, List<? extends Object> objLists, String[] header, String[] property) throws IOException {
        XSSFWorkbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet();
        generateExcelSheet(sheet, objLists, header, property);
        wb.write(outputStream);
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
        XSSFWorkbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet(sheetName);
        generateExcelSheet(sheet, objLists, header);
        wb.write(outputStream);
    }

    public static void generateExcel(OutputStream outputStream, List<Object[]> objLists, String[] header) throws IOException {
        XSSFWorkbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet();
        generateExcelSheet(sheet, objLists, header);
        wb.write(outputStream);
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
                                     List<? extends Object> objLists, Class<?> objType) throws IOException {
        XSSFWorkbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet(sheetName);
        generateExcelSheet(sheet, objLists, objType);
        wb.write(outputStream);
    }

    public static void generateExcel(OutputStream outputStream, List<? extends Object> objLists, Class<?> objType) throws IOException {
        XSSFWorkbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet();
        generateExcelSheet(sheet, objLists, objType);
        wb.write(outputStream);
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
                                         List<? extends Object> objLists, String[] header, String[] property) throws IOException {
        HSSFWorkbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet(sheetName);
        generateExcelSheet(sheet, objLists, header, property);
        wb.write(outputStream);
    }

    public static void generateExcel2003(OutputStream outputStream, List<? extends Object> objLists,
                                         String[] header, String[] property) throws IOException {
        HSSFWorkbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet();
        generateExcelSheet(sheet, objLists, header, property);
        wb.write(outputStream);
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
        HSSFWorkbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet(sheetName);
        generateExcelSheet(sheet, objLists, header);
        wb.write(outputStream);
    }

    public static void generateExcel2003(OutputStream outputStream, List<Object[]> objLists, String[] header) throws IOException {
        HSSFWorkbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet();
        generateExcelSheet(sheet, objLists, header);
        wb.write(outputStream);
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
                                         List<? extends Object> objLists, Class<?> objType) throws IOException {
        HSSFWorkbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet(sheetName);
        generateExcelSheet(sheet, objLists, objType);
        wb.write(outputStream);
    }

    public static void generateExcel2003(OutputStream outputStream, List<? extends Object> objLists, Class<?> objType) throws IOException {
        HSSFWorkbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet();
        generateExcelSheet(sheet, objLists, objType);
        wb.write(outputStream);
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
                                                  List<? extends Object> objLists, String[] header, String[] property) throws IOException {
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream()) {
            generateExcel(bout, sheetName, objLists, header, property);
            return new ByteArrayInputStream(bout.toByteArray());
        }
    }

    public static InputStream generateExcelStream(List<? extends Object> objLists, String[] header, String[] property) throws IOException {
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
                                                  List<? extends Object> objLists, Class<?> objType) throws IOException {
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream()) {
            generateExcel(bout, sheetName, objLists, objType);
            return new ByteArrayInputStream(bout.toByteArray());
        }
    }

    public static InputStream generateExcelStream(List<? extends Object> objLists, Class<?> objType) throws IOException {
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
    public static InputStream generateExcel2003Stream(String sheetName, List<? extends Object> objLists, String[] header, String[] property) throws IOException {
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream()) {
            generateExcel2003(bout, sheetName, objLists, header, property);
            return new ByteArrayInputStream(bout.toByteArray());
        }
    }

    public static InputStream generateExcel2003Stream(List<? extends Object> objLists,
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
                                                      List<? extends Object> objLists, Class<?> objType) throws IOException {
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream()) {
            generateExcel2003(bout, sheetName, objLists, objType);
            return new ByteArrayInputStream(bout.toByteArray());
        }
    }

    public static InputStream generateExcel2003Stream(List<? extends Object> objLists, Class<?> objType) throws IOException {
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream()) {
            generateExcel2003(bout, objLists, objType);
            return new ByteArrayInputStream(bout.toByteArray());
        }
    }

    //---------------------------------------------------------------------
    public static void generateExcelSheet(Sheet sheet, List<? extends Object> objLists, Class<?> objType) {
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
    public static void generateExcelSheet(Sheet sheet, List<? extends Object> objLists,
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
            throw new StatReportException(e);
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
    private static void generateExcelText(Sheet sheet, List<? extends Object> objLists, String[] property, int beginRow) throws NoSuchFieldException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
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
                cell.setCellType(NUMERIC);
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
                cell.setCellType(NUMERIC);
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

    public static void saveObjectsToExcelSheet(Sheet sheet, List<? extends Object> objects, Map<Integer, String> fieldDesc, int beginRow, boolean createRow) {
        int nRowCount = objects.size();
        //CellStyle cellStyle = getDefaultCellStyle(sheet.getWorkbook());
        Row excelRow = sheet.getRow(beginRow);
        int nColCount = excelRow.getLastCellNum() + 1;

        if(createRow){
            for(int i=1; i<nRowCount; i++)
                copyRow(sheet.getWorkbook(), sheet, beginRow, beginRow+i, 1, true,  nColCount);
        }
        for (int i = 0; i < nRowCount; i++) {
            excelRow = /*createRow ? sheet.createRow(beginRow + i) : */sheet.getRow(beginRow + i);
            Object rowObj = objects.get(i);
            if (rowObj != null && excelRow != null) {

                for (Map.Entry<Integer, String> ent : fieldDesc.entrySet()) {
                    Cell cell = excelRow.getCell(ent.getKey());
                    if (cell == null) {
                        cell = excelRow.createCell(ent.getKey());
                    }
                    cell.setCellValue(StringBaseOpt.objectToString(ReflectionOpt.attainExpressionValue(rowObj, ent.getValue())));
                }
            }
        }
        //return 0;
    }

    public static void saveObjectsToExcelSheet(Sheet sheet, List<Object[]> objects, int beginCol, int beginRow, boolean createRow) {
        int nRowCount = objects.size();
        //CellStyle cellStyle = getDefaultCellStyle(sheet.getWorkbook());
        Row excelRow = sheet.getRow(beginRow);
        int nColCount = excelRow.getLastCellNum() + 1;

        if(createRow){
            for(int i=1; i<nRowCount; i++)
                copyRow(sheet.getWorkbook(), sheet, beginRow, beginRow+i, 1, true,  nColCount);
        }

        for (int i = 0; i < nRowCount; i++) {
            excelRow = sheet.getRow(beginRow + i);
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
                    cell.setCellValue(StringBaseOpt.objectToString(rowObj[j]));
                }
            }
        }
        //return 0;
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
    public static void generateExcelByTemplate(String excelTemplateFilePath, String excelFilePath, String sheetName, List<? extends Object> objects, Map<Integer, String> fieldDesc, int beginRow, boolean createRow) throws IOException {

        ExcelTypeEnum excelType = ExcelTypeEnum.checkFileExcelType(excelTemplateFilePath);
        Workbook wb;

        try (InputStream excelFile = new FileInputStream(new File(excelTemplateFilePath))) {
            wb = excelType == ExcelTypeEnum.HSSF ? new HSSFWorkbook(excelFile) : new XSSFWorkbook(excelFile);
            Sheet sheet = (StringUtils.isBlank(sheetName)) ? wb.getSheetAt(0) : wb.getSheet(sheetName);
            saveObjectsToExcelSheet(sheet, objects, fieldDesc, beginRow, createRow);

        }

        try (OutputStream newExcelFile = new FileOutputStream(new File(excelFilePath))) {
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
                                               List<? extends Object> objects, Map<Integer, String> fieldDesc, int beginRow, boolean createRow) throws IOException {

        ExcelTypeEnum excelType = ExcelTypeEnum.checkFileExcelType(excelTemplateFilePath);
        Workbook wb;

        try (InputStream excelFile = new FileInputStream(new File(excelTemplateFilePath))) {
            wb = excelType == ExcelTypeEnum.HSSF ? new HSSFWorkbook(excelFile) : new XSSFWorkbook(excelFile);
            Sheet sheet = wb.getSheetAt(sheetIndex);
            saveObjectsToExcelSheet(sheet, objects, fieldDesc, beginRow, createRow);

        }

        try (OutputStream newExcelFile = new FileOutputStream(new File(excelFilePath))) {
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

        try (InputStream excelFile = new FileInputStream(new File(excelTemplateFilePath))) {
            wb = excelType == ExcelTypeEnum.HSSF ? new HSSFWorkbook(excelFile) : new XSSFWorkbook(excelFile);
            Sheet sheet = (StringUtils.isBlank(sheetName)) ? wb.getSheetAt(0) : wb.getSheet(sheetName);
            saveObjectsToExcelSheet(sheet, objects, beginCol, beginRow, createRow);

        }

        try (OutputStream newExcelFile = new FileOutputStream(new File(excelFilePath))) {
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

        try (InputStream excelFile = new FileInputStream(new File(excelTemplateFilePath))) {
            wb = excelType == ExcelTypeEnum.HSSF ? new HSSFWorkbook(excelFile) : new XSSFWorkbook(excelFile);
            Sheet sheet = wb.getSheetAt(sheetIndex);
            saveObjectsToExcelSheet(sheet, objects, beginCol, beginRow, createRow);
        }

        try (OutputStream newExcelFile = new FileOutputStream(new File(excelFilePath))) {
            wb.write(newExcelFile);
        }
    }

    public static void appendDataToExcelSheet(String excelFilePath, String sheetName,
                                              List<? extends Object> objLists, String[] header, String[] property) throws IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, NoSuchFieldException {

        ExcelTypeEnum excelType = ExcelTypeEnum.checkFileExcelType(excelFilePath);
        Workbook wb;
        Sheet sheet;
        File file = new File(excelFilePath);
        if (file.exists()) {
            InputStream excelFile = new FileInputStream(new File(excelFilePath));
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
        try (OutputStream newExcelFile = new FileOutputStream(new File(excelFilePath))) {
            if (wb != null) {
                wb.write(newExcelFile);
            }
        }
    }

    public static void appendDataToExcelSheet(String excelFilePath, int sheetIndex,
                                              List<? extends Object> objLists, String[] header, String[] property) throws IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, NoSuchFieldException {
        ExcelTypeEnum excelType = ExcelTypeEnum.checkFileExcelType(excelFilePath);
        Workbook wb;
        Sheet sheet;
        File file = new File(excelFilePath);
        if (file.exists()) {
            InputStream excelFile = new FileInputStream(new File(excelFilePath));
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
        try (OutputStream newExcelFile = new FileOutputStream(new File(excelFilePath))) {
            if (wb != null) {
                wb.write(newExcelFile);
            }
        }
    }

    public static void appendDataToExcelSheet(String excelFilePath, String sheetName, List<Object[]> objLists, String[] header) throws IOException {
        ExcelTypeEnum excelType = ExcelTypeEnum.checkFileExcelType(excelFilePath);
        Workbook wb;
        Sheet sheet;
        File file = new File(excelFilePath);
        if (file.exists()) {
            InputStream excelFile = new FileInputStream(new File(excelFilePath));
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
            generateExcelSheet(sheet, objLists, header);
        } else {
            int beginRow = sheet.getLastRowNum() + 1;
            generateExcelText(sheet, objLists, beginRow);
        }
        try (OutputStream newExcelFile = new FileOutputStream(new File(excelFilePath))) {
            if (wb != null) {
                wb.write(newExcelFile);
            }
        }
    }

    public static void appendDataToExcelSheet(String excelFilePath, int sheetIndex, List<Object[]> objLists, String[] header) throws IOException {
        ExcelTypeEnum excelType = ExcelTypeEnum.checkFileExcelType(excelFilePath);
        Workbook wb;
        Sheet sheet;
        File file = new File(excelFilePath);
        if (file.exists()) {
            InputStream excelFile = new FileInputStream(new File(excelFilePath));
            wb = excelType == ExcelTypeEnum.HSSF ? new HSSFWorkbook(excelFile) : new XSSFWorkbook(excelFile);
            sheet = wb.getSheetAt(sheetIndex);
        } else {
            wb = excelType == ExcelTypeEnum.HSSF ? new HSSFWorkbook() : new XSSFWorkbook();
            sheet = wb.createSheet();
        }
        if (sheet.getLastRowNum() == 0) {
            generateExcelSheet(sheet, objLists, header);
        } else {
            int beginRow = sheet.getLastRowNum() + 1;
            generateExcelText(sheet, objLists, beginRow);
        }
        try (OutputStream newExcelFile = new FileOutputStream(new File(excelFilePath))) {
            if (wb != null) {
                wb.write(newExcelFile);
            }
        }
    }

    // 复制于csdn
    public static void copyRow(Workbook workbook, Sheet sheet, int fromRowIndex, int toRowIndex, int copyRowNum, boolean insertFlag
        , Integer colNum) {
        for (int i = 0; i < copyRowNum; i++) {
            Row fromRow = sheet.getRow(fromRowIndex + i);
            Row toRow = sheet.getRow(toRowIndex + i);
            if (insertFlag) {
                //复制行超出原有sheet页的最大行数时，不需要移动直接插入
                if (toRowIndex + i <= sheet.getLastRowNum()) {
                    //先移动要插入的行号所在行及之后的行
                    sheet.shiftRows(toRowIndex + i, sheet.getLastRowNum(), 1, true, false);
                }
                //然后再插入行
                toRow = sheet.createRow(toRowIndex + i);
                //设置行高
                toRow.setHeight(fromRow.getHeight());
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
        //获取合并单元格
        List<CellRangeAddress> cellRangeAddressList = sheet.getMergedRegions();
        Map<Integer, List<CellRangeAddress>> rowCellRangeAddressMap = cellRangeAddressList!=null && cellRangeAddressList.size()>0?
            cellRangeAddressList.stream().collect(Collectors.groupingBy(x ->
            x.getFirstRow())) : new HashMap<>();
        //获取形状（线条）
        XSSFDrawing drawing = (XSSFDrawing) sheet.getDrawingPatriarch();
        List<XSSFShape> shapeList = new ArrayList<>();
        Map<Integer, List<XSSFShape>> rowShapeMap = new HashMap<>();
        if (drawing != null) {
            shapeList = drawing.getShapes();
            rowShapeMap = shapeList.stream().filter(x -> x.getAnchor() != null)
                .collect(Collectors.groupingBy(x -> ((XSSFClientAnchor) x.getAnchor()).getRow1()));
        }
        List<XSSFShape> insertShapeList = new ArrayList<>();
        for (int i = 0; i < copyRowNum; i++) {
            Row toRow = sheet.getRow(toRowIndex + i);
            //复制合并单元格
            List<CellRangeAddress> rowCellRangeAddressList = rowCellRangeAddressMap.get(fromRowIndex + i);
            if (rowCellRangeAddressList!=null && rowCellRangeAddressList.size()>0) {
                for (CellRangeAddress cellRangeAddress : rowCellRangeAddressList) {
                    CellRangeAddress newCellRangeAddress = new CellRangeAddress(toRow.getRowNum(), (toRow.getRowNum() +
                        (cellRangeAddress.getLastRow() - cellRangeAddress.getFirstRow())), cellRangeAddress
                        .getFirstColumn(), cellRangeAddress.getLastColumn());
                    sheet.addMergedRegionUnsafe(newCellRangeAddress);
                }
            }
            //复制形状（线条）
            List<XSSFShape> rowShapeList = rowShapeMap.get(fromRowIndex + i);
            if (rowShapeList!=null && rowShapeList.size()>0) {
                for (XSSFShape shape : rowShapeList) {
                    //复制描点
                    XSSFClientAnchor fromAnchor = (XSSFClientAnchor) shape.getAnchor();
                    XSSFClientAnchor toAnchor = new XSSFClientAnchor();
                    toAnchor.setDx1(fromAnchor.getDx1());
                    toAnchor.setDx2(fromAnchor.getDx2());
                    toAnchor.setDy1(fromAnchor.getDy1());
                    toAnchor.setDy2(fromAnchor.getDy2());
                    toAnchor.setRow1(toRow.getRowNum());
                    toAnchor.setRow2(toRow.getRowNum() + fromAnchor.getRow2() - fromAnchor.getRow1());
                    toAnchor.setCol1(fromAnchor.getCol1());
                    toAnchor.setCol2(fromAnchor.getCol2());
                    //复制形状
                    if (shape instanceof XSSFConnector) {
                        copyXSSFConnector((XSSFConnector) shape, drawing, toAnchor);
                    } else if (shape instanceof XSSFSimpleShape) {
                        copyXSSFSimpleShape((XSSFSimpleShape) shape, drawing, toAnchor);
                    }
                }
            }
        }
    }

    /**
     * 复制XSSFSimpleShape类
     *
     * @param fromShape
     * @param drawing
     * @param anchor
     * @return
     */
    public static XSSFSimpleShape copyXSSFSimpleShape(XSSFSimpleShape fromShape, XSSFDrawing drawing, XSSFClientAnchor anchor) {
        XSSFSimpleShape toShape = drawing.createSimpleShape(anchor);
        CTShape ctShape = fromShape.getCTShape();
        CTShapeProperties ctShapeProperties = ctShape.getSpPr();
        CTLineProperties lineProperties = ctShapeProperties.isSetLn() ? ctShapeProperties.getLn() : ctShapeProperties.addNewLn();
        CTPresetLineDashProperties dashStyle = lineProperties.isSetPrstDash() ? lineProperties.getPrstDash() : CTPresetLineDashProperties.Factory.newInstance();
        STPresetLineDashVal.Enum dashStyleEnum = dashStyle.isSetVal() ? dashStyle.getVal() : STPresetLineDashVal.Enum.forInt(1);
        CTSolidColorFillProperties fill = lineProperties.isSetSolidFill() ? lineProperties.getSolidFill() : lineProperties.addNewSolidFill();
        CTSRgbColor rgb = fill.isSetSrgbClr() ? fill.getSrgbClr() : CTSRgbColor.Factory.newInstance();
        // 设置形状类型
        toShape.setShapeType(fromShape.getShapeType());
        // 设置线宽
        toShape.setLineWidth(lineProperties.getW() * 1.0 / Units.EMU_PER_POINT);
        // 设置线的风格
        toShape.setLineStyle(dashStyleEnum.intValue() - 1);
        // 设置线的颜色
        byte[] rgbBytes = rgb.getVal();
        if (rgbBytes == null) {
            toShape.setLineStyleColor(0, 0, 0);
        } else {
            toShape.setLineStyleColor(rgbBytes[0], rgbBytes[1], rgbBytes[2]);
        }
        return toShape;
    }

    /**
     * 复制XSSFConnector类
     *
     * @param fromShape
     * @param drawing
     * @param anchor
     * @return
     */
    public static XSSFConnector copyXSSFConnector(XSSFConnector fromShape, XSSFDrawing drawing, XSSFClientAnchor anchor) {
        XSSFConnector toShape = drawing.createConnector(anchor);
        CTConnector ctConnector = fromShape.getCTConnector();
        CTShapeProperties ctShapeProperties = ctConnector.getSpPr();
        CTLineProperties lineProperties = ctShapeProperties.isSetLn() ? ctShapeProperties.getLn() : ctShapeProperties.addNewLn();
        CTPresetLineDashProperties dashStyle = lineProperties.isSetPrstDash() ? lineProperties.getPrstDash() : CTPresetLineDashProperties.Factory.newInstance();
        STPresetLineDashVal.Enum dashStyleEnum = dashStyle.isSetVal() ? dashStyle.getVal() : STPresetLineDashVal.Enum.forInt(1);
        CTSolidColorFillProperties fill = lineProperties.isSetSolidFill() ? lineProperties.getSolidFill() : lineProperties.addNewSolidFill();
        CTSRgbColor rgb = fill.isSetSrgbClr() ? fill.getSrgbClr() : CTSRgbColor.Factory.newInstance();
        // 设置形状类型
        toShape.setShapeType(fromShape.getShapeType());
        // 设置线宽
        toShape.setLineWidth(lineProperties.getW() * 1.0 / Units.EMU_PER_POINT);
        // 设置线的风格
        toShape.setLineStyle(dashStyleEnum.intValue() - 1);
        // 设置线的颜色
        byte[] rgbBytes = rgb.getVal();
        if (rgbBytes == null) {
            toShape.setLineStyleColor(0, 0, 0);
        } else {
            toShape.setLineStyleColor(rgbBytes[0], rgbBytes[1], rgbBytes[2]);
        }
        return toShape;
    }

    /**
     * 复制单元格
     *
     * @param srcCell
     * @param distCell
     */
    public static void copyCell(Workbook workbook, Cell srcCell, Cell distCell) {
        CellStyle newStyle = workbook.createCellStyle();
        copyCellStyle(srcCell.getCellStyle(), newStyle, workbook);
        //样式
        distCell.setCellStyle(newStyle);
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
        } else {
        }
    }

    /**
     * 复制一个单元格样式到目的单元格样式
     *
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
