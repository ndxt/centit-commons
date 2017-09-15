package com.centit.support.compiler;

import java.util.Collection;

import com.centit.support.algorithm.DatetimeOpt;
import com.centit.support.algorithm.ReflectionOpt;
import com.centit.support.algorithm.StringRegularOpt;

public class ObjectTranslate implements VariableTranslate {

    private Object varObj;
    //public
    public ObjectTranslate(){
        varObj = null;
    }
    public ObjectTranslate(Object varObj) {
        this.varObj = varObj;
    }

     /**
     * 默认返回业务模型对象的属性值 , request 队形的参数
      /**变量名 -》变量值的转变
      *变量 是用 ${变量名}
      *如果这个变量不存在，返回空字符串 "''"
      * @param varName 变量
      * @return 值
      */
    @Override
    public String getVarValue(String varName) {
        
        if(varObj==null)
            return "\"\"";

        Object obj = 
                ReflectionOpt.attainExpressionValue
                /*ReflectionOpt.forceGetProperty*/(varObj, varName);
        if(obj==null)
            return "\"\"";
        return objectToFormulaString(obj);
    }    

    public static String objectToFormulaString(Object objValue){
        if(objValue==null)
            return "\"\"";

        if(objValue instanceof Object[]){
            Object [] objs=(Object[]) objValue;

            if(objs.length>0){
                StringBuilder sb = new StringBuilder();
                for(int i=0;i<objs.length;i++){
                    if(i>0)
                        sb.append(',');
                    if(objs[i]!=null){
                        if(ReflectionOpt.isNumberType(objs[i].getClass()))
                            sb.append( objs[i].toString());
                        else if(objs[i] instanceof java.util.Date)
                            sb.append(StringRegularOpt.quotedString(
                                    DatetimeOpt.convertDatetimeToString((java.util.Date) objs[i])));
                        else if(objs[i] instanceof java.sql.Date)
                            sb.append(StringRegularOpt.quotedString(DatetimeOpt.convertDatetimeToString(
                                    DatetimeOpt.convertUtilDate(
                                            (java.sql.Date) objs[i]))));
                        else
                            sb.append(StringRegularOpt.quotedString(objs[i].toString()));
                    }
                }
                return sb.toString();
            }else{
              return "\"\"";
            }
        }else if(objValue instanceof Collection){
            StringBuilder sb = new StringBuilder();
            int vc = 0;
            Collection<?> valueList = (Collection<?> )objValue;
            for(Object ov : valueList){
                if(ov!=null){
                    if(vc>0)
                        sb.append(",");
                    if(ReflectionOpt.isNumberType(ov.getClass()))
                        sb.append( ov.toString());
                    else if(ov instanceof java.util.Date)
                        sb.append(
                                StringRegularOpt.quotedString(
                                        DatetimeOpt.convertDatetimeToString((java.util.Date) ov)));
                    else if(ov instanceof java.sql.Date)
                        sb.append(
                                StringRegularOpt.quotedString(DatetimeOpt.convertDatetimeToString(
                                DatetimeOpt.convertUtilDate(
                                        (java.sql.Date) ov))));
                    else
                        sb.append(StringRegularOpt.quotedString(ov.toString()));
                    vc++;
                }
            }
            if(vc==0)
                return "\"\"";
            return sb.toString();
        }else if(objValue instanceof java.util.Date){
            return StringRegularOpt.quotedString(
                    DatetimeOpt.convertDatetimeToString((java.util.Date) objValue));
        }else if(objValue instanceof java.sql.Date){
            return StringRegularOpt.quotedString( DatetimeOpt.convertDatetimeToString(
                    DatetimeOpt.convertUtilDate(
                    (java.sql.Date) objValue)));
        }else
            return StringRegularOpt.quotedString(objValue.toString());
    }

    @Override
    public String getLabelValue(String labelName) {
        return getVarValue(labelName);
        /*if("''".equals(res))//res == null ||
            return labelName;
        return res;*/
    }

    public void setVarObject(Object varObj) {
        this.varObj = varObj;
    }

}

