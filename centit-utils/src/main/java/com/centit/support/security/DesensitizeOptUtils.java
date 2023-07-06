package com.centit.support.security;

import org.apache.commons.lang3.StringUtils;

/**
 * @author codefan
 * 脱敏操作类
 * @see //https://blog.51cto.com/u_15895329/5894220
 * 重新设计
 */
public abstract class DesensitizeOptUtils {

    public static final String SYMBOL_STAR = "*";
    public static final int ADDRESS_SENSITIVE_SIZE = 7;

    /**
     * 需要脱敏的字段类型
     * @author xh
     * @Date 2022/7/20
     */
    public enum SensitiveTypeEnum {
        /**
         * 中文名
         */
        CHINESE_NAME,
        /**
         * 身份证号
         */
        ID_CARD,
        /**
         * 座机号
         */
        FIXED_PHONE,
        /**
         * 手机号
         */
        MOBILE_PHONE,
        /**
         * 地址
         */
        ADDRESS,
        /**
         * 电子邮件
         */
        EMAIL,
        /**
         * 银行卡
         */
        BANK_CARD,
        /**
         * 虚拟账号
         */
        ACCOUNT,
        /**
         * 密码
         */
        PASSWORD;
    }

    public static String desensitize(String sensitive, SensitiveTypeEnum sensitiveType){

        switch (sensitiveType) {
            case CHINESE_NAME: {
                return DesensitizeOptUtils.chineseName(sensitive, true);
            }
            case ID_CARD: {
                return DesensitizeOptUtils.idCardNum(sensitive);
            }
            case FIXED_PHONE: {
                return DesensitizeOptUtils.fixedPhone(sensitive);
            }
            case MOBILE_PHONE: {
                return DesensitizeOptUtils.mobilePhone(sensitive);
            }
            case ADDRESS: {
                return DesensitizeOptUtils.address(sensitive, ADDRESS_SENSITIVE_SIZE);
            }
            case EMAIL: {
                return DesensitizeOptUtils.email(sensitive);
            }
            case BANK_CARD: {
                return DesensitizeOptUtils.bankCard(sensitive);
            }
            case PASSWORD: {
                return DesensitizeOptUtils.password(sensitive);
            }
            case ACCOUNT:{
                return DesensitizeOptUtils.account(sensitive);
            }
            default:
                return sensitive;
        }
    }

    /**
     * 【中文姓名】只显示第一个汉字，其他隐藏为2个星号，比如：李**
     *
     * @param fullName 姓名
     * @param hideName true 隐藏名， false 隐藏姓
     * @return 脱敏后的姓名
     */
    public static String chineseName(String fullName, boolean hideName) {
        if (StringUtils.isBlank(fullName)) {
            return "";
        }
        if(hideName) {
            String name = StringUtils.left(fullName, 1);
            return StringUtils.rightPad(name, StringUtils.length(fullName), SYMBOL_STAR);
        } else {
            return SYMBOL_STAR + StringUtils.substring(fullName, 1);
        }
    }

    /**
     * 【身份证号】  18位: 显示第5、6位和最后4位 ， 15位: 显示第5、6位和最后3位
     *
     * @param id 身份证号
     * @return 脱敏后的身份证号
     */
    public static String idCardNum(String id) {
        if (StringUtils.isBlank(id)) {
            return "";
        }
        StringBuilder idCard = new StringBuilder(StringUtils.rightPad(SYMBOL_STAR, 4, SYMBOL_STAR));
        idCard.append(StringUtils.substring(id, 4,6));
        if(StringUtils.length(id) == 18){
            idCard.append(StringUtils.rightPad(SYMBOL_STAR, 8, SYMBOL_STAR));
            idCard.append(StringUtils.substring(id, -4));
        } else {
            idCard.append(StringUtils.rightPad(SYMBOL_STAR, StringUtils.length(id) - 9, SYMBOL_STAR));
            idCard.append(StringUtils.substring(id, -3));
        }
        return idCard.toString();
    }

    private static String showHeadAndTail(String str, int head, int tail){
        if (StringUtils.isBlank(str)) {
            return "";
        }
        if(head<=0){
            return StringUtils.rightPad(SYMBOL_STAR, StringUtils.length(str)-tail, SYMBOL_STAR) +
                StringUtils.right(str, tail);
        }
        return StringUtils.join( StringUtils.left(str, head),
            StringUtils.rightPad(SYMBOL_STAR, StringUtils.length(str)-head-tail, SYMBOL_STAR),
            StringUtils.right(str, tail));
    }
    /**
     * 【虚拟账号】显示前2位和最后2位
     *
     * @param id 账号
     * @return 脱敏后的账号
     */
    public static String account(String id) {
        return showHeadAndTail(id,2,2);
    }


    /**
     * 【固定电话】有区号的显示前3后4，其他的只显示后四位，其他隐藏，比如1234
     *
     * @param num 电话号码
     * @return 脱敏后的账号
     */
    public static String fixedPhone(String num) {
        if (StringUtils.isBlank(num)) {
            return "";
        }
        return showHeadAndTail(num,
            StringUtils.startsWithAny(num, "0","+")?3:0,
            4);
    }

    /**
     * 【手机号码】前三位，后四位，其他隐藏，比如135****6810
     *
     * @param num 手机号码
     * @return 前三位，后四位
     */
    public static String mobilePhone(String num) {
        return showHeadAndTail(num,3,4);
    }

    /**
     * 【地址】只显示到地区，不显示详细地址，比如：北京市海淀区****
     *
     * @param address 地址
     * @param sensitiveSize 敏感信息长度
     * @return 隐藏后面 sensitiveSize 位
     */
    public static String address(String address, int sensitiveSize) {
        if (StringUtils.isBlank(address)) {
            return "";
        }
        int length = StringUtils.length(address);
        return StringUtils.rightPad(StringUtils.left(address, length - sensitiveSize), length, SYMBOL_STAR);
    }

    /**
     * 【电子邮箱 邮箱前缀仅显示第一个字母，前缀其他隐藏，用星号代替，@及后面的地址显示，比如：d**@126.com>
     *
     * @param email 邮件地址
     * @return 脱敏后的邮件地址
     */
    public static String email(String email) {
        if (StringUtils.isBlank(email)) {
            return "";
        }
        int index = StringUtils.indexOf(email, "@");
        if (index <= 1)
            return email;
        else
            return StringUtils.rightPad(StringUtils.left(email, 1), index, SYMBOL_STAR) +
                 StringUtils.substring(email, index, StringUtils.length(email));
    }

    /**
     * 【银行卡号】前4位，后3位，其他用星号隐藏每位1个星号，比如：6217 **** **** **** 567>
     *
     * @param cardNum 【银行卡号】
     * @return 【银行卡号】前4位，后3位
     */
    public static String bankCard(String cardNum) {
        return showHeadAndTail(cardNum,4,3);
    }

    /**
     * 【密码】密码的全部字符都用*代替，比如：******
     *
     * @param password 密码
     * @return ******
     */
    public static String password(String password) {
        if (StringUtils.isBlank(password)) {
            return "";
        }
        return StringUtils.rightPad(SYMBOL_STAR, StringUtils.length(password), SYMBOL_STAR);
    }

}
