package com.centit.support.compiler;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.centit.support.algorithm.StringBaseOpt;
import com.centit.support.algorithm.StringRegularOpt;

public class Formula {

	private Lexer lex;
	public Formula()
	{
		lex = new Lexer();
		//m_preTreat.setVariableTranslate(new SimpleTranslate("1"));
	}

	final static private int getFuncNo(String  sFuncName)
	{
		return EmbedFunc.getFuncNo(sFuncName);
	}
	
	public static final int getOptID(String sOptName)
	{
		int sl = sOptName.length();
		if(sl == 0) return -1;
		char sp = sOptName.charAt(0), sp2= '\0';
		if(sl>1) sp2 = sOptName.charAt(1);
		switch(sp){
			case '=':
				if(sp2 == '=') {
					return(ConstDefine.OP_EQ);//m_iPreIsn = ConstDefine.OP_EQ ;return ConstDefine.OP_EQ;
				}
				return(ConstDefine.OP_EVALUATE);//	m_iPreIsn = ConstDefine.OP_EVALUATE;return ConstDefine.OP_EVALUATE;
			case '+': return(ConstDefine.OP_ADD); //{ m_iPreIsn = ConstDefine.OP_ADD ; return ConstDefine.OP_ADD ;}
			case '-': return(ConstDefine.OP_SUB);//{ m_iPreIsn = ConstDefine.OP_SUB ; return ConstDefine.OP_SUB;}
			case '*': return(ConstDefine.OP_MUL);//m_iPreIsn = ConstDefine.OP_MUL; return ConstDefine.OP_MUL;
			case '/': return(ConstDefine.OP_DIV);//m_iPreIsn = ConstDefine.OP_DIV; return ConstDefine.OP_DIV;
			case '^': return(ConstDefine.OP_POWER);//m_iPreIsn = ConstDefine.OP_POWER; return ConstDefine.OP_POWER;
			case '>':
				if(sp2 == '=') {
					return(ConstDefine.OP_EB);//	return ConstDefine.OP_EB;	m_iPreIsn = ConstDefine.OP_EB ;
				}
				if(sp2 == '>') 
					return (ConstDefine.OP_LMOV);
				return(ConstDefine.OP_BG);//m_iPreIsn = ConstDefine.OP_BG ;	return ConstDefine.OP_BG ;
			case '<':
				if(sp2 == '=') {
					return(ConstDefine.OP_EL);//return ConstDefine.OP_EL; m_iPreIsn = ConstDefine.OP_EL;
				}
				if(sp2 == '>') {
					return(ConstDefine.OP_NE);//	return ConstDefine.OP_NE;	m_iPreIsn = ConstDefine.OP_NE ;
				}
				if(sp2 == '<') {
					return(ConstDefine.OP_RMOV);//	return ConstDefine.OP_NE;	m_iPreIsn = ConstDefine.OP_NE ;
				}
				return(ConstDefine.OP_LT);//m_iPreIsn = ConstDefine.OP_LT ;return ConstDefine.OP_LT ;
			case '&': 
				if(sp2 == '&') {
					return(ConstDefine.OP_AND);//return ConstDefine.OP_EL; m_iPreIsn = ConstDefine.OP_EL;
				}
				return(ConstDefine.OP_BITAND);//m_iPreIsn = ConstDefine.OP_AND ; return  ConstDefine.OP_AND;
			case '|': 
				if(sp2 == '|') {
					return(ConstDefine.OP_OR);//return ConstDefine.OP_EL; m_iPreIsn = ConstDefine.OP_EL;
				}
				return(ConstDefine.OP_BITOR);//m_iPreIsn = ConstDefine.OP_OR; return  ConstDefine.OP_OR;
			case '!':
				if(sp2 == '=') {
					return(ConstDefine.OP_NE);//return ConstDefine.OP_NE;	m_iPreIsn = ConstDefine.OP_NE;
				}
				return(ConstDefine.OP_NOT);//m_iPreIsn = ConstDefine.OP_NOT; return  ConstDefine.OP_NOT;
		}
		if(sOptName.equalsIgnoreCase("LIKE"))
			return(ConstDefine.OP_LIKE);	
		if(sOptName.equalsIgnoreCase("AND"))
			return(ConstDefine.OP_LOGICAND);	
		if(sOptName.equalsIgnoreCase("OR"))
			return(ConstDefine.OP_LOGICOR);	
		if(sOptName.equalsIgnoreCase("NOT"))
			return(ConstDefine.OP_NOT);	
		if(sOptName.equalsIgnoreCase("IN"))
			return(ConstDefine.OP_IN);	
		if(sOptName.equalsIgnoreCase("DIV"))
			return(ConstDefine.OP_DIV);	
		return -1;
	}
	
