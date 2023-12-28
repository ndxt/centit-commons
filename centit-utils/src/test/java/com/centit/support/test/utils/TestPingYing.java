package com.centit.support.test.utils;

import com.centit.support.algorithm.StringBaseOpt;
import net.sourceforge.pinyin4j.PinyinHelper;

import java.util.Arrays;

public class TestPingYing {
    public static void main(String[] args) {
        //1.展示单个汉字的字母拼写
        String[] res1= PinyinHelper.toHanyuPinyinStringArray('芮');
        System.out.println(Arrays.toString(res1));

        System.out.println(StringBaseOpt.getFirstLetter("芮"));
    }

}
