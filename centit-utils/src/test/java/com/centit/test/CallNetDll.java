package com.centit.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.centit.support.algorithm.ReflectionOpt;

public class CallNetDll {
	 public static void main(String arg[]){
		 Map<String ,Object> sourceObj = new HashMap<String ,Object>();
		 Map<String ,Object> valueObj = new HashMap<String ,Object>();
		 valueObj.put("def", 100);
		 List<Object> abc = new ArrayList<Object>();
		 abc.add(1);
		 abc.add(2);
		 abc.add(3);
		 abc.add(valueObj);
		 sourceObj.put("abc", abc);
		 System.out.println(JSON.toJSONString(ReflectionOpt.attainExpressionValue(sourceObj, "abc[3].def")));
	 }
}
