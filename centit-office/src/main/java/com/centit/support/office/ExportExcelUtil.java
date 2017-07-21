package com.centit.support.office;

import com.centit.support.algorithm.ReflectionOpt;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * 生成基本EXCEL工具类
 *
 * @author codefan@sina.com
 * 2013-6-25
 */
@SuppressWarnings("unused")
public class ExportExcelUtil {


    public static InputStream generateExcel(List<? extends Object> objLists) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        HSSFSheet sheet = ExportExcelUtil.createDefaultSheet();
        try {
            generateObjText(sheet, objLists);
            sheet.getWorkbook().write(baos);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new ByteArrayInputStream(baos.toByteArray());
    }


    /**
     * 生成Excel字节流
     *
     * @param objLists 对象集合
     * @param header   Excel页头
     * @param property 需要显示的属性
     * @return InputStream excel 文件流
     */
    public static InputStream generateExcel(List<? extends Object> objLists, String[] header, String[] property) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        HSSFSheet sheet = ExportExcelUtil.createDefaultSheet();

        generateHeader(sheet, header);

        try {
            generateText(sheet, objLists, property);
            sheet.getWorkbook().write(baos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

        return new ByteArrayInputStream(baos.toByteArray());
    }

    /**
     * 生成Excel字节流
     *
     * @param objLists 对象集合
     * @param header   Excel页头
     * @param property 需要显示的属性
     * @return InputStream excel 文件流
     */
    public static InputStream generateExcel(List<Object[]> objLists, String[] header, int[] property) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        HSSFSheet sheet = ExportExcelUtil.createDefaultSheet();

        generateHeader(sheet, header);

        try {
            generateText(sheet, objLists, property);
            sheet.getWorkbook().write(baos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

        return new ByteArrayInputStream(baos.toByteArray());
    }


    /**
     * 生成Excel字节流
     *
     * @param objLists 对象数组集合
     * @param header   Excel页头
     * @return InputStream excel 文件流
     */
    public static InputStream generateExcel(List<Object[]> objLists, String[] header) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        HSSFSheet sheet = ExportExcelUtil.createDefaultSheet();
        generateHeader(sheet, header);
        generateText(sheet, objLists);

        try {
            sheet.getWorkbook().write(baos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new ByteArrayInputStream(baos.toByteArray());

    }


    private static HSSFSheet createDefaultSheet() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet();

        return new ExportExcelUtil(wb, sheet).getSheet();
    }


    private static void generateHeader(HSSFSheet sheet, String[] header) {
        HSSFRow headerRow = sheet.createRow(0);
        for (int i = 0; i < header.length; i++) {
            HSSFCell cell = headerRow.createCell(i);
            setCellStyle(sheet.getWorkbook(), cell);

            cell.setCellValue(header[i]);
        }
    }

    @SuppressWarnings("unchecked")
    private static void generateText(HSSFSheet sheet, List<? extends Object> objLists, String[] property) throws NoSuchFieldException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        for (int i = 0; i < objLists.size(); i++) {
            HSSFRow textRow = sheet.createRow(i + 1);
            for (int j = 0; j < property.length; j++) {
                HSSFCell cell = textRow.createCell(j);
                setCellStyle(sheet.getWorkbook(), cell);

                //List中是Map类型
                if (objLists.get(i) instanceof Map) {
                    Object val = ((Map<String,Object>) objLists.get(i)).get(property[j]);
                    cell.setCellValue(null == val ? "" : val.toString());
                    continue;
                }

                //List中是普通Po类型
                
                Method method = ReflectionOpt.getGetterMethod(objLists.get(i).getClass(), property[j]);
                if(method == null)
                    method = ReflectionOpt.getBooleanGetterMethod(objLists.get(i).getClass(), property[j]);
                
                Object val = method.invoke(objLists.get(i));
                
                cell.setCellValue(null == val ? "" : val.toString());
            }
        }
    }

    private static void generateText(HSSFSheet sheet, List<Object[]> objLists, int[] property) throws NoSuchFieldException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        for (int i = 0; i < objLists.size(); i++) {
            HSSFRow textRow = sheet.createRow(i + 1);
            for (int j = 0; j < property.length; j++) {
                HSSFCell cell = textRow.createCell(j);
                setCellStyle(sheet.getWorkbook(), cell);

                //if (ClassUtils.isAssignableValue(Object.class, objLists.get(i).getClass())) {
                Object val = (objLists.get(i))[property[j]];
                cell.setCellValue(null == val ? "" : val.toString());
                //}
            }
        }
    }

    private static void generateObjText(HSSFSheet sheet, List<? extends Object> objLists) throws InvocationTargetException, IllegalAccessException {
        for (int i = 0; i < objLists.size(); i++) {
            HSSFRow textRow = sheet.createRow(i);

            List<Method> getMethods = ReflectionOpt.getAllGetterMethod(objLists.get(i).getClass());
            //Field[] fields = objLists.get(i).getClass().getDeclaredFields();
            int errorLen = 0;
            for (int j = 0; j < getMethods.size(); j++) {
              

                HSSFCell cell = textRow.createCell(j - errorLen);
                setCellStyle(sheet.getWorkbook(), cell);

                Object val = getMethods.get(j).invoke(objLists.get(i));
                cell.setCellValue(null == val ? "" : val.toString());
            }
        }
    }

    private static void generateText(HSSFSheet sheet, List<Object[]> objLists) {
        for (int i = 0; i < objLists.size(); i++) {
            HSSFRow textRow = sheet.createRow(i + 1);
            for (int j = 0; j < objLists.get(i).length; j++) {
                HSSFCell cell = textRow.createCell(j);

                setCellStyle(sheet.getWorkbook(), cell);

                cell.setCellValue(null == objLists.get(i)[j] ? "" : objLists.get(i)[j].toString());
            }
        }
    }

    private static void setCellStyle(HSSFWorkbook wb, HSSFCell cell) {
        cell.setCellType( CellType.forInt(HSSFCell.ENCODING_UTF_16) );
        cell.setCellStyle(getDefaultCellStyle(wb));
    }

    /**
     * 设置单元格默认样式
     *
     * @param wb
     * @return
     */
    private static HSSFCellStyle getDefaultCellStyle(HSSFWorkbook wb) {
        HSSFCellStyle cellStyle = wb.createCellStyle();

        // 指定单元格居中对齐
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        // 指定单元格垂直居中对齐
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
//        cellStyle.setWrapText(true);// 指定单元格自动换行

        // 设置单元格字体
        HSSFFont font = wb.createFont();
//        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        font.setFontName("宋体");
//        font.setFontHeight((short) 300);
        cellStyle.setFont(font);

        return cellStyle;
    }


    private HSSFWorkbook wb = null;

    private HSSFSheet sheet = null;

    private ExportExcelUtil() {
    }

    /**
     * @param wb
     * @param sheet
     */
    private ExportExcelUtil(HSSFWorkbook wb, HSSFSheet sheet) {
        super();
        this.wb = wb;
        this.sheet = sheet;
    }

    /**
     * @return the sheet
     */
    public HSSFSheet getSheet() {
        return sheet;
    }

    /**
     * @param sheet the sheet to set
     */
    public void setSheet(HSSFSheet sheet) {
        this.sheet = sheet;
    }

    /**
     * @return the wb
     */
    public HSSFWorkbook getWb() {
        return wb;
    }

    /**
     * @param wb the wb to set
     */
    public void setWb(HSSFWorkbook wb) {
        this.wb = wb;
    }


}
