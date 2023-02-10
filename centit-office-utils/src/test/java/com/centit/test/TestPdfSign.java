package com.centit.test;

import com.centit.support.office.PdfSignatureUtil;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;

public class TestPdfSign {
    public static final char[] PASSWORD = "123456".toCharArray();// keystory密码

    public static void main(String[] args) {
        try {

            String base="/home/codefan/projects/framework/centit-common-modules/centit-office-commons/src/main/resources/template/";
            // 将证书文件放入指定路径，并读取keystore ，获得私钥和证书链
            String pkPath = "client1.p12";
            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(new FileInputStream(base+pkPath), PASSWORD);
            String alias = ks.aliases().nextElement();
            PrivateKey pk = (PrivateKey) ks.getKey(alias, PASSWORD);
            // 得到证书链
            Certificate[] chain = ks.getCertificateChain(alias);
            //需要进行签章的pdf
            String path = "fff.pdf";
            //签章后的pdf路径
            PdfSignatureUtil.sign(base+path, base+"output.pdf",
                PdfSignatureUtil.createSingInfo().reason("理由")
                    .location("位置")
                    .image(base+"yinzhang.jpg")
                    .privateKey(pk)
                    .certificate(chain)
                    .field("demo")
                    .page(1)
                    .rect(100,800,200,40));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
