package com.centit.support.test.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.centit.support.algorithm.CollectionsOpt;
import com.centit.support.file.FileIOOpt;
import com.centit.support.json.JSONOpt;
import com.centit.support.json.JSONTransformer;
import com.centit.support.json.JsonDifferent;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class TestJsonObject {

    public static void main(String arg[]) throws IOException {
        JSONOpt.fastjsonGlobalConfig();
        InputStream in = TestJsonObject.class
            .getResourceAsStream("/page-v1.json");
        Object object = JSON.parse(FileIOOpt.readStringFromInputStream(in));
        in = TestJsonObject.class
            .getResourceAsStream("/page-v1-1.json");
        Object object2  = JSON.parse(FileIOOpt.readStringFromInputStream(in));
        JsonDifferent diff = JSONOpt.diff(object, object2, "$item.id");
        System.out.println(JSON.toJSONString(diff.toJSONObject()));
    }

    public static void main2(String arg[]) throws IOException {
        JSONOpt.fastjsonGlobalConfig();
        InputStream in = TestJsonObject.class
            .getResourceAsStream("/test.json");
        Object object = JSON.parse(FileIOOpt.readStringFromInputStream(in));
        in = TestJsonObject.class
            .getResourceAsStream("/template.json");
        Object template  = JSON.parse(FileIOOpt.readStringFromInputStream(in));

        System.out.println(JSON.toJSONString(JSONTransformer.transformer(template, object)));
    }
    public static void testObjectToJson() {
        System.out.println(JSON.toJSONString(new Object[]{5, 6, "hello"}));

        Map<String, Object> mp = CollectionsOpt.createHashMap("osid",
            CollectionsOpt.createHashMap("type", "string", "index", "analyzed"),
            "optid",
            CollectionsOpt.createHashMap("type", "string", "index", "analyzed"));
        JSONObject jo = new JSONObject();
        JSONOpt.setAttribute(jo, "object.properties", mp);
        System.out.println(jo.toString());



      /*    B b = new B();
          A a = new A();

          a.firstValue = "a1";
          b.firstValue = 100;
          a.setSecondValue("a2");
          b.setThirdValueTemp(a);
          b.setSecondValue("b2");


          char [][] aInt = new char[5][2];
          aInt[0][0] = 1;
          aInt[1][0] = 2;
          aInt[2][0] = 3;
          aInt[3][0] = 4;
          aInt[4][0] = 5;
          aInt[0][1] = 1;
          aInt[1][1] = 2;
          aInt[2][1] = 3;
          aInt[3][1] = 4;
          aInt[4][1] = 5;
          System.out.println( (ReflectionOpt.isArray(aInt[0]))?"array":"simple");
          JSONArray jAray= JSONOpt.arrayToJSONArray(aInt);
        System.out.println( jAray.toString());

          JSONObject jObj = JSONOpt.objectToJSONObject(b);
        System.out.println( jObj.toString());
        a.setSecondValue("a22");
        JSONOpt.appendData(jObj , "thirdValue", JSONOpt.objectToJSONObject(a));
        System.out.println( jObj.toString());*/
        JSONObject json = new JSONObject();
        //json.fromString("{a:{b:[{h:{c:\"hello\"}},{c:[\"chartDiv\",\"chartDiv2\",\"chartDiv3\"]}]}}");
        /*Map<String ,Object> map = new HashMap<String ,Object>();

        map.put("renderTo", new Object[]{"chartDiv","chartDiv2","chartDiv3"});
        map.put("defaultSeriesType", "line");

        json.appendSeries("renderTo", new Object[]{"chartDiv","chartDiv2","chartDiv3"});
        json.appendSeries("renderTo2", new Object[]{"chartDiv","chartDiv2","chartDiv3"});
        json.appendSeries("renderTo3", new Object[]{"chartDiv","chartDiv2","chartDiv3"});

         * //json.appendYAxisPlotLines(map);
        map = new HashMap<String ,Object>();
        map.put("renderTo2", "chartDiv");
        map.put("defaultSeriesType", "line");
        //json.appendYAxisPlotLines(map);
         map = new HashMap<String ,Object>();
        map.put("renderTo2", "chartDiv");
        map.put("defaultSeri", "line");
        JSONOpt.setAttribute(json.getChartJson(), "a.b[0]", map);
        */
        JSONOpt.setAttribute(json, "a.b.c.d.e.f.g", new Object[]{"chartDiv", "chartDiv2", "chartDiv3"});
        JSONOpt.setAttribute(json, "b.b", "a");

        JSONOpt.setAttribute(json, "b.d", "你好");

        //json.appendData("series", null);
        //json.appendData("series", new Object[]{"chartDiv","chartDiv2","chartDiv3"});
        //json.appendData("series", new Object[]{"chartDiv4","chartDiv5","chartDiv6"});
        //json.appendXAxisCategories( new Object[]{"chartDiv","chartDiv2","chartDiv3"});
        //json.appendXAxisCategories( new Object[]{"chartDiv","chartDiv2","chartDiv3"});

        System.out.println(json.toString());
    }
}
