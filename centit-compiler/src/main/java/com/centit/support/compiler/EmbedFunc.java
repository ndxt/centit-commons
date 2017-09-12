package com.centit.support.compiler;

import java.util.Date;
import java.util.List;

import com.centit.support.algorithm.DatetimeOpt;
import com.centit.support.algorithm.ListOpt;
import com.centit.support.algorithm.NumberBaseOpt;
import com.centit.support.algorithm.StringBaseOpt;
import com.centit.support.algorithm.StringRegularOpt;

public abstract class EmbedFunc {
	private EmbedFunc() {
		throw new IllegalAccessError("Utility class");
	}

	final static public int functionsSum = 46;
	final static protected FunctionInfo functionsList[]={
		new FunctionInfo("ave",-1, ConstDefine.FUNC_AVE, ConstDefine.TYPE_NUM),	//求均值  ave (1,2,3)=2
		new FunctionInfo("getat",-1, ConstDefine.FUNC_GET_AT, ConstDefine.TYPE_ANY),//求数组中的一个值  getat (0,"2","3")= "2"  getat (0,2,3)= 2
		new FunctionInfo("byte",2, ConstDefine.FUNC_BYTE,ConstDefine.TYPE_NUM),	//求位值  byte (4321.789,2)=2  
										//		  byte (4321.789,-2)=8  
										//		  byte ("4321.789",2)=3
		new FunctionInfo("capital",1,ConstDefine.FUNC_CAPITAL,ConstDefine.TYPE_STR),  // capital (123.45)="一百二十三点四五"
		new FunctionInfo("if",3,ConstDefine.FUNC_IF,ConstDefine.TYPE_STR),      // if (1,2,3)= 2  if (0,"2","3")= "3"
		new FunctionInfo("case",2,ConstDefine.FUNC_CASE,ConstDefine.TYPE_STR),      // if (1,2,3)= 2  if o(0,"2","3")= "3"
		new FunctionInfo("match",2,ConstDefine.FUNC_MATCH,ConstDefine.TYPE_NUM), //匹配*?为通配符 match ("abcd","a??d")=1
											//             match ("abcd","a*d")=1 
		new FunctionInfo("max",-1,ConstDefine.FUNC_MAX,ConstDefine.TYPE_NUM),   // 求最大值 max (1,2,3,5,4) = 5
		new FunctionInfo("min",-1,ConstDefine.FUNC_MIN,ConstDefine.TYPE_NUM),	// 求最小值 min (1,2,3,5,4) = 1
		
		new FunctionInfo("count",-1,ConstDefine.FUNC_COUNT,ConstDefine.TYPE_NUM),	// 计数 count(1,"2",3,"5",1,1,4) = 7
		new FunctionInfo("countnotnull",-1,ConstDefine.FUNC_COUNTNOTNULL,ConstDefine.TYPE_NUM),	// 计数 非空参数 countnotnull(1,,"2",,,,1,1,4) = 5
		new FunctionInfo("countnull",-1,ConstDefine.FUNC_COUNTNULL,ConstDefine.TYPE_NUM),	// 计数孔参数  countnull(1,,"2",,,,1,1,4) = 4
		
		new FunctionInfo("round",1,ConstDefine.FUNC_ROUND,ConstDefine.TYPE_NUM),	// 四舍五入
		new FunctionInfo("concat",-1,ConstDefine.FUNC_STRCAT,ConstDefine.TYPE_STR),	// 连接字符串 concat ("12","34","56")="123456"
		new FunctionInfo("strcat",-1,ConstDefine.FUNC_STRCAT,ConstDefine.TYPE_STR),	// 连接字符串 strcat ("12","34","56")="123456"
		new FunctionInfo("isempty",1,ConstDefine.FUNC_ISEMPTY,ConstDefine.TYPE_NUM),	// 判断参数是否为空 isempty("")=1
		new FunctionInfo("isnotempty",1,ConstDefine.FUNC_NOTEMPTY,ConstDefine.TYPE_NUM),	// 判断参数是否为空 notempty("")=0
		
		new FunctionInfo("sum",-1,ConstDefine.FUNC_SUM,ConstDefine.TYPE_NUM),	// 求和 sum (1,2,3,4,5) = 15
		new FunctionInfo("stddev",-1,ConstDefine.FUNC_STDDEV,ConstDefine.TYPE_NUM),	// 求标准偏差
		
		new FunctionInfo("log",1,ConstDefine.FUNC_LOG,ConstDefine.TYPE_NUM),	// 求以10为底的对数
		new FunctionInfo("ln",1,ConstDefine.FUNC_LN,ConstDefine.TYPE_NUM),		// 求自然对数
		new FunctionInfo("sin",1,ConstDefine.FUNC_SIN,ConstDefine.TYPE_NUM),	// 求正弦
		new FunctionInfo("cos",1,ConstDefine.FUNC_COS,ConstDefine.TYPE_NUM),	// 求余弦
		new FunctionInfo("tan",1,ConstDefine.FUNC_TAN,ConstDefine.TYPE_NUM),	// 求正切
		new FunctionInfo("ctan",1,ConstDefine.FUNC_CTAN,ConstDefine.TYPE_NUM),	// 求正切
		new FunctionInfo("exp",1,ConstDefine.FUNC_EXP,ConstDefine.TYPE_NUM),	// 求以e为底的指数
		new FunctionInfo("sqrt",1,ConstDefine.FUNC_SQRT,ConstDefine.TYPE_NUM),	// 求平方根
		new FunctionInfo("upcase",1,ConstDefine.FUNC_UPCASE,ConstDefine.TYPE_STR), // 字符串大写
		new FunctionInfo("lowcase",1,ConstDefine.FUNC_LOWCASE,ConstDefine.TYPE_STR), // 字符串小写
		new FunctionInfo("substr",2,ConstDefine.FUNC_SUBSTR,ConstDefine.TYPE_STR), // 求字符串子串 substr ("123456",2,3)="345"
		new FunctionInfo("find",2,ConstDefine.FUNC_FIND,ConstDefine.TYPE_NUM),  //求子串位置 find ("123456","34")=2  find ("123456","35")=-1
		new FunctionInfo("frequence",2,ConstDefine.FUNC_FREQUENCE,ConstDefine.TYPE_NUM), // 求子串个数 find ("12345236","23")=2 
		new FunctionInfo("int",1,ConstDefine.FUNC_INT,ConstDefine.TYPE_NUM), // 求整数部分 int (12.34)=12 int -12.34)=-12 
		new FunctionInfo("integer",1,ConstDefine.FUNC_INT,ConstDefine.TYPE_NUM), // 求整数部分 integer (12.34)=12 int (-12.34)=-12 
		new FunctionInfo("frac",1,ConstDefine.FUNC_FRAC,ConstDefine.TYPE_NUM), // 求小数部分 frac (12.34)=0.34 frac (-12.34)=-0.34
		
		new FunctionInfo("today",-1,ConstDefine.FUNC_TODAY,ConstDefine.TYPE_STR),//当前日期  
		new FunctionInfo("day",-1,ConstDefine.FUNC_DAY,ConstDefine.TYPE_STR),//日期函数  
		new FunctionInfo("month",-1,ConstDefine.FUNC_MONTH,ConstDefine.TYPE_STR),//日期函数   
		new FunctionInfo("year",-1,ConstDefine.FUNC_YEAR,ConstDefine.TYPE_STR),//日期函数  

		new FunctionInfo("dayspan",-1,ConstDefine.FUNC_DAY_SPAN,ConstDefine.TYPE_NUM),//日期函数  求两日期之间的天数
		//new FunctionInfo("monthspan",-1,ConstDefine.FUNC_MONTH_SPAN,ConstDefine.TYPE_NUM),//日期函数   求两日期之间的月数
		//new FunctionInfo("yearspan",-1,ConstDefine.FUNC_YEAR_SPAN,ConstDefine.TYPE_NUM),//日期函数   求两日期之间的年数

		new FunctionInfo("adddays",2,ConstDefine.FUNC_ADD_DAYS,ConstDefine.TYPE_STR),//日期函数  加天数
		new FunctionInfo("addmonths",2,ConstDefine.FUNC_ADD_MONTHS,ConstDefine.TYPE_STR),//日期函数  加月数
		new FunctionInfo("addyears",2,ConstDefine.FUNC_ADD_YEARS,ConstDefine.TYPE_STR),//日期函数   加年数
		new FunctionInfo("truncday",-1,ConstDefine.FUNC_TRUNC_DAY,ConstDefine.TYPE_STR),//日期函数   截断日期  第二个参数  Y ，M , D 分别返回一年、月的第一天 ，或者一日的零点
		new FunctionInfo("firstofmonth",-1,ConstDefine.FUNC_FIRST_OF_MONTH,ConstDefine.TYPE_STR),//日期函数   求这个月的第一天
		
		//new FunctionInfo("getsysstr",1,ConstDefine.FUNC_GET_STR,ConstDefine.TYPE_STR),//取系统字符串
		new FunctionInfo("getpy",1,ConstDefine.FUNC_GET_PY,ConstDefine.TYPE_STR)//取汉字拼音
	};
	
