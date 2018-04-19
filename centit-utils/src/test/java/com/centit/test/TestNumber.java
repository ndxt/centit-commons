package com.centit.test;

public class TestNumber {
    public static void main(String[] args) {
        Double data = 7568542412.123;
        //Integer.reverseBytes()
        System.out.println(Double.toHexString(data));
        long longDate = Long.valueOf(Double.toHexString(data),16);
        System.out.println(Double.toHexString(longDate));
    }
}
