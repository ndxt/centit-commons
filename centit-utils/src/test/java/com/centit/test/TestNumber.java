package com.centit.test;

import com.centit.support.algorithm.ByteBaseOpt;

public class TestNumber {
    public static void main(String[] args) {
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
