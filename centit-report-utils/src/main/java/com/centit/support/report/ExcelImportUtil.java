package com.centit.support.report;

import com.centit.support.algorithm.*;
import com.centit.support.common.JavaBeanField;
import com.centit.support.common.JavaBeanMetaData;
import com.centit.support.common.LeftRightPair;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;


/**
 * Created by codefan on 17-9-20.
 *
 * @author codefan@sina.com
 */
@SuppressWarnings("unused")
public abstract class ExcelImportUtil {

    private ExcelImportUtil() {
        throw new IllegalAccessError("Utility class");
    }

    protected static final Logger logger = LoggerFactory.getLogger(ExcelImportUtil.class);

    public static void setObjectFieldValue(Object object, JavaBeanField field, Cell cell) {
        switch (field.getFieldJavaTypeShortName()) {
            case "int":
            case "Integer":
            case "long":
            case "Long":
            case "float":
            case "Float":
            case "double":
            case "Double":
            case "BigDecimal":
            case "BigInteger":
                if (cell.getCellType() == CellType.NUMERIC) {
                    field.setObjectFieldValue(object, cell.getNumericCellValue());
                } else {
                    field.setObjectFieldValue(object, cell.toString());
                }
                break;

            case "Date":
            case "sqlDate":
            case "sqlTimestamp":
                if (cell.getCellType() == CellType.NUMERIC) {
                    field.setObjectFieldValue(object, cell.getDateCellValue());
                } else {
                    field.setObjectFieldValue(object, cell.toString());
                }
                break;
            case "boolean":
            case "Boolean":
                if (cell.getCellType() == CellType.BOOLEAN) {
                    field.setObjectFieldValue(object, cell.getBooleanCellValue());
                } else {
                    field.setObjectFieldValue(object, cell.toString());
                }
                break;
            case "byte[]":
            case "String":
            default:
                field.setObjectFieldValue(object, cell.toString());
                break;
        }
    }

    /**
     * 将Excel的列编号转换成数值
     *
     * @param column 编号
     * @return 编号代表的数值
     */
    public static int mapColumnIndex(String column) {
        if (StringRegularOpt.isDigit(column)) {
            return NumberBaseOpt.castObjectToInteger(column);
        }
        char[] chars = column.toUpperCase().toCharArray();
        int index = 0;
        for (int i = 0; i < chars.length; i++) {
            index = index * 26 + column.charAt(i) - 'A' + 1;
        }
        return index - 1;
    }

    public static Map<Integer, String> mapColumnIndex(Map<String, String> fieldDesc) {
        Map<Integer, String> fieldIndexDesc = new HashMap<>(fieldDesc.size() + 4);
        for (Map.Entry<String, String> ent : fieldDesc.entrySet()) {
            fieldIndexDesc.put(mapColumnIndex(ent.getKey()), ent.getValue());
        }
        return fieldIndexDesc;
    }

