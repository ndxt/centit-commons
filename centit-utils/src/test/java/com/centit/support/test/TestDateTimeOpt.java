package com.centit.support.test;

import com.centit.support.algorithm.DatetimeOpt;
import com.centit.support.algorithm.StringBaseOpt;
import com.centit.support.algorithm.StringRegularOpt;
import com.centit.support.network.UrlOptUtils;

import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class TestDateTimeOpt {
    public static void main(String[] args) {
        TimeZone timeZone = DatetimeOpt.fetchTimeZone("UTC");
        System.out.println(timeZone.getRawOffset() +"   |   "+ timeZone.getDisplayName()
            +"   |   " + timeZone.getDisplayName(Locale.US));
        timeZone = DatetimeOpt.fetchTimeZone("UTC+08");
        System.out.println(timeZone.getRawOffset() +"   |   "+ timeZone.getDisplayName()
            +"   |   " + timeZone.getDisplayName(Locale.US));
        timeZone = DatetimeOpt.fetchTimeZone("UTC+0800");
        System.out.println(timeZone.getRawOffset() +"   |   "+ timeZone.getDisplayName()
            +"   |   " + timeZone.getDisplayName(Locale.US));
        timeZone = DatetimeOpt.fetchTimeZone("UTC+8:00");
        System.out.println(timeZone.getRawOffset() +"   |   "+ timeZone.getDisplayName()
            +"   |   " + timeZone.getDisplayName(Locale.US));
        timeZone = DatetimeOpt.fetchTimeZone("UTC+800");
        System.out.println(timeZone.getRawOffset() +"   |   "+ timeZone.getDisplayName()
            +"   |   " + timeZone.getDisplayName(Locale.US));
        timeZone = DatetimeOpt.fetchTimeZone("8");
        System.out.println(timeZone.getRawOffset() +"   |   "+ timeZone.getDisplayName()
            +"   |   " + timeZone.getDisplayName(Locale.US));
        timeZone = DatetimeOpt.fetchTimeZone("+8");
        System.out.println(timeZone.getRawOffset() +"   |   "+ timeZone.getDisplayName()
            +"   |   " + timeZone.getDisplayName(Locale.US));
        timeZone = DatetimeOpt.fetchTimeZone("UTC08");
        System.out.println(timeZone.getRawOffset() +"   |   "+ timeZone.getDisplayName()
            +"   |   " + timeZone.getDisplayName(Locale.US));

        timeZone = DatetimeOpt.fetchTimeZone("UTC800");
        System.out.println(timeZone.getRawOffset() +"   |   "+ timeZone.getDisplayName()
            +"   |   " + timeZone.getDisplayName(Locale.US));

        /*TimeZone timeZone = DatetimeOpt.fetchTimeZone("+12");
        System.out.println(timeZone.getRawOffset() +"   |   "+ timeZone.getDisplayName()
            +"   |   " + timeZone.getDisplayName(Locale.US));
        */
        /*Date currentDate = DatetimeOpt.currentUtilDate();

        System.out.println(currentDate);
        System.out.println(DatetimeOpt.convertDateToGMTString(currentDate));
        System.out.println(currentDate);
        System.out.println(DatetimeOpt.convertDateToString(currentDate, "yyyy-MM-dd HH:mm:ss.SSS (zzz)"));
        //testDateTime();
        //System.out.println(JSON.toJSONString(ZoneInfoFile.getAliasMap()));
        currentDate = DatetimeOpt.convertStringToDate(
            "2022-12-12 12:12:12", "yyyy-MM-dd HH:mm:ss");
        System.out.println(currentDate);
        System.out.println(DatetimeOpt.smartPraseDate("2022-12-12 12:12:12"));
        System.out.println(DatetimeOpt.convertDateToString(
            currentDate, "yyyy-MM-dd HH:mm:ss.SSS zzz"));
        currentDate = DatetimeOpt.convertStringToDate(
            "2022-12-12 12:12:12 PST", "yyyy-MM-dd HH:mm:ss zzz");
        System.out.println(currentDate);
        System.out.println(DatetimeOpt.smartPraseDate("2022-12-12 12:12:12 PST"));

        currentDate = DatetimeOpt.convertStringToDate(
            "2022-12-12 12:12:12 GMT", "yyyy-MM-dd HH:mm:ss zzz");
        System.out.println(currentDate);
        System.out.println(DatetimeOpt.smartPraseDate("2022-12-12 12:12:12 GMT"));

        currentDate = DatetimeOpt.convertStringToDate(
            "2022-12-12 12:12:12 CST", "yyyy-MM-dd HH:mm:ss zzz");
        System.out.println(currentDate);*/
        /*
        System.out.println(DatetimeOpt.smartPraseDate("2022-12-12 12:12:12 GMT+8"));
        System.out.println(DatetimeOpt.smartPraseDate("2022-12-12 12:12:12 GMT+9"));*/
       /* for(Map.Entry<String, String> ent : ZoneInfoFile.getAliasMap().entrySet()) {
            TimeZone timeZone = TimeZone.getTimeZone(ent.getValue());
            if (timeZone.getRawOffset() == 12 * 3600000) {
                System.out.println("+12:"+ent.getValue());
            }
            if (timeZone.getRawOffset() == 10 * 3600000) {
                System.out.println("+10:"+ent.getValue());
            }
        }


        System.out.println(DatetimeOpt.convertDateToString(currentDate, "yyyy-MM-dd HH:mm:ss.SSS"));
        System.out.println(DatetimeOpt.convertDateToString(currentDate, "yyyy-MM-dd HH:mm:ss.SSS (zzz)"));
        System.out.println(DatetimeOpt.convertDateToString(currentDate, "zone:en:GMT yyyy-MM-dd HH:mm:ss.SSS"));

        System.out.println(DatetimeOpt.convertDateToString(currentDate, "zone:en:PST yyyy-MM-dd HH:mm:ss.SSS"));
        System.out.println(DatetimeOpt.convertDateToString(currentDate, "zone:en:-11 yyyy-MM-dd HH:mm:ss.SSS"));
        System.out.println(DatetimeOpt.convertDateToString(currentDate, "zone:en:-10 yyyy-MM-dd HH:mm:ss.SSS"));
        System.out.println(DatetimeOpt.convertDateToString(currentDate, "zone:en:-09 yyyy-MM-dd HH:mm:ss.SSS"));
        System.out.println(DatetimeOpt.convertDateToString(currentDate, "zone:en:-08 yyyy-MM-dd HH:mm:ss.SSS"));
        System.out.println(DatetimeOpt.convertDateToString(currentDate, "zone:en:-07 yyyy-MM-dd HH:mm:ss.SSS"));
        System.out.println(DatetimeOpt.convertDateToString(currentDate, "zone:en:-06 yyyy-MM-dd HH:mm:ss.SSS"));
        System.out.println(DatetimeOpt.convertDateToString(currentDate, "zone:en:-05 yyyy-MM-dd HH:mm:ss.SSS"));
        System.out.println(DatetimeOpt.convertDateToString(currentDate, "zone:en:-04 yyyy-MM-dd HH:mm:ss.SSS"));
        System.out.println(DatetimeOpt.convertDateToString(currentDate, "zone:en:-03 yyyy-MM-dd HH:mm:ss.SSS"));
        System.out.println(DatetimeOpt.convertDateToString(currentDate, "zone:en:-02 yyyy-MM-dd HH:mm:ss.SSS"));
        System.out.println(DatetimeOpt.convertDateToString(currentDate, "zone:en:-01 yyyy-MM-dd HH:mm:ss.SSS"));
        System.out.println(DatetimeOpt.convertDateToString(currentDate, "zone:en:+00 yyyy-MM-dd HH:mm:ss.SSS"));
        System.out.println(DatetimeOpt.convertDateToString(currentDate, "zone:en:+01 yyyy-MM-dd HH:mm:ss.SSS"));
        System.out.println(DatetimeOpt.convertDateToString(currentDate, "zone:en:+02 yyyy-MM-dd HH:mm:ss.SSS"));
        System.out.println(DatetimeOpt.convertDateToString(currentDate, "zone:en:+03 yyyy-MM-dd HH:mm:ss.SSS"));
        System.out.println(DatetimeOpt.convertDateToString(currentDate, "zone:en:+04 yyyy-MM-dd HH:mm:ss.SSS"));
        System.out.println(DatetimeOpt.convertDateToString(currentDate, "zone:en:+05 yyyy-MM-dd HH:mm:ss.SSS"));
        System.out.println(DatetimeOpt.convertDateToString(currentDate, "zone:en:+06 yyyy-MM-dd HH:mm:ss.SSS"));
        System.out.println(DatetimeOpt.convertDateToString(currentDate, "zone:en:+07 yyyy-MM-dd HH:mm:ss.SSS"));
        System.out.println(DatetimeOpt.convertDateToString(currentDate, "zone:en:+08 yyyy-MM-dd HH:mm:ss.SSS"));
        System.out.println(DatetimeOpt.convertDateToString(currentDate, "zone:en:+09 yyyy-MM-dd HH:mm:ss.SSS"));
        System.out.println(DatetimeOpt.convertDateToString(currentDate, "zone:en:+10 yyyy-MM-dd HH:mm:ss.SSS"));
        System.out.println(DatetimeOpt.convertDateToString(currentDate, "zone:en:+11 yyyy-MM-dd HH:mm:ss.SSS"));
        System.out.println(DatetimeOpt.convertDateToString(currentDate, "zone:en:+11 yyyy-MM-dd HH:mm:ss.SSS"));
       */ /*System.out.println(DatetimeOpt.convertDateToString(DatetimeOpt.castObjectToDate("10/16/2022")));
        System.out.println(DatetimeOpt.createUtilDate(1980,2,1).getTime());
        System.out.println(DatetimeOpt.createUtilDate(2200,1,1).getTime());
        System.out.println(System.currentTimeMillis());
        System.out.println(StringRegularOpt.trimDateString("2020-06-09T09:34:05.790Z"));
        System.out.println(DatetimeOpt.smartPraseDate("201906090934051234567"));
        System.out.println(StringRegularOpt.trimDateString("20190609"));
        System.out.println(DatetimeOpt.smartPraseDate("20190609"));
        System.out.println(DatetimeOpt.smartPraseDate("1591772196532"));
        System.out.println(DatetimeOpt.smartPraseDate("20190609093405"));
        System.out.println(DatetimeOpt.smartPraseDate("2020-06-09T09:34:05.790Z"));*/
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


        s = "2005-6-7 8:9:10.011";
        System.out.println(StringRegularOpt.trimDateString
            (s));

        System.out.println(
            DatetimeOpt.smartPraseDate(s));

        s = "5-6-7-8-9-10-11-12.067";
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
