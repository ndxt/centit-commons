package com.centit.support.algorithm;

import org.apache.commons.lang3.ObjectUtils;

/**
 * Created by codefan on 17-9-7.
 * @author codefan
 */
@SuppressWarnings("unused")
public abstract  class GeneralAlgorithm {

    private GeneralAlgorithm() {
        throw new IllegalAccessError("Utility class");
    }

    public static <T> T nvl(T obj, T obj2){
        return obj==null ? obj2 : obj;
    }

    public static <T> T nvl2(Object obj, T obj2, T obj3){
        return obj==null ? obj3 : obj2;
    }

    public static int compareTwoObject(Object operand , Object operand2){
        if (operand == null && operand2 == null) {
            return 0;
        }

        if(operand == null ){
            return -1;
        }

        if(operand2 == null ){
            return 1;
        }

        if (NumberBaseOpt.isNumber(operand)
                && NumberBaseOpt.isNumber(operand2)) {
            return NumberBaseOpt.castObjectToDouble(operand).
                    compareTo( NumberBaseOpt.castObjectToDouble(operand2));
        }

        return ObjectUtils.compare(
                StringBaseOpt.objectToString(operand),
                StringBaseOpt.objectToString(operand2));
    }

}
