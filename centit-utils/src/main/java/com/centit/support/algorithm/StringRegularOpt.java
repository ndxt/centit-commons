package com.centit.support.algorithm;

import java.util.Calendar;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Contract;

@SuppressWarnings("unused")
public abstract class StringRegularOpt {

	private StringRegularOpt() {
		throw new IllegalAccessError("Utility class");
	}

	public static String  trimString(String szWord)
	{
		if(szWord==null)
			return "";
		String strWord = szWord.trim();
		
		int	sl = strWord.length();
		if(sl >= 2 && (( strWord.charAt(0)=='\"' && strWord.charAt(sl-1)=='\"') || 
				   ( strWord.charAt(0)=='\'' && strWord.charAt(sl-1)=='\'')) ){
			if(sl>2)
				strWord = strWord.substring(1,sl-1);
			else
				strWord = "";
		}
		return strWord;
	}
	
		
	public static String  quotedString(String szWord)
	{
		if(szWord==null)
			return "\"\"";
		return "\"" + StringUtils.replace(szWord.trim(), "\"", "'") + "\"";
	}
	
	public static boolean isDigit(String pszNum){
		return StringUtils.isNumeric(pszNum);		
	}
	
	public static boolean isNumber(String pszNum)
	{
		String szNum = trimString(pszNum);
		int sl = szNum.length();
		if (sl<1) return false;
		int sp=0;
		while(sp<sl && (szNum.charAt(sp)==' ' ||  szNum.charAt(sp)== 9)) sp++;
		if(sp<sl && (szNum.charAt(sp)=='-' || szNum.charAt(sp)=='+')) sp++;
		if( sp==sl || (sp+1==sl && szNum.charAt(sp)=='.')) return false;//
		while(sp<sl){
			if(szNum.charAt(sp)>='0' &&  szNum.charAt(sp)<= '9'){
				sp++;
				continue;
			}
			if(szNum.charAt(sp) == '.') {
				sp++;
				break;
			}
			return false;
		}

		//if ((sp==sl) && (sl>1) && (szNum.charAt(sp-1)!='.') && (szNum.charAt(0)=='0')) return false;;

		while(sp<sl){
			if(szNum.charAt(sp)>='0' &&  szNum.charAt(sp)<= '9'){
				sp++;
			}else
				return false;
		}	
		return true;
	}
	
	public static boolean isString(String szWord)
	{
		if(szWord==null)
			return false;		
		String strWord = szWord.trim();
		int	sl = strWord.length();
		return (sl >= 2 && (( strWord.charAt(0)=='\"' && strWord.charAt(sl-1)=='\"') || 
				   ( strWord.charAt(0)=='\'' && strWord.charAt(sl-1)=='\'')) );
	}
	
	/**
	 * 判断字符串是否为空(null || ""),是：true,否：false
	 * 和StringUtils中的isBlank等价
	 * @param str str
	 * @return boolean 字符串是否为空
	 */
	public static boolean isNvl(String str) {
		return (str == null) || "".equals(str.trim());
	}

