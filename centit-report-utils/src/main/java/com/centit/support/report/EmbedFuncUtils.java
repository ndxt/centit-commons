package com.centit.support.report;

import com.centit.support.algorithm.DatetimeOpt;
import com.centit.support.algorithm.NumberBaseOpt;
import com.centit.support.algorithm.StringBaseOpt;
import com.centit.support.common.LeftRightPair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class EmbedFuncUtils {

    public static final EmbedFuncUtils intance = new EmbedFuncUtils();

    private EmbedFuncUtils() {
    }


    private static LeftRightPair<Integer, List<Object>> flatOperands(Object[] slOperand) {
        int nCount = 0;
        List<Object> ret = new ArrayList<>();
        if (slOperand != null && slOperand.length > 0) {
            for (Object obj : slOperand) {
                if (obj instanceof Object[]) {
                    Object[] objs = (Object[]) obj;
                    for (Object obj1 : objs) {
                        ret.add(obj1);
                        nCount++;
                    }
                } else if (obj instanceof Collection) {
                    ret.addAll((Collection) obj);
                    nCount += ((Collection) obj).size();
                } else {
                    ret.add(obj);
                    nCount++;
                }
            }
        }
        return new LeftRightPair<>(nCount, ret);
    }


    public Double ave(Object... slOperand) {
        LeftRightPair<Integer, List<Object>> opt = flatOperands(slOperand);
        double dbtemp = 0.0;
        int nOpSum = 0;
        for (int i = 0; i < opt.getLeft(); i++) {
            Double db = NumberBaseOpt.castObjectToDouble(
                opt.getRight().get(i));
            if (db != null) {
                dbtemp += db;
                nOpSum++;
            }
        }
        if (nOpSum > 0)
            return dbtemp / nOpSum;//"%f",
        else
            return null;
    }

    public List<Object> distinct(Object... slOperand) {
        LeftRightPair<Integer, List<Object>> opt = flatOperands(slOperand);
        if (opt.getLeft() < 2) {
            return opt.getRight();
        }
        List<Object> retObjs = new ArrayList<>();
        for (Object obj : opt.getRight()) {
            if (obj == null || retObjs.contains(obj)) {
                continue;
            }
            retObjs.add(obj);
        }
        return retObjs;
    }

    public Object getAt(Object... slOperand) {
        int nOpSum = (slOperand == null) ? 0 : slOperand.length;
        if (nOpSum < 2)
            return null;
        LeftRightPair<Integer, List<Object>> opt = flatOperands(slOperand);
        Object objTemp = slOperand[0];
        if (NumberBaseOpt.isNumber(objTemp)) {
            Integer nbit = NumberBaseOpt.castObjectToInteger(objTemp);
            if (nbit != null) {
                if (nbit < 0) {
                    nbit = opt.getLeft() - 1 + nbit;
                }

                if (nbit >= 0 && nbit < opt.getLeft() - 1) {
                    return opt.getRight().get(nbit + 1);
                }
            }
        }
        return null;
    }

    public String formatDate(Object... slOperand) {
        int nOpSum = (slOperand == null) ? 0 : slOperand.length;
        if (nOpSum == 1 ){
            Object obj1=slOperand[0];
            if(obj1 instanceof Date){
                return DatetimeOpt.convertDateToSmartString((Date) obj1);
            }
            return DatetimeOpt.convertDateToString(DatetimeOpt.currentUtilDate(),
                StringBaseOpt.castObjectToString(slOperand[0]));
        }
        if (nOpSum > 1 ){
            Object obj1=slOperand[0];
            Object obj2=slOperand[1];
            if(obj1 instanceof Date){
                Object temp = obj2;
                obj2 = obj1;
                obj1 = temp;
            }
            String dateFormat = StringBaseOpt.castObjectToString(obj1);
            Date dt = DatetimeOpt.castObjectToDate(obj2);
            return DatetimeOpt.convertDateToString(dt, dateFormat);
        }
        return null;
    }
}
