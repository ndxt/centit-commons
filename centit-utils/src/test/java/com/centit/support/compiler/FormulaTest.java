package com.centit.support.compiler;

import com.centit.support.algorithm.CollectionsOpt;
import com.centit.support.algorithm.DatetimeOpt;
import com.centit.support.algorithm.NumberBaseOpt;
import com.centit.support.algorithm.StringBaseOpt;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FormulaTest {
    public static final String [] timeZones = {"Asia/Shanghai","Asia/Hong_Kong","Asia/Taipei",
	"Asia/Seoul","Asia/Tokyo","America/New_York","America/Denver",
	"America/Costa_Rica","America/Chicago","America/Mexico_City",
    "America/Regina","America/Los_Angeles","Pacific/Majuro","Pacific/Midway",
    "Pacific/Honolulu","America/Anchorage","America/Tijuana","America/Phoenix",
    "America/Chihuahua","America/Bogota","America/Caracas","America/Barbados",
    "America/Manaus","America/St_Johns","America/Santiago","America/Argentina/Buenos_Aires",
    "America/Godthab","America/Montevideo","America/Sao_Paulo","Atlantic/South_Georgia",
    "Atlantic/Azores","Atlantic/Cape_Verde","Africa/Casablanca",
    "Europe/London","Europe/Amsterdam","Europe/Belgrade","Europe/Brussels",
    "Europe/Sarajevo","Africa/Brazzaville","Africa/Windhoek","Asia/Amman",
    "Europe/Athens","Asia/Beirut","Africa/Cairo","Europe/Helsinki","Asia/Jerusalem",
    "Africa/Harare","Europe/Minsk","Asia/Baghdad","Europe/Moscow","Asia/Kuwait",
    "Africa/Nairobi","Asia/Tehran","Asia/Baku","Asia/Tbilisi","Asia/Yerevan",
    "Asia/Dubai","Asia/Kabul","Asia/Karachi","Asia/Oral","Asia/Yekaterinburg",
    "Asia/Calcutta","Asia/Colombo","Asia/Katmandu","Asia/Almaty","Asia/Rangoon",
    "Asia/Krasnoyarsk","Asia/Bangkok","Asia/Irkutsk","Asia/Kuala_Lumpur",
    "Australia/Perth","Asia/Yakutsk","Australia/Darwin","Australia/Brisbane",
    "Asia/Vladivostok","Pacific/Guam","Australia/Adelaide","Australia/Hobart",
    "Australia/Sydney","Asia/Magadan","Pacific/Auckland","Pacific/Fiji","Pacific/Tongatapu"};
    public static void main(String[] args) throws ParseException {

        /*TimeZone pdt = DatetimeOpt.fetchTimeZone("PDT");
        System.out.println( pdt.getRawOffset() + " : " + pdt.getDisplayName());

        for(String s : timeZones){
            TimeZone tz = TimeZone.getTimeZone(s);
            System.out.println(s + ":" + tz.getRawOffset() / 3600000 + " : " + tz.getDisplayName());
        }

        Object strDate = VariableFormula.calculate(
            "formatdate('zone:en:PST MMM d, yyyy h:m:s aa (zzz)',currentDatetime())");
        System.out.println("zone:en:PST -> " + strDate);

        strDate = VariableFormula.calculate(
            "formatdate('zone:en:CST MMM d, yyyy h:m:s aa (zzz)',currentDatetime())");
        System.out.println("zone:en:CST -> " + strDate);

        strDate = VariableFormula.calculate(
            "formatdate('zone:en:PDT MMM d, yyyy h:m:s aa (zzz)',currentDatetime())");
        System.out.println("zone:en:PDT -> " + strDate);

        strDate = VariableFormula.calculate(
            "formatdate('zone:en:-08 MMM d, yyyy h:m:s aa (zzz)',currentDatetime())");
        System.out.println("zone:en:-08 -> " + strDate);
        strDate = VariableFormula.calculate(
            "formatdate('lang:en MMM d, yyyy h:m:s aa (zzz)',currentDatetime())");
        System.out.println("lang:en -> " + strDate);
        */
        Object date =  VariableFormula.calculate(
            "toDate('Oct 1, 2022 10:13:52 PM (PDT)','zone:en:+08 MMM d, yyyy h:m:s aa (zzz)')");
        System.out.println("toDate en -> " + date.toString());
        System.out.println("toDate en -> " + DatetimeOpt.convertDateToString((Date) date,
            "zone:en:+08 MMM d, yyyy h:m:s aa (zzz)"));
        //testFormula2();
    }
    public static void testFormula5(){
        System.out.println(VariableFormula.calculate("strcat(capital(floor(a)),'元'," +
                "if(byte(a,-1)=0 and byte(a,-2)=0, '整', if(byte(a,-2)=0, strcat(capital(byte(a,-1)),'角')," +
                " strcat( capital(byte(a,-1)),'角',capital(byte(a,-2)),'分') ) ) )",
            CollectionsOpt.createHashMap("a",100032.01f)));

        System.out.println(VariableFormula.calculate(
            "match('a*d','aadbd')"));
        System.out.println(VariableFormula.calculate(
            "regexmatchvalue('a\\S','abcdacda2ef')"));
        System.out.println(VariableFormula.calculate("nvl(你2好,'我不好')",
            CollectionsOpt.createHashMap("你好","我的世界","地球",
                CollectionsOpt.createList(
                    CollectionsOpt.createHashMap("陆地","七大洲","海洋","四大洋2"),
                    CollectionsOpt.createHashMap("陆地","七大洲","海洋","四大洋3") ))));

        //System.out.println(VariableFormula.calculate(" regexMatch('^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$','codefan@sina.com')",
        //    new ObjectTranslate(CollectionsOpt.createHashMap() ), null));
        /*System.out.println(VariableFormula.calculate("if(false, 'error')",
            new ObjectTranslate(CollectionsOpt.createHashMap() ), null));
        System.out.println(VariableFormula.calculate("if('true', 'error','ok')",
            new ObjectTranslate(CollectionsOpt.createHashMap() ), null));
        System.out.println(VariableFormula.calcMultiFormula("(1+2, 3+'sdf')",
            new ObjectTranslate(CollectionsOpt.createHashMap() ), null));*/

    }

    public static void testRandomHash() {
        System.out.println(VariableFormula.calculate("random()"));
        System.out.println(VariableFormula.calculate("random(100)"));
        System.out.println(VariableFormula.calculate("random(5, 100)"));
        System.out.println(VariableFormula.calculate("random('string', 38)"));
        System.out.println(VariableFormula.calculate("random('string', 'uuid')"));
        System.out.println(VariableFormula.calculate("hash('hello world')"));
        System.out.println(VariableFormula.calculate("hash('hello world','sha')"));
        System.out.println(VariableFormula.calculate("hash('hello world','macsha','nihao')"));

        System.out.println(VariableFormula.calculate("hash('hello world','sha','base64')"));
        System.out.println(VariableFormula.calculate("hash('hello world','macsha','nihao', 'base64')"));
        System.out.println("Done!");
    }

    public static void testFormula4() {
        System.out.println(VariableFormula.calculate("'25.36'*5"));
        System.out.println(VariableFormula.calculate(".36*5"));
        System.out.println(VariableFormula.calculate("tonumber('',12)"));
        System.out.println(VariableFormula.calculate("tonumber(,34)"));
        System.out.println(VariableFormula.calculate("tonumber('123')"));
        System.out.println(VariableFormula.calculate("round('1232325.3236',2)"));
        System.out.println(VariableFormula.calculate("round('2355.3236',-2)"));
        System.out.println(VariableFormula.calculate("floor('1232325.3236',2)"));
        System.out.println(VariableFormula.calculate("floor('2355.3236',-2)"));
        System.out.println(VariableFormula.calculate("ceil('1232325.3236',2)"));
        System.out.println(VariableFormula.calculate("ceil('2355.3236',-2)"));
        System.out.println(VariableFormula.checkFormula("today"));
        System.out.println(VariableFormula.checkFormula("a+(b)"));
        System.out.println(VariableFormula.checkFormula("a+(b,c)"));
        System.out.println(VariableFormula.checkFormula("a+(b,+)"));
        System.out.println(VariableFormula.checkFormula("a,b"));
        System.out.println(VariableFormula.checkFormula("(a,b,c,(a,b),c)"));
        System.out.println(VariableFormula.checkFormula("a+b()"));
        System.out.println("Done!");
    }

    public static void testFormula() {
        System.out.println(VariableFormula.calculate("byte (4321.789,0)"));
        System.out.println(VariableFormula.calculate("byte (4321.789,2)"));
        System.out.println(VariableFormula.calculate("byte (4321.789,-2)"));
        System.out.println(VariableFormula.calculate("capital (123.45)"));
        System.out.println(VariableFormula.calculate("capital (123.45, true)"));

        VariableFormula formula = new VariableFormula();
        formula.addExtendFunc("ex", (a) -> NumberBaseOpt.castObjectToInteger(a[0]) * NumberBaseOpt.castObjectToInteger(a[0]));
        formula.addExtendFunc("pi", (a) -> 3.14159);
        Object r = formula.calcFormula("ex(4) + pi()");
        System.out.println(r);

        Object s = VariableFormula.calculate("-1 + 8 + ${a}");
        System.out.println(s);
        s = VariableFormula.calculate(
            "concat('Y' , year(), 'M', month() ,no)", CollectionsOpt.createHashMap("no", 100));
        System.out.println(s);

        s = VariableFormula.calculate(
            "SINGLETON( [1,2,2,4,5,,3] ,5, 6, 7 , -1, 8)");
        System.out.println(s);
        s = VariableFormula.calculate("[1,2,2,4,5,,3] - 1");
        System.out.println(s);
        s = VariableFormula.calculate("toDate('2012-12-12') + 1");
        System.out.println(s);
        s = VariableFormula.calculate("round(today() - toDate('2018-12-12'))");
        System.out.println(s);
        s = VariableFormula.calculate("2 in ( 3.03,[1,2,'123'])");
        System.out.println(s);
        s = VariableFormula.calculate("25+toString(60)");
        System.out.println(s);
        s = VariableFormula.calculate("getpy('杨淮生')");
        System.out.println(s);
    }

    public static void testFormula2() {

        Map<String, Object> varMap = new HashMap<>();
        Map<String, Object> varA = new HashMap<>();
        varA.put("aa", new Integer[]{100, 200, 300});
        varA.put("ab", 200);
        Map<String, Object> varB = new HashMap<>();
        varB.put("ba", 300);
        varB.put("bb", 400);
        varMap.put("a", varA);
        varMap.put("depart", "你好");
        varMap.put("c", null);
        varMap.put("ideaCode", "T");
        //System.out.println(f.checkFormula("a.aa[1]+a.aa[2]"));
        //String s = f.calculate("${a.aa[0]}+ ${a.aa[2]}",varMap);gei
        //System.out.println(s);
        String formula = "value('a.aa[0]') + value('a.aa[2]')" ;//"getpy(depart ) + rpad(lpad(ideaCode,9,'abcd'),20,'def') + a.aa ";
        System.out.println(formula);
        Object s = VariableFormula.calculate(formula, varMap);
        System.out.println(StringBaseOpt.castObjectToString(s));
        System.out.println("Done!");
    }


    public static void testFormula3() {

        Map<String, Object> varMap = new HashMap<>();

        varMap.put("a", 10);
        varMap.put("b", 4);
        String formula = "(a*a-b)/b";
        Object s = VariableFormula.calculate(formula, varMap);
        System.out.println(StringBaseOpt.castObjectToString(s));
        System.out.println("Done!");
    }

    public static void testLexer() {
        Lexer l = new Lexer("hello jane , jan say!");
        System.out.println(l.findWord("jan", true, true));
        //assertEquals("nihao,地球 !",s);

        VariableFormula f = new VariableFormula();
        Map<String, Object> varMap = new HashMap<>();
        varMap.put("usercode", "U00001");

        Map<String, Object> varB = new HashMap<>();
        varB.put("usercode", 300);
        varMap.put("unit打法  code", 500);
        varMap.put("user", varB);
        Object s = f.calculate(
            "today +':'+  today() + ' usercode' + usercode + ${unit打法  code} + ':' +user.usercode }",
            varMap);
        System.out.println(s);
    }

    public static void testDate() {
        System.out.println(VariableFormula.calculate("today + ' ' + hello2"));
        //System.out.println( VariableFormula.calculate("currentDate()"));
        //System.out.println( VariableFormula.calculate("currentDatetime()"));
        //System.out.println( VariableFormula.calculate("currentTimestamp()"));
    }

    public static void testStringTemplate() {
        //testFormula();
        //testDate();//

        // testFormula2();
        //testLexer();
        Map<String, Object> map = new HashMap<>(5);
        Map<String, Object> usreInfo = new HashMap<>(5);
        usreInfo.put("userCode", "admin");
        usreInfo.put("userName", "管理员");
        map.put("userInfo", usreInfo);
        String str = Pretreatment.mapTemplateString(
            "转义符\\\\又一个转义符\\{ ${无法找到的变量} " +
                "\"引号中的\\和${都不会被处理}\" 你的姓名是${userInfo.userName}",
            map,"[没有赋值]", false);
        System.out.println(str);
        str = Pretreatment.mapTemplateString("", map, "{没有赋值}");
        System.out.println(str);
        str = Pretreatment.mapTemplateString(null, map, "{没有赋值}");
        System.out.println(str);
        str = Pretreatment.mapTemplateString("{}！{}2{][[}}[][]3{  }4{3}", map);
        System.out.println(str);
        System.out.println("123123.4545" + (String) null);

    }
}
