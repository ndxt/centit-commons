package com.centit.support.algorithm;

@SuppressWarnings("unused")
public abstract class NumberBaseOpt {

	/*
	 * 获得某一位上的数值，如果 nBit<0 则获得小数点后面的位数
	 */
	static public char getNumByte(String szNum , int nBit)
	{
		int sl = szNum.length();
		int nPos = 0;
		while(nPos<sl && szNum.charAt(nPos) != '.' ) nPos ++ ;
		if(nBit<0)
			nPos = nPos - nBit;
		else
			nPos = nPos - nBit - 1;
		if( nPos < 0 || nPos >= sl) return '0';
		return szNum.charAt(nPos);
	}
	
	final static private String CNum[]={"零","壹","贰","叁","肆","伍","陆","柒","捌","玖"};
	final static private String CNum2[]={"〇","一","二","三","四","五","六","七","八","九"};
	final static private String CBit[]={"","拾","佰","仟"};
	      //拾佰仟万拾佰仟亿拾佰仟萬
	/*
	 * 将数值大写
	 */
	public static String capitalization(String szNum)
	{
		StringBuilder resstr = new StringBuilder();
		String tmpstr = szNum.trim();		
		int sl = tmpstr.length();
		int sp=0;
		int dotpos = tmpstr.indexOf('.');
		if(dotpos != -1){
			while(sl>1 && tmpstr.charAt(sl-1) == '0') sl--;
			if(tmpstr.charAt(sl-1)=='.') sl--;
			if(sl != tmpstr.length()){
				tmpstr = tmpstr.substring(0,sl);
			}
		}else dotpos = sl;
		if(sl<1) return CNum[0];
		if(tmpstr.charAt(0) == '-'){
			resstr.append("负");
			sp = 1;
		}
		String integerNum = tmpstr.substring(sp,dotpos-sp);
		String decimalNum ="";
		if(dotpos+1<sl) decimalNum = tmpstr.substring(dotpos+1);
		sl = integerNum.length();
		sp=0; while(sp<sl && integerNum.charAt(sp)=='0') sp++;
		if(sp > 0) integerNum = integerNum.substring(sp);
		int inl = integerNum.length();
		if(inl>0){
			int h = (inl-1) % 4 ;
			int j = (inl-1) / 4 + 1;
			sp=0;
			boolean allzero = false;
			boolean preallzero = false;
			for(;j>0;j--){
				int k=h;
				h = 3;
				boolean preiszero = allzero;
				allzero = true;
				for(;k>=0;k--,sp++){
					if(integerNum.charAt(sp) == '0')
						preiszero = true;
					else{
						allzero = false;
						if(preiszero)
							resstr.append("零");
						preiszero = false;
						resstr.append(CNum[(byte) (integerNum.charAt(sp)) - 48]).append(CBit[k]);
					}
				}// end for k
				if(/*j!=0 &&*/ j % 2 == 0 ){
					if(!allzero) 
						resstr.append("万");
				}
				else
				{
					if(!allzero || !preallzero){ 
						int repyi = j/2;
						for(int i=0; i<repyi; i++)
							resstr.append("亿");
					}
				}
				preallzero = allzero;
			}//end for j
		}else
			resstr.append("零");
		
		int dnl = decimalNum.length();
		if(dnl>0){
			resstr.append("点");
			for(int i=0; i<dnl; i++){
				resstr.append(CNum[(byte)(decimalNum.charAt(i))-48]);
			}
		}
		return resstr.toString();		
	}
	/*
	 * 仅仅是把 0~9 转换为 "〇","一","二","三","四","五","六","七","八","九"
	 */
	public static String uppercaseCN(String szNum)
	{
		StringBuilder resstr = new StringBuilder();
		String tmpstr = szNum.trim();		
		int sl = tmpstr.length();
		int sp=0;

		if(sl<1) return CNum2[0];
		for(;sp<sl;sp++)
			if(tmpstr.charAt(sp)>='0' && tmpstr.charAt(sp)<='9')
				resstr.append(CNum2[tmpstr.charAt(sp)-'0']);
			else
				resstr.append(tmpstr.charAt(sp));
		return resstr.toString();
	}


