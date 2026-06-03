package com.centit.support.xml;

import com.alibaba.fastjson2.JSONObject;
import org.apache.xerces.jaxp.validation.XMLSchemaFactory;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.InputStream;

public abstract class XMLSchemaValidationUtil {
    /**
     * 验证XML文件是否符合XSD 1.1规范
     * @param xsdPath XSD文件输入流
     * @param xmlPath XML文件输入流
     * @return 验证结果JSON对象
     */
    public static JSONObject validate(InputStream xsdPath, InputStream xmlPath) {
        try {
            // 使用Xerces2支持XSD 1.1
            SchemaFactory factory = new XMLSchemaFactory();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            // 下面注释的语句禁用外部实体访问，提高安全性， 禁用会导致错误
            // factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            // factory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

            Schema schema = factory.newSchema(new StreamSource(xsdPath));
            Validator validator = schema.newValidator();
            XMLErrorHandler validationErrors = new XMLErrorHandler();
            // 设置错误处理器
            validator.setErrorHandler(validationErrors);
            validator.validate(new StreamSource(xmlPath));
            return validationErrors.toJSONObject();

        } catch (SAXException | IOException e) {
            return XMLErrorHandler.createFatalError(e.getMessage());
        }
    }
}
