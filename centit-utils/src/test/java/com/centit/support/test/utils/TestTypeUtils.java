package com.centit.support.test.utils;

import com.centit.support.security.SM2Util;

public class TestTypeUtils {

    public static void main(String[] args) throws Exception {
        //System.out.println(SecurityOptUtils.decodeSecurityString("cipher:lQqCRjjuOBIYmxtOLzDKEw=="));
        //System.out.println(BooleanBaseOpt.castObjectToBoolean("  ", true));
        //System.out.println(NumberBaseOpt.parseInteger("1", null));

        String skey = "MIICtTCCAlqgAwIBAgIIICMGIAMHGAgwCgYIKoEcz1UBg3UwbzELMAkGA1UEBhMCQ04xEDAOBgNVBAgMB1NIQUFOWEkxDTALBgNVBAcMBFhJQU4xKzApBgNVBAoMIlNoYWFueGlEaWdpdGFsQ2VydGlmaWNhdGVBdXRob3JpdHkxEjAQBgNVBAMMCVNoYWFueGlDQTAeFw0yMzA2MjAwMjU1NDlaFw0yNTA2MjYwMjU0NDVaMHMxCzAJBgNVBAYTAkNOMUswSQYDVQQKDEI5MTYxMDEzMU1BNlRYNzVNNTLpmZXopb/np6blvqHlpKnkuIvkv6Hmga/mioDmnK/mnInpmZDotKPku7vlhazlj7gxFzAVBgNVBAMMDjYxMDExMzU0NTUwOTkwMFkwEwYHKoZIzj0CAQYIKoEcz1UBgi0DQgAEGGXx+Y1/a0UGYoi7N09iEz8jMNpI7M5QhOk4cOxV71V/+XSVDBxcmrna9IOdJLu0GnExQm8Uqog1jBO+eeUZVKOB2zCB2DAfBgNVHSMEGDAWgBQcKBgXb7Dm9vVofDTlNs9qnmrCPTAdBgNVHQ4EFgQUKMhb+8ZG+hp+1MwlCR3IOszciuowDgYDVR0PAQH/BAQDAgbAMEkGA1UdHwRCMEAwPqA8oDqGOGh0dHA6Ly8xMTcuMzIuMTMyLjc0OjM2OTAxL0NBMjAxNTA4MDcwMDAwMDE0MmNybC9jcmwuY3JsMAkGA1UdEwQCMAAwFgYDVR0gBA8wDTALBgkqgRyHhCIEAQowGAYFKlYLBwsEDwwNNTAwNDI5OTEyMDk3MTAKBggqgRzPVQGDdQNJADBGAiEAzm2mPB23rgMgPVBOwxhyhBqUiiHW6C3M9NQkh9H7L7oCIQDU5Cf0jgaQNw92fI61YDpHw1znG15dCPXA+to8qoby1Q==";

        System.out.println(SM2Util.obtainPublicKey(skey));
    }
}
