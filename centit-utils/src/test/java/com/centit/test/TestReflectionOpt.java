package com.centit.test;

import com.centit.support.algorithm.Lunar;
import com.centit.support.algorithm.ReflectionOpt;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class TestReflectionOpt {
    public static void main(String arg[]) {
         Method [] methods = Lunar.class.getDeclaredMethods();
         for(Method md : methods){
             System.out.println(md.getName());
             Parameter[] params = md.getParameters();
             if(params!=null){
                 for(Parameter p : params){
                     System.out.println("   "+p.getName());
                 }
             }
             System.out.println();
         }
    }
    public static void getTypeMatch(){
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
