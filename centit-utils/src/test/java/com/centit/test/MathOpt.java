package com.centit.test;

import com.centit.support.algorithm.Mathematics;
import com.centit.support.algorithm.NumberBaseOpt;
import com.centit.support.algorithm.StringRegularOpt;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by codefan on 2017/11/3.
 * @author codefan
 */
public class MathOpt {


    //将 数字和操作排序
    public static void sortFormulaOpt( List<Integer> rList ){
        for(Integer i : rList){
            System.out.print(i);
            System.out.print(" ");
        }
        System.out.println();

    }

    public static void main(String arg[]) throws IOException {

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.println("输入用空格隔开整数，推出输入exit");
            String s = br.readLine().trim();
            if(StringUtils.isBlank(s)){
                continue;
            }
            if(StringUtils.equalsIgnoreCase("exit",s)){
                break;
            }
            int nSelect = -1;
            String[] nums = s.split(" ");
            List<Integer> alist = new ArrayList<>(4);
            for(int i=0; i<nums.length; i++){
                if(StringRegularOpt.isNumber(nums[i])){
                    if( nSelect == -1 ){
                        nSelect = NumberBaseOpt.castObjectToInteger(nums[i]);
                    }else {
                        alist.add(NumberBaseOpt.castObjectToInteger(nums[i]));
                    }
                }
            }

            if( alist.size() < 1){
                continue;
            }

            Mathematics.permutationAndCombination(
                    alist,nSelect, Integer::compare, MathOpt::sortFormulaOpt
            );
        }
    }
}

