package com.centit.support.compiler;

import com.centit.support.algorithm.BooleanBaseOpt;
import com.centit.support.algorithm.DatetimeOpt;
import com.centit.support.algorithm.NumberBaseOpt;
import com.centit.support.algorithm.StringBaseOpt;

import java.util.Date;

public class EmbedFuncUtils {

    public static final EmbedFuncUtils instance = new EmbedFuncUtils();

    private EmbedFuncUtils() {
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

    public String capital(Object object){
        return NumberBaseOpt.capitalization(
            StringBaseOpt.objectToString(object));
    }

    public String capitalRMB(Object object) {
        Double db = NumberBaseOpt.castObjectToDouble(object);
        Long yuan = db.longValue();
        int jiao = (int) ((db - yuan) * 10);
        int fen = (int) ((db - yuan) * 100f + 0.5f) % 10;
        if (jiao == 0 && fen == 0){
            return NumberBaseOpt.capitalization(String.valueOf(yuan)) + "元整";
        }
        StringBuilder sValue = new StringBuilder(NumberBaseOpt.capitalization(String.valueOf(yuan)));
        sValue.append("元").append(NumberBaseOpt.capitalization(String.valueOf(jiao))).append("角")
            .append(NumberBaseOpt.capitalization(String.valueOf(fen))).append("分");
        return sValue.toString();
    }

    public Object ifElse(Object... slOperand){
        int nOpSum = (slOperand == null) ? 0 : slOperand.length;
        if (nOpSum == 1 ){
            return slOperand[0];
        }
        if (nOpSum == 2 ){
            if(slOperand[0]==null)
                return slOperand[1];
            return slOperand[0];
        }
        if (nOpSum > 2 ){
            boolean b = BooleanBaseOpt.castObjectToBoolean(slOperand[0],false);
            return b ? slOperand[1] : slOperand[2];
        }
        return null;
    }

}
