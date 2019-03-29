package com.centit.support.algorithm;

@SuppressWarnings("unused")
public abstract class BooleanBaseOpt {
    public static final String ONE_CHAR_TRUE = "T";
    public static final String ONE_CHAR_FALSE = "F";
    public static final String STRING_TRUE = "true";
    public static final String STRING_FALSE = "false";

    private BooleanBaseOpt() {
        throw new IllegalAccessError("Utility class");
    }

    static Boolean check4CharStrIsTrue(String str){
        final char ch0 = str.charAt(0);
        final char ch1 = str.charAt(1);
        final char ch2 = str.charAt(2);
        final char ch3 = str.charAt(3);
        if ((ch0 == 't' || ch0 == 'T') &&
                (ch1 == 'r' || ch1 == 'R') &&
                (ch2 == 'u' || ch2 == 'U') &&
                (ch3 == 'e' || ch3 == 'E') ) {
            return true;
        }
        return false;
    }

    static Boolean check5CharStrIsFlase(String str){
        final char ch0 = str.charAt(0);
        final char ch1 = str.charAt(1);
        final char ch2 = str.charAt(2);
        final char ch3 = str.charAt(3);
        final char ch4 = str.charAt(4);
        if ((ch0 == 'f' || ch0 == 'F') &&
                (ch1 == 'a' || ch1 == 'A') &&
                (ch2 == 'l' || ch2 == 'L') &&
                (ch3 == 's' || ch3 == 'S') &&
                (ch4 == 'e' || ch4 == 'E')) {
            return true;
        }
        return false;
    }

    static public Boolean castObjectToBoolean(Object obj){
        if(obj==null)
            return null;
        if(obj instanceof Boolean)
            return (Boolean)obj;
        if (obj instanceof Number)
            return ((Number) obj).intValue() != 0;

        final String str = StringBaseOpt.objectToString(obj);

        switch (str.length()) {
            case 1: {
                final char ch0 = str.charAt(0);
                if (ch0 == 'y' || ch0 == 'Y' ||
                        ch0 == 't' || ch0 == 'T' /*|| ch0 == '1'*/ ) {
                    return true;
                }
                if (ch0 == 'n' || ch0 == 'N' ||
                        ch0 == 'f' || ch0 == 'F' /*|| ch0 == '0'*/) {
                    return false;
                }
                break;
            }
            case 2: {
                final char ch0 = str.charAt(0);
                final char ch1 = str.charAt(1);
                if ((ch0 == 'o' || ch0 == 'O') &&
                        (ch1 == 'n' || ch1 == 'N') ) {
                    return true; // om /off
                }
                if ((ch0 == 'n' || ch0 == 'N') &&
                        (ch1 == 'o' || ch1 == 'O')) {
                    return false; // no / yes
                }
                break;
            }
            case 3: {
                final char ch0 = str.charAt(0);
                final char ch1 = str.charAt(1);
                final char ch2 = str.charAt(2);
                if ((ch0 == 'y' || ch0 == 'Y') &&
                        (ch1 == 'e' || ch1 == 'E') &&
                        (ch2 == 's' || ch2 == 'S') ) {
                    return true;
                }
                if((ch0 == 'o' || ch0 == 'O') &&
                        (ch1 == 'f' || ch1 == 'F') &&
                        (ch2 == 'f' || ch2 == 'F') ){
                    return false;
                }
                break;
            }
            case 4: {
                if(check4CharStrIsTrue(str)){
                    return true;
                }
                break;
            }
            case 5: {
                if(check5CharStrIsFlase(str)){
                    return false;
                }
                break;
            }
            default:
                break;
        }
        return null;//ringRegularOpt.isNumber(str);
    }

    public static Boolean castObjectToBoolean(Object obj, Boolean defaultValue){
        return GeneralAlgorithm.nvl(castObjectToBoolean(obj),defaultValue);
    }

    static public boolean isBoolean(Object obj){
        return BooleanBaseOpt.castObjectToBoolean(obj)!= null;
    }
}
