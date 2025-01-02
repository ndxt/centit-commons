package com.centit.support.test.utils;

import com.centit.support.security.SM2Util;

public class TestTypeUtils {

    public static void main(String[] args) throws Exception {
        //System.out.println(SecurityOptUtils.decodeSecurityString("cipher:lQqCRjjuOBIYmxtOLzDKEw=="));
        //System.out.println(BooleanBaseOpt.castObjectToBoolean("  ", true));
        //System.out.println(NumberBaseOpt.parseInteger("1", null));

        String skey = "MIIBzTCCAXCgAwIBAgIGAXKnMKNyMAwGCCqBHM9VAYN1BQAwSTELMAkGA1UEBhMC"+
            "Q04xDjAMBgNVBAoTBUdNU1NMMRAwDgYDVQQLEwdQS0kvU00yMRgwFgYDVQQDEw9S"+
            "b290Q0EgZm9yIFRlc3QwIhgPMjAxNTEyMzExNjAwMDBaGA8yMDM1MTIzMDE2MDAw"+
            "MFowSTELMAkGA1UEBhMCQ04xDjAMBgNVBAoTBUdNU1NMMRAwDgYDVQQLEwdQS0kv"+
            "U00yMRgwFgYDVQQDEw9Sb290Q0EgZm9yIFRlc3QwWTATBgcqhkjOPQIBBggqgRzP"+
            "VQGCLQNCAATj+apYlL+ddWXZ7+mFZXZJGbcJFXUN+Fszz6humeyWZP4qEEr2N0+a"+
            "Zdwo/21ft232yo0jPLzdscKB261zSQXSoz4wPDAZBgNVHQ4EEgQQnGnsD7oaOcWv"+
            "6CTrspwSBDAPBgNVHRMBAf8EBTADAQH/MA4GA1UdDwEB/wQEAwIAxjAMBggqgRzP"+
            "VQGDdQUAA0kAMEYCIQCEnW5BlQh0vmsOLxSoXYc/7zs++wWyFc1tnBHENR4ElwIh"+
            "AI1Lwu6in1ruflZhzseWulXwcITf3bm/Y5X1g1XFWQUH";
        System.out.println(skey.length());
        System.out.println(SM2Util.obtainPublicKey(skey));
    }
//e6p0XuHlF7Sqir5SNLY1PyQDc7RLEv4gV7ycl3RrcFQ/mDnOikETc1xKMDyho+YQJ/Q/pAPCfaOldc5EDiBsSG/neXLzE4wJLmvwDGnS7eTXAhS4dj64gIDRYMV7lTMRbhem6FruPXMvR06OqagP0KMRPDSMxhxhZVD1sJhYMc4W0nxklRupztAZ766Cra+Ve6p0XuHlF7Sqir5SNLY1PyQDc7RLEv4gV7ycl3RrcFQ/mDnOikETc1xKMDyho+YQJ/Q/pAPCfaOldc5EDiBsSG/neXLzE4wJLmvwDGnS7eTXAhS4dj64gIDRYMV7lTMRbhem6FruPXMvR06OqagP0KMRPDSMxhxhZVD1sJhYMc4W0nxklRupztAZ766Cra+Ve6p0XuHlF7Sqir5SNLY1PyQDc7RLEv4gV7ycl3RrcFQ/mDnOikETc1xKMDyho+YQJ/Q/pAPCfaOldc5EDiBsSG/neXLzE4wJLmvwDGnS7eTXAhS4dj64gIDRYMV7lTMRbhem6FruPXMvR06OqagP0KMRPDSMxhxhZVD1sJhYMc4W0nxklRupztAZ766Cra+V
}
