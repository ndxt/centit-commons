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
import java.util.function.BinaryOperator;

/**
 * Created by codefan on 2017/11/3.
 * @author codefan
 */
public class MathOpt {
    //逆波兰式
    static float calcReversePolishRepresentation (Object [] reversePolish ) {
        float[] stack = new float[4];
        int j = 0;
        for (int i = 0; i < 7; i++) {
            if (reversePolish[i] instanceof Integer) {
                stack[j] =  ((Integer) reversePolish[i]).floatValue();
                j++;
            } else {
                switch ((String) reversePolish[i]) {
                    case "+":
                        stack[j - 2] = stack[j - 2] + stack[j - 1];
                        break;
                    case "-":
                        stack[j - 2] = stack[j - 2] - stack[j - 1];
                        break;
                    case "*":
                        stack[j - 2] = stack[j - 2] * stack[j - 1];
                        break;
                    case "/":
                        if (stack[j - 1] == 0) {
                            return -1;
                        }
                        stack[j - 2] = stack[j - 2] / stack[j - 1];
                        break;
                }
                j--;
            }
        }
        return stack[0];
    }

    // 算24点
    public static void calc24(Object [] reversePolish){
        if( Math.abs(calcReversePolishRepresentation(reversePolish) - 24) < 0.0001f  ){
            for(int i=0;i<7;i++){
                System.out.print(reversePolish[i]);
                System.out.print(" ");
            }
            System.out.println();
        }
    }

    //将 数字和操作排序
    public static void sortFormulaOpt( List<Integer> rList ){
        String[] opts = {"+","-","*","/"};
        Object [] stack = new Object[7];
        for(int i=0;i<4;i++){
            for(int j=0;j<4;j++){
                for(int k=0;k<4;k++){
                    // a b + c + d +
                    stack[0] = rList.get(0);
                    stack[1] = rList.get(1);

                    stack[2] = opts[i];
                    stack[3] = rList.get(2);
                    stack[4] = opts[j];
                    stack[5] = rList.get(3);

                    stack[6] = opts[k];
                    calc24(stack);
                    // a b + c d + +
                    stack[3] = rList.get(2);
                    stack[4] = rList.get(3);
                    stack[5] = opts[j];
                    calc24(stack);
                    // a b c + d + +
                    stack[2] = rList.get(2);
                    stack[3] = opts[i];
                    stack[4] = rList.get(3);
                    calc24(stack);
                    // a b c + + d +
                    stack[3] = opts[i];
                    stack[4] = opts[j];
                    stack[5] = rList.get(3);
                    calc24(stack);
                    // a b c d + + +
                    stack[3] = rList.get(3);
                    stack[4] = opts[i];
                    stack[5] = opts[j];
                    calc24(stack);
                }
            }
        }

    }

    public static void main(String arg[]) throws IOException {

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.println("输入用空格隔开的4在1和10之间的整数，推出输入exit");
            String s = br.readLine().trim();
            if(StringUtils.isBlank(s)){
                continue;
            }
            if(StringUtils.equalsIgnoreCase("exit",s)){
                break;
            }
            String[] nums = s.split(" ");
            List<Integer> alist = new ArrayList<>(4);
            for(int i=0; i<nums.length; i++){
                if(StringRegularOpt.isNumber(nums[i])){
                    alist.add(NumberBaseOpt.castObjectToInteger(nums[i]));
                    if( alist.size() == 4){
                        break;
                    }
                }
            }

            if( alist.size() < 4){
                continue;
            }

            Mathematics.combination(
                    alist, Integer::compare, MathOpt::sortFormulaOpt
            );
        }
    }
}

