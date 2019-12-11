package com.centit.support.compiler;

public abstract class ConstDefine {

    private ConstDefine() {
        throw new IllegalAccessError("Utility class");
    }

    public static final int   TYPE_NUM = 1;
    public static final int   TYPE_STR = 2;
    public static final int   TYPE_DATE = 3;
    public static final int   TYPE_ANY = 0;

    public static final int   FUNC_AVE        = 100;
    public static final int   FUNC_BYTE        = 101;
    public static final int   FUNC_CAPITAL    = 102;
    public static final int   FUNC_MAX        = 103;
    public static final int   FUNC_MIN        = 104;
    public static final int   FUNC_SUM        = 105;
    public static final int   FUNC_STRCAT    = 106;
    public static final int   FUNC_ROUND    = 107;
    public static final int   FUNC_IF        = 108;
    public static final int   FUNC_PRECISION    = 109;
    public static final int   FUNC_SUBSTR   = 110;
    public static final int   FUNC_MATCH    = 111;
    public static final int   FUNC_COUNT    = 112;
    public static final int   FUNC_LN       = 113;
    public static final int   FUNC_EXP        =114;
    public static final int   FUNC_SQRT     = 115;
    public static final int   FUNC_CASE        = 116;
    public static final int   FUNC_UPCASE        = 142;
    public static final int   FUNC_LOWCASE        = 143;


    public static final int   FUNC_LOG      = 117;
    public static final int   FUNC_SIN      = 118;
    public static final int   FUNC_COS        = 119;
    public static final int   FUNC_TAN      = 120;
    public static final int   FUNC_CTAN        = 121;
    public static final int   FUNC_FIND        = 122;
    public static final int   FUNC_FREQUENCE = 123;
    public static final int   FUNC_INT        = 124;
    public static final int   FUNC_FRAC        = 125;

    public static final int   FUNC_DAY        = 126;
    public static final int   FUNC_MONTH    = 127;
    public static final int   FUNC_YEAR        = 128;

    public static final int   FUNC_DAY_SPAN    = 129;
    public static final int   FUNC_MONTH_SPAN    = 130;
    public static final int   FUNC_YEAR_SPAN    = 131;
    public static final int   FUNC_CURRENT_DATE = 132;
    public static final int   FUNC_CURRENT_DATETIME = 149;
    public static final int   FUNC_CURRENT_TIMESTAMP = 155;
    public static final int   FUNC_ADD_DAYS        = 137;
    public static final int   FUNC_ADD_MONTHS    = 138;
    public static final int   FUNC_ADD_YEARS    = 139;
    public static final int   FUNC_TRUNC_DATE = 140;
    public static final int   FUNC_LAST_OF_MONTH    = 141;

    public static final int   FUNC_STDDEV    = 133;
    public static final int   FUNC_GET_STR    = 134;
    public static final int   FUNC_GET_PY    = 135;
    public static final int   FUNC_PRINT        = 136;

    public static final int   FUNC_COUNTNULL    = 144;
    public static final int   FUNC_COUNTNOTNULL    = 145;
    public static final int   FUNC_ISEMPTY    = 146;
    public static final int   FUNC_NOTEMPTY    = 147;
    public static final int   FUNC_GET_AT    = 148;
    public static final int   FUNC_LPAD    = 150;
    public static final int   FUNC_RPAD    = 151;

    public static final int   FUNC_TO_DATE    = 152;
    public static final int   FUNC_TO_STRING    = 153;
    public static final int   FUNC_TO_NUMBER    = 154;
    public static final int   FUNC_SINGLETON    = 156;
    public static final int   FUNC_DATE_SPAN    = 157;
    public static final int   FUNC_ADD_DATE   = 158;
    public static final int   FUNC_REG_MATCH    = 159;
    public static final int   FUNC_REG_MATCH_VALUES    = 160;
    public static final int   FUNC_WEEK    = 161;
    public static final int   FUNC_WEEK_DAY    = 162;
    public static final int   FUNC_FORMAT_DATE    = 163;
    public static final int   FUNC_DATE_INFO    = 164;
    public static final int   FUNC_FLOOR    = 165;
    public static final int   FUNC_CEIL    = 166;

    public static final int   OP_BASE        = 30;    // +
    public static final int   OP_ADD         = 30;    // +
    public static final int   OP_SUB         = 31;  // -
    public static final int   OP_MUL         = 32;  // *
    public static final int   OP_DIV         = 33;  // /
    public static final int   OP_EQ          = 34;    //==
    public static final int   OP_BG          = 35;  //>
    public static final int   OP_LT          = 36;  //<
    public static final int   OP_EL          = 37;  //<=
    public static final int   OP_EB          = 38;  //>=
    public static final int   OP_NE          = 39;  //!=
    public static final int   OP_BITOR         = 40;  //|
    public static final int   OP_BITAND      = 41;  //&
    public static final int   OP_NOT         = 42;  //!
    public static final int   OP_POWER       = 43;  //^
    public static final int   OP_LMOV        = 44;  // <<
    public static final int   OP_RMOV        = 45;  // >>
    public static final int   OP_LIKE        = 46;  //LIKE
    public static final int   OP_IN          = 47 ; //IN
    public static final int   OP_EVALUATE    = OP_EQ;      //=
    public static final int   OP_OR          = OP_BITOR;  //||
    public static final int   OP_LOGICOR     = 48;  // or
    public static final int   OP_AND         = OP_BITAND;  //&&
    public static final int   OP_LOGICAND    = 49;  // and
    public static final int   OP_MOD         = 50;  // mod % 取整后取模
    public static final int   OP_DBMOD       = 51;  // dbmod 取模
}
