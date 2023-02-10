package com.centit.search.utils;

import com.centit.support.network.HttpExecutor;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * 将业务对象解析成文本
 * 不支持对象循环引用，请手工将循环引用对象放入excludes属性中，否则将引虚拟机栈溢出异常
 */
@SuppressWarnings("unused")
public abstract class TikaTextExtractor {

    private static Logger logger = LoggerFactory.getLogger(TikaTextExtractor.class);

    public static String extractInputStreamText(InputStream inputStream) throws IOException, TikaException, SAXException {
        Parser tikaParser = new AutoDetectParser();//AutoDetectParser根据不同的二进制文件的特殊格式 (比如说Magic Code)，来寻找适合的Parser。
        ContentHandler handler = new BodyContentHandler(500*1024*1024);
        Metadata metadata = new Metadata();
        ParseContext parseContext = new ParseContext();
        parseContext.set(Parser.class, tikaParser);
        /*/
         * 参数的含义为：
         * inputStream：文件输入流
         * ContentHandler：所有解析出来的内容会放到它的子类BodyContentHandler中
         * Metadata ：Tika解析文档时会生成的一组说明数据
         * ParseContext:用来存储需要填入的参数,最少需要设置tikaParser本身
         * **/
        tikaParser.parse(inputStream, handler, metadata, parseContext);
        /*System.out.println(handler.toString());
        System.out.println("---------------------------");
        for(String name :metadata.names()){
            System.out.println(name+"-->"+metadata.get(name));
        }*/
        return handler.toString();
    }

    public static String extractFileText(File file) throws IOException, TikaException, SAXException {
        try(InputStream is = new FileInputStream(file)) {
            return extractInputStreamText(is);
        }
    }

    public static String extractFileText(String filePath) throws IOException, TikaException, SAXException {
        try(InputStream is = new FileInputStream(new File(filePath))) {
            return extractInputStreamText(is);
        }
    }

    public static String extractUrlText(String urlPath) throws IOException  {
        return HttpExecutor.fetchInputStreamByUrl(urlPath,
                (is) -> {
                            try {
                                return extractInputStreamText(is);
                            } catch (TikaException | SAXException e) {
                                logger.error(e.getMessage(),e);
                                return "";
                            }
                        }
                    );
    }
}
