package com.centit.test;

import java.security.KeyPair;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.centit.support.file.FileIOOpt;
import com.centit.support.security.DESSecurityUtils;
import com.centit.support.security.RSASecurityUtils;

public class TestRSA {

    public static void main(String[] args) throws Exception {
        testDes();
        testCreateKey();
        testRsa();
    }

    public static void testDes(){
        String str="恭喜发财!恭喜发财!恭喜发财!恭喜发财";
        String key = "0123456789abcdefghijklmnopqrstuvwxyzABCDEF";
        System.out.println("密码为:"+key);
        System.out.println("---------------加密方法DES- 中文--------------------");
        System.out.println("原文为:("+str.length()+")"+str);


        String sec = DESSecurityUtils.encryptAndBase64(str, key);
        System.out.println("加密后密文为：("+sec.length()+")"+ sec);
        sec = DESSecurityUtils.decryptBase64String(sec, key);
        System.out.println("解密后为:"+sec);

        str="codefan@sina.com+www.centit.com";
        System.out.println("---------------加密方法DES-- 英文和数字-------------------");
        System.out.println("原文为:("+str.length()+")"+str);

        sec = DESSecurityUtils.encryptAndBase64(str, key);
        System.out.println("加密后密文为：("+sec.length()+")"+ sec);
        sec = DESSecurityUtils.decryptBase64String(sec, key);
        System.out.println("解密后为:"+sec);
    }

    public static void testCreateKey() throws Exception {
        KeyPair keyPair = RSASecurityUtils.generateKeyPair(1024);
        FileIOOpt.writeStringToFile(RSASecurityUtils.keyPairToJson(keyPair),
                "D:/Projects/RunData/temp/keyPair.json");
        FileIOOpt.writeObjectToFile(keyPair, "D:/Projects/RunData/temp/keyPair.txt");
    }

    public static void testRsa() throws Exception {
        String source = "恭喜发财!恭喜发财!恭喜发财!恭喜发财";// 要加密的字符串
        System.out.println("---------------RSA 非对称加密---------------------");
        System.out.println("原文为：" + source);
        System.out.println("密码对为:");
        String sjson = FileIOOpt.readStringFromFile("D:/Projects/RunData/temp/keyPair.json");

        JSONObject keyJson =  (JSONObject) JSON.parseObject(sjson);
        System.out.println("公约:");
        System.out.println(keyJson.getString("public"));
        System.out.println("私钥:");
        System.out.println(keyJson.getString("private"));
        /*Map<String,KeyRep> keyPair = FileIOOpt.readObjectAsJsonFromFile(
                "D:/Projects/RunData/temp/keyPair.json", HashMap<String,KeyRep>.);
        */
        KeyPair keyPair =RSASecurityUtils.keyPairFromJson(sjson);

        String cryptograph = RSASecurityUtils.encrypt(source,keyPair.getPublic());// 生成的密文
        System.out.print("用公钥加密后的结果为:" + cryptograph);
        System.out.println();

        String target = RSASecurityUtils.decrypt(cryptograph,keyPair.getPrivate());// 解密密文
        System.out.println("用私钥解密后的字符串为：" + target);
        System.out.println();

        cryptograph = RSASecurityUtils.encrypt(source,keyPair.getPrivate());// 生成的密文
        System.out.print("用私钥加密后的结果为:" + cryptograph);
        System.out.println();

        target = RSASecurityUtils.decrypt(cryptograph,keyPair.getPublic());// 解密密文
        System.out.println("用公钥解密后的字符串为：" + target);
        System.out.println();
    }
}
