package com.centit.support.compiler;

import java.util.HashMap;
import java.util.Map;

public class FormulaTest {

    public static void testFormula() {
        String formula = " T == 'T' ";

        Formula f = new Formula();
        String s = f.calculate(formula, new HashMap<>());
        System.out.println(s);
        //assertEquals("nihao,地球 !",s);
    }

    public static void testFormula2() {

        VariableFormula f = new VariableFormula();
        Map<String,Object> varMap = new HashMap<String,Object>();
        Map<String,Object> varA = new HashMap<String,Object>();
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
        String formula = " depart != '你好' ";
        System.out.println(formula);
        Object s = f.calculate(formula, varMap);
        System.out.println(s);
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
                "today +':'+  today() + ' '+'usercode' + usercode +'$'+ $ { otherValue } + ':' + $ { user . usercode }",
                varMap);
        System.out.println(s);
    }

    public  static void  main(String[] args)   {
        //testFormula2();//
        // testFormula2();
        testLexer();
        System.out.println("123123.4545" + (String)null );

    }
}
