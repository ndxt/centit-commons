package com.centit.support.algorithm;

import org.apache.commons.lang3.StringUtils;

@SuppressWarnings("unused")
public abstract class BooleanBaseOpt {
    public static final String ONE_CHAR_TRUE = "T";
    public static final String ONE_CHAR_FALSE = "F";
    public static final String STRING_TRUE = "true";
    public static final String STRING_FALSE = "false";

    public static final String ONE_CHAR_YES = "Y";
    public static final String ONE_CHAR_NO = "N";
    public static final String STRING_YES = "yes";
    public static final String STRING_NO = "no";

    public static final String STRING_ON = "on";
    public static final String STRING_OFF = "off";

    private BooleanBaseOpt() {
        throw new IllegalAccessError("Utility class");
    }

    static public Boolean castObjectToBoolean(Object obj) {
        if (obj == null)
            return null;
        if (obj instanceof Boolean)
            return (Boolean) obj;
        if (obj instanceof Number)
            return ((Number) obj).intValue() != 0;

        final String str = StringBaseOpt.objectToString(obj);
        if (StringUtils.equalsAnyIgnoreCase(str, "y", "yes", "t", "true", "on")) {
            return true;
        }
        if (StringUtils.equalsAnyIgnoreCase(str, "n", "no", "f", "false", "off")) {
            return false;
        }
        return null;//StringRegularOpt.isNumber(str);
    }

    public static Boolean castObjectToBoolean(Object obj, Boolean defaultValue) {
        return GeneralAlgorithm.nvl(castObjectToBoolean(obj), defaultValue);
    }

    static public boolean isBoolean(Object obj) {
        return BooleanBaseOpt.castObjectToBoolean(obj) != null;
    }
}
