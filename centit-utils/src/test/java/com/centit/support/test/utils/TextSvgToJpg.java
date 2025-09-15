package com.centit.support.test.utils;

import com.centit.support.algorithm.DatetimeOpt;
import com.centit.support.file.FileType;
import com.centit.support.image.SvgUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;


public class TextSvgToJpg {

    /**
     * @param args
     */
    public static void main(String[] args) {
        boolean b = SvgUtils.removeSvgJSAction("/Users/codefan/Documents/temp/test.svg",
            "/Users/codefan/Documents/temp/test.svg");
        System.out.println(b);
    }

    public static void testDate() {
        Date date = DatetimeOpt.currentUtilDate();

        System.out.println(DatetimeOpt.convertTimestampToString(date));

        try {
            String[] sArray = new String[3];
            sArray[1] = "hello!";
            List<String> sList = new ArrayList<>();
            sList.add(sArray[1]);

            Object obj1 = sArray;
            Object obj2 = sList;

            if (obj1 instanceof Collection) {//end of map
                System.out.println("sArray is Collection");
            } else if (obj1 instanceof Object[]) {
                System.out.println("sArray is Object[]");
            } else
                System.out.println("sArray is unknown");

            if (obj2 instanceof Collection) {//end of map
                System.out.println("sList is Collection");
            } else if (obj2 instanceof Object[]) {
                System.out.println("sList is Object[]");
            } else
                System.out.println("sList is unknown");


            //System.out.println(StringBaseOpt.getFirstLetter("杨淮生is a professional！"));


            //System.out.println(PinyinHelper.toHanyuPinyinStringArray('杨')[0]);

            //JPEGImageWriter jpgWriter = new JPEGImageWriter();
            //ImageWriterRegistry.getInstance().register(jpgWriter);

            /*ImageWriter writer = ImageWriterRegistry.getInstance().getWriterFor("image/jpeg");
            if(writer==null)
                System.out.println("null point!");
            else
                System.out.println("not null point!");

            JPEGTranscoder t=new JPEGTranscoder();
            t.addTranscodingHint(JPEGTranscoder.KEY_QUALITY, new Float(0.8));
            InputStream istream = new FileInputStream(new File("D:/Projects/RunData/cs.xml"));

            TranscoderInput input = new TranscoderInput(istream);

            File file = new File("D:/Projects/RunData/out2.jpg");
            //file.createNewFile();
            OutputStream jpg = new FileOutputStream(file);

            TranscoderOutput output = new TranscoderOutput(jpg);

            t.transcode(input, output);

            jpg.flush();
            jpg.close();*/

        } catch (Exception e) {
            //logger.error(e.getMessage(), e);
        }
    }


}
