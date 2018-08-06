package com.centit.test;

import com.centit.support.algorithm.ByteBaseOpt;
import com.centit.support.algorithm.NumberBaseOpt;

public class TestNumber {

    public static String rmbDX(String f){
        return NumberBaseOpt.capitalization(
                        (f.indexOf('.')>=0? f.substring(0,f.indexOf('.')):f)) + "元"
        + NumberBaseOpt.capitalization(
                String.valueOf(NumberBaseOpt.getNumByte(f,-1)))+"角"
        + NumberBaseOpt.capitalization(
                String.valueOf(NumberBaseOpt.getNumByte(f,-2)))+"分";
    }

    public static void main(String[] args) {
        System.out.println(rmbDX("123.45"));
        System.out.println(rmbDX("3456700000000089123.45"));
    }

    public static void testByte() {
        byte [] buf = new byte[1024];

        short s = (short) 62342;
        int n = s & 0xFFFF;
        System.out.println(n);
        ByteBaseOpt.writeUInt16(buf, s, 0);

        System.out.println(s);

        System.out.println(ByteBaseOpt.readUInt16(buf,  0));
        System.out.println(ByteBaseOpt.readInt16(buf,  0));
    }
}
