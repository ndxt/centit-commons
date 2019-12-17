package com.centit.test;

import com.centit.support.network.UrlOptUtils;
import com.centit.support.security.Md5Encoder;
import com.centit.support.security.Sha1Encoder;

public class TestMD5 {

    public static void main(String[] args) {
        String base64Str = "HxUYHgIQJgwKHB0mDRYmChgAJgAcHA0mEAsWFxAaGBUVACYbDA0mFxYOJhANCiYbHBoWFBwmCRgLDSYWHyYUACYPFhoYGwwVGAsABA==";
        String baiduUrl = "https://www.ba%E7%A0%81&rsvf=8&rsv_bp=1&rsv_idx=2&ie=utf-8&rqlang=cn&tn=baiduhome_pg&rsv_enter=1&oq=linux%2520%25E6%2596%2587%25E4%25BB%25B6%25E4%25BC%25A0%25E8%25BE%2593&inputT=5376&rsv_t=ed53IX2wDletwdF1CgNmCkaxZyubycicseiMBiYvR3o2nc26RKYbvRqmDdPCH7Cpfj5T&rsv_pq=e655bc0500032ee7&rsv_sug3=13&rsv_sug1=12&rsv_sug7=100&sug=base64%2520%25E8%25A7%25A3%25E7%25A0%2581&rsv_n=1&rsv_sug2=0&rsv_sug4=5376";
        //System.out.println(new String(Hex.encodeHex(Base64.decodeBase64(base64Str))));
        System.out.println(UrlOptUtils.shortenCodeUrl(base64Str, 2));
        System.out.println(UrlOptUtils.shortenCodeUrl(baiduUrl, 2));
        System.out.println(UrlOptUtils.shortenCodeUrl(base64Str, 12));
        System.out.println(UrlOptUtils.shortenCodeUrl(baiduUrl, 12));
        System.out.println(UrlOptUtils.shortenCodeUrl(base64Str, 100));
        System.out.println(UrlOptUtils.shortenCodeUrl(baiduUrl, 100));

        System.out.println(Md5Encoder.encodePasswordAsJasigCas("78910", "123456", 1));
        System.out.println(Md5Encoder.encode("12345678910"));
        System.out.println(Sha1Encoder.encode("12345678910"));
    }
}
