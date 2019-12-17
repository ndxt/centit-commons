package com.centit.test;

import com.centit.support.algorithm.DatetimeOpt;
import com.centit.support.algorithm.StringBaseOpt;
import com.centit.support.algorithm.StringRegularOpt;
import com.centit.support.network.UrlOptUtils;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class TestDateTimeOpt {
    public static void main(String[] args) throws Exception {

        System.out.println(DatetimeOpt.truncateToWeek(DatetimeOpt.currentUtilDate()));

        System.out.println(DatetimeOpt.seekEndOfWeek(DatetimeOpt.currentUtilDate()));
        System.out.println(DatetimeOpt.seekEndOfWeek(DatetimeOpt.createUtilDate(2019, 4, 7)));
        System.out.println(DatetimeOpt.truncateToWeek(DatetimeOpt.createUtilDate(2019, 4, 8)));
        System.out.println(DatetimeOpt.seekEndOfWeek(DatetimeOpt.createUtilDate(2019, 4, 12)));
        System.out.println(DatetimeOpt.truncateToWeek(DatetimeOpt.createUtilDate(2019, 4, 13)));
        System.out.println(DatetimeOpt.seekEndOfWeek(DatetimeOpt.createUtilDate(2019, 4, 14)));
        System.out.println(DatetimeOpt.seekEndOfMonth(DatetimeOpt.currentUtilDate()));
        System.out.println(DatetimeOpt.seekEndOfYear(DatetimeOpt.currentUtilDate()));

        System.out.println(DatetimeOpt.calcSpanDays(
            DatetimeOpt.smartPraseDate("2018-6-1"), DatetimeOpt.smartPraseDate("2018-6-2")));
        System.out.println(DatetimeOpt.calcSpanDays(
            DatetimeOpt.createUtilDate(2018, 6, 1), DatetimeOpt.createUtilDate(2018, 6, 2)));


        //return addDays(truncateToMonth( addMonths(date,1) ),-1);
        System.out.println(DatetimeOpt.convertDateToString(
            DatetimeOpt.seekEndOfYear(DatetimeOpt.createUtilDate(2018, 7, 1))));

    }

    //T代表后面跟着时间，Z代表UTC统一时间


    public static void testTPTime() throws Exception {
        System.out.println(System.currentTimeMillis());
        System.out.println(Instant.now().toString());
        System.out.println(DatetimeOpt.convertDateToString(
            DatetimeOpt.currentUtilDate(), "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
    }

    public static void testUrlOpt() {

        String sUrl = "http://codefan:781023@dl13.yunpan.360.cn/intf.php?method=Preview.outputPic&qid=260381977&fname=%2F马甸中学20周年聚会%2FIMG_2802.JPG&fhash=342e16140c54055cf578363b27016a7b9b0afcec&dt=13.6b894db11bd878296829f48cca0f4739&v=1.0.1&rtick=13953917127584&devtype=web&sign=020a79ce62b5ad6fd644ee56b532198e&#pagemake";
        String sParam = UrlOptUtils.getUrlParamter(sUrl);
        System.out.println(sParam);
        Map<String, String> params = UrlOptUtils.splitUrlParamter(sParam);
        for (Map.Entry<String, String> ent : params.entrySet()) {
            System.out.println(ent.getKey() + ":" + ent.getValue());
        }
    }

    public static void testLunar() {
        //System.out.println( (new Lunar(DatetimeOpt.createUtilDate(2009, 8, 8)) ).toString());
        Map<String, String> params = new HashMap<String, String>();
        params.put("zitou", "交通局");
        //params.put("year", "2012");
        System.out.println(StringBaseOpt.clacDocumentNo("$zitou$发[$year$]$N5$", 1, params));
    }

    public static void testDateTime() {
        String s = "请94年5月6日 上午8点6分秒来南京你哈飞";
        System.out.println(StringRegularOpt.trimDateString
            (s));

        System.out.println(
            DatetimeOpt.smartPraseDate(s));

        s = "2005-5";
        System.out.println(StringRegularOpt.trimDateString
            (s));

        System.out.println(
            DatetimeOpt.smartPraseDate(s));
        s = "05-6-7";
        System.out.println(StringRegularOpt.trimDateString
            (s));

        System.out.println(
            DatetimeOpt.smartPraseDate(s));
        s = "05-6-7-8";
        System.out.println(StringRegularOpt.trimDateString
            (s));

        System.out.println(
            DatetimeOpt.smartPraseDate(s));
        s = "5-6-7-8-9";
        System.out.println(StringRegularOpt.trimDateString
            (s));

        System.out.println(
            DatetimeOpt.smartPraseDate(s));

        s = "5-6-7-8-10-11";
        System.out.println(StringRegularOpt.trimDateString
            (s));

        System.out.println(
            DatetimeOpt.smartPraseDate(s));


        s = "5-6-7-8-9-10-11";
        System.out.println(StringRegularOpt.trimDateString
            (s));

        System.out.println(
            DatetimeOpt.smartPraseDate(s));

        s = "5-6-7-8-9-10-11-12";
        System.out.println(StringRegularOpt.trimDateString
            (s));

        System.out.println(
            DatetimeOpt.smartPraseDate(s));

    /*    Date currime = DatetimeOpt.currentUtilDate();
        Date otherDate = DatetimeOpt.addDays(currime, 8);
        System.out.println(currime);
        System.out.println(otherDate);
        System.out.println(
                DatetimeOpt.calcWeekDays(currime, otherDate, 6)
                );
        System.out.println(
                DatetimeOpt.calcWeekDays(currime, otherDate, 5)
                );
        System.out.println(
                DatetimeOpt.calcWeekDays(currime, otherDate, 4)
                );
        System.out.println(
                DatetimeOpt.calcWeekDays(currime, otherDate, 3)
                );

        otherDate = DatetimeOpt.addDays(currime, 4);
        System.out.println(currime);
        System.out.println( DatetimeOpt.calcSpanDays(currime, otherDate));
        System.out.println(
                DatetimeOpt.calcWeekDays(currime, otherDate, 0)
                );
        System.out.println(
                DatetimeOpt.calcWeekDays(currime, otherDate, 1)
                );
        System.out.println(
                DatetimeOpt.calcWeekDays(currime, otherDate, 2)
                );
        System.out.println(
                DatetimeOpt.calcWeekDays(currime, otherDate, 3)
                );
        System.out.println(
                DatetimeOpt.calcWeekDays(currime, otherDate, 4)
                );
        System.out.println(
                DatetimeOpt.calcWeekDays(currime, otherDate, 5)
                );
        System.out.println(
                DatetimeOpt.calcWeekDays(currime, otherDate, 6)
                );    */
    }
}
