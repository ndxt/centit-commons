package com.centit.support.test.utils;

import com.alibaba.fastjson2.JSON;
import com.centit.support.algorithm.StringBaseOpt;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestRegEX {
    public static void main(String[] args) {

        String sValues = "knownwordD://mydir//mydir2//mydir3//yourfile.docx";
        Pattern p = Pattern.compile("^[^/]+/(.+)/");
        Matcher m = p.matcher(sValues); // 获取 matcher 对象
        List<String> matchValues = new ArrayList<>();
        while (m.find()) {
            if(m.groupCount()==1){
                matchValues.add(m.group(1));
            }else{
                matchValues.add(m.group());
            }
            //matchValues.add(sValues.substring(m.start(), m.end()));
            //matchValues.add("\r\nnextLine\r\n");
        }
        System.out.println(JSON.toJSONString(matchValues));
    }
}
