package com.centit.support.network;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public abstract class UrlOptUtils {
	private UrlOptUtils() {
		throw new IllegalAccessError("Utility class");
	}

	protected static final Logger logger = LoggerFactory.getLogger(UrlOptUtils.class);
	private static final String ALLOWED_CHARS = 
			"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_.!~*'()";

	public static final String getUrlParamter(String szUrl) {
		String sQuery;
		try {
			java.net.URL url = new java.net.URL(szUrl);
			sQuery = url.getQuery();
		} catch (MalformedURLException e) {
			int n = szUrl.indexOf('?');
			int nM = szUrl.lastIndexOf('#');
			if (nM > 0 && nM > n) {
				if (n > 0)
					sQuery = szUrl.substring(n + 1, nM);
				else
					sQuery = szUrl.substring(0, nM);
			} else if (n > 0)
				sQuery = szUrl.substring(n + 1);
			else
				sQuery = "";
		}
		return sQuery;
	}

	public static final Map<String, String> splitUrlParamter(
			String szUrlParameter) {
		Map<String, String> params = new HashMap<String, String>();
		int bpos = 0;
		while (true) {
			int n = szUrlParameter.indexOf('=', bpos);
			if (n < 0)
				break;
			String name = szUrlParameter.substring(bpos, n);
			int n2 = szUrlParameter.indexOf('&', n + 1);
			if (n2 < 0) {
				String value = szUrlParameter.substring(n + 1);
				try {
					value = java.net.URLDecoder.decode(value, "utf-8");
				} catch (UnsupportedEncodingException e) {
				}
				params.put(name, value);
				break;
			} else {
				String value = szUrlParameter.substring(n + 1, n2);
				try {
					value = java.net.URLDecoder.decode(value, "utf-8");
				} catch (UnsupportedEncodingException e) {
					logger.error(e.getMessage(),e);//e.printStackTrace();
				}
				params.put(name, value);
				bpos = n2 + 1;
			}
		}
		return params;
	}


	public static String encodeURIComponent(String input) {
		if (StringUtils.isEmpty(input)) {
			return input;
		}

		int l = input.length();
		StringBuilder o = new StringBuilder(l * 3);
		try {
			for (int i = 0; i < l; i++) {
				String e = input.substring(i, i + 1);
				if (ALLOWED_CHARS.indexOf(e) == -1) {
					byte[] b = e.getBytes("utf-8");
					o.append(getHex(b));
					continue;
				}
				o.append(e);
			}
			return o.toString();
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage(),e);//e.printStackTrace();
		}
		return input;
	}

	private static String getHex(byte buf[]) {
		StringBuilder o = new StringBuilder(buf.length * 3);
		for (int i = 0; i < buf.length; i++) {
			int n = (int) buf[i] & 0xff;
			o.append("%");
			if (n < 0x10) {
				o.append("0");
			}
			o.append(Long.toString(n, 16).toUpperCase());
		}
		return o.toString();
	}
	
	/*
	 * 获取域名
	 */
	public static String getUrlDomain(String curl){
		try{
			return new URL(curl).getHost();
		}catch(Exception e){
			logger.error(e.getMessage(),e);//e.printStackTrace();
			return null;
		}
	}
}
