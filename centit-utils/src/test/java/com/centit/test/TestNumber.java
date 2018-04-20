package com.centit.test;

import com.centit.support.algorithm.ByteBaseOpt;

public class TestNumber {
    public static void main(String[] args) {
        byte [] buf = new byte[1024];
        Double data = 7568542412.123;
        float f = 23423.23423f;
        long l = 20180909121212123L;
        Integer i = 20180909;

        System.out.println(Integer.toBinaryString(i));
        //Integer.reverseBytes()
        ByteBaseOpt.writeInt32(buf,i,0);
        ByteBaseOpt.writeInt64(buf,l,4);
        ByteBaseOpt.writeFloat(buf,f,12);
        ByteBaseOpt.writeDouble(buf,data,16);
        ByteBaseOpt.writeString(buf,"hello world!",24);

        System.out.println(ByteBaseOpt.readInt32(buf,0));
        System.out.println(ByteBaseOpt.readInt64(buf,4));
        System.out.println(ByteBaseOpt.readFloat(buf,12));
        System.out.println(ByteBaseOpt.readDouble(buf,16));
        System.out.println(ByteBaseOpt.readString(buf,20 ,24));
    }
}
