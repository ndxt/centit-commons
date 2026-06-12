package com.centit.support.xml;

import com.alibaba.fastjson2.JSONObject;
import org.apache.xerces.jaxp.validation.XMLSchemaFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

public abstract class XMLSchemaValidationUtil {
    /**
     * 验证XML文件是否符合XSD 1.1规范
     * @param xsdStream XSD文件输入流
     * @param xmlStream XML文件输入流
     * @return 验证结果JSON对象
     */
    public static JSONObject validate(InputStream xsdStream, InputStream xmlStream) {
        try {
            // 读取XML内容进行特殊字符检测
            String xmlContent = readInputStream(xmlStream);
            XMLErrorHandler validationErrors = new XMLErrorHandler();

            // 只检测XML文本内容中的特殊字符（不包括标签）
            String textContent = extractTextContent(xmlContent);
            if (!textContent.isEmpty()) {
                String specialCharsResult = XsdErrorTranslator.detectInvalidCharacters(textContent);
                if (specialCharsResult != null) {
                    // 添加特殊字符错误到验证结果
                    validationErrors.addError("特殊字符检测: " + specialCharsResult + "，建议检查XML文本内容中是否包含非法字符");
                }
            }

            // 重新创建输入流进行验证
            ByteArrayInputStream xmlInputStream = new ByteArrayInputStream(
                textContent.getBytes(StandardCharsets.UTF_8)
            );

            // 使用Xerces2支持XSD 1.1
            SchemaFactory factory = new XMLSchemaFactory();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            // 下面注释的语句禁用外部实体访问，提高安全性， 禁用会导致错误
            // factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            // factory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            Schema schema = factory.newSchema(new StreamSource(xsdStream));
            Validator validator = schema.newValidator();
            // 设置错误处理器
            validator.setErrorHandler(validationErrors);
            validator.validate(new StreamSource(xmlInputStream));
            return validationErrors.toJSONObject();

        } catch (SAXException | IOException e) {
            return XMLErrorHandler.createFatalError(e.getMessage());
        }
    }

    /**
     * 读取输入流内容为字符串
     */
    private static String readInputStream(InputStream inputStream) throws IOException {
        StringBuilder content = new StringBuilder();
        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            content.append(new String(buffer, 0, bytesRead, StandardCharsets.UTF_8));
        }
        return content.toString();
    }

    /**
     * 从XML中提取纯文本内容（不包括标签）
     * 使用SAX解析器来安全地提取文本
     */
    private static String extractTextContent(String xmlContent) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setNamespaceAware(true);
            SAXParser parser = factory.newSAXParser();

            TextContentHandler handler = new TextContentHandler();
            parser.parse(new InputSource(new StringReader(xmlContent)), handler);
            return handler.getTextContent();

        } catch (Exception e) {
            // 如果解析失败，返回空字符串
            return "";
        }
    }

    /**
     * SAX处理器，用于提取XML中的文本内容
     */
    private static class TextContentHandler extends DefaultHandler {
        private final StringBuilder textContent = new StringBuilder();

        @Override
        public void characters(char[] ch, int start, int length) {
            // 只收集非空白文本内容
            String text = new String(ch, start, length).trim();
            if (!text.isEmpty()) {
                if (!textContent.isEmpty()) {
                    textContent.append(" ");
                }
                textContent.append(text);
            }
        }

        public String getTextContent() {
            return textContent.toString();
        }
    }
}
