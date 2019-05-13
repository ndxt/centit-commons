package com.centit.test;

import com.centit.support.network.UrlOptUtils;
import com.centit.support.security.Md5Encoder;

/**
 * 网上找来的代码有严重缺陷
 */
public class ShortUrlGenerator {
    public static void main(String[] args) {
        // 长连接： http://tech.sina.com.cn/i/2011-03-23/11285321288.shtml
        // 新浪解析后的短链接为： http://t.cn/h1jGSC
        String [] sLongUrl  = { "http://tech.sina.com.cn/i/2011-03-23/11285321288.shtml",
                "HxUYHgIQJgwKHB0mDRYmChgAJgAcHA0mEAsWFxAaGBUVACYbDA0mFxYOJhANCiYbHBoWFBwmCRgLDSYWHyYUACYPFhoYGwwVGAsABA==",
                "https://www.ba%E7%A0%81&rsvf=8&rsv_bp=1&rsv_idx=2&ie=utf-8&rqlang=cn&tn=baiduhome_pg&rsv_enter=1&oq=linux%2520%25E6%2596%2587%25E4%25BB%25B6%25E4%25BC%25A0%25E8%25BE%2593&inputT=5376&rsv_t=ed53IX2wDletwdF1CgNmCkaxZyubycicseiMBiYvR3o2nc26RKYbvRqmDdPCH7Cpfj5T&rsv_pq=e655bc0500032ee7&rsv_sug3=13&rsv_sug1=12&rsv_sug7=100&sug=base64%2520%25E8%25A7%25A3%25E7%25A0%2581&rsv_n=1&rsv_sug2=0&rsv_sug4=5376"} ;
        // 3BD768E58042156E54626860E241E999
        for (int i = 0; i < sLongUrl.length; i++) {
            System.out.println("[" + i + "] : " + UrlOptUtils.shortenCodeUrl(sLongUrl[i],8));
        }

        for (int i = 0; i < sLongUrl.length; i++) {
            System.out.println("[" + i + "] : " + UrlOptUtils.shortenCodeUrl(sLongUrl[i],30));
        }

        for(int j=0;j<3; j++) {
            String[] aResult = shortUrl(sLongUrl[j]);
            // 打印出结果
            for (int i = 0; i < aResult.length; i++) {
                System.out.println("[" + i + "]:::" + aResult[i]);
            }
        }
    }


    public static String[] shortUrl(String url) {
        // 可以自定义生成 MD5 加密字符传前的混合 KEY
        String key = "wuguowei" ;
        // 要使用生成 URL 的字符
        String[] chars = new String[] { "a" , "b" , "#" , "#" , "e" , "f" , "#" , "#" ,
                "i" , "j" , "#" , "#" , "m" , "n" , "#" , "#" , "q" , "r" , "#" , "#" ,
                "u" , "v" , "#" , "#" , "y" , "z" , "#" , "#" , "2" , "3" , "#" , "#" ,
                "6" , "7" , "#" , "#" , "A" , "B" , "#" , "#" , "E" , "F" , "#" , "#" ,
                "I" , "J" , "#" , "#" , "M" , "N" , "#" , "#" , "Q" , "R" , "#" , "#" ,
                "U" , "V" , "#" , "#" , "Y" , "Z"
        };

        // 对传入网址进行 MD5 加密

        String sMD5EncryptResult = Md5Encoder.encode(key + url);
        String hex = sMD5EncryptResult;
        String[] resUrl = new String[4];
        for ( int i = 0; i < 4; i++) {
            // 把加密字符按照 8 位一组 16 进制与 0x3FFFFFFF 进行位与运算
            String sTempSubString = hex.substring(i * 8, i * 8 + 8);
            // 这里需要使用 long 型来转换，因为 Inteper .parseInt() 只能处理 31 位 , 首位为符号位 , 如果不用 long ，则会越界
            long lHexLong = 0x3FFFFFFF & Long.parseLong (sTempSubString, 16);
            String outChars = "" ;
            for ( int j = 0; j < 6; j++) {
                // 把得到的值与 0x0000003D 进行位与运算，取得字符数组 chars 索引
                long index = 0x0000003D & lHexLong;
                // 把取得的字符相加
                outChars += chars[( int ) index];
                // 每次循环按位右移 5 位
                lHexLong = lHexLong >> 5;
            }

            // 把字符串存入对应索引的输出数组
            resUrl[i] = outChars;
        }
        return resUrl;
    }

}

