package com.centit.support.office.commons;

import com.centit.support.algorithm.BooleanBaseOpt;
import com.centit.support.algorithm.DatetimeOpt;
import com.centit.support.algorithm.StringRegularOpt;
import com.itextpdf.text.Font;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class Excel2PdfUtils {

    public static int getRGB(Color color) {
        int result = 0x00FFFFFF;

        int red = 0;
        int green = 0;
        int blue = 0;

        if (color instanceof HSSFColor) {
            HSSFColor hssfColor = (HSSFColor) color;
            short[] rgb = hssfColor.getTriplet();
            red = rgb[0];
            green = rgb[1];
            blue = rgb[2];
        }

        if (color instanceof XSSFColor) {
            XSSFColor xssfColor = (XSSFColor) color;
            byte[] rgb = xssfColor.getRGB();
            if (rgb != null) {
                red = (rgb[0] < 0) ? (rgb[0] + 256) : rgb[0];
                green = (rgb[1] < 0) ? (rgb[1] + 256) : rgb[1];
                blue = (rgb[2] < 0) ? (rgb[2] + 256) : rgb[2];
            }
        }

        if (red != 0 || green != 0 || blue != 0) {
            result = new java.awt.Color(red, green, blue).getRGB();
        }

        return result;
    }

    /**
     * 获取边框的颜色
     *
     * @param wb    文档对象
     * @param index 颜色版索引
     * @return RGB 三通道颜色
     */
    public static int getBorderRBG(Workbook wb, short index) {
        int result = 0;

        if (wb instanceof HSSFWorkbook) {
            HSSFWorkbook hwb = (HSSFWorkbook) wb;
            HSSFColor color = hwb.getCustomPalette().getColor(index);
            if (color != null) {
                result = getRGB(color);
            }
        }

        if (wb instanceof XSSFWorkbook) {
            XSSFColor color = new XSSFColor();
            color.setIndexed(index);
            result = getRGB(color);
        }

        return result;
    }

    public static CellRangeAddress getColspanRowspanByExcel(Sheet sheet, int rowIndex, int colIndex) {
        CellRangeAddress result = null;
        int num = sheet.getNumMergedRegions();
        for (int i = 0; i < num; i++) {
            CellRangeAddress range = sheet.getMergedRegion(i);
            if (range.getFirstColumn() == colIndex && range.getFirstRow() == rowIndex) {
                result = range;
            }
        }
        return result;
    }

    public static int getPOIColumnWidth(Sheet sheet, Cell cell) {
        int poiCWidth = sheet.getColumnWidth(cell.getColumnIndex());
        int colWidthpoi = poiCWidth;
        int widthPixel = 0;
        if (colWidthpoi >= 416) {
            widthPixel = (int) (((colWidthpoi - 416.0) / 256.0) * 8.0 + 13.0 + 0.5);
        } else {
            widthPixel = (int) (colWidthpoi / 416.0 * 13.0 + 0.5);
        }
        return widthPixel;
    }

    public static String getCellString(Cell cell) {
        if (cell == null) {
            return null;
        }
        /*if(cell instanceof XSSFCell){
            return ((XSSFCell) cell).getRawValue();
        }*/

        String value;
        CellType cellType = cell.getCellType();
        if(cellType == CellType.FORMULA){
            cellType = cell.getCachedFormulaResultType();
        }
        switch (cellType) {
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {

                    String dataFormat = cell.getCellStyle().getDataFormatString();
                    if(StringUtils.containsIgnoreCase(dataFormat, "yy")) {
                        value = DatetimeOpt.convertTimestampToString(cell.getDateCellValue());
                    } else {
                        value = DatetimeOpt.convertTimeWithSecondToString(cell.getDateCellValue());
                    }
                } else {
                    String dataFormat = cell.getCellStyle().getDataFormatString();
                    int dotPos = dataFormat.indexOf(".");
                    if(dotPos>=0){
                        int ePos = dataFormat.indexOf("%");
                        if(ePos>=0){
                            value = new DecimalFormat(StringUtils.rightPad("#.",ePos-dotPos+1, '0'))
                                .format(cell.getNumericCellValue()*100)+"%";
                        }else{
                            ePos = dataFormat.indexOf("_");
                            if(ePos<0){
                                ePos = dataFormat.length();
                            }
                            value = new DecimalFormat(StringUtils.rightPad("#.",ePos-dotPos+1, '0'))
                                .format(cell.getNumericCellValue());
                        }
                    }else {
                        value = new DecimalFormat("#.00000000000").format(cell.getNumericCellValue());
                        value = StringRegularOpt.trimRightZeroInNumber(value);
                    }
                }
                break;
            case BOOLEAN:
                value = cell.getBooleanCellValue() ? BooleanBaseOpt.STRING_TRUE : BooleanBaseOpt.STRING_FALSE;
                break;

            case STRING:
                value = cell.getRichStringCellValue().getString().trim();
                break;

            case ERROR:
                return ErrorEval.getText(cell.getErrorCellValue());

            case FORMULA:
            case BLANK:
            default:
                value = cell.toString();
                break;
        }
        return value;
    }

    public static Phrase getPhrase(Workbook wb, Cell cell, boolean hasAnchor, int sheetIndex) {
        if(hasAnchor){
            return new Phrase(getCellString(cell),
                getFontByExcel(wb, cell.getCellStyle()));
        } else {
            Anchor anchor = new Anchor(getCellString(cell),
                getFontByExcel(wb, cell.getCellStyle()));
            anchor.setName("excel_sheet_" + sheetIndex);
            return anchor;
        }
    }

    public static PdfPTable toParseContent(Workbook wb, Sheet sheet, int sheetIndex) throws BadElementException, IOException {

        List<PdfPCell> cells = new ArrayList<>();
        List<Integer> colRanges= new ArrayList<>(100);
        float[] widths = {30F,400F};
        float mw = 0;
        boolean hasAnchor = false;
        for (int i = sheet.getFirstRowNum(); i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if(row==null){
                continue;
            }
            int columns = row.getLastCellNum();
            if(columns <= 0){
                for(int k=0; k<colRanges.size(); k++){
                    PdfPCell pdfpCell = new PdfPCell();
                    pdfpCell.setBorder(Rectangle.NO_BORDER);
                    pdfpCell.setFixedHeight(20);
                    cells.add(pdfpCell);
                }
                continue;
            }
            for(int j=colRanges.size();j<columns; j++){
                colRanges.add(i);
            }
            float[] cws = new float[columns];
            for (int j = 0; j < columns; j++) {
                int lastRow = colRanges.get(j);
                if(i<lastRow) {
                    continue;
                }
                Cell cell = row.getCell(j);
                if (cell == null) {
                    PdfPCell pdfpCell = new PdfPCell();
                    cells.add(pdfpCell);
                } else {
                    float cw = getPOIColumnWidth(sheet, cell);
                    cws[cell.getColumnIndex()] = cw;

                    //cell.setCellType(CellType.STRING);
                    CellRangeAddress range = getColspanRowspanByExcel(sheet, row.getRowNum(), cell.getColumnIndex());

                    int rowspan = 1;
                    int colspan = 1;
                    if (range != null) {
                        rowspan = range.getLastRow() - range.getFirstRow() + 1;
                        colspan = range.getLastColumn() - range.getFirstColumn() + 1;
                        if(rowspan>1){
                            for(int k=0; k<colspan; k++){
                                colRanges.set(j+k, i+rowspan);
                            }
                        }
                        if(colspan>1){
                            j += colspan - 1;
                        }
                    }

                    PdfPCell pdfpCell = new PdfPCell();
                    pdfpCell.setBackgroundColor(new BaseColor(getRGB(
                        cell.getCellStyle().getFillForegroundColorColor())));
                    pdfpCell.setColspan(colspan);
                    pdfpCell.setRowspan(rowspan);
                    pdfpCell.setVerticalAlignment(getVAlignByExcel(cell.getCellStyle().getVerticalAlignment()));
                    pdfpCell.setHorizontalAlignment(getHAlignByExcel(cell.getCellStyle().getAlignment()));
                    pdfpCell.setPhrase(getPhrase(wb, cell, hasAnchor, sheetIndex));
                    hasAnchor = true;

                    if (sheet.getDefaultRowHeightInPoints() != row.getHeightInPoints()) {
                        pdfpCell.setFixedHeight(getPixelHeight(row.getHeightInPoints()));
                    }
                    addBorderByExcel(wb, pdfpCell, cell.getCellStyle());
                    addImageByPOICell(sheet, pdfpCell, cell);
                    cells.add(pdfpCell);
                }
            }

            float rw = 0;
            for (int j = 0; j < cws.length; j++) {
                rw += cws[j];
            }
            if (rw > mw || mw == 0) {
                widths = cws;
                mw = rw;
            }
        }

        PdfPTable table = new PdfPTable(widths);
        table.setWidthPercentage(100);
//        table.setLockedWidth(true);
        for (PdfPCell pdfpCell : cells) {
            table.addCell(pdfpCell);
        }
        return table;
    }

    public static void toCreateContentIndexes(Document document, int sheetSize) throws DocumentException{
        PdfPTable table = new PdfPTable(1);
        table.setKeepTogether(true);
        table.getDefaultCell().setBorder(PdfPCell.NO_BORDER);
        //
        Font font = new Font(PDFPageEvent.BASE_FONT_CHINESE , 12 , Font.NORMAL);
        font.setColor(new BaseColor(0,0,255));
        //
        for (int i = 0; i < sheetSize; i++) {
            Anchor anchor = new Anchor("excel_sheet_" + i , font);
            anchor.setReference("#excel_sheet_" + i);
            //
            PdfPCell cell = new PdfPCell(anchor);
            cell.setBorder(0);
            //
            table.addCell(cell);
        }
        //
        document.add(table);
    }

    public static float getPixelHeight(float poiHeight){
        float pixel = poiHeight / 28.6f * 26f;
        return pixel;
    }


    public static void addImageByPOICell(Sheet sheet,
                                     PdfPCell pdfpCell , Cell cell) throws BadElementException, IOException{
        if (sheet instanceof HSSFSheet) {
            HSSFSheet hssfSheet = (HSSFSheet) sheet;
            if (hssfSheet.getDrawingPatriarch() != null) {
                List<HSSFShape> shapes = hssfSheet.getDrawingPatriarch().getChildren();
                for (HSSFShape shape : shapes) {
                    HSSFClientAnchor anchor = (HSSFClientAnchor) shape.getAnchor();
                    if (shape instanceof HSSFPicture) {
                        HSSFPicture pic = (HSSFPicture) shape;
                        PictureData data = pic.getPictureData();
                        //String extension = data.suggestFileExtension();
                        int row1 = anchor.getRow1();
                        //int row2 = anchor.getRow2();
                        int col1 = anchor.getCol1();
                        //int col2 = anchor.getCol2();
                        if (row1 == cell.getRowIndex() && col1 == cell.getColumnIndex()) {
                            //Dimension dimension = pic.getImageDimension();
                            //this.anchor = anchor;
                            byte[] bytes = data.getData();
                            if(bytes != null){
                                pdfpCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                                pdfpCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                Image image = Image.getInstance(bytes);
                                pdfpCell.setImage(image);
                            }
                        }
                    }
                }
            }
        }
    }

    public static int getVAlignByExcel(VerticalAlignment align) {
        int result = 0;
        if (align == VerticalAlignment.BOTTOM) {
            result = Element.ALIGN_BOTTOM;
        }
        if (align == VerticalAlignment.CENTER) {
            result = Element.ALIGN_MIDDLE;
        }
        if (align == VerticalAlignment.JUSTIFY) {
            result = Element.ALIGN_JUSTIFIED;
        }
        if (align == VerticalAlignment.TOP) {
            result = Element.ALIGN_TOP;
        }
        return result;
    }

    public static int getHAlignByExcel(HorizontalAlignment align) {
        int result = 0;
        if (align == HorizontalAlignment.LEFT) {
            result = Element.ALIGN_LEFT;
        }
        if (align == HorizontalAlignment.RIGHT) {
            result = Element.ALIGN_RIGHT;
        }
        if (align == HorizontalAlignment.JUSTIFY) {
            result = Element.ALIGN_JUSTIFIED;
        }
        if (align == HorizontalAlignment.CENTER) {
            result = Element.ALIGN_CENTER;
        }
        return result;
    }


    public static Font getFontByExcel(Workbook wb, CellStyle style) {
        Font result = new Font(PDFPageEvent.BASE_FONT_CHINESE , 8 , Font.NORMAL);
        int index = style.getFontIndex();
        org.apache.poi.ss.usermodel.Font font = wb.getFontAt(index);

        if(font.getBold()){
            result.setStyle(Font.BOLD);
        }

        HSSFColor color = HSSFColor.getIndexHash().get(font.getColor());

        if(color != null){
            int rbg = getRGB(color);
            result.setColor(new BaseColor(rbg));
        }

        FontUnderline underline = FontUnderline.valueOf(font.getUnderline());
        if(underline == FontUnderline.SINGLE){
            String ulString = Font.FontStyle.UNDERLINE.getValue();
            result.setStyle(ulString);
        }
        return result;
    }

    public static void addBorderByExcel(Workbook wb, PdfPCell cell , CellStyle style) {
        cell.setBorderColorLeft(new BaseColor(getBorderRBG(wb,style.getLeftBorderColor())));
        cell.setBorderColorRight(new BaseColor(getBorderRBG(wb,style.getRightBorderColor())));
        cell.setBorderColorTop(new BaseColor(getBorderRBG(wb,style.getTopBorderColor())));
        cell.setBorderColorBottom(new BaseColor(getBorderRBG(wb,style.getBottomBorderColor())));
    }
}