	final static private String runFunc(List<String> slOperand,int funcID)
	{
		return EmbedFunc.runFunc(slOperand, funcID);
	}
	
	public static final boolean isKeyWord(String sWord)
	{
		if (sWord == null || sWord.length()==0)
			return false;
		char firstChar = sWord.charAt(0); 
		if ((firstChar == ',') || (firstChar == '(') || (firstChar == ')'))
				return true;
		if ((firstChar =='-') && (sWord.length()>1)) return false;
		return getOptID(sWord)>0;	
	}
	
	private String getItem()
	{
		String str = lex.getAWord();
		if( str == null || str.length()==0) return null;
		if(str.charAt(0) == ')' || str.charAt(0) == ',' ){
			lex.setPreword(str);
			return null;
		}
		if(str.charAt(0) == '('){
			String resstr = getFormula();
			str = lex.getAWord();
			if( str == null || str.length()==0 || str.charAt(0) != ')') return null;
			return resstr;
		}else if( (str.charAt(0) == '!') || str.equalsIgnoreCase("NOT") ) {
			str = getItem();
			if(StringRegularOpt.isTrue(str))
				return  "0";
			else
				return  "1";
		}

		String optstr = str.toLowerCase();

		int funcNo = getFuncNo(optstr);
		if( funcNo != -1)
			str = getFunc(funcNo,str);
		return str;
	}

	private String runOperate(String operand,String operand2,int optID)
	{
		String str = "";
		String str1 = StringRegularOpt.trimString(operand);
		String str2 = StringRegularOpt.trimString(operand2);

		switch(optID){
			case ConstDefine.OP_LOGICOR:{
				boolean bop = StringRegularOpt.isTrue(str1), bop2 = StringRegularOpt.isTrue(str2);
				if(bop||bop2) return "1";
				return "0";
			}
			case ConstDefine.OP_AND:
			case ConstDefine.OP_LOGICAND:{
				boolean bop = StringRegularOpt.isTrue(str1), bop2 = StringRegularOpt.isTrue(str2);
				if(bop&&bop2) return "1";
				return "0";
			}
		}
		
		if( !StringRegularOpt.isNumber(str1) || !StringRegularOpt.isNumber(str2)){

			int res = str1.compareTo(str2);
			switch(optID){
				case ConstDefine.OP_OR:
				case ConstDefine.OP_ADD:
				case ConstDefine.OP_MUL:{
					return String.format("\"%s%s\"", str1 , str2);
							}
				case ConstDefine.OP_EQ:
					if( res ==0 ) return "1";
					return "0";
				case ConstDefine.OP_BG:
					if( res > 0) return "1";
					return "0";
				case ConstDefine.OP_LT:
					if( res < 0) return "1";
					return "0";
				case ConstDefine.OP_EL:
					if( res <=0) return "1";
					return "0";
				case ConstDefine.OP_EB:
					if( res >=0) return "1";
					return "0";
				case ConstDefine.OP_NE:
					if( res !=0) return "1";
					return "0";
				case ConstDefine.OP_LMOV:
					if( StringRegularOpt.isNumber(str2) ){
						int nP2 = Double.valueOf(str2).intValue();
	
						if(nP2>=0 && str1.length() > nP2){
							str = str1.substring(0,str1.length() - nP2);
							return str;
						}
					}
					return null;
				case ConstDefine.OP_RMOV:
					if( StringRegularOpt.isNumber(str2) ){
						int nP2 = Double.valueOf(str2).intValue();
						if(nP2>=0 && str1.length() > nP2){
							str = str1.substring(nP2);
							return str;
						}
					}
					return null;
				case ConstDefine.OP_LIKE:
					if(StringRegularOpt.isMatch(str1,str2)) return "1";
						return "0";
			}
			return null;
		}

		double dbop = Double.valueOf(str1);
		double dbop2 = Double.valueOf(str2);
		switch(optID){
		case ConstDefine.OP_OR:{
			boolean bop = StringRegularOpt.isTrue(str1), bop2 = StringRegularOpt.isTrue(str2);
			if(bop||bop2) return "1";
			return "0";
		}
		case ConstDefine.OP_ADD:
			return String.format("%f",dbop+dbop2);
		case ConstDefine.OP_SUB:
			return String.format("%f",dbop-dbop2);
		case ConstDefine.OP_MUL:
			return String.format("%f",dbop*dbop2);
		case ConstDefine.OP_DIV:
		{
			if(BigDecimal.valueOf(dbop2).compareTo(BigDecimal.ZERO)==0)
				return "0";
			return String.format("%f",dbop/dbop2);
		}
		case ConstDefine.OP_EQ:
		{
			if(BigDecimal.valueOf(dbop).compareTo(BigDecimal.valueOf(dbop2))==0) 
				return "1";
			return "0";
		}	
		case ConstDefine.OP_BG:
			if( dbop>dbop2) return "1";
			return "0";
		case ConstDefine.OP_LT:
			if(dbop<dbop2) return "1";
			return "0";
		case ConstDefine.OP_EL:
			if(dbop<=dbop2) return "1";
			return "0";
		case ConstDefine.OP_EB:			
			if(dbop>=dbop2) 
				return "1";
			return "0";
		case ConstDefine.OP_NE:
		{
			if(BigDecimal.valueOf(dbop).compareTo(BigDecimal.valueOf(dbop2))!=0) 
				return "1";
			return "0";
		}
		case ConstDefine.OP_POWER:
			return String.format("%f",Math.pow(dbop,dbop2));
		case ConstDefine.OP_LMOV:{
			int nP= Double.valueOf(dbop).intValue(); //int(dbop);
			int nP2 = Double.valueOf(dbop2).intValue();// int (dbop2);
			return String.format("%d",nP>>nP2);
					 }
		case ConstDefine.OP_RMOV:{
			int nP= Double.valueOf(dbop).intValue(); //int(dbop);
			int nP2 = Double.valueOf(dbop2).intValue();// int (dbop2);
			return String.format("%d",nP<<nP2);
					 }
		case ConstDefine.OP_LIKE:
			if(StringRegularOpt.isMatch(str1,str2)) return "1";
			return "0";
		}	

		return null;
	}
	
