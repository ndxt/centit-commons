package com.centit.support.compiler;

public abstract class ConstDefine {

	private ConstDefine() {
		throw new IllegalAccessError("Utility class");
	}

	final static public int   TYPE_NUM = 1;
	final static public int   TYPE_STR = 2;
	final static public int   TYPE_ANY = 0;

	final static public int   FUNC_AVE		= 100;
	final static public int   FUNC_BYTE		= 101;
	final static public int   FUNC_CAPITAL	= 102;
	final static public int   FUNC_MAX		= 103;
	final static public int   FUNC_MIN		= 104;
	final static public int   FUNC_SUM		= 105;
	final static public int   FUNC_STRCAT	= 106;
	final static public int   FUNC_ROUND	= 107;
	final static public int   FUNC_IF		= 108;
	final static public int   FUNC_PRECISION	= 109;
	final static public int   FUNC_SUBSTR   = 110;
	final static public int   FUNC_MATCH	= 111;
	final static public int   FUNC_COUNT	= 112;
	final static public int   FUNC_LN       = 113;
	final static public int   FUNC_EXP		=114;
	final static public int   FUNC_SQRT     = 115;
	final static public int   FUNC_CASE		= 116;
	final static public int   FUNC_UPCASE		= 142;	
	final static public int   FUNC_LOWCASE		= 143;
	
	
	final static public int   FUNC_LOG      = 117;
	final static public int   FUNC_SIN      = 118;
	final static public int   FUNC_COS		= 119;
	final static public int   FUNC_TAN      = 120;
	final static public int   FUNC_CTAN		= 121;
	final static public int   FUNC_FIND		= 122;
	final static public int   FUNC_FREQUENCE = 123;
	final static public int   FUNC_INT		= 124;
	final static public int   FUNC_FRAC		= 125;
	
	final static public int   FUNC_DAY		= 126;
	final static public int   FUNC_MONTH	= 127;
	final static public int   FUNC_YEAR		= 128;
	
	final static public int   FUNC_DAY_SPAN	= 129;
	final static public int   FUNC_MONTH_SPAN	= 130;
	final static public int   FUNC_YEAR_SPAN	= 131;
	final static public int   FUNC_TODAY		= 132;
	final static public int   FUNC_ADD_DAYS		= 137;
	final static public int   FUNC_ADD_MONTHS	= 138;
	final static public int   FUNC_ADD_YEARS	= 139;
	final static public int   FUNC_TRUNC_DAY	= 140;
	final static public int   FUNC_FIRST_OF_MONTH	= 141;
	
	final static public int   FUNC_STDDEV	= 133;
	final static public int   FUNC_GET_STR	= 134;
	final static public int   FUNC_GET_PY	= 135;
	final static public int   FUNC_PRINT		= 136;	
	
	final static public int   FUNC_COUNTNULL	= 144;
	final static public int   FUNC_COUNTNOTNULL	= 145;
	final static public int   FUNC_ISEMPTY	= 146;
	final static public int   FUNC_NOTEMPTY	= 147;
	final static public int   FUNC_GET_AT	= 148;
	
	final static public int   OP_BASE        = 30;	// +
	final static public int   OP_ADD         = 30;	// +
	final static public int   OP_SUB		 = 31;  // -
	final static public int   OP_MUL		 = 32;  // *
	final static public int   OP_DIV		 = 33;  // /
	final static public int   OP_EQ          = 34;	//==
	final static public int   OP_BG          = 35;  //>
	final static public int   OP_LT          = 36;  //<
	final static public int   OP_EL          = 37;  //<=
	final static public int   OP_EB          = 38;  //>=
	final static public int   OP_NE          = 39;  //!=
	final static public int   OP_BITOR		 = 40;  //|	
	final static public int   OP_BITAND      = 41;  //&
	final static public int   OP_NOT         = 42;  //!	
	final static public int   OP_POWER       = 43;  //^
	final static public int   OP_LMOV        = 44;  // >>
	final static public int   OP_RMOV        = 45;  // <<
	final static public int   OP_LIKE        = 46;  //LIKE
	final static public int   OP_IN          = 47 ; //IN
	final static public int   OP_EVALUATE    = OP_EQ;  	//=
	final static public int   OP_OR			 = OP_BITOR;  //||	
	final static public int   OP_LOGICOR	 = 48;  // or 	
	final static public int   OP_AND         = OP_BITAND;  //&&
	final static public int   OP_LOGICAND	 = 49;  // and	
}
