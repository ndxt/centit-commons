package com.centit.test;

import com.centit.support.algorithm.ByteBaseOpt;
import com.centit.support.algorithm.DatetimeOpt;

import java.util.Date;

public class TestNumber {
    public static void main(String[] args) {
        byte [] buf = new byte[1024];

        Date now = DatetimeOpt.currentUtilDate();
        ByteBaseOpt.writeDateAsInt32(buf, now, 0);
        ByteBaseOpt.writeDatetimeAsInt64(buf, now, 4);
        ByteBaseOpt.writeTimestampAsInt64(buf, now, 12);

        System.out.println(now);
        System.out.println(ByteBaseOpt.readDateAsInt32(buf,  0));
        System.out.println(ByteBaseOpt.readDatetimeAsInt64(buf,  4));
        System.out.println(ByteBaseOpt.readTimestampAsInt64(buf,  12));

        System.out.println(ByteBaseOpt.readDateAsInt32(buf,  0).getTime());
        System.out.println(ByteBaseOpt.readDatetimeAsInt64(buf,  4).getTime());
        System.out.println(ByteBaseOpt.readTimestampAsInt64(buf,  12).getTime());
    }
}