	final static public int	getFuncNo(String sFuncName)
	{
		for(int i=0; i<functionsSum; i++)
			if( sFuncName.equalsIgnoreCase(functionsList[i].sName)) return i;
		return -1;
	}
	

	final static public String runFunc(List<String> slOperand,int funcID)
	{
		int nOpSum = 0;

		if( slOperand != null )
			nOpSum = slOperand.size();
		String str = "",tempstr;
		double dbtemp = 0.0;
		switch(funcID){
		
		case ConstDefine.FUNC_AVE :// 100
			for(int i=0; i<nOpSum; i++){
				if(StringRegularOpt.isNumber(slOperand.get(i) )) 
					dbtemp += Double.valueOf(StringRegularOpt.trimString(slOperand.get(i)));
			}
			if (nOpSum > 0) 
				str = String.format("%f",dbtemp/nOpSum);//"%f",
			else
				str = "0";
			return str;
		case ConstDefine.FUNC_GET_AT://148
			if (nOpSum < 2)
				return "";
			tempstr = slOperand.get(0);
			if( StringRegularOpt.isNumber(tempstr)){
				int nbit = Integer.valueOf(tempstr);
				if(nbit < nOpSum -1){
					return slOperand.get(nbit+1);
				}
			}
			return "";
		case ConstDefine.FUNC_BYTE:// 101
			if(nOpSum<2 || ! StringRegularOpt.isNumber(slOperand.get(1)) )
				return "";
			tempstr = slOperand.get(0);
			int nbit = Integer.valueOf(slOperand.get(1));			
			if( StringRegularOpt.isNumber(tempstr)){
				str = String.valueOf(NumberBaseOpt.getNumByte(tempstr,nbit));
			}else{
				int sl = tempstr.length();
				if( nbit >=0 && nbit < sl) 
					str = String.valueOf(tempstr.charAt(nbit));
				else 
					str = "";
			}
			return str;

		case ConstDefine.FUNC_MATCH:
			if(nOpSum<2) return "0";
			if(StringRegularOpt.isMatch(slOperand.get(0),slOperand.get(1))) return "1";
			return "0";

		case ConstDefine.FUNC_CAPITAL:// 102
			{
				if (nOpSum <1) return ""; 
				boolean nT = false;
				if(nOpSum > 1)
					nT = StringRegularOpt.isTrue(slOperand.get(1));
				if( !StringRegularOpt.isNumber(slOperand.get(0)) ) return "";
				str = NumberBaseOpt.capitalization(slOperand.get(0),nT);
			}
			return str;
		case ConstDefine.FUNC_MAX:{// 103
			if (nOpSum <1) return "";
			List<String> trimOperand = ListOpt.removeNullItem(slOperand);
			if(trimOperand==null||trimOperand.size()<1)
				return "";
			boolean hasNotNumber=false;
			dbtemp = -1;
			if(StringRegularOpt.isNumber(trimOperand.get(0)))
				dbtemp = Double.valueOf(StringRegularOpt.trimString(trimOperand.get(0)));
			else
				hasNotNumber = true;
			
			for(int i=1; i<trimOperand.size(); i++){
				if(StringRegularOpt.isNumber(trimOperand.get(i))){
					double tMidF = Double.valueOf(
							StringRegularOpt.trimString(trimOperand.get(i)));
					if (dbtemp < tMidF)
						dbtemp = tMidF;
				}else
					hasNotNumber  = true;
			}
			if(! hasNotNumber)
				return String.valueOf(dbtemp);
			
			String strRet = trimOperand.get(0);
			for(int i=1; i<trimOperand.size(); i++){
				if(strRet.compareTo(trimOperand.get(i))<0)
					strRet=trimOperand.get(i);
			}		
			return strRet;
		}
		case ConstDefine.FUNC_MIN:{// 104
			
			if (nOpSum <1) return "";
			List<String> trimOperand = ListOpt.removeNullItem(slOperand);
			if(trimOperand==null||trimOperand.size()<1)
				return "";
			dbtemp = -1;
			boolean hasNotNumber=false;
			
			if(StringRegularOpt.isNumber(trimOperand.get(0)))
				dbtemp = Double.valueOf(StringRegularOpt.trimString(trimOperand.get(0)));
			else
				hasNotNumber = true;
			for(int i=1; i<trimOperand.size(); i++){
				if(StringRegularOpt.isNumber(trimOperand.get(i))){
					double tMidF = Double.valueOf(StringRegularOpt.trimString(trimOperand.get(i)));
					if (dbtemp > tMidF)
						dbtemp = tMidF;
				}else
					hasNotNumber = true;
			}
			
			if(! hasNotNumber)
				return String.valueOf(dbtemp);

			String strRet = trimOperand.get(0);
			for(int i=1; i<trimOperand.size(); i++){
				if(strRet.compareTo(trimOperand.get(i))>0)
					strRet=trimOperand.get(i);
			}
			return strRet;
		}
		case ConstDefine.FUNC_COUNT:// 112
			return String.valueOf(nOpSum);		
			
		case ConstDefine.FUNC_COUNTNOTNULL:// 145
			{
				if(nOpSum==0)			
					return "0";
				int nc=0;
				for(String s:slOperand){
					if(s!=null && s.length()>0 &&
							!"''".equals(s)  && !"\"\"".equals(s))
						nc++;
				}
				return String.valueOf(nc);
			}
		case ConstDefine.FUNC_COUNTNULL:// 144
			{
				if(nOpSum==0)			
					return "0";
				int nc=0;
				for(String s:slOperand){
					if(s==null || s.length()==0 ||
							"''".equals(s) || "\"\"".equals(s))
						nc++;
				}
				return String.valueOf(nc);
			}
		case ConstDefine.FUNC_SUM:// 105	
			for(int i=0; i<nOpSum; i++)
				if(StringRegularOpt.isNumber(slOperand.get(i)))
					dbtemp += Double.valueOf(StringRegularOpt.trimString(slOperand.get(i)));

			return String.valueOf(dbtemp);

		case ConstDefine.FUNC_STDDEV:// 133	
			{

				if(nOpSum<2) return  "0";
				for(int i=0; i<nOpSum; i++)
					if(StringRegularOpt.isNumber(slOperand.get(i)))
						dbtemp += Double.valueOf(StringRegularOpt.trimString(slOperand.get(i)));
				
				double dbAvg = dbtemp/nOpSum;
				dbtemp = 0.0;
				for(int i=0; i<nOpSum; i++)
					if(StringRegularOpt.isNumber(slOperand.get(i))){
						double dtp = Double.valueOf(StringRegularOpt.trimString(slOperand.get(i)))
								- dbAvg;
						dbtemp += dtp*dtp;
					}

				dbtemp = Math.sqrt(dbtemp/(nOpSum-1));
				str = String.format("%f",dbtemp);
				return str;
			}
		case ConstDefine.FUNC_STRCAT:// 106
			{
				if (nOpSum <1) return "";
				StringBuilder sb= new StringBuilder("\"");
				
				for(int i=0; i<nOpSum; i++)
					sb.append(StringRegularOpt.trimString(slOperand.get(i)));

				sb.append('\"');
				return sb.toString();
			}
		case ConstDefine.FUNC_SUBSTR:{
			if(nOpSum<2 ) 
				return slOperand.get(0);
			if(slOperand.get(0)==null)
				return "";
			int nStart=0,nLength;
			if( StringRegularOpt.isNumber(slOperand.get(1) ))
				nStart = Integer.valueOf(StringRegularOpt.trimString(slOperand.get(1)));
			
			if(nOpSum>2 && StringRegularOpt.isNumber(slOperand.get(2)))
				nLength = Integer.valueOf(StringRegularOpt.trimString(slOperand.get(2)));
			else 
				nLength = slOperand.get(0).length();

			if(nLength<=0) nLength = 1;
				
			tempstr = slOperand.get(0).substring(nStart,nStart + nLength);
			str  = '"' +tempstr + '"';
			return str;
					 }

		case ConstDefine.FUNC_FIND:{ //index
				if (nOpSum < 2) return "-1";
				int nStart = 0;
				if(nOpSum>2 && StringRegularOpt.isNumber(slOperand.get(2)))
					nStart = Integer.valueOf(StringRegularOpt.trimString(slOperand.get(2)));
				tempstr = StringRegularOpt.trimString(slOperand.get(0));
				str = StringRegularOpt.trimString(slOperand.get(1));
				int nS = tempstr.indexOf(str,nStart); 
				return String.format("%d",nS);
					 }
		
		
		
		case ConstDefine.FUNC_UPCASE://upcase
		{
			if (nOpSum <1) return "";
			return slOperand.get(0).toUpperCase();
		}
		case ConstDefine.FUNC_LOWCASE://lowcase
		{
			if (nOpSum <1)
				return "";
			return slOperand.get(0).toLowerCase();
		}
		case ConstDefine.FUNC_FREQUENCE:{
			if (nOpSum < 2) return "-1";
			if(slOperand.get(0)==null)
				return "0";
			tempstr = StringRegularOpt.trimString(slOperand.get(0));
			str = StringRegularOpt.trimString(slOperand.get(1));
			
			int nSt = 0,sl=str.length(),nC=0;
			nSt = tempstr.indexOf(str,nSt);
			while(nSt>=0){
				nC++;
				nSt+=sl;
				nSt = tempstr.indexOf(str,nSt);
			}
			return String.valueOf(nC);
							}
		case ConstDefine.FUNC_ROUND:{
			if (nOpSum <1) return "";
			if ( StringRegularOpt.isNumber(slOperand.get(0)) ) return slOperand.get(0);
			int nS = 0;
			if ( nOpSum > 1 && StringRegularOpt.isNumber(slOperand.get(1)) )
				nS = Double.valueOf(StringRegularOpt.trimString(slOperand.get(1))).intValue();
			if(nS<0) nS = 0;
			return String.format("%."+String.valueOf(nS)+"f",slOperand.get(1));
						}

		case ConstDefine.FUNC_ISEMPTY: //判断参数是否为空
			if (nOpSum <1) 
				return "1";

			if (StringBaseOpt.isNvl(slOperand.get(0))
					|| "''".equals(slOperand.get(0))|| "\"\"".equals(slOperand.get(0)))
				return "1";
			return "0";		
			
		case ConstDefine.FUNC_NOTEMPTY: //判断参数是否为空
			if (nOpSum <1) 
				return "0";

			if (StringBaseOpt.isNvl(slOperand.get(0))
					|| "''".equals(slOperand.get(0))|| "\"\"".equals(slOperand.get(0)))
				return "0";
			return "1";					
			
		case ConstDefine.FUNC_LN:
			{
				if (nOpSum <1) return "";
				if(!StringRegularOpt.isNumber(slOperand.get(0))) return "";
				double af = Double.valueOf(StringRegularOpt.trimString(slOperand.get(0)));
				af = Math.log(af);
				return String.valueOf(af);
			}
		case ConstDefine.FUNC_LOG:
			{
				if (nOpSum <1) return "";
				if(!StringRegularOpt.isNumber(slOperand.get(0))) return "";
				double af = Double.valueOf(StringRegularOpt.trimString(slOperand.get(0)));
				af = Math.log10(af);
				return String.valueOf(af);
			}
		case ConstDefine.FUNC_SIN://sin
			{
				if (nOpSum <1) return "";
				if(!StringRegularOpt.isNumber(slOperand.get(0))) return "";
				double af = Double.valueOf(StringRegularOpt.trimString(slOperand.get(0)));
				af = Math.sin(af);
				return String.valueOf(af);
			}
		case ConstDefine.FUNC_COS://cos
			{
				if (nOpSum <1) return "";
				if(!StringRegularOpt.isNumber(slOperand.get(0))) return "";
				double af = Double.valueOf(StringRegularOpt.trimString(slOperand.get(0)));
				af = Math.cos(af);
				return String.valueOf(af);
			}
		case ConstDefine.FUNC_TAN://tan
			{
				if (nOpSum <1) return "";
				if(!StringRegularOpt.isNumber(slOperand.get(0))) return "";
				double af = Double.valueOf(StringRegularOpt.trimString(slOperand.get(0)));
				af = Math.tan(af);
				return String.valueOf(af);
			}
		case ConstDefine.FUNC_CTAN://ctan
			{
				if (nOpSum <1) return "";
				if(!StringRegularOpt.isNumber(slOperand.get(0))) return "";
				double af = Double.valueOf(StringRegularOpt.trimString(slOperand.get(0)));
				af = Math.atan(af);
				return String.valueOf(af);
			}
		case ConstDefine.FUNC_INT://取整
			{
				if (nOpSum <1) return "";
				if(!StringRegularOpt.isNumber(slOperand.get(0))) return "";
				int nN = Double.valueOf(StringRegularOpt.trimString(slOperand.get(0))).intValue();
				return String.valueOf(nN);
			}
		case ConstDefine.FUNC_FRAC://取小数
			{
				if (nOpSum <1) return "";
				if(!StringRegularOpt.isNumber(slOperand.get(0))) return "";
				double af = Double.valueOf(StringRegularOpt.trimString(slOperand.get(0)));
				int nN = Double.valueOf(StringRegularOpt.trimString(slOperand.get(0))).intValue();
				return String.valueOf(af - nN);
			}

		case ConstDefine.FUNC_EXP:
			{
				if (nOpSum <1) return "";
				if(!StringRegularOpt.isNumber(slOperand.get(0))) return "";
				double af = Double.valueOf(StringRegularOpt.trimString(slOperand.get(0)));
				af = Math.exp(af);
				return String.valueOf(af);

			}
		case ConstDefine.FUNC_SQRT:
			{
				if (nOpSum <1) return "";
				if(!StringRegularOpt.isNumber(slOperand.get(0))) return "";
				double af = Double.valueOf(StringRegularOpt.trimString(slOperand.get(0)));
				af = Math.sqrt(af);
				return String.valueOf(af);
			}

		case ConstDefine.FUNC_IF:
			{// 108
				if (nOpSum < 2) return "";
				String str1="";
				if(nOpSum>2) 
					str1 = slOperand.get(2);				
				if(StringRegularOpt.isTrue(slOperand.get(0)) )
					 return slOperand.get(1);
				return str1;
			}

		case ConstDefine.FUNC_CASE:// 116
			{
				if (nOpSum < 2) return "";
				tempstr = StringRegularOpt.trimString( slOperand.get(0));
				int MatchType = 0;
				if(tempstr.equalsIgnoreCase("true") )
					MatchType = 1;
				else{
					if(StringRegularOpt.isNumber(tempstr)){
						dbtemp = Double.valueOf(tempstr);
						MatchType = 2;
					}
				}
				int i=1;
				for( ;i+1<nOpSum; i+=2){
					if( MatchType ==1 ){
						if (StringRegularOpt.isTrue(slOperand.get(i)))
							return slOperand.get(i+1);
					}else if ( MatchType == 2 ){
						if (StringRegularOpt.isNumber(slOperand.get(i))){
							if( Math.abs( dbtemp - Double.valueOf(
									StringRegularOpt.trimString(slOperand.get(i)))) < 0.0001)
								return slOperand.get(i+1);
						}
					}else {
						if ( tempstr.equals(StringRegularOpt.trimString(slOperand.get(i))) )
							return slOperand.get(i+1);
					}
				}
				if( nOpSum % 2 == 0 ) 
					return slOperand.get(nOpSum-1);
				return "";
			}
		case ConstDefine.FUNC_TODAY:// 
		{
			return DatetimeOpt.convertDateToString(DatetimeOpt.currentUtilDate());
		}
		
		case ConstDefine.FUNC_DAY:// 
		{
			Date dt = null;
			if (nOpSum > 0){
				tempstr = StringRegularOpt.trimString( slOperand.get(0));
				dt = DatetimeOpt.smartPraseDate(tempstr);
			}
			if(dt==null)
				dt = DatetimeOpt.currentUtilDate();
			return String.valueOf(DatetimeOpt.getDay(dt));
		}			
		case ConstDefine.FUNC_MONTH:// 
		{
			Date dt = null;
			if (nOpSum > 0){
				tempstr = StringRegularOpt.trimString( slOperand.get(0));
				dt = DatetimeOpt.smartPraseDate(tempstr);
			}
			if(dt==null)
				dt = DatetimeOpt.currentUtilDate();
			return String.valueOf(DatetimeOpt.getMonth(dt));
		}
		case ConstDefine.FUNC_YEAR:// 
		{
			Date dt = null;
			if (nOpSum > 0){
				tempstr = StringRegularOpt.trimString( slOperand.get(0));
				dt = DatetimeOpt.smartPraseDate(tempstr);
			}
			if(dt==null)
				dt = DatetimeOpt.currentUtilDate();
			return String.valueOf(DatetimeOpt.getYear(dt));
		}
		case ConstDefine.FUNC_DAY_SPAN:// 
		{
			if (nOpSum < 2) return "";
			str = StringRegularOpt.trimString( slOperand.get(0));
			tempstr = StringRegularOpt.trimString( slOperand.get(1));
			Date dt = DatetimeOpt.smartPraseDate(str);
			Date dt2 = DatetimeOpt.smartPraseDate(tempstr);
			
			if(dt==null || dt2==null)
				return "";
			return String.valueOf(DatetimeOpt.calcSpanDays(dt, dt2));
		}
		
		case ConstDefine.FUNC_ADD_DAYS:// 
		{
			if (nOpSum < 2) return "";
			str = StringRegularOpt.trimString( slOperand.get(0));
			tempstr = StringRegularOpt.trimString( slOperand.get(1));
			Date dt = DatetimeOpt.smartPraseDate(str);
			if(dt==null )
				return "";
			if (!StringRegularOpt.isNumber(tempstr))
				return DatetimeOpt.convertDateToString(dt);
		
			int ti = Double.valueOf(StringRegularOpt.trimString(tempstr)).intValue();

			return DatetimeOpt.convertDateToString(DatetimeOpt.addDays(dt, ti));
		}
		case ConstDefine.FUNC_ADD_MONTHS:// 
		{
			if (nOpSum < 2) return "";
			str = StringRegularOpt.trimString( slOperand.get(0));
			tempstr = StringRegularOpt.trimString( slOperand.get(1));
			Date dt = DatetimeOpt.smartPraseDate(str);
			if(dt==null )
				return "";
			if (!StringRegularOpt.isNumber(tempstr))
				return DatetimeOpt.convertDateToString(dt);
		
			int ti = Double.valueOf(tempstr).intValue();

			return DatetimeOpt.convertDateToString(DatetimeOpt.addMonths(dt, ti));
		}
		
		case ConstDefine.FUNC_ADD_YEARS:// 
		{
			if (nOpSum < 2) return "";
			str = StringRegularOpt.trimString( slOperand.get(0));
			tempstr = StringRegularOpt.trimString( slOperand.get(1));
			Date dt = DatetimeOpt.smartPraseDate(str);
			if(dt==null )
				return "";
			if (!StringRegularOpt.isNumber(tempstr))
				return DatetimeOpt.convertDateToString(dt);
		
			int ti = Double.valueOf(tempstr).intValue();

			return DatetimeOpt.convertDateToString(DatetimeOpt.addYears(dt, ti));
		}
		
		case ConstDefine.FUNC_TRUNC_DAY:// 
		{
			if (nOpSum < 1) return "";
			str = StringRegularOpt.trimString( slOperand.get(0));
			Date dt = DatetimeOpt.smartPraseDate(str);
			if(dt==null )
				return "";
			tempstr = "D";
			if(nOpSum>1){
				tempstr = StringRegularOpt.trimString( slOperand.get(1));
			}
			if("M".equalsIgnoreCase(tempstr))
				dt = DatetimeOpt.truncateToMonth(dt);
			else if("Y".equalsIgnoreCase(tempstr))
				dt = DatetimeOpt.truncateToYear(dt);
			else
				dt = DatetimeOpt.truncateToDay(dt);
			
			return DatetimeOpt.convertDateToString(dt);
		}
		
		case ConstDefine.FUNC_FIRST_OF_MONTH:// 
		{
			Date dt = null;
			if (nOpSum > 0){ 
				tempstr = StringRegularOpt.trimString( slOperand.get(0));
				dt = DatetimeOpt.smartPraseDate(tempstr);
			}
			if(dt==null )
				dt = DatetimeOpt.currentUtilDate();
			
			dt = DatetimeOpt.truncateToMonth(dt);
			return DatetimeOpt.convertDateToString(dt);
		}
		
		case ConstDefine.FUNC_GET_PY:// 
		{
			if (nOpSum < 1) return "";
			tempstr = StringRegularOpt.trimString( slOperand.get(0));
			return StringBaseOpt.getFirstLetter(tempstr);
		}
		
			/*s
			 * 
		new FunctionInfo("truncday",-1,ConstDefine.FUNC_TRUNC_DAY,ConstDefine.TYPE_STR),//日期函数   截断日期  第二个参数  Y ，M , D 分别返回一年、月的第一天 ，或者一日的零点
		new FunctionInfo("fisrtOfMonth",1,ConstDefine.FUNC_FIRST_OF_MONTH,ConstDefine.TYPE_STR),//日期函数   求这个月的第一天
		
		//new FunctionInfo("getsysstr",1,ConstDefine.FUNC_GET_STR,ConstDefine.TYPE_STR),//取系统字符串
		new FunctionInfo("getpy",1,ConstDefine.FUNC_GET_PY,ConstDefine.TYPE_STR)//取汉字拼音
		*/

		}
		return "";		
	}	
}
