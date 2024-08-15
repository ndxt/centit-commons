package com.centit.support.test.utils;

import com.centit.support.algorithm.StringBaseOpt;
import net.sourceforge.pinyin4j.PinyinHelper;

import java.util.Arrays;

public class TestPingYing {
    public static void main(String[] args) {
        //1.展示单个汉字的字母拼写
        String[] res1= PinyinHelper.toHanyuPinyinStringArray('吕');
        System.out.println(Arrays.toString(res1));
        res1= PinyinHelper.toHanyuPinyinStringArray('女');
        System.out.println(Arrays.toString(res1));
        res1= PinyinHelper.toHanyuPinyinStringArray('许');
        System.out.println(Arrays.toString(res1));
        res1= PinyinHelper.toHanyuPinyinStringArray('句');
        System.out.println(Arrays.toString(res1));

        res1= PinyinHelper.toHanyuPinyinStringArray('去');
        System.out.println(Arrays.toString(res1));

        char  a ='a', b='芮';
        System.out.println((int)a);
        System.out.println((int)b);
        System.out.println(StringBaseOpt.getPinYin("吕飞去需女"));
        System.out.println(StringBaseOpt.getPinYin("南大先腾"));
    }

}