	public static String capitalization(String szNum,final boolean isSimple)
	{
		if (isSimple){
			return uppercaseCN(szNum);
		}		
		return capitalization(szNum);
	}
	/*
	 * 这个仅仅是对Long.parseLong进行简单的封装避免重复的输入try catch
	 */
	static public Long parseLong(String sNum, Long errorValue){
		Long lValue;
		try{
			lValue = Long.parseLong(sNum);
		}catch(NumberFormatException e){
			lValue = errorValue;
		}
		return lValue;
	}
	
	
	static public Long parseLong(String sNum){
		return parseLong(sNum,null);
	}
	
	/*
	 * 这个仅仅是对Integer.parseInteger进行简单的封装避免重复的输入try catch
	 */
	static public Integer parseInteger(String sNum, Integer errorValue){
		Integer lValue;
		try{
			lValue = Integer.parseInt(sNum);
		}catch(NumberFormatException e){
			lValue = errorValue;
		}
		return lValue;
	}

	
	
	/*
	 * 这个仅仅是对Integer.parseInteger进行简单的封装避免重复的输入try catch
	 */
	static public Integer parseInteger(String sNum){
		return parseInteger(sNum,null);
	}

	/*
	 * 这个仅仅是对Double.parseDouble进行简单的封装避免重复的输入try catch
	 */
    public static Double parseDouble(String sNum, Double errorValue){
		Double lValue;
		try{
			lValue = Double.parseDouble(sNum);
		}catch(NumberFormatException e){
			lValue = errorValue;
		}
		return lValue;
	}
	
	static public Double parseDouble(String sNum){
		return parseDouble(sNum,null);
	}
	
	/*
	 * 将一个Object转换为 long
	 */
	public static Long castObjectToLong(Object obj){
		if (obj == null)
			return null;
		if (obj instanceof Long)
			return (Long) obj;
		if (obj instanceof Double)
			return ((Double) obj).longValue();
		if (obj instanceof Integer)
			return ((Integer) obj).longValue();
		if (obj instanceof Float)
			return ((Float) obj).longValue();
		if (obj instanceof String)
			return parseLong((String)obj,null);
		if (obj instanceof Number)
			return ((Number) obj).longValue();
		return parseLong(StringBaseOpt.objectToString(obj),null);
	}
	
	public static Integer castObjectToInteger(Object obj){
		if (obj == null)
			return null;
		if (obj instanceof Integer)
			return (Integer) obj;
		if (obj instanceof Long)
			return ((Long) obj).intValue();
		if (obj instanceof Double)
			return ((Double) obj).intValue();
		if (obj instanceof Float)
			return ((Float) obj).intValue();
		if (obj instanceof String)
			return parseInteger((String)obj,null);
		if (obj instanceof Number)
			return ((Number) obj).intValue();
		return parseInteger(StringBaseOpt.objectToString(obj),null);
	}
	/*
	 * 将一个Object转换为 Double
	 */
	public static Double castObjectToDouble(Object obj){
		if (obj == null)
			return null;
		if (obj instanceof Double)
			return (Double) obj;
		if (obj instanceof Integer)
			return ((Integer) obj).doubleValue();
		if (obj instanceof Long)
			return ((Long) obj).doubleValue();
		if (obj instanceof Float)
			return ((Float) obj).doubleValue();
		if (obj instanceof String)
			return parseDouble((String)obj,null);
		if (obj instanceof Number)
			return ((Number) obj).doubleValue();
		return parseDouble(StringBaseOpt.objectToString(obj),null);
	}

	public int compareTwoLong(Long l1 , Long l2){
		return (l1 == null && l2 == null) ? 0:(
				l1 == null?-1:(
						l2 == null ? 1 :(
								Long.compare(l1,l2)
								)
						)
				);
	}

	public int compareTwoInteger(Integer i1 , Integer i2){
		return (i1 == null && i2 == null) ? 0:(
				i1 == null?-1:(
						i2 == null ? 1 :(
								Integer.compare(i1,i2)
						)
				)
		);
	}

	public int compareTwoDouble(Double d1 , Double d2){
		return (d1 == null && d2 == null) ? 0:(
				d1 == null?-1:(
						d2 == null ? 1 :(
								Double.compare(d1,d2)
						)
				)
		);
	}
}