	private String getFormula()
	{
		List<String> slOperand = new ArrayList<String>();
		OptStack  optStack = new OptStack();
		String str;
		while(true){
			str = getItem();
			slOperand.add(0,str);
			if( str == null || str.length() == 0) 
				break;
			
			str = lex.getAWord();
			if( str==null || str.length()==0)
				break;
			
			int optID = getOptID(str);
			if( optID == -1){
				lex.setPreword(str);
				break;
			}
	//--------run OP_IN----------------------------------
			if(optID == ConstDefine.OP_IN){ // Specail Opt For In multi operand
				String sInRes = "0"; 
				String str1Operand = StringRegularOpt.trimString(slOperand.remove(0));
				str = lex.getAWord();
				if( str==null || str.length()==0 || !str.equals("(") ) return null;
				List<String> slInnerOperand = new ArrayList<String>();

				while(true)
				{
					str = getFormula();
					slInnerOperand.add(str);
					str = lex.getAWord();
					if( str==null || str.length()==0 ||(  !str.equals(",")  && !str.equals(")") ) ) return null;
					if( str.equals(")") )
						break;
				}
				for(int i=0; i< slInnerOperand.size(); i++){
					str = StringRegularOpt.trimString(slInnerOperand.get(i));
					if (str1Operand.equals(str)){
						sInRes ="1";
						break;
					}
				}

				slOperand.add(0,sInRes);
				str = lex.getAWord();
				optID = getOptID(str);
				if( optID == -1){
					lex.setPreword(str);
					break;
				}
			}
	//----------end opt in--------------------------------
			for(int op = optStack.pushOpt(optID); op != 0; op = optStack.pushOpt(optID)){
				String operand2 = slOperand.remove(0);
				String operand = slOperand.remove(0);
				str = runOperate(operand,operand2,op);
				slOperand.add(0,str);
			}
		}
		for(int op = optStack.popOpt(); op != 0; op = optStack.popOpt()){
			String operand2 = slOperand.remove(0);
			String operand = slOperand.remove(0);
			str = runOperate(operand,operand2,op);
			slOperand.add(0,str);
		}
		return  slOperand.get(0);
	}
	
