package com.centit.support.office;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class PdfBaseOpt {

    public static List<BufferedImage> fetchPdfImages(String pdfFilePath) throws IOException, DocumentException {

        PdfReader pdf = new PdfReader(pdfFilePath);
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
}
