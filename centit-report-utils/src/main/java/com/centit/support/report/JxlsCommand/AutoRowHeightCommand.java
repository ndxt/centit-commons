package com.centit.support.report.JxlsCommand;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.jxls.area.Area;
import org.jxls.command.AbstractCommand;
import org.jxls.common.CellRef;
import org.jxls.common.Context;
import org.jxls.common.Size;
import org.jxls.transform.poi.PoiTransformer;

/**
 * 自定义列高指令
 * 如：
 * jx:autoRowHeight(lastCell ="C3")
 *
 * 还需要在对应的主程序中调用
 */
public class AutoRowHeightCommand extends AbstractCommand {

    /**
     * 批注中的自定义指令
     * @return
     */
    @Override
    public String getName() {
        return "autoRowHeight";
    }

    @Override
    public Size applyAt(CellRef cellRef, Context context) {
        Area area=getAreaList().get(0);
        Size size = area.applyAt(cellRef, context);
        PoiTransformer transformer = (PoiTransformer) area.getTransformer();
        Sheet sheet = transformer.getWorkbook().getSheet(cellRef.getSheetName());
        Row row = sheet.getRow(cellRef.getRow());
        row.setHeight((short) -1);

        return size;
    }
}
