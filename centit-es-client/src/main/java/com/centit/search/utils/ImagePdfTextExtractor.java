package com.centit.search.utils;

import com.alibaba.fastjson2.JSONObject;
import com.centit.support.algorithm.CollectionsOpt;
import com.centit.support.common.ObjectException;
import com.centit.support.image.ImageOpt;
import com.centit.support.network.HttpExecutor;
import com.centit.support.network.HttpExecutorContext;
import com.centit.support.security.SecurityOptUtils;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.*;
import lombok.Data;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public abstract class ImagePdfTextExtractor {

    public static List<BufferedImage> fetchPdfImages(InputStream inputStream) throws IOException, DocumentException {

        PdfReader pdf = new PdfReader(inputStream);
        int pageSum = pdf.getNumberOfPages();
        List<BufferedImage> images = new ArrayList<>(pageSum+5);
        for (int p = 0; p < pageSum; p++) {
            PdfDictionary pg = pdf.getPageN(p + 1);
            PdfDictionary res =
                (PdfDictionary) PdfReader.getPdfObject(pg.get(PdfName.RESOURCES));
            PdfDictionary xobj =
                (PdfDictionary) PdfReader.getPdfObject(res.get(PdfName.XOBJECT));
            if (xobj != null) {
                for (PdfName pdfName : xobj.getKeys()) {
                    PdfObject obj = xobj.get(pdfName);
                    if (obj.isIndirect()) {
                        PdfDictionary tg = (PdfDictionary) PdfReader.getPdfObject(obj);
                        PdfName type =
                            (PdfName) PdfReader.getPdfObject(tg.get(PdfName.SUBTYPE));
                        if (PdfName.IMAGE.equals(type)) {
                            int XrefIndex = ((PRIndirectReference) obj).getNumber();
                            PdfObject pdfObj = pdf.getPdfObject(XrefIndex);
                            PdfStream pdfStrem = (PdfStream) pdfObj;
                            byte[] bytes = PdfReader.getStreamBytesRaw((PRStream) pdfStrem);
                            if ((bytes != null)) {
                                BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
                                if (image != null) {
                                    images.add(image);
                                }
                                //ImageIO.write(image, "jpg", new File("/Users/codefan/Documents/temp/images/image1.jpg"));
                                //System.out.println(bytes.length);
                            }
                        }
                    }
                }
            }
        }
        return images;
    }

    @Data
    public static class OcrServerHost{
        String authorUrl;
        String orcUrl;

        String  userName;

        String  password;
    }

    public static OcrServerHost fetchDefaultOrrServer(){
        OcrServerHost ocrServer = new OcrServerHost();
        ocrServer.setAuthorUrl("http://192.168.133.61:8080/token");
        ocrServer.setOrcUrl("http://192.168.133.61:8080/file-upload/ocr");
        ocrServer.setUserName("cipher:yKxmpDSt7xoAdTs1onNgjQ==");
        ocrServer.setPassword("cipher:Eo/8Mu1xoKe6fBkfA8OMdQ==");
        return ocrServer;
    }

    public static String imagesToText(List<InputStream> imageFile, OcrServerHost ocrServer)  {
        try (CloseableHttpClient client = HttpExecutor.createHttpClient()){
            HttpExecutorContext executorContext = HttpExecutorContext.create(client);
            String response = HttpExecutor.formPost(executorContext, ocrServer.getAuthorUrl(),
                CollectionsOpt.createHashMap("grant_type", "password",
                    "username", SecurityOptUtils.decodeSecurityString(ocrServer.getUserName()),
                    "password", SecurityOptUtils.decodeSecurityString(ocrServer.getPassword())));
            JSONObject jsonObject = JSONObject.parseObject(response);

            executorContext.header("Authorization", "Bearer " + jsonObject.getString("access_token"));

            HttpPost httpPost = new HttpPost(ocrServer.getOrcUrl());
            httpPost.setHeader("Content-Type", HttpExecutor.multiPartTypeHead);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setBoundary(HttpExecutor.BOUNDARY);
            int n = 0;
            for (InputStream image : imageFile) {
                builder.addBinaryBody("files", image,
                    ContentType.IMAGE_JPEG, "image" + n + ".jpg");
                n++;
            }
            httpPost.setEntity(builder.build());
            response = HttpExecutor.httpExecute(executorContext, httpPost);
            JSONObject jsonObject1 = JSONObject.parseObject(response);
            response = ObjectTextExtractor.extractText(jsonObject1.get("documents"), true, true);
            return response;
        } catch (IOException e) {
            throw new ObjectException(e);
        }
    }

    public static String imagePdfToText(InputStream pdfFile, OcrServerHost ocrServer)  {
        try {
            List<BufferedImage> images = ImagePdfTextExtractor.fetchPdfImages(pdfFile);
            List<InputStream> imagesFile = new ArrayList<>();
            for (BufferedImage image : images) {
                imagesFile.add(ImageOpt.imageToInputStream(image));
            }
            return imagesToText(imagesFile, ocrServer);
        } catch (DocumentException | IOException e) {
            throw new ObjectException(e);
        }
    }

    public static String imageToText(InputStream imageFile, OcrServerHost ocrServer)  {
        return imagesToText(CollectionsOpt.createList(imageFile), ocrServer);
    }

}
