package com.centit.support.test.security;

import com.centit.support.security.AESSecurityUtils;
import com.centit.support.security.SM4Util;

public class TestSecurity {
    public static void main(String[] args) {
        System.out.println(SM4Util.encryptAsCBCType("hello world!",
            SM4Util.SM4_SECRET_KEY_SPEC, SM4Util.SM4_IV_PARAMETER_SPEC));
        //
        System.out.println(SM4Util.decryptAsCBCType("4QreoyoSGvdR7AF6odj62g==",
            SM4Util.SM4_SECRET_KEY_SPEC, SM4Util.SM4_IV_PARAMETER_SPEC));

        System.out.println(AESSecurityUtils.encryptAsCBCType("hello world!",
            SM4Util.SM4_SECRET_KEY_SPEC, SM4Util.SM4_IV_PARAMETER_SPEC));
        //
        System.out.println(AESSecurityUtils.decryptAsCBCType("WsbUSJEjQ+fcsn9D3QPLNg==",
            SM4Util.SM4_SECRET_KEY_SPEC, SM4Util.SM4_IV_PARAMETER_SPEC));
    }
}
//"shiduochangahaic",