    /**
     * 获取 excel 表格单元中的数据
     *
     * @param cell 单元
     * @return object 数值
     */
    public static Object getCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        Object value = null;
        switch (cell.getCellType()) {
            case STRING:
                value = cell.getRichStringCellValue().getString();
                break;
            case NUMERIC:
                String dataFormat = cell.getCellStyle().getDataFormatString();
                if (StringUtils.containsIgnoreCase(dataFormat, "yy")
                    && StringUtils.containsIgnoreCase(dataFormat, "m")
                    && StringUtils.containsIgnoreCase(dataFormat, "d")) {
                    value = cell.getDateCellValue();
                } else {
                    value = cell.getNumericCellValue();
                }
                break;
            case BOOLEAN:
                value = cell.getBooleanCellValue();
                break;
            case BLANK:
                value = null;
                break;
            default:
                value = cell.toString();
                break;
        }
        return value;
    }

    public static String getCellString(Cell cell) {
        if (cell == null) {
            return "";
        }
        String value = null;
        switch (cell.getCellType()) {
            case STRING:
                value = cell.getRichStringCellValue().getString();
                break;
            case NUMERIC:
                String dataFormat = cell.getCellStyle().getDataFormatString();
                if (StringUtils.containsIgnoreCase(dataFormat, "yy")
                    && StringUtils.containsIgnoreCase(dataFormat, "m")
                    && StringUtils.containsIgnoreCase(dataFormat, "d")) {
                    value = DatetimeOpt.convertTimestampToString(cell.getDateCellValue());
                } else {
                    value = StringBaseOpt.castObjectToString(cell.getNumericCellValue());
                }
                break;
            case BOOLEAN:
                value = cell.getBooleanCellValue() ? BooleanBaseOpt.STRING_TRUE : BooleanBaseOpt.STRING_FALSE;
                break;
            case BLANK:
                value = "";
                break;
            default:
                value = cell.toString();
                break;
        }
        return value;
    }

    private static <T> List<T> loadObjectFromExcelSheet(Sheet sheet, Class<T> beanType,
                                                        Map<Integer, String> fieldDesc, int beginRow, int endRow)
        throws IllegalAccessException, InstantiationException {

        if (sheet == null)
            return null;

        JavaBeanMetaData metaData = JavaBeanMetaData.createBeanMetaDataFromType(beanType);

        List<T> datas = new ArrayList<>(endRow - beginRow + 1);

        for (int row = beginRow; row < endRow; row++) {

            Row excelRow = sheet.getRow(row);
            if (excelRow == null)
                continue;
            int i = 0;
            T rowObj = beanType.newInstance();
            boolean hasValue = false;
            //excelRow.getFirstCellNum()
            for (Map.Entry<Integer, String> ent : fieldDesc.entrySet()) {
                Cell cell = excelRow.getCell(ent.getKey());
                JavaBeanField field = metaData.getFiled(ent.getValue());
                if (cell != null && StringUtils.isNotBlank(cell.toString())) {
                    hasValue = true;
                    setObjectFieldValue(rowObj, field, cell);
                }
            }
            if (hasValue) {
                datas.add(rowObj);
            }
        }

        return datas;
    }

    /**
     * @param excelFile 文件流
     * @param excelType excel 版本 2003 还是新版本
     * @param sheetName sheet名称 如果为空为 第一个页面
     * @param beanType  对象类型
     * @param fieldDesc 字段对应关系
     * @param beginRow  起始行 0 base 包含
     * @param endRow    结束行 0 base 不包含
     * @param <T>       返回的对象类型
     * @return 对象列表
     * @throws IllegalAccessException 异常
     * @throws InstantiationException 异常
     * @throws IOException            异常
     */
    public static <T> List<T> loadObjectFromExcel(InputStream excelFile, ExcelTypeEnum excelType, String sheetName,
                                                  Class<T> beanType, Map<Integer, String> fieldDesc, int beginRow, int endRow)
        throws IllegalAccessException, InstantiationException, IOException {
        Sheet sheet = loadExcelFileSheet(excelFile, excelType, sheetName);
        return loadObjectFromExcelSheet(sheet, beanType, fieldDesc, beginRow, endRow);
    }

    /**
     * @param filePath  文件名，通过后缀名判断excel版本号
     * @param sheetName sheet名称 如果为空为 第一个页面
     * @param beanType  对象类型
     * @param fieldDesc 字段对应关系
     * @param beginRow  起始行 0 base 包含
     * @param endRow    结束行 0 base 不包含
     * @param <T>       返回的对象类型
     * @return 对象列表
     * @throws IllegalAccessException 异常
     * @throws InstantiationException 异常
     * @throws IOException            异常
     */
    public static <T> List<T> loadObjectFromExcel(String filePath, String sheetName,
                                                  Class<T> beanType, Map<Integer, String> fieldDesc, int beginRow, int endRow)
        throws IllegalAccessException, InstantiationException, IOException {
        ExcelTypeEnum excelType = ExcelTypeEnum.checkFileExcelType(filePath);
        try (InputStream excelFile = new FileInputStream(new File(filePath))) {
            return loadObjectFromExcel(excelFile, excelType, sheetName,
                beanType, fieldDesc, beginRow, endRow);
        }
    }

    /**
     * @param excelFile 文件流
     * @param excelType excel 版本 2003 还是新版本
     * @param sheetName sheet名称 如果为空为 第一个页面
     * @param beanType  对象类型
     * @param fieldDesc 字段对应关系
     * @param beginRow  起始行 0 base 包含
     * @param <T>       返回的对象类型
     * @return 对象列表
     * @throws IllegalAccessException 异常
     * @throws InstantiationException 异常
     * @throws IOException            异常
     */
    public static <T> List<T> loadObjectFromExcel(InputStream excelFile, ExcelTypeEnum excelType, String sheetName,
                                                  Class<T> beanType, Map<Integer, String> fieldDesc, int beginRow)
        throws IllegalAccessException, InstantiationException, IOException {

        Sheet sheet = loadExcelFileSheet(excelFile, excelType, sheetName);
        return loadObjectFromExcelSheet(sheet, beanType, fieldDesc, beginRow, sheet.getLastRowNum() + 1);
    }

    /**
     * @param filePath  文件名，通过后缀名判断excel版本号
     * @param sheetName sheet名称 如果为空为 第一个页面
     * @param beanType  对象类型
     * @param fieldDesc 字段对应关系
     * @param beginRow  起始行 0 base 包含
     * @param <T>       返回的对象类型
     * @return 对象列表
     * @throws IllegalAccessException 异常
     * @throws InstantiationException 异常
     * @throws IOException            异常
     */
    public static <T> List<T> loadObjectFromExcel(String filePath, String sheetName,
                                                  Class<T> beanType, Map<Integer, String> fieldDesc, int beginRow)
        throws IllegalAccessException, InstantiationException, IOException {
        ExcelTypeEnum excelType = ExcelTypeEnum.checkFileExcelType(filePath);
        try (InputStream excelFile = new FileInputStream(new File(filePath))) {
            return loadObjectFromExcel(excelFile, excelType, sheetName,
                beanType, fieldDesc, beginRow);
        }
    }

    /**
     * @param excelFile  文件流
     * @param excelType  excel 版本 2003 还是新版本
     * @param sheetIndex sheet 序号 0 base
     * @param beanType   对象类型
     * @param fieldDesc  字段对应关系
     * @param beginRow   起始行 0 base 包含
     * @param endRow     结束行 0 base 不包含
     * @param <T>        返回的对象类型
     * @return 对象列表
     * @throws IllegalAccessException 异常
     * @throws InstantiationException 异常
     * @throws IOException            异常
     */
    public static <T> List<T> loadObjectFromExcel(InputStream excelFile, ExcelTypeEnum excelType, int sheetIndex,
                                                  Class<T> beanType, Map<Integer, String> fieldDesc, int beginRow, int endRow)
        throws IllegalAccessException, InstantiationException, IOException {
        Sheet sheet = loadExcelFileSheet(excelFile, excelType, sheetIndex);
        return loadObjectFromExcelSheet(sheet, beanType, fieldDesc, beginRow, endRow);
    }

    /**
     * @param filePath   文件名，通过后缀名判断excel版本号
     * @param sheetIndex sheet 序号 0 base
     * @param beanType   对象类型
     * @param fieldDesc  字段对应关系
     * @param beginRow   起始行 0 base 包含
     * @param endRow     结束行 0 base 不包含
     * @param <T>        返回的对象类型
     * @return 对象列表
     * @throws IllegalAccessException 异常
     * @throws InstantiationException 异常
     * @throws IOException            异常
     */
    public static <T> List<T> loadObjectFromExcel(String filePath, int sheetIndex,
                                                  Class<T> beanType, Map<Integer, String> fieldDesc, int beginRow, int endRow)
        throws IllegalAccessException, InstantiationException, IOException {

        ExcelTypeEnum excelType = ExcelTypeEnum.checkFileExcelType(filePath);

        try (InputStream excelFile = new FileInputStream(new File(filePath))) {
            return loadObjectFromExcel(excelFile, excelType, sheetIndex,
                beanType, fieldDesc, beginRow, endRow);
        }
    }

    /**
     * @param excelFile  文件流
     * @param excelType  excel 版本 2003 还是新版本
     * @param sheetIndex sheet 序号 0 base
     * @param beanType   对象类型
     * @param fieldDesc  字段对应关系
     * @param beginRow   起始行 0 base 包含
     * @param <T>        返回的对象类型
     * @return 对象列表
     * @throws IllegalAccessException 异常
     * @throws InstantiationException 异常
     * @throws IOException            异常
     */
    public static <T> List<T> loadObjectFromExcel(InputStream excelFile, ExcelTypeEnum excelType, int sheetIndex,
                                                  Class<T> beanType, Map<Integer, String> fieldDesc, int beginRow)
        throws IllegalAccessException, InstantiationException, IOException {

        Workbook wb = excelType == ExcelTypeEnum.HSSF ?
            new HSSFWorkbook(excelFile) : /*new SXSSFWorkbook(*/new XSSFWorkbook(excelFile);
        Sheet sheet = wb.getSheetAt(sheetIndex);

        return loadObjectFromExcelSheet(sheet, beanType, fieldDesc, beginRow, sheet.getLastRowNum() + 1);
    }

    /**
     * @param filePath   文件名，通过后缀名判断excel版本号
     * @param sheetIndex sheet 序号 0 base
     * @param beanType   对象类型
     * @param fieldDesc  字段对应关系
     * @param beginRow   起始行 0 base 包含
     * @param <T>        返回的对象类型
     * @return 对象列表
     * @throws IllegalAccessException 异常
     * @throws InstantiationException 异常
     * @throws IOException            异常
     */
    public static <T> List<T> loadObjectFromExcel(String filePath, int sheetIndex,
                                                  Class<T> beanType, Map<Integer, String> fieldDesc, int beginRow)
        throws IllegalAccessException, InstantiationException, IOException {

        ExcelTypeEnum excelType = ExcelTypeEnum.checkFileExcelType(filePath);
        try (InputStream excelFile = new FileInputStream(new File(filePath))) {
            return loadObjectFromExcel(excelFile, excelType, sheetIndex,
                beanType, fieldDesc, beginRow);
        }
    }


    private static List<String[]> loadDataFromExcelSheet(Sheet sheet,
                                                         int[] columnList, int[] rowList) {
        if (sheet == null)
            return null;

        List<String[]> datas = new ArrayList<>(rowList.length + 1);
        for (int row : rowList) {
            String[] rowObj = new String[columnList.length];
            Row excelRow = sheet.getRow(row);
            if (excelRow == null) {
                datas.add(null);
            } else {
                int i = 0;
                //excelRow.getFirstCellNum()
                for (int col : columnList) {
                    Cell cell = excelRow.getCell(col);
                    rowObj[i++] = cell == null ? null : cell.toString();
                }
                datas.add(rowObj);
            }
        }

        return datas;
    }

    private static Sheet loadExcelFileSheet(InputStream excelFile, String sheetName)
        throws IOException {
        Workbook wb = WorkbookFactory.create(excelFile);
        return (StringUtils.isBlank(sheetName)) ?
            wb.getSheetAt(0) : wb.getSheet(sheetName);
    }

    private static Sheet loadExcelFileSheet(InputStream excelFile, int sheetIndex)
        throws IOException {
        Workbook wb = WorkbookFactory.create(excelFile);
        return wb.getSheetAt(sheetIndex);
    }

    private static Sheet loadExcelFileSheet(InputStream excelFile, ExcelTypeEnum excelType, String sheetName)
        throws IOException {
        Workbook wb = excelType == ExcelTypeEnum.HSSF ?
            new HSSFWorkbook(excelFile) :  /*new SXSSFWorkbook(*/new XSSFWorkbook(excelFile);
        return (StringUtils.isBlank(sheetName)) ?
            wb.getSheetAt(0) : wb.getSheet(sheetName);
    }

    private static Sheet loadExcelFileSheet(InputStream excelFile, ExcelTypeEnum excelType, int sheetIndex)
        throws IOException {
        Workbook wb = excelType == ExcelTypeEnum.HSSF ?
            new HSSFWorkbook(excelFile) : /*new SXSSFWorkbook(*/new XSSFWorkbook(excelFile);
        return wb.getSheetAt(sheetIndex);
    }

    /**
     * @param excelFile  excel 文件流
     * @param excelType  excel 版本 2003 还是新版本
     * @param sheetName  读取的页面名称
     * @param columnList 读取的列
     * @param rowList    读取的行
     * @return 返回二维数组
     * @throws IOException Stream操作异常
     */
    public static List<String[]> loadDataFromExcel(InputStream excelFile, ExcelTypeEnum excelType, String sheetName,
                                                   int[] columnList, int[] rowList)
        throws IOException {
        Sheet sheet = loadExcelFileSheet(excelFile, excelType, sheetName);
        return loadDataFromExcelSheet(sheet, columnList, rowList);
    }

    public static List<String[]> loadDataFromExcel(String filePath, String sheetName,
                                                   int[] columnList, int[] rowList)
        throws IOException {

        ExcelTypeEnum excelType = ExcelTypeEnum.checkFileExcelType(filePath);
        try (InputStream excelFile = new FileInputStream(new File(filePath))) {
            return loadDataFromExcel(excelFile, excelType, sheetName,
                columnList, rowList);
        }
    }

    /**
     * @param excelFile  excel 文件流
     * @param excelType  excel 版本 2003 还是新版本
     * @param sheetIndex 读取的页面序号 0 base
     * @param columnList 读取的列
     * @param rowList    读取的行
     * @return 返回二维数组
     * @throws IOException 异常
     **/
    public static List<String[]> loadDataFromExcel(InputStream excelFile, ExcelTypeEnum excelType, int sheetIndex,
                                                   int[] columnList, int[] rowList)
        throws IOException {
        Sheet sheet = loadExcelFileSheet(excelFile, excelType, sheetIndex);
        return loadDataFromExcelSheet(sheet, columnList, rowList);
    }

    /**
     * @param filePath   文件名，通过后缀名判断excel版本号
     * @param sheetIndex 读取的页面序号 0 base
     * @param columnList 读取的列
     * @param rowList    读取的行
     * @return 返回二维数组
     * @throws IOException 异常
     */
    public static List<String[]> loadDataFromExcel(String filePath, int sheetIndex,
                                                   int[] columnList, int[] rowList)
        throws IOException {

        ExcelTypeEnum excelType = ExcelTypeEnum.checkFileExcelType(filePath);

        try (InputStream excelFile = new FileInputStream(new File(filePath))) {
            return loadDataFromExcel(excelFile, excelType, sheetIndex,
                columnList, rowList);
        }
    }


    private static List<String[]> loadDataFromExcelSheet(Sheet sheet,
                                                         int beginCol, int endCol, int beginRow, int endRow) {
        if (sheet == null)
            return null;

        List<String[]> datas = new ArrayList<>(endRow - beginRow + 1);
        for (int row = beginRow; row < endRow; row++) {

            Row excelRow = sheet.getRow(row);
            if (excelRow == null)
                continue;
            int i = 0;
            String[] rowObj = new String[endCol - beginCol + 1];
            //excelRow.getFirstCellNum()
            boolean hasValue = false;
            for (int col = beginCol; col <= endCol; col++) {
                Cell cell = excelRow.getCell(col);
                if (cell != null) {
                    rowObj[i] = cell.toString();// cell.getStringCellValue();
                    hasValue = true;
                }
                i++;
            }
            if (hasValue) {
                datas.add(rowObj);
            }
        }

        return datas;
    }

    /**
     * 所有的行列都是 0 Base的
     *
     * @param excelFile  excel 文件流
     * @param excelType  excel 版本 2003 还是新版本
     * @param sheetIndex 读取的页面序号 0 base
     * @param beginCol   起始列  包含 beginCol
     * @param endCol     终止列 不包含 endCol
     * @param beginRow   起始行 包含 beginRow
     * @param endRow     起始行 不包含 endRow
     * @return 返回二维数组
     * @throws IOException 异常
     */
    public static List<String[]> loadDataFromExcel(InputStream excelFile, ExcelTypeEnum excelType, int sheetIndex,
                                                   int beginCol, int endCol, int beginRow, int endRow)
        throws IOException {
        Sheet sheet = loadExcelFileSheet(excelFile, excelType, sheetIndex);
        return loadDataFromExcelSheet(sheet, beginCol, endCol, beginRow, endRow);
    }

    /**
     * 所有的行列都是 0 Base的
     *
     * @param filePath   文件名，通过后缀名判断excel版本号
     * @param sheetIndex 读取的页面序号 0 base
     * @param beginCol   起始列  包含 beginCol
     * @param endCol     终止列 不包含 endCol
     * @param beginRow   起始行 包含 beginRow
     * @param endRow     起始行 不包含 endRow
     * @return 返回二维数组
     * @throws IOException 异常
     */
    public static List<String[]> loadDataFromExcel(String filePath, int sheetIndex,
                                                   int beginCol, int endCol, int beginRow, int endRow)
        throws IOException {

        ExcelTypeEnum excelType = ExcelTypeEnum.checkFileExcelType(filePath);

        try (InputStream excelFile = new FileInputStream(new File(filePath))) {
            return loadDataFromExcel(excelFile, excelType, sheetIndex,
                beginCol, endCol, beginRow, endRow);
        }
    }

    /**
     * 所有的行列都是 0 Base的
     *
     * @param excelFile excel 文件流
     * @param excelType excel 版本 2003 还是新版本
     * @param sheetName 读取的页面名称 , 如果为空则 读取第一个页面
     * @param beginCol  起始列  包含 beginCol
     * @param endCol    终止列 不包含 endCol
     * @param beginRow  起始行 包含 beginRow
     * @param endRow    起始行 不包含 endRow
     * @return 返回二维数组
     * @throws IOException 异常
     */
    public static List<String[]> loadDataFromExcel(InputStream excelFile, ExcelTypeEnum excelType, String sheetName,
                                                   int beginCol, int endCol, int beginRow, int endRow)
        throws IOException {
        Sheet sheet = loadExcelFileSheet(excelFile, excelType, sheetName);
        return loadDataFromExcelSheet(sheet, beginCol, endCol, beginRow, endRow);
    }

    /**
     * 所有的行列都是 0 Base的
     *
     * @param filePath  文件名，通过后缀名判断excel版本号
     * @param sheetName 读取的页面名称 , 如果为空则 读取第一个页面
     * @param beginCol  起始列  包含 beginCol
     * @param endCol    终止列 不包含 endCol
     * @param beginRow  起始行 包含 beginRow
     * @param endRow    起始行 不包含 endRow
     * @return 返回二维数组
     * @throws IOException 异常
     */
    public static List<String[]> loadDataFromExcel(String filePath, String sheetName,
                                                   int beginCol, int endCol, int beginRow, int endRow)
        throws IOException {

        ExcelTypeEnum excelType = ExcelTypeEnum.checkFileExcelType(filePath);

        try (InputStream excelFile = new FileInputStream(new File(filePath))) {
            return loadDataFromExcel(excelFile, excelType, sheetName,
                beginCol, endCol, beginRow, endRow);
        }
    }

    /**
     * 判断 excel流类型
     *
     * @param input excelStreamInput
     * @return excel 类型 和 inputStream
     * @throws IOException IO异常
     */
    public static LeftRightPair<ExcelTypeEnum, InputStream> checkExcelInputStreamType(InputStream input) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        byte[] headerBuf = new byte[1024];
        int len = input.read(headerBuf);
        if (len > 0) {
            baos.write(headerBuf, 0, len);
        }
        do {
            len = input.read(buffer);
            if (len > 0) {
                baos.write(buffer, 0, len);
            }
        } while (len > -1);
        baos.flush();
        return new LeftRightPair<>(ExcelTypeEnum.checkFileExcelType(headerBuf), new ByteArrayInputStream(baos.toByteArray()));
    }

    /**
     * 所有的行列都是 0 Base的
     *
     * @param excelFile  excel 文件流
     * @param excelType  excel 版本 2003 还是新版本
     * @param sheetIndex 读取的页面序号 0 base
     * @param beginCol   起始列  包含 beginCol
     * @param endCol     终止列 不包含 endCol
     * @param beginRow   起始行 包含 beginRow
     * @return 返回二维数组
     * @throws IOException 异常
     */
    public static List<String[]> loadDataFromExcel(InputStream excelFile, ExcelTypeEnum excelType, int sheetIndex,
                                                   int beginCol, int endCol, int beginRow)
        throws IOException {
        Sheet sheet = loadExcelFileSheet(excelFile, excelType, sheetIndex);
        return loadDataFromExcelSheet(sheet, beginCol, endCol, beginRow, sheet.getLastRowNum() + 1);
    }

    /**
     * 所有的行列都是 0 Base的
     *
     * @param filePath   文件名，通过后缀名判断excel版本号
     * @param sheetIndex 读取的页面序号 0 base
     * @param beginCol   起始列  包含 beginCol
     * @param endCol     终止列 不包含 endCol
     * @param beginRow   起始行 包含 beginRow
     * @return 返回二维数组
     * @throws IOException 异常
     */
    public static List<String[]> loadDataFromExcel(String filePath, int sheetIndex,
                                                   int beginCol, int endCol, int beginRow)
        throws IOException {

        ExcelTypeEnum excelType = ExcelTypeEnum.checkFileExcelType(filePath);

        try (InputStream excelFile = new FileInputStream(new File(filePath))) {
            return loadDataFromExcel(excelFile, excelType, sheetIndex,
                beginCol, endCol, beginRow);
        }
    }

    /**
     * 所有的行列都是 0 Base的
     *
     * @param excelFile excel 文件流
     * @param excelType excel 版本 2003 还是新版本
     * @param sheetName 读取的页面名称
     * @param beginCol  起始列  包含 beginCol
     * @param endCol    终止列 不包含 endCol
     * @param beginRow  起始行 包含 beginRow
     * @return 返回二维数组
     * @throws IOException 异常
     */
    public static List<String[]> loadDataFromExcel(InputStream excelFile, ExcelTypeEnum excelType, String sheetName,
                                                   int beginCol, int endCol, int beginRow)
        throws IOException {
        Sheet sheet = loadExcelFileSheet(excelFile, excelType, sheetName);
        return loadDataFromExcelSheet(sheet, beginCol, endCol, beginRow, sheet.getLastRowNum() + 1);
    }

    /**
     * 所有的行列都是 0 Base的
     *
     * @param filePath  文件名，通过后缀名判断excel版本号
     * @param sheetName 读取的页面名称
     * @param beginCol  起始列  包含 beginCol
     * @param endCol    终止列 不包含 endCol
     * @param beginRow  起始行 包含 beginRow
     * @return 返回二维数组
     * @throws IOException 异常
     */
    public static List<String[]> loadDataFromExcel(String filePath, String sheetName,
                                                   int beginCol, int endCol, int beginRow)
        throws IOException {

        ExcelTypeEnum excelType = ExcelTypeEnum.checkFileExcelType(filePath);

        try (InputStream excelFile = new FileInputStream(new File(filePath))) {
            return loadDataFromExcel(excelFile, excelType, sheetName,
                beginCol, endCol, beginRow);
        }
    }

    private static List<String[]> loadDataFromExcelSheet(Sheet sheet,
                                                         int beginCol, int beginRow)
        throws IOException {

        if (sheet == null)
            return null;
        int maxRow = sheet.getLastRowNum();
        List<String[]> datas = new ArrayList<>(maxRow - beginRow + 1);
        for (int row = beginRow; row <= maxRow; row++) {

            Row excelRow = sheet.getRow(row);
            if (excelRow == null)
                continue;

            int endCol = excelRow.getLastCellNum();
            String[] rowObj = new String[endCol - beginCol + 1];
            int i = 0;
            //excelRow.getFirstCellNum()
            boolean hasValue = false;
            for (int col = beginCol; col <= endCol; col++) {
                Cell cell = excelRow.getCell(col);
                if (cell != null) {
                    hasValue = true;
                    rowObj[i] = getCellString(cell);
                }
                i++;
            }
            if (hasValue) {
                datas.add(rowObj);
            }
        }

        return datas;
    }

    /**
     * 所有的行列都是 0 Base的
     *
     * @param excelFile  excel 文件流
     * @param excelType  excel 版本 2003 还是新版本
     * @param sheetIndex 读取的页面序号 0 base
     * @param beginCol   起始列  包含 beginCol
     * @param beginRow   起始行 包含 beginRow
     * @return 返回二维数组
     * @throws IOException 异常
     */
    public static List<String[]> loadDataFromExcel(InputStream excelFile, ExcelTypeEnum excelType, int sheetIndex,
                                                   int beginCol, int beginRow)
        throws IOException {
        Sheet sheet = loadExcelFileSheet(excelFile, excelType, sheetIndex);
        return loadDataFromExcelSheet(sheet, beginCol, beginRow);
    }

    /**
     * 所有的行列都是 0 Base的
     *
     * @param filePath   文件名，通过后缀名判断excel版本号
     * @param sheetIndex 读取的页面序号 0 base
     * @param beginCol   起始列  包含 beginCol
     * @param beginRow   起始行 包含 beginRow
     * @return 返回二维数组
     * @throws IOException 异常
     */
    public static List<String[]> loadDataFromExcel(String filePath, int sheetIndex,
                                                   int beginCol, int beginRow)
        throws IOException {

        ExcelTypeEnum excelType = ExcelTypeEnum.checkFileExcelType(filePath);

        try (InputStream excelFile = new FileInputStream(new File(filePath))) {
            return loadDataFromExcel(excelFile, excelType, sheetIndex,
                beginCol, beginRow);
        }
    }

    /**
     * 所有的行列都是 0 Base的
     *
     * @param excelFile excel 文件流
     * @param excelType excel 版本 2003 还是新版本
     * @param sheetName 读取的页面名称
     * @param beginCol  起始列  包含 beginCol
     * @param beginRow  起始行 包含 beginRow
     * @return 返回二维数组
     * @throws IOException 异常
     */
    public static List<String[]> loadDataFromExcel(InputStream excelFile, ExcelTypeEnum excelType, String sheetName,
                                                   int beginCol, int beginRow)
        throws IOException {
        Sheet sheet = loadExcelFileSheet(excelFile, excelType, sheetName);
        return loadDataFromExcelSheet(sheet, beginCol, beginRow);
    }

    /**
     * 所有的行列都是 0 Base的
     *
     * @param filePath  文件名，通过后缀名判断excel版本号
     * @param sheetName 读取的页面名称
     * @param beginCol  起始列  包含 beginCol
     * @param beginRow  起始行 包含 beginRow
     * @return 返回二维数组
     * @throws IOException 异常
     */
    public static List<String[]> loadDataFromExcel(String filePath, String sheetName,
                                                   int beginCol, int beginRow)
        throws IOException {

        ExcelTypeEnum excelType = ExcelTypeEnum.checkFileExcelType(filePath);

        try (InputStream excelFile = new FileInputStream(new File(filePath))) {
            return loadDataFromExcel(excelFile, excelType, sheetName,
                beginCol, beginRow);
        }
    }

    /**
     * @param sheet     excel sheet对象类型
     * @param headerRow 属性名行 0 base
     * @param beginRow  起始行 0 base 包含
     * @param endRow    结束行 0 base 不包含
     * @return Map对象列表
     * throws IllegalAccessException 异常
     * throws InstantiationException 异常
     * throws IOException  异常
     */

    private static List<Map<String, Object>> loadMapFromExcelSheet(Sheet sheet, int headerRow, int beginRow, int endRow, int beginColumn, int endColumn) {
        Row headRow = sheet.getRow(headerRow);
        List<String> header = new ArrayList<>();
        if (endColumn <= 0) {
            endColumn = headRow.getLastCellNum() + 1;
        }
        if (beginColumn < 0) {
            beginColumn = 0;
        }
        for (int i = beginColumn; i < endColumn; i++) {
            Cell cell = headRow.getCell(i);
            header.add(cell == null ? "column" + i : cell.toString());
        }
        if (endRow <= 0) {
            endRow = sheet.getLastRowNum() + 1;
        }
        if (beginRow <= headerRow) {
            beginRow = headerRow + 1;
        }
        List<Map<String, Object>> datas = new ArrayList<>();
        // 遍历当前sheet中的所有行
        for (int row = beginRow; row < endRow; row++) {
            Row dataRow = sheet.getRow(row);
            if (dataRow == null) {
                continue;
            }
            Map<String, Object> rowData = new LinkedHashMap<>();
            // 遍历所有的列
            for (int column = beginColumn; column <= dataRow.getLastCellNum(); column++) {
                Object cellValue = ExcelImportUtil.getCellValue(dataRow.getCell(column));
                if (cellValue != null) {
                    String key = column <= endColumn ? header.get(column) : "column" + column;
                    rowData.put(key, cellValue);
                }
            }
            // while
            if (!rowData.isEmpty()) {
                datas.add(rowData);
            }
        }
        return datas;
    }


    private static List<Map<String, Object>> loadMapFromExcelSheet(Sheet sheet, int headerRow, int beginRow, int endRow) {
        return loadMapFromExcelSheet(sheet, headerRow, beginRow, endRow, 0, -1);
    }

    /**
     * @param filePath  excel 文件名
     * @param sheetName sheet 名称
     * @return Map对象列表
     * @throws IOException 异常
     */
    public static List<Map<String, Object>> loadMapFromExcelSheet(String filePath, String sheetName)
        throws IOException {
        ExcelTypeEnum excelType = ExcelTypeEnum.checkFileExcelType(filePath);
        try (InputStream excelFile = new FileInputStream(new File(filePath))) {
            Sheet sheet = loadExcelFileSheet(excelFile, excelType, sheetName);
            int firstRow = sheet.getFirstRowNum();
            return loadMapFromExcelSheet(sheet, firstRow, firstRow + 1, sheet.getLastRowNum() + 1);
        }
    }

    /**
     * @param filePath   excel 文件名
     * @param sheetIndex sheetIndex 0 base
     * @return Map对象列表
     * @throws IOException 异常
     */
    public static List<Map<String, Object>> loadMapFromExcelSheet(String filePath, int sheetIndex)
        throws IOException {
        ExcelTypeEnum excelType = ExcelTypeEnum.checkFileExcelType(filePath);
        try (InputStream excelFile = new FileInputStream(new File(filePath))) {
            Sheet sheet = loadExcelFileSheet(excelFile, excelType, sheetIndex);
            int firstRow = sheet.getFirstRowNum();
            return loadMapFromExcelSheet(sheet, firstRow, firstRow + 1, sheet.getLastRowNum() + 1);
        }
    }

    /*
     * 获取excl第一行列
     * @param filePath
     * @param sheetIndex
     * @return
     * @throws IOException
     */
    public static String[] loadColumnsFromExcel(String filePath, int sheetIndex)
        throws IOException {
        ExcelTypeEnum excelType = ExcelTypeEnum.checkFileExcelType(filePath);
        try (InputStream excelFile = new FileInputStream(new File(filePath))) {
            Sheet sheet = loadExcelFileSheet(excelFile, excelType, sheetIndex);
            int firstRow = sheet.getFirstRowNum();
            return loadDataFromExcelSheet(sheet, 0, firstRow).get(0);
        }
    }

    /**
     * @param filePath  excel 文件名
     * @param sheetName sheet 名称
     * @param headerRow 属性名行 0 base
     * @param beginRow  起始行 0 base 包含
     * @return Map对象列表
     * @throws IOException 异常
     */
    public static List<Map<String, Object>> loadMapFromExcelSheet(String filePath, String sheetName, int headerRow, int beginRow)
        throws IOException {
        ExcelTypeEnum excelType = ExcelTypeEnum.checkFileExcelType(filePath);
        try (InputStream excelFile = new FileInputStream(new File(filePath))) {
            Sheet sheet = loadExcelFileSheet(excelFile, excelType, sheetName);
            return loadMapFromExcelSheet(sheet, headerRow, beginRow, sheet.getLastRowNum() + 1);
        }
    }

    /**
     * @param filePath   excel 文件名
     * @param sheetIndex sheetIndex 0 base
     * @param headerRow  属性名行 0 base
     * @param beginRow   起始行 0 base 包含
     * @return Map对象列表
     * @throws IOException 异常
     */
    public static List<Map<String, Object>> loadMapFromExcelSheet(String filePath, int sheetIndex, int headerRow, int beginRow)
        throws IOException {
        ExcelTypeEnum excelType = ExcelTypeEnum.checkFileExcelType(filePath);
        try (InputStream excelFile = new FileInputStream(new File(filePath))) {
            Sheet sheet = loadExcelFileSheet(excelFile, excelType, sheetIndex);
            return loadMapFromExcelSheet(sheet, headerRow, beginRow, sheet.getLastRowNum() + 1);
        }
    }

    /**
     * @param filePath  excel 文件名
     * @param sheetName sheet 名称
     * @param headerRow 属性名行 0 base
     * @param beginRow  起始行 0 base 包含
     * @param endRow    结束行 0 base 不包含
     * @return Map对象列表
     * @throws IOException 异常
     */
    public static List<Map<String, Object>> loadMapFromExcelSheet(String filePath, String sheetName, int headerRow, int beginRow, int endRow)
        throws IOException {
        ExcelTypeEnum excelType = ExcelTypeEnum.checkFileExcelType(filePath);
        try (InputStream excelFile = new FileInputStream(new File(filePath))) {
            Sheet sheet = loadExcelFileSheet(excelFile, excelType, sheetName);
            return loadMapFromExcelSheet(sheet, headerRow, beginRow, endRow);
        }
    }

    /**
     * @param filePath   excel 文件名
     * @param sheetIndex sheetIndex 0 base
     * @param headerRow  属性名行 0 base
     * @param beginRow   起始行 0 base 包含
     * @param endRow     结束行 0 base 不包含
     * @return Map对象列表
     * @throws IOException 异常
     */
    public static List<Map<String, Object>> loadMapFromExcelSheet(String filePath, int sheetIndex, int headerRow, int beginRow, int endRow)
        throws IOException {
        ExcelTypeEnum excelType = ExcelTypeEnum.checkFileExcelType(filePath);
        try (InputStream excelFile = new FileInputStream(new File(filePath))) {
            Sheet sheet = loadExcelFileSheet(excelFile, excelType, sheetIndex);
            return loadMapFromExcelSheet(sheet, headerRow, beginRow, endRow);
        }
    }
    //------------------------------------------------------------

    /**
     * @param excelFile excel 文件名
     * @param sheetName sheet 名称
     * @return Map对象列表
     * @throws IOException 异常
     */
    public static List<Map<String, Object>> loadMapFromExcelSheet(InputStream excelFile, String sheetName)
        throws IOException {
        Sheet sheet = loadExcelFileSheet(excelFile, sheetName);
        int firstRow = sheet.getFirstRowNum();
        return loadMapFromExcelSheet(sheet, firstRow, firstRow + 1, sheet.getLastRowNum() + 1);
    }

    /**
     * @param excelFile  excel 文件名
     * @param sheetIndex sheetIndex 0 base
     * @return Map对象列表
     * @throws IOException 异常
     */
    public static List<Map<String, Object>> loadMapFromExcelSheet(InputStream excelFile, int sheetIndex)
        throws IOException {
        Sheet sheet = loadExcelFileSheet(excelFile, sheetIndex);
        int firstRow = sheet.getFirstRowNum();
        return loadMapFromExcelSheet(sheet, firstRow, firstRow + 1, sheet.getLastRowNum() + 1);
    }

    /**
     * @param excelFile excel 文件名
     * @param sheetName sheet 名称
     * @param headerRow 属性名行 0 base
     * @param beginRow  起始行 0 base 包含
     * @return Map对象列表
     * @throws IOException 异常
     */
    public static List<Map<String, Object>> loadMapFromExcelSheet(InputStream excelFile, String sheetName, int headerRow, int beginRow)
        throws IOException {
        Sheet sheet = loadExcelFileSheet(excelFile, sheetName);
        return loadMapFromExcelSheet(sheet, headerRow, beginRow, sheet.getLastRowNum() + 1);
    }

    /**
     * @param excelFile  excel 文件名
     * @param sheetIndex sheetIndex 0 base
     * @param headerRow  属性名行 0 base
     * @param beginRow   起始行 0 base 包含
     * @return Map对象列表
     * @throws IOException 异常
     */
    public static List<Map<String, Object>> loadMapFromExcelSheet(InputStream excelFile, int sheetIndex, int headerRow, int beginRow)
        throws IOException {
        Sheet sheet = loadExcelFileSheet(excelFile, sheetIndex);
        return loadMapFromExcelSheet(sheet, headerRow, beginRow, sheet.getLastRowNum() + 1);
    }

    /**
     * @param excelFile excel 文件名
     * @param sheetName sheet 名称
     * @param headerRow 属性名行 0 base
     * @param beginRow  起始行 0 base 包含
     * @param endRow    结束行 0 base 不包含
     * @return Map对象列表
     * @throws IOException 异常
     */
    public static List<Map<String, Object>> loadMapFromExcelSheet(InputStream excelFile, String sheetName, int headerRow, int beginRow, int endRow)
        throws IOException {
        Sheet sheet = loadExcelFileSheet(excelFile, sheetName);
        return loadMapFromExcelSheet(sheet, headerRow, beginRow, endRow);
    }

    /**
     * @param excelFile  excel 文件名
     * @param sheetIndex sheetIndex 0 base
     * @param headerRow  属性名行 0 base
     * @param beginRow   起始行 0 base 包含
     * @param endRow     结束行 0 base 不包含
     * @return Map对象列表
     * @throws IOException 异常
     */
    public static List<Map<String, Object>> loadMapFromExcelSheet(InputStream excelFile, int sheetIndex, int headerRow, int beginRow, int endRow)
        throws IOException {
        Sheet sheet = loadExcelFileSheet(excelFile, sheetIndex);
        return loadMapFromExcelSheet(sheet, headerRow, beginRow, endRow);
    }


    /**
     * @param excelFile   excel 文件名
     * @param sheetName   sheet 名称
     * @param headerRow   属性名行 0 base
     * @param beginRow    起始行 0 base 包含
     * @param endRow      结束行 0 base 不包含
     * @param beginColumn 起始列 0 base 包含
     * @param endColumn   结束列 0 base 不包含
     * @return Map对象列表
     * @throws IOException 异常
     */
    public static List<Map<String, Object>> loadMapFromExcelSheet(InputStream excelFile, String sheetName, int headerRow, int beginRow, int endRow, int beginColumn, int endColumn)
        throws IOException {
        Sheet sheet = loadExcelFileSheet(excelFile, sheetName);
        return loadMapFromExcelSheet(sheet, headerRow, beginRow, endRow, beginColumn, endColumn);
    }

    /**
     * @param excelFile   excel 文件名
     * @param sheetIndex  sheetIndex 0 base
     * @param headerRow   属性名行 0 base
     * @param beginRow    起始行 0 base 包含
     * @param endRow      结束行 0 base 不包含
     * @param beginColumn 起始列 0 base 包含
     * @param endColumn   结束列 0 base 不包含
     * @return Map对象列表
     * @throws IOException 异常
     */
    public static List<Map<String, Object>> loadMapFromExcelSheet(InputStream excelFile, int sheetIndex, int headerRow, int beginRow, int endRow, int beginColumn, int endColumn)
        throws IOException {
        Sheet sheet = loadExcelFileSheet(excelFile, sheetIndex);
        return loadMapFromExcelSheet(sheet, headerRow, beginRow, endRow, beginColumn, endColumn);
    }


    private static List<Map<String, Object>> loadMapFromExcelSheet(Sheet sheet,
                                                                   Map<Integer, String> fieldDesc, int beginRow, int endRow) {
        if (sheet == null)
            return null;
        int columns = fieldDesc.size();
        List<Map<String, Object>> datas = new ArrayList<>(endRow - beginRow + 1);
        for (int row = beginRow; row < endRow; row++) {
            Row excelRow = sheet.getRow(row);
            if (excelRow == null)
                continue;
            int i = 0;
            Map<String, Object> rowObj = new HashMap<>(columns + 2);
            boolean hasValue = false;
            //excelRow.getFirstCellNum()
            for (Map.Entry<Integer, String> ent : fieldDesc.entrySet()) {
                Cell cell = excelRow.getCell(ent.getKey());
                if (cell != null && StringUtils.isNotBlank(cell.toString())) {
                    hasValue = true;
                    rowObj.put(ent.getValue(), getCellValue(cell));
                }
            }
            if (hasValue) {
                datas.add(rowObj);
            }
        }
        return datas;
    }

    /**
     * @param excelFile  excel 文件名
     * @param sheetIndex sheetIndex 0 base
     * @param fieldDesc  字段对应关系
     * @param endRow     结束行包含
     * @param beginRow   起始行 0 base 包含
     * @return Map对象列表
     * @throws IOException 异常
     */
    public static List<Map<String, Object>> loadMapFromExcelSheet(InputStream excelFile, int sheetIndex, Map<Integer, String> fieldDesc, int beginRow, int endRow)
        throws IOException {
        Sheet sheet = loadExcelFileSheet(excelFile, sheetIndex);
        return loadMapFromExcelSheet(sheet, fieldDesc, beginRow, endRow);
    }

    /**
     * @param excelFile excel 文件名
     * @param sheetName sheetIndex 0 base
     * @param fieldDesc 字段对应关系
     * @param beginRow  起始行 0 base 包含
     * @param endRow    结束行包含
     * @return Map对象列表
     * @throws IOException 异常
     */
    public static List<Map<String, Object>> loadMapFromExcelSheet(InputStream excelFile, String sheetName, Map<Integer, String> fieldDesc, int beginRow, int endRow)
        throws IOException {
        Sheet sheet = loadExcelFileSheet(excelFile, sheetName);
        return loadMapFromExcelSheet(sheet, fieldDesc, beginRow, endRow);
    }


    /**
     * @param excelFile 文件流
     * @param excelType excel 版本 2003 还是新版本
     * @param sheetName sheet名称 如果为空为 第一个页面
     * @param fieldDesc 字段对应关系
     * @param beginRow  起始行 0 base 包含
     * @param endRow    结束行 0 base 不包含
     * @return 对象列表
     * @throws IOException 异常
     */
    public static List<Map<String, Object>> loadMapFromExcel(InputStream excelFile, ExcelTypeEnum excelType, String sheetName,
                                                             Map<Integer, String> fieldDesc, int beginRow, int endRow)
        throws IOException {
        Sheet sheet = loadExcelFileSheet(excelFile, excelType, sheetName);
        return loadMapFromExcelSheet(sheet, fieldDesc, beginRow, endRow);
    }

    /**
     * @param filePath  文件名，通过后缀名判断excel版本号
     * @param sheetName sheet名称 如果为空为 第一个页面
     * @param fieldDesc 字段对应关系
     * @param beginRow  起始行 0 base 包含
     * @param endRow    结束行 0 base 不包含
     * @return 对象列表
     * @throws IOException 异常
     */
    public static List<Map<String, Object>> loadMapFromExcel(String filePath, String sheetName,
                                                             Map<Integer, String> fieldDesc, int beginRow, int endRow)
        throws IOException {
        ExcelTypeEnum excelType = ExcelTypeEnum.checkFileExcelType(filePath);
        try (InputStream excelFile = new FileInputStream(new File(filePath))) {
            return loadMapFromExcel(excelFile, excelType, sheetName,
                fieldDesc, beginRow, endRow);
        }
    }


    /**
     * @param excelFile 文件流
     * @param excelType excel 版本 2003 还是新版本
     * @param sheetName sheet名称 如果为空为 第一个页面
     * @param fieldDesc 字段对应关系
     * @param beginRow  起始行 0 base 包含
     * @return 对象列表
     * @throws IOException 异常
     */
    public static List<Map<String, Object>> loadMapFromExcel(InputStream excelFile, ExcelTypeEnum excelType, String sheetName,
                                                             Map<Integer, String> fieldDesc, int beginRow)
        throws IOException {
        Sheet sheet = loadExcelFileSheet(excelFile, excelType, sheetName);
        return loadMapFromExcelSheet(sheet, fieldDesc, beginRow, sheet.getLastRowNum() + 1);
    }

    /**
     * @param filePath  文件名，通过后缀名判断excel版本号
     * @param sheetName sheet名称 如果为空为 第一个页面
     * @param fieldDesc 字段对应关系
     * @param beginRow  起始行 0 base 包含
     * @return 对象列表
     * @throws IOException 异常
     */
    public static List<Map<String, Object>> loadMapFromExcel(String filePath, String sheetName,
                                                             Map<Integer, String> fieldDesc, int beginRow)
        throws IOException {
        ExcelTypeEnum excelType = ExcelTypeEnum.checkFileExcelType(filePath);
        try (InputStream excelFile = new FileInputStream(new File(filePath))) {
            return loadMapFromExcel(excelFile, excelType, sheetName,
                fieldDesc, beginRow);
        }
    }

    /**
     * @param excelFile  文件流
     * @param excelType  excel 版本 2003 还是新版本
     * @param sheetIndex sheet 序号 0 base
     * @param fieldDesc  字段对应关系
     * @param beginRow   起始行 0 base 包含
     * @param endRow     结束行 0 base 不包含
     * @return 对象列表
     * @throws IOException 异常
     */
    public static List<Map<String, Object>> loadMapFromExcel(InputStream excelFile, ExcelTypeEnum excelType, int sheetIndex,
                                                             Map<Integer, String> fieldDesc, int beginRow, int endRow)
        throws IOException {
        Sheet sheet = loadExcelFileSheet(excelFile, excelType, sheetIndex);
        return loadMapFromExcelSheet(sheet, fieldDesc, beginRow, endRow);
    }

    /**
     * @param filePath   文件名，通过后缀名判断excel版本号
     * @param sheetIndex sheet 序号 0 base
     * @param fieldDesc  字段对应关系
     * @param beginRow   起始行 0 base 包含
     * @param endRow     结束行 0 base 不包含
     * @return 对象列表
     * @throws IOException 异常
     */
    public static List<Map<String, Object>> loadMapFromExcel(String filePath, int sheetIndex,
                                                             Map<Integer, String> fieldDesc, int beginRow, int endRow)
        throws IOException {
        ExcelTypeEnum excelType = ExcelTypeEnum.checkFileExcelType(filePath);
        try (InputStream excelFile = new FileInputStream(filePath)) {
            return loadMapFromExcel(excelFile, excelType, sheetIndex,
                fieldDesc, beginRow, endRow);
        }
    }

    /**
     * @param excelFile  文件流
     * @param excelType  excel 版本 2003 还是新版本
     * @param sheetIndex sheet 序号 0 base
     * @param fieldDesc  字段对应关系
     * @param beginRow   起始行 0 base 包含
     * @return 对象列表
     * @throws IOException 异常
     */
    public static List<Map<String, Object>> loadMapFromExcel(InputStream excelFile, ExcelTypeEnum excelType, int sheetIndex,
                                                             Map<Integer, String> fieldDesc, int beginRow)
        throws IOException {

        Workbook wb = excelType == ExcelTypeEnum.HSSF ?
            new HSSFWorkbook(excelFile) :  /*new SXSSFWorkbook(*/new XSSFWorkbook(excelFile);
        Sheet sheet = wb.getSheetAt(sheetIndex);

        return loadMapFromExcelSheet(sheet, fieldDesc, beginRow, sheet.getLastRowNum() + 1);
    }

    /**
     * @param filePath   文件名，通过后缀名判断excel版本号
     * @param sheetIndex sheet 序号 0 base
     * @param fieldDesc  字段对应关系
     * @param beginRow   起始行 0 base 包含
     * @return 对象列表
     * @throws IOException 异常
     */
    public static List<Map<String, Object>> loadMapFromExcel(String filePath, int sheetIndex,
                                                             Map<Integer, String> fieldDesc, int beginRow)
        throws IOException {
        ExcelTypeEnum excelType = ExcelTypeEnum.checkFileExcelType(filePath);
        try (InputStream excelFile = new FileInputStream(filePath)) {
            return loadMapFromExcel(excelFile, excelType, sheetIndex,
                fieldDesc, beginRow);
        }
    }


}
