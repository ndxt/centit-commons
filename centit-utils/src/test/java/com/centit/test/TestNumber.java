package com.centit.test;

import com.centit.support.algorithm.ByteBaseOpt;
import com.centit.support.algorithm.EnumBaseOpt;
import com.centit.support.algorithm.NumberBaseOpt;
import com.centit.support.algorithm.StringRegularOpt;
import com.centit.support.common.DoubleAspect;

import java.math.BigDecimal;

public class TestNumber {

    public static String rmbDX(String f) {
        return NumberBaseOpt.capitalization(
            (f.indexOf('.') >= 0 ? f.substring(0, f.indexOf('.')) : f)) + "元"
            + NumberBaseOpt.capitalization(
            String.valueOf(NumberBaseOpt.getNumByte(f, -1))) + "角"
            + NumberBaseOpt.capitalization(
            String.valueOf(NumberBaseOpt.getNumByte(f, -2))) + "分";
    }


    public static void main(String[] args) {
        DoubleAspect yes = DoubleAspect.YES;
        DoubleAspect on = DoubleAspect.ON;
        System.out.println(DoubleAspect.class.isPrimitive());
        System.out.println(EnumBaseOpt.ordinalToEnum(DoubleAspect.class, 5));
        System.out.println(yes);
        System.out.println(EnumBaseOpt.enumToOrdinal(yes));
        System.out.println(yes.equals(on));
        System.out.println(yes.sameAspect(on));
        System.out.println(yes.matchAspect(on));

        System.out.println(BigDecimal.valueOf(1000, -2));
        String cs = "chinese我是中文";
        System.out.println(cs.length());
        System.out.println(cs.substring(6, 10));
        System.out.println(StringRegularOpt.isDoubleByteChar(cs.charAt(7)));
        //System.out.println(rmbDX("123.45"));
        //System.out.println(rmbDX("3456700000000089123.45"));
    }

    public static void testByte() {
        byte[] buf = new byte[1024];

        short s = (short) 62342;
        int n = s & 0xFFFF;
        System.out.println(n);
        ByteBaseOpt.writeUInt16(buf, s, 0);

        System.out.println(s);

        System.out.println(ByteBaseOpt.readUInt16(buf, 0));
        System.out.println(ByteBaseOpt.readInt16(buf, 0));
    }
}
