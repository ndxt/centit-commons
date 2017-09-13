package com.centit.test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.centit.support.algorithm.ReflectionOpt;

public class TestReflectionOpt {
	 public static void main(String arg[]){
		 int [] objArray = new int [10];
		 if(int[].class.isAssignableFrom(objArray.getClass())){

			 System.out.println("OK!");
		 }
		 System.out.println(java.util.Date.class.isAssignableFrom(java.sql.Date.class));
		 System.out.println(java.util.Date.class.isAssignableFrom(java.util.Date.class));
		 System.out.println(java.sql.Date.class.isAssignableFrom(java.util.Date.class));

		 for(Method md : ReflectionOpt.getAllGetterMethod(String.class)){
			 System.out.println(md.getName());

		 }
	 }
}
