package com.centit.test;

import com.centit.support.security.Md5Encoder;
import com.centit.support.security.Sha1Encoder;

public class TestMD5 {

	public static void main(String[] args) {
		System.out.println(Md5Encoder.encodePasswordAsJasigCas("78910","123456",1));
		System.out.println(Md5Encoder.encode("12345678910"));
		
		System.out.println(Sha1Encoder.encode("12345678910"));
	}
}
