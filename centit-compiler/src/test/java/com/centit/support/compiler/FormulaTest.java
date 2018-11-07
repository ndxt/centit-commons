package com.centit.support.compiler;

import com.centit.support.algorithm.CollectionsOpt;
import com.centit.support.algorithm.StringBaseOpt;

import java.util.HashMap;
import java.util.Map;

public class FormulaTest {

    public static void testFormula() {

        Object s =  VariableFormula.calculate("toNumber(5.12 dbmod 3.03)");
        System.out.println(s);
        s =  VariableFormula.calculate("5.12 mod 3.03");
        System.out.println(s);
        s =  VariableFormula.calculate("5.12 dbmod 3.03");
        System.out.println(s);
        s =  VariableFormula.calculate("toString(25+60)");
        System.out.println(s);
        s =  VariableFormula.calculate("toDate('2012-12-12')");
        System.out.println(s);
    }

    public static void testFormula2() {

        Map<String,Object> varMap = new HashMap<>();
        Map<String,Object> varA = new HashMap<>();
        varA.put("aa", new Integer[]{100,200,300});
        varA.put("ab", 200);
        Map<String,Object> varB = new HashMap<>();
        varB.put("ba", 300);
        varB.put("bb", 400);
        varMap.put("a", varA);
        varMap.put("depart", "你好");
        varMap.put("c", null);
        varMap.put("ideaCode", "T");
        //System.out.println(f.checkFormula("a.aa[1]+a.aa[2]"));
        //String s = f.calculate("${a.aa[0]}+ ${a.aa[2]}",varMap);
        //System.out.println(s);
        String formula = "getpy(depart ) + rpad(lpad(ideaCode,9,'abcd'),20,'def') + a.aa ";
        System.out.println(formula);
        Object s = VariableFormula.calculate(formula, varMap);
        System.out.println(StringBaseOpt.castObjectToString(s));
        System.out.println("Done!");
    }


    public static void testFormula3() {

        Map<String,Object> varMap = new HashMap<>();

        varMap.put("a", 10);
        varMap.put("b", 4);
        String formula = "(a*a-b)/b";
        Object s = VariableFormula.calculate(formula, varMap);
        System.out.println(StringBaseOpt.castObjectToString(s));
        System.out.println("Done!");
    }



    public static void testLexer() {
        Lexer l = new Lexer("hello jane , jan say!");
        System.out.println(l.findWord("jan",true,true));
        //assertEquals("nihao,地球 !",s);

        VariableFormula f = new VariableFormula();
        Map<String,Object> varMap = new HashMap<>();
        varMap.put("usercode", "U00001");

        Map<String,Object> varB = new HashMap<>();
        varB.put("usercode", 300);
        varB.put("unitcode", 500);
        varMap.put("user", varB);
        Object s = f.calculate(
                "today +':'+  today() + ' usercode' + usercode + $ { otherValue } + ':' +user.usercode }",
                varMap);
        System.out.println(s);
    }

    public static void testDate() {
        System.out.println(VariableFormula.calculate("today + ' ' + hello2"));
        //System.out.println( VariableFormula.calculate("currentDate()"));
        //System.out.println( VariableFormula.calculate("currentDatetime()"));
        //System.out.println( VariableFormula.calculate("currentTimestamp()"));
    }
    public  static void  main(String[] args)   {
        //testFormula();
        //testDate();//

        // testFormula2();
        //testLexer();
        Map<String,Object> map = new HashMap<>(5);
        Map<String,Object> usreInfo = new HashMap<>(5);
        usreInfo.put("userCode","admin");
        usreInfo.put("userName","管理员");
        map.put("userInfo",usreInfo);
        String str = Pretreatment.mapTemplateString("你的\\\\\\{\\{\\ {unitCode} 用户号是 \\ \"合\\{理}哦\" \\ { userInfo.userCode } 你的姓名是\\{{userInfo.userName}，分", map, "[没有赋值]");
        System.out.println(str );
        str = Pretreatment.mapTemplateString("", map, "{没有赋值}");
        System.out.println(str );
        str = Pretreatment.mapTemplateString(null, map, "{没有赋值}");
        System.out.println(str );
        str = Pretreatment.mapTemplateString("{}！{}2{][[}}[][]3{  }4{3}", map);
        System.out.println(str );
        System.out.println("123123.4545" + (String)null );

    }
}
