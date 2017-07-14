package com.centit.support.compiler;

public class OptStack {

	/*final static public int   OP_BASE        = 30;	// +
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
	final static public int   OP_LOGICOR	 = 48;  // or 	
	final static public int   OP_AND         = OP_BITAND;  //&&
	final static public int   OP_LOGICAND	 = 49;  // and	
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
