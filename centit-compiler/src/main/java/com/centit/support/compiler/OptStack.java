package com.centit.support.compiler;

public class OptStack {

	/*public static final int   OP_BASE        = 30;	// +
	public static final int   OP_ADD         = 30;	// +
	public static final int   OP_SUB		 = 31;  // -
	public static final int   OP_MUL		 = 32;  // *
	public static final int   OP_DIV		 = 33;  // /
	public static final int   OP_EQ          = 34;	//==
	public static final int   OP_BG          = 35;  //>
	public static final int   OP_LT          = 36;  //<
	public static final int   OP_EL          = 37;  //<=
	public static final int   OP_EB          = 38;  //>=
	public static final int   OP_NE          = 39;  //!=
	public static final int   OP_BITOR		 = 40;  //|
	public static final int   OP_BITAND      = 41;  //&
	public static final int   OP_NOT         = 42;  //!
	public static final int   OP_POWER       = 43;  //^
	public static final int   OP_LMOV        = 44;  // >>
	public static final int   OP_RMOV        = 45;  // <<
	public static final int   OP_LIKE        = 46;  //LIKE
	public static final int   OP_IN          = 47 ; //IN
	public static final int   OP_LOGICOR	 = 48;  // or
	public static final int   OP_AND         = OP_BITAND;  //&&
	public static final int   OP_LOGICAND	 = 49;  // and
	*/	
	/**
	 * @param args 数值越小优先级越低
	 */
	final static private int optsPri[]={ 5, 5,6,6, 4,4,4, 4, 4, 4, 2,3,9,8,5,5,  4,   7, 2,  3, 4 , 4};
									    //+ - * / == > < <=  >= != | & ! ^ >><<like  in or and
									    //5 is normal
	private int sourceLen;
	private int optsStack[];
	
	public OptStack()
	{
		sourceLen = 0;
		optsStack = new int[10];
	}
	
	public void empty()
	{
		sourceLen = 0;
	}
		
	public int  pushOpt(int optID)
	{
		if( sourceLen == 0 || optsPri[optID - ConstDefine.OP_BASE] > optsPri[ optsStack[sourceLen-1]-ConstDefine.OP_BASE]){
			optsStack[sourceLen] = optID;
			sourceLen ++;
			return 0;
		}else
			return popOpt();
	}

	public int popOpt()
	{
		if(sourceLen>0) 
			return optsStack[--sourceLen];
		return 0;
	}

}