    public static boolean isTrue(String str )
	{
		/**
		 * //SONAR 检查不通过，但是这个是对的，式作为常量的加速判断
		 */
		if (str == "true") {
			return true;
		}
		if (str == null) {
			return false;
		}
		switch (str.length()) {
			case 1: {
				final char ch0 = str.charAt(0);
				if (ch0 == 'y' || ch0 == 'Y' ||
						ch0 == 't' || ch0 == 'T' || ch0 == '1') {
					return true;
				}
				break;
			}
			case 2: {
				final char ch0 = str.charAt(0);
				final char ch1 = str.charAt(1);
				if ((ch0 == 'o' || ch0 == 'O') &&
						(ch1 == 'n' || ch1 == 'N') ) {
					return true;
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
				break;
			}
			case 4: {
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
				break;
			}
			default:
                if(! isNumber(str)) return false;
                long bRes =  Math.round(Double.valueOf(str));
                return bRes!=0;
		}

		return false;
	}

	public static boolean isFalse(String str )
	{
		/**
		 * //SONAR 检查不通过，但是这个是对的，式作为常量的加速判断
		 */
		if (str == "false") {
			return true;
		}
		if (str == null) {
			return false;
		}
		switch (str.length()) {
			case 1: {
				final char ch0 = str.charAt(0);
				if (ch0 == 'n' || ch0 == 'N' ||
						ch0 == 'f' || ch0 == 'F' || ch0 == '0') {
					return true;
				}
				break;
			}
			case 2: {
				final char ch0 = str.charAt(0);
				final char ch1 = str.charAt(1);
				if ((ch0 == 'n' || ch0 == 'N') &&
						(ch1 == 'o' || ch1 == 'O') ) {
					return true;
				}
				break;
			}
			case 3: {
				final char ch0 = str.charAt(0);
				final char ch1 = str.charAt(1);
				final char ch2 = str.charAt(2);
				if ((ch0 == 'o' || ch0 == 'O') &&
						(ch1 == 'f' || ch1 == 'F') &&
						(ch2 == 'f' || ch2 == 'F') ) {
					return true;
				}
				break;
			}
			case 5: {
				final char ch0 = str.charAt(0);
				final char ch1 = str.charAt(1);
				final char ch2 = str.charAt(2);
				final char ch3 = str.charAt(3);
				final char ch4 = str.charAt(4);
				if ((ch0 == 'f' || ch0 == 'F') &&
						(ch1 == 'a' || ch1 == 'A') &&
						(ch2 == 'l' || ch2 == 'L') &&
						(ch3 == 's' || ch3 == 'S') &&
						(ch4 == 'e' || ch4 == 'E') ) {
					return true;
				}
				break;
			}
			default:
                if(! isNumber(str)) return false;
                long bRes =  Math.round(Double.valueOf(str));
                return bRes==0;
		}
		return false;
	}

	public static boolean isDatetime(String szTime ,Calendar  t_time)
	{
		if(szTime==null)
			return false;
		//t_time.setTime(time)
		int sl = szTime.length();
		int sp=0;
		int s=0;
		String c="";
		int y,m,d,h,min,sec;
		while(sp<sl && (szTime.charAt(sp)<'0' || szTime.charAt(sp)> '9')) sp++;
		while(sp<sl ){
			if(szTime.charAt(sp)>='0' &&  szTime.charAt(sp)<= '9'){
				if(s<4) 
					c += szTime.charAt(sp);
				sp++;s++;
				continue;
			}
			//if(szTime[sp] == '-') 
				sp++; 
			break;
		}//check year		
		y = Integer.valueOf(c);
		if(y < 1970 || y>2038) return false;
		
		while(sp<sl && (szTime.charAt(sp)<'0' || szTime.charAt(sp)> '9')) sp++;
		s=0; c="";
		while(sp<sl ){
			if(szTime.charAt(sp)>='0' &&  szTime.charAt(sp)<= '9'){
				if(s<2)
					c += szTime.charAt(sp);
				sp++;s++;
				continue;
			}
			//if(szTime.charAt(sp) == '-') 
			sp++; 
			break;
		}//check month
		if(s > 2) return false;
		m = Integer.valueOf(c);
		if(m<1 || m >12) return false;
		
		while(sp<sl && (szTime.charAt(sp)<'0' || szTime.charAt(sp)> '9')) sp++;
		s=0; c="";
		while(sp<sl ){
			if(szTime.charAt(sp)>='0' &&  szTime.charAt(sp)<= '9'){
				if(s<2) c += szTime.charAt(sp);
				sp++;s++;
				continue;
			}
			//if(szTime.charAt(sp) == '-') 
				sp++; 
			break;
		}//check day
		if(s > 2) return false;
		d = Integer.valueOf(c);
		if(d<1 || d >31) return false;
		
		while(sp<sl && (szTime.charAt(sp)<'0' || szTime.charAt(sp)> '9')) sp++;
		s=0; c="";
		while(sp<sl ){
			if(szTime.charAt(sp)>='0' &&  szTime.charAt(sp)<= '9'){
				if(s<2) c += szTime.charAt(sp);
				sp++;s++;
				continue;
			}
			//if(szTime.charAt(sp) == ':') 
				sp++; 
			break;
		}//check hour
		if(s > 2) return false;
		h = Integer.valueOf(c);
		if(h<0 || h >24) return false;
		
		while(sp<sl && (szTime.charAt(sp)<'0' || szTime.charAt(sp)> '9')) sp++;
		s=0; c="";
		while(sp<sl ){
			if(szTime.charAt(sp)>='0' &&  szTime.charAt(sp)<= '9'){
				if(s<2) c += szTime.charAt(sp);
				sp++;s++;
				continue;
			}
			//if(szTime.charAt(sp) == ':') 
				sp++; 
			break;
		}//check minute
		if(s > 2) return false;
		min = Integer.valueOf(c);
		if(min<0 || min >60) return false;
		s=0;  c="";
		while(sp<sl && (szTime.charAt(sp)<'0' || szTime.charAt(sp)> '9')) sp++;
		while(sp<sl ){
			if(szTime.charAt(sp)>='0' &&  szTime.charAt(sp)<= '9'){
				if(s<2) c += szTime.charAt(sp);
				sp++;s++;
				continue;
			}
			break;
		}//check second
		if(s > 2) return false;
		sec = Integer.valueOf(c);
		if(sec<0 || sec >60) return false;
		t_time.set(y,m,d,h,min,sec);
		return true;
	}
	
	public static boolean isDatetime(String szTime ){
		Calendar t_time = Calendar.getInstance();
		boolean b = isDatetime(szTime ,t_time);
		return b;
	}
	
	public static boolean isDate(String szTime ,Calendar  t_time)
	{
		if(szTime==null)
			return false;
		//t_time.setTime(time)
		int sl = szTime.length();
		int sp=0;
		int s=0;
		String c="";
		int y,m,d;
		while(sp<sl && (szTime.charAt(sp)<'0' || szTime.charAt(sp)> '9')) sp++;
		while(sp<sl ){
			if(szTime.charAt(sp)>='0' &&  szTime.charAt(sp)<= '9'){
				if(s<4) 
					c += szTime.charAt(sp);
				sp++;s++;
				continue;
			}
			//if(szTime[sp] == '-') 
				sp++; 
			break;
		}//check year		
		y = Integer.valueOf(c);
		if(y < 1970 || y>2038) return false;
		
		while(sp<sl && (szTime.charAt(sp)<'0' || szTime.charAt(sp)> '9')) sp++;
		s=0; c="";
		while(sp<sl ){
			if(szTime.charAt(sp)>='0' &&  szTime.charAt(sp)<= '9'){
				if(s<2)
					c += szTime.charAt(sp);
				sp++;s++;
				continue;
			}
			//if(szTime.charAt(sp) == '-') 
			sp++; 
			break;
		}//check month
		if(s > 2) return false;
		m = Integer.valueOf(c);
		if(m<1 || m >12) return false;
		
		while(sp<sl && (szTime.charAt(sp)<'0' || szTime.charAt(sp)> '9')) sp++;
		s=0; c="";
		while(sp<sl ){
			if(szTime.charAt(sp)>='0' &&  szTime.charAt(sp)<= '9'){
				if(s<2) c += szTime.charAt(sp);
				sp++;s++;
				continue;
			}
			//if(szTime.charAt(sp) == '-') 
				sp++; 
			break;
		}//check day
		if(s > 2) return false;
		d = Integer.valueOf(c);
		if(d<1 || d >31) return false;
		
		t_time.set(y,m,d,0,0,0);	
		return true;
	}

	public static boolean isDate(String szTime ){
		Calendar t_time = Calendar.getInstance();
		boolean b = isDate(szTime ,t_time);
		return b;
	}
	
	public static boolean isTime(String szTime ,Calendar   t_time)
	{
		if(szTime==null)
			return false;
		//t_time.setTime(time)
		int sl = szTime.length();
		int sp=0;
		int s=0;
		String c="";
		int h,min,sec;
		
		while(sp<sl && (szTime.charAt(sp)<'0' || szTime.charAt(sp)> '9')) sp++;
		s=0; c="";
		while(sp<sl ){
			if(szTime.charAt(sp)>='0' &&  szTime.charAt(sp)<= '9'){
				if(s<2) c += szTime.charAt(sp);
				sp++;s++;
				continue;
			}
			//if(szTime.charAt(sp) == ':') 
				sp++; 
			break;
		}//check hour
		if(s > 2) return false;
		h = Integer.valueOf(c);
		if(h<0 || h >24) return false;
		
		while(sp<sl && (szTime.charAt(sp)<'0' || szTime.charAt(sp)> '9')) sp++;
		s=0; c="";
		while(sp<sl ){
			if(szTime.charAt(sp)>='0' &&  szTime.charAt(sp)<= '9'){
				if(s<2) c += szTime.charAt(sp);
				sp++;s++;
				continue;
			}
			//if(szTime.charAt(sp) == ':') 
				sp++; 
			break;
		}//check minute
		if(s > 2) return false;
		min = Integer.valueOf(c);
		if(min<0 || min >60) return false;
		s=0;  c="";
		while(sp<sl && (szTime.charAt(sp)<'0' || szTime.charAt(sp)> '9')) sp++;
		while(sp<sl ){
			if(szTime.charAt(sp)>='0' &&  szTime.charAt(sp)<= '9'){
				if(s<2) c += szTime.charAt(sp);
				sp++;s++;
				continue;
			}
			break;
		}//check second
		if(s > 2) return false;
		sec = Integer.valueOf(c);
		if(sec<0 || sec >60) return false;
		t_time.set(2010,10,10,h,min,sec);
		return true;
	}

	public static boolean isTime(String szTime ){
		Calendar t_time = Calendar.getInstance();
		boolean b = isTime(szTime ,t_time);
		return b;
	}
	
	public static String trimDateString(String szDateStr)
	{
		if(szDateStr==null)
			return null;
		int sl = szDateStr.length();
		StringBuilder sB = new StringBuilder();
		String sTmp2 ="";
		int nPart = 0;
		boolean bDot = false;
		for( int j=0; j< sl; j++){
			if (szDateStr.charAt(j) >= '0' && szDateStr.charAt(j) <= '9'){
				if (bDot){
					if(!"".equals(sTmp2)){
						
						if(nPart>0 && nPart<3)
							sB.append('-'); 
						else if(nPart==3)
							sB.append(' ');
						else if(nPart>3 && nPart<6)
							sB.append(':');
						
						nPart ++;
						
						if(sTmp2.length() == 1){
							sB.append('0');
						}
						
						if(sTmp2.length()>0)
							sB.append(sTmp2);
						
						sTmp2 ="";
						
						
					}
					bDot = false;
				}
				if(nPart>=6)
					break;
				sTmp2 = sTmp2 + szDateStr.charAt(j);
			}else{ //if(! sTmp2.equals(""))
				bDot = true;				
			}
			
		}
		
		if(!"".equals(sTmp2)){
			
			if(nPart>0 && nPart<3)
				sB.append('-'); 
			else if(nPart==3)
				sB.append(' ');
			else if(nPart>3 && nPart<6)
				sB.append(':');
			
			if(sTmp2.length() == 1){
				sB.append('0');
			}
			
			if(sTmp2.length()>0)
				sB.append(sTmp2);
		
		}
		
		return sB.toString();
	}
	
	public static String trimDigits(String szDigits)
	{
		if(szDigits==null)
			return null;
		int sl = szDigits.length();
		StringBuilder sTmp2 = new StringBuilder("");
		for( int j=0; j< sl; j++){
			if (szDigits.charAt(j) >= '0' && szDigits.charAt(j) <= '9')
				sTmp2.append(szDigits.charAt(j));
		}
		return sTmp2.toString();
	}
	
	
	
	public static String trimNumber(String szNumber)
	{
		if(szNumber==null)
			return null;
		int sp=0;
		int sl = szNumber.length();
		StringBuilder sTmp2 = new StringBuilder("");
		for( int j=0; j< sl; j++){
			if (szNumber.charAt(j) >= '0' && szNumber.charAt(j) <= '9')
				sTmp2.append(szNumber.charAt(j));
			else if(sp==0 && szNumber.charAt(j) == '.'){
				sTmp2.append(szNumber.charAt(j));
				sp=1;
			}
		}
		return sTmp2.toString();
	}
	
	public static String sqlMatchToRegex(String sTempl){
		return "^"+sTempl.replaceAll("%", "\\\\S*").replaceAll("_", "\\\\S")+"$";
	}
	
	/*{
			if(szValue==null || szTempl==null)
				return false;	
			int nLV = szValue.length();
			int nLT = szTempl.length();
			if(nLV == 0){
				for(int i=0;i<nLT;i++)
					if(szTempl.charAt(i)!='*')
						return  false;
				return true;
			}
			int i =0,j=0;
			while(i<nLV && j<nLT){
				if( (szTempl.charAt(j) != '*' && szValue.charAt(i) == szTempl.charAt(j)) ||
					szTempl.charAt(j) == '?'){
					i++;j++;
				}else if(szTempl.charAt(j) == '*'){
					if(szTempl.charAt(j+1) == '*'){
						if (szValue.charAt(i) == '*'){
							i++; j+=2;
						}else return false;
					} else {
						j++;
						while(j<nLT && (szTempl.charAt(j)=='?' )){
							j++;
							i++;
						}
						int nStart = j;
						if(nStart == nLT) return true;
						while(j<nLT && szTempl.charAt(j)!='?' && szTempl.charAt(j)!='*') j++;
						int nSubStr = j-nStart;
	
						for(int k=i;k<=nLV-nSubStr;k++){
							boolean bMatchSub = true;
							for(int l=nStart;l<j;l++)
								if(szValue.charAt(k+l-nStart) != szTempl.charAt(l)){
									bMatchSub = false;
									break;
								}
							if(bMatchSub && innerMatch(szValue+k+nSubStr ,szTempl+j)) return true;
						}
						return false;
					}
				}else
					return false;
			}
			return (i==nLV && j==nLT);
		}
	*/
	public static boolean  isMatch(String szValue , String szTempl)
	{// ?_  *% 是通配符
		if( szValue == null || szTempl == null) return false;
		if( szValue.equals(szTempl)) return true;
		int nLV = szValue.length();
		int nLT = szTempl.length();
		if(nLV == 0 && nLT == 0) return true;

		szValue = trimString(szValue);
		szTempl = trimString(szTempl);
		
		//return Pattern.matches(sqlMatchToRegex(szTempl), szValue);//
		return Pattern.compile(sqlMatchToRegex(szTempl)).matcher(szValue).find();
	}

	/**
	 * 判断是否为中文
	 * @param c 中文
	 * @return 判断是否为中文
	 */
	public static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {
            return true;
        }
        return false;
    }
	/**
	 * 判断是否为中文，剔除标点符号
	 * @param c 中文
	 * @return 判断是否为中文
	 */
	public static boolean isChineseEscapeSymbol(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS 
        		|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A 
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                //|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION 
                //|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                //|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
                ) {
            return true;
        }
        return false;
    }
	/**
	 * 获得首个中文位置 
	 * @param strName strName
	 * @return -1 没有中文
	 */
    public static int getFirstChinesePos(String strName) {
        char[] ch = strName.toCharArray();
        for (int i = 0; i < ch.length; i++) {
            char c = ch[i];
            if (isChinese(c)) {
                return i;
            }
        }
        return -1;
    }
    
    /**
	 * 获得首个中文位置 ，标点符号不算
	 * @param strName strName
	 * @return -1 没有中文
	 */
    public static int getFirstChinesePosEscapeSymbol(String strName) {
        char[] ch = strName.toCharArray();
        for (int i = 0; i < ch.length; i++) {
            char c = ch[i];
            if (isChineseEscapeSymbol(c)) {
                return i;
            }
        }
        return -1;
    }
	
}