	private String getFunc(int nFuncNo,String funcName)
	{
		String str = lex.getAWord();
		if( str==null || str.length()==0 || !str.equals("(") ) {
			if(str!=null && str.length()>0)
				lex.setPreword(str);
			return funcName;
		}
		int prmNo = 0;
	    String sRes = "";
	    // IF 语句单独处理
		if( EmbedFunc.functionsList[nFuncNo].nFuncID == ConstDefine.FUNC_IF){
			String sCondition = getFormula();
			if(sCondition==null) return null;
			
			str = lex.getAWord();
			if( str==null || str.length()==0 ||  !str.equals(",") ) return null;
			
			if( StringRegularOpt.isTrue(sCondition) ){
				sRes =  getFormula();
				str = lex.getAWord();
				if( str==null || str.length()==0 || ( !str.equals(",") && !str.equals(")")) ) return null;
				if( str.equals(")") ) return sRes;
				// 特殊处理的地方就在这儿
				lex.skipAOperand();
				str = lex.getAWord();
				if( str==null || str.length()==0 || !str.equals(")") ) return null;
				return sRes;
			}else {
				// 特殊处理的地方就在这儿
				lex.skipAOperand();
				str = lex.getAWord();
				if( str==null || str.length()==0 || !str.equals(",") && !str.equals(")") ) return null;
				if( str.equals(")") ) return sRes;
				sRes = getFormula();
				str = lex.getAWord();
				if( str==null || str.length()==0 || !str.equals(")") ) return null;
				return sRes;
			}
			//return sRes;
		}

		List<String> slOperand = new ArrayList<String>();

		while( true )
			//( m_sFunctionList[nFuncNo].nPrmSum == -1 
			//  || prmNo < m_sFunctionList[nFuncNo].nPrmSum )
		{
			prmNo ++;
			str = getFormula();
			slOperand.add(str);
			str = lex.getAWord();
			if( str==null || str.length()==0 || ( !str.equals(",") && !str.equals(")"))  )
				return null;
			if( str.equals(")") ){
				break;
			}
		}
		//str = m_lex.getAWord();
		if(/* str==null || str.length()==0 || */ !str.equals(")") ) return null;
		if( EmbedFunc.functionsList[nFuncNo].nPrmSum != -1 
			//&& prmNo != m_sFunctionList[nFuncNo].nPrmSum) return null;
			&& prmNo < EmbedFunc.functionsList[nFuncNo].nPrmSum) return null;
		str = runFunc(slOperand,EmbedFunc.functionsList[nFuncNo].nFuncID);
		return str;	
	}
	//不一致的问题 需要修改
	/**
	 * 计算表达式，使用 setVariableTranslate 中设置的变量转换器
	 * @param szExpress 表达式
	 * @return 计算结果
	 */
	public String calculate(String szExpress)
	{
		lex.setFormula(szExpress);
		String sRes = getFormula();
		if(sRes == null || sRes.length()==0) return "";
		return StringRegularOpt.trimString(sRes);
	}
	
	/**
	 * @param szExpress 表达式
	 * @param varMap 这个参数可以为null 如果为null表达式中的标识符和变量都会被替换为""这个空字符串
	 * @return 计算结果
	 */
	public String calculate(String szExpress,Map<String,Object> varMap) 
	{
		return calculate(szExpress,new MapTranslate(varMap));
	}
	
	public String calculate(String szExpress,VariableTranslate varTrans) 
	{
		return calculate(Pretreatment.runPretreatment(szExpress,varTrans));
	}
	
	// return the error point
	/**
	 * 
	 * @param szExpress 表达式
	 * @return 返回出错的位置，0 表示表达式格式检查通过
	 */
	public int checkFormula(String szExpress)
	{
		/*if(hasPreTreat)
			szExpress=preTreat.runPretreatment(szExpress);*/		
		int nNextType = 1;
		int nBrackets = 0;

		lex.setFormula(szExpress);
		String sWord;
		sWord =	lex.getAWord();
		while(!StringBaseOpt.isNvl(sWord)){
			boolean bKW = isKeyWord(sWord);
			if(nNextType == 1){
				if(bKW){
					if ("(".equals(sWord) )
						nBrackets ++;
					//else if (")".equals(sWord))
					//	nBrackets --;
					else if ((!sWord.equalsIgnoreCase("NOT")) 
							 && (! "!".equals(sWord))) // sWord!="!"
						return lex.getCurrPos()+1;
				}else nNextType = 0;
			}else{
				if(bKW){
					if (")".equals(sWord))
						nBrackets --;
					else if ("(".equals(sWord)){
						nBrackets ++;
						nNextType = 1;
					}
					else nNextType = 1;
				}else 
					return lex.getCurrPos()+1;
			}
			if(nBrackets<0)
				return lex.getCurrPos()+1;
			sWord =	lex.getAWord();
		}	
		if(nBrackets ==0)
			return 0;
		else
			return lex.getCurrPos()+1;
	}
}
