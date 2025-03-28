package com.centit.support.test.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.centit.support.algorithm.Snowflake;
import com.centit.support.security.Md5Encoder;

import java.io.UnsupportedEncodingException;

public class TestMD5 {

    public static void main(String[] args) throws UnsupportedEncodingException {

        // 1392c730239402b38926b7c3c9944202
        // 1392c730239402b38926b7c3c9944202
        System.out.println(Md5Encoder.encode("Wilbur6038f2e6b04e4ca155aa46b4aa76c7f05824"));
        Snowflake sf = new Snowflake();
        System.out.println(sf.nextId());
        System.out.println(sf.nextId());
        System.out.println(sf.nextId());
        System.out.println(sf.nextIdStr());
        System.out.println(sf.nextIdHex());
        System.out.println(sf.nextIdHex());
        System.out.println(sf.nextIdHex());
        System.out.println(sf.nextIdBase64());
        System.out.println(sf.nextIdBase64());
        System.out.println(sf.nextIdBase64());
        System.out.println(sf.nextIdBase64());

//        System.out.println(HmacSha1Encoder.encode("hello world", "nihao"));
//        String base64Str = "HxUYHgIQJgwKHB0mDRYmChgAJgAcHA0mEAsWFxAaGBUVACYbDA0mFxYOJhANCiYbHBoWFBwmCRgLDSYWHyYUACYPFhoYGwwVGAsABA==";
//        String baiduUrl = "https://www.ba%E7%A0%81&rsvf=8&rsv_bp=1&rsv_idx=2&ie=utf-8&rqlang=cn&tn=baiduhome_pg&rsv_enter=1&oq=linux%2520%25E6%2596%2587%25E4%25BB%25B6%25E4%25BC%25A0%25E8%25BE%2593&inputT=5376&rsv_t=ed53IX2wDletwdF1CgNmCkaxZyubycicseiMBiYvR3o2nc26RKYbvRqmDdPCH7Cpfj5T&rsv_pq=e655bc0500032ee7&rsv_sug3=13&rsv_sug1=12&rsv_sug7=100&sug=base64%2520%25E8%25A7%25A3%25E7%25A0%2581&rsv_n=1&rsv_sug2=0&rsv_sug4=5376";
        //System.out.println(new String(Hex.encodeHex(Base64.decodeBase64(base64Str))));
//        System.out.println(UrlOptUtils.shortenCodeUrl(base64Str, 2));
//        System.out.println(UrlOptUtils.shortenCodeUrl(baiduUrl, 2));
//        System.out.println(UrlOptUtils.shortenCodeUrl(base64Str, 12));
//        System.out.println(UrlOptUtils.shortenCodeUrl(baiduUrl, 12));
//        System.out.println(UrlOptUtils.shortenCodeUrl(base64Str, 100));
//        System.out.println(UrlOptUtils.shortenCodeUrl(baiduUrl, 100));
//
//        System.out.println(Md5Encoder.encodePasswordAsJasigCas("78910", "123456", 1));
//        System.out.println(Md5Encoder.encode("12345678910"));
//        System.out.println(Sha1Encoder.encode("12345678910"));
//        System.out.println(new String(Base64.getDecoder().decode("YjZjY2JkYTgtMTk1ZC00NmMyLTllYTAtYTNlNjI4YzZlMTU4")));
        JSONArray jsonArray = JSON.parseArray("[{'1':'2'},{'2':'3'}]");
        jsonArray.forEach(System.out::println);
 //       System.out.println(jsonArray);
    }
}
