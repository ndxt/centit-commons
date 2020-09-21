package com.centit.test;

import com.centit.support.algorithm.*;
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

    public static void chengfakoujue() {
        for(int j=1;j<10;j++){
            for(int i=1;i<10;i++){
                System.out.print(NumberBaseOpt.capitalization(String.valueOf(i), true));
                System.out.print(NumberBaseOpt.capitalization(String.valueOf(j), true));
                if(i*j<10) {
                    System.out.print("得");
                }
                System.out.print(NumberBaseOpt.capitalizationCN(String.valueOf(i*j)));
                System.out.print("\t");
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        chengfakoujue();
    }

    public static void lastmain(String[] args) {
        long START_STMP = DatetimeOpt.createUtilDate(2020,1,1).getTime();//1480166465631L;
        System.out.println(START_STMP);
        //System.out.println(System.currentTimeMillis() - START_STMP);
        System.out.println(System.currentTimeMillis());
        System.out.println(System.currentTimeMillis() - START_STMP);
        long serial =  ((System.currentTimeMillis() - START_STMP)  * 10000)
            + Math.round( Math.random() * 10000);
        System.out.println(serial);
        for(int i=0;i<100;i++) {
            serial = ((System.currentTimeMillis() - START_STMP) * 10000)
                + Math.round(Math.random() * 10000);
            System.out.println(serial);
        }
    }


    public static void lastmain2(String[] args) {
        System.out.println(EnumBaseOpt.stringToEnum(DoubleAspect.class,"negative", true));

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
