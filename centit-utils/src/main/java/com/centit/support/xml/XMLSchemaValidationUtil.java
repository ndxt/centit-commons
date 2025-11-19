package com.centit.support.xml;

import com.alibaba.fastjson2.JSONObject;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public abstract class XMLSchemaValidationUtil {
    public static JSONObject validate(InputStream xsdPath, InputStream xmlPath) {
        try {
            SchemaFactory factory =
                SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            // 禁用外部实体访问，提高安全性

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
