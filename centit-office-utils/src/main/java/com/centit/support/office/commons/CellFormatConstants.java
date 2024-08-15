package com.centit.support.office.commons;

import java.util.Arrays;
import java.util.List;
/**
 * 常量
 */
public interface CellFormatConstants {
    /**
     * 所有日期和时间
     */
    List<Short> ALL_EXCEL_FORMAT_INDEX = Arrays.asList(new Short[] { 14, 15, 16, 17, 18, 19, 20, 21, 22, 30, 31, 32, 33,
        45, 46, 47, 176, 177, 178, 179, 180, 181, 182, 55, 183, 56, 184, 185, 57, 186, 58, 187, 188, 189, 190, 191,
        192, 193, 194, 195, 196, 197, 198, 199, 200, 201, 202, 203, 204, 205, 206, 207, 208 });
    /**
     * 星期 默认格式
     */
    String COMMON_DATE_FORMAT_XQ = "星期";
    /**
     * 周 默认格式
     */
    String COMMON_DATE_FORMAT_Z = "周";
    /**
     * 07版时间=time的Cell.getCellStyle().getDataFormat()值
     */
    List<Short> EXCEL_FORMAT_INDEX_07_TIME = Arrays.asList(new Short[] { 18, 19, 20, 21, 32, 33, 45, 46, 47, 55, 56,
        176, 177, 178, 179, 180, 181, 182, 183, 184, 185, 186 });
    /**
     * 07版日期date的Cell.getCellStyle().getDataFormat()值
     */
    List<Short> EXCEL_FORMAT_INDEX_07_DATE = Arrays.asList(new Short[] { 14, 15, 16, 17, 22, 30, 31, 57, 58, 187, 188,
        189, 190, 191, 192, 193, 194, 195, 196, 197, 198, 199, 200, 201, 202, 203, 204, 205, 206, 207, 208 });
    /**
     * 03版时间time的Cell.getCellStyle().getDataFormat()值
     */
    List<Short> EXCEL_FORMAT_INDEX_03_TIME = Arrays.asList(new Short[] { 18, 19, 20, 21, 32, 33, 45, 46, 47, 55, 56,
        176, 177, 178, 179, 180, 181, 182, 183, 184, 185, 186 });
    /**
     * 03版日期 总date的Cell.getCellStyle().getDataFormat()值
     */
    List<Short> EXCEL_FORMAT_INDEX_03_DATE = Arrays.asList(new Short[] { 14, 15, 16, 17, 22, 30, 31, 57, 58, 187, 188,
        189, 190, 191, 192, 193, 194, 195, 196, 197, 198, 199, 200, 201, 202, 203, 204, 205, 206, 207, 208 });
    /**
     * date-年月日时分秒-Cell.getCellStyle().getDataFormatString()
     */
    List<String> EXCEL_FORMAT_INDEX_DATE_NYRSFM_STRING = Arrays.asList("yyyy/m/d\\ h:mm;@", "m/d/yy h:mm",
        "yyyy/m/d\\ h:mm\\ AM/PM", "[$-409]yyyy/m/d\\ h:mm\\ AM/PM;@", "yyyy/mm/dd\\ hh:mm:dd",
        "yyyy/mm/dd\\ hh:mm", "yyyy/m/d\\ h:m", "yyyy/m/d\\ h:m:s", "yyyy/m/d\\ h:mm", "m/d/yy h:mm;@",
        "yyyy/m/d\\ h:mm\\ AM/PM;@");
    /**
     * date-年月日Cell.getCellStyle().getDataFormatString()
     */
    List<String> EXCEL_FORMAT_INDEX_DATE_NYR_STRING = Arrays.asList("m/d/yy", "[$-F800]dddd\\,\\ mmmm\\ dd\\,\\ yyyy",
        "[DBNum1][$-804]yyyy\"年\"m\"月\"d\"日\";@", "yyyy\"年\"m\"月\"d\"日\";@", "yyyy/m/d;@", "yy/m/d;@", "m/d/yy;@",
        "[$-409]d/mmm/yy", "[$-409]dd/mmm/yy;@", "reserved-0x1F", "reserved-0x1E", "mm/dd/yy;@", "yyyy/mm/dd",
        "d-mmm-yy", "[$-409]d\\-mmm\\-yy;@", "[$-409]d\\-mmm\\-yy", "[$-409]dd\\-mmm\\-yy;@",
        "[$-409]dd\\-mmm\\-yy", "[DBNum1][$-804]yyyy\"年\"m\"月\"d\"日\"", "yy/m/d", "mm/dd/yy", "dd\\-mmm\\-yy");
    /**
     * date-年月-Cell.getCellStyle().getDataFormatString()
     */
    List<String> EXCEL_FORMAT_INDEX_DATE_NY_STRING = Arrays.asList("[DBNum1][$-804]yyyy\"年\"m\"月\";@",
        "[DBNum1][$-804]yyyy\"年\"m\"月\"", "yyyy\"年\"m\"月\";@", "yyyy\"年\"m\"月\"", "[$-409]mmm\\-yy;@",
        "[$-409]mmm\\-yy", "[$-409]mmm/yy;@", "[$-409]mmm/yy", "[$-409]mmmm/yy;@", "[$-409]mmmm/yy",
        "[$-409]mmmmm/yy;@", "[$-409]mmmmm/yy", "mmm-yy", "yyyy/mm", "mmm/yyyy", "[$-409]mmmm\\-yy;@",
        "[$-409]mmmmm\\-yy;@", "mmmm\\-yy", "mmmmm\\-yy");
    /**
     * date-月日Cell.getCellStyle().getDataFormatString()
     */
    List<String> EXCEL_FORMAT_INDEX_DATE_YR_STRING = Arrays.asList("[DBNum1][$-804]m\"月\"d\"日\";@",
        "[DBNum1][$-804]m\"月\"d\"日\"", "m\"月\"d\"日\";@", "m\"月\"d\"日\"", "[$-409]d/mmm;@", "[$-409]d/mmm", "m/d;@",
        "m/d", "d-mmm", "d-mmm;@", "mm/dd", "mm/dd;@", "[$-409]d\\-mmm;@", "[$-409]d\\-mmm");
    /**
     * date-星期X-Cell.getCellStyle().getDataFormatString()
     */
    List<String> EXCEL_FORMAT_INDEX_DATE_XQ_STRING = Arrays.asList("[$-804]aaaa;@", "[$-804]aaaa");
    /**
     * date-周X-Cell.getCellStyle().getDataFormatString()
     */
    List<String> EXCEL_FORMAT_INDEX_DATE_Z_STRING = Arrays.asList("[$-804]aaa;@", "[$-804]aaa");
    /**
     * date-月X-Cell.getCellStyle().getDataFormatString()
     */
    List<String> EXCEL_FORMAT_INDEX_DATE_Y_STRING = Arrays.asList("[$-409]mmmmm;@", "mmmmm", "[$-409]mmmmm");
    /**
     * time - 时间-Cell.getCellStyle().getDataFormatString()
     */
    List<String> EXCEL_FORMAT_INDEX_TIME_STRING = Arrays.asList("mm:ss.0", "h:mm", "h:mm\\ AM/PM", "h:mm:ss",
        "h:mm:ss\\ AM/PM", "reserved-0x20", "reserved-0x21", "[DBNum1]h\"时\"mm\"分\"", "[DBNum1]上午/下午h\"时\"mm\"分\"",
        "mm:ss", "[h]:mm:ss", "h:mm:ss;@", "[$-409]h:mm:ss\\ AM/PM;@", "h:mm;@", "[$-409]h:mm\\ AM/PM;@",
        "h\"时\"mm\"分\";@", "h\"时\"mm\"分\"\\ AM/PM;@", "h\"时\"mm\"分\"ss\"秒\";@", "h\"时\"mm\"分\"ss\"秒\"_ AM/PM;@",
        "上午/下午h\"时\"mm\"分\";@", "上午/下午h\"时\"mm\"分\"ss\"秒\";@", "[DBNum1][$-804]h\"时\"mm\"分\";@",
        "[DBNum1][$-804]上午/下午h\"时\"mm\"分\";@", "h:mm AM/PM", "h:mm:ss AM/PM", "[$-F400]h:mm:ss\\ AM/PM");
    /**
     * date-当formatString为空的时候-年月
     */
    Short EXCEL_FORMAT_INDEX_DATA_EXACT_NY = 57;
    /**
     * date-当formatString为空的时候-月日
     */
    Short EXCEL_FORMAT_INDEX_DATA_EXACT_YR = 58;
    /**
     * time-当formatString为空的时候-时间
     */
    List<Short> EXCEL_FORMAT_INDEX_TIME_EXACT = Arrays.asList(new Short[] { 55, 56 });
    /**
     * 格式化星期或者周显示
     */
    String[] WEEK_DAYS = { "日", "一", "二", "三", "四", "五", "六" };

}

