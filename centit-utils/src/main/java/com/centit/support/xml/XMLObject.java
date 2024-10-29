package com.centit.support.xml;

import com.centit.support.algorithm.*;
import com.centit.support.common.JavaBeanField;
import com.centit.support.common.JavaBeanMetaData;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;

/**
 * Created by codefan on 17-6-30.
 */
@SuppressWarnings("unused")
public abstract class XMLObject {

    protected static final Logger logger = LoggerFactory.getLogger(XMLObject.class);

    private XMLObject() {
        throw new IllegalAccessError("Utility class");
    }

    public static Element createXMLElement(String elementName, String valueType, Object value) {
        Element element = DocumentHelper.createElement(elementName);
        element.addAttribute("type", valueType);
        element.setText(StringBaseOpt.objectToString(value));
        return element;
    }

    @SuppressWarnings("unchecked")
    public static Element createXMLElementFromObject(String elementName, Object object, HashSet<Object> hasSerialized) {
        if (object instanceof String) {
            return createXMLElement(elementName, "String", object);
        }
        if (object instanceof Long) {
            return createXMLElement(elementName, "Long", object);
        }
        if (object instanceof BigDecimal) {
            return createXMLElement(elementName, "BigDecimal", object);
        }
        if (object instanceof Boolean) {
            return createXMLElement(elementName, "Boolean", object);
        }
        if (object instanceof Integer) {
            return createXMLElement(elementName, "Integer", object);
        }
        if (object instanceof Number) {
            return createXMLElement(elementName, "Number", object);
        }
        if (object instanceof Date) {
            return createXMLElement(elementName, "Date", object);
        }
        if (object instanceof Map) {
            Element element = DocumentHelper.createElement(elementName);
            element.addAttribute("type", "Object");
            element.addAttribute("class", object.getClass().getName());
            for (Map.Entry<Object, Object> jo : ((Map<Object, Object>) object).entrySet()) {
                if (jo.getValue() != null) {
                    Element entry = createXMLElementFromObject("entry", jo.getValue(), hasSerialized);
                    entry.addAttribute("key", StringBaseOpt.objectToString(jo.getKey()));
                    element.add(entry);
                }
            }
            return element;
        }

        if (object instanceof Object[]) {
            Element element = DocumentHelper.createElement(elementName);
            element.addAttribute("type", "Array");
            element.addAttribute("class", object.getClass().getName());
            for (Object obj : (Object[]) object) {
                if (obj != null) {
                    element.add(createXMLElementFromObject("item", obj, hasSerialized));
                }
            }
            return element;
        } else if (object instanceof Collection) {
            Element element = DocumentHelper.createElement(elementName);
            element.addAttribute("type", "Array");
            element.addAttribute("class", object.getClass().getName());
            for (Object obj : (Collection<?>) object) {
                if (obj != null) {
                    element.add(createXMLElementFromObject("item", obj, hasSerialized));
                }
            }
            return element;
        }

        if (ReflectionOpt.isScalarType(object.getClass())) {
            return createXMLElement(elementName, "String", object);
        } else {
            if(hasSerialized.contains(object)){
                return createXMLElement(elementName, "recursion", object.getClass().getName());
            }
            hasSerialized.add(object);
            JavaBeanMetaData jbm = JavaBeanMetaData.createBeanMetaDataFromType(object.getClass());
            Map<String, JavaBeanField> fields = jbm.getFileds();
            if (fields == null)
                return createXMLElement(elementName, "String", object);

            Element element = DocumentHelper.createElement(elementName);
            element.addAttribute("type", "Object");
            element.addAttribute("class", object.getClass().getName());
            for (Map.Entry<String, JavaBeanField> field : fields.entrySet()) {
                Object obj = field.getValue().getObjectFieldValue(object);
                if (obj != null)
                    element.add(createXMLElementFromObject(field.getKey(), obj, hasSerialized));

            }
            return element;
        }
    }

    public static String jsonObjectToXMLString(Map<String, Object> json) {
        HashSet<Object> hasSerialized = new HashSet<>();
        Element element = createXMLElementFromObject("object", json, hasSerialized);
        return element.asXML();
        //return DocumentHelper.createDocument(element).asXML();
    }

    public static String objectToXMLString(String rootName, Object object) {
        HashSet<Object> hasSerialized = new HashSet<>();
        Element element = createXMLElementFromObject(rootName, object, hasSerialized);
        return element.asXML();
        //return DocumentHelper.createDocument(element).asXML();
    }

    public static String objectToXMLString(Object object) {
        return objectToXMLString("object", object);
    }

    public static Object elementToObject(Element element) {
        //Map<String, Object> objectMap = new HashMap<>();
        Attribute attr = element.attribute("type");
        String sType = attr == null ? null : element.attribute("type").getValue();
        if (StringUtils.equals("Date", sType)) {
            return DatetimeOpt.smartPraseDate(element.getTextTrim());
        } else if (StringUtils.equals("Long", sType)) {
            return NumberBaseOpt.castObjectToLong(element.getTextTrim());
        } else if (StringUtils.equals("Integer", sType)) {
            return NumberBaseOpt.castObjectToInteger(element.getTextTrim());
        } else if (StringUtils.equals("Number", sType)) {
            return NumberBaseOpt.castObjectToDouble(element.getTextTrim());
        } else if (StringUtils.equals("Boolean", sType)) {
            return StringRegularOpt.isTrue(element.getTextTrim());
        } else if (StringUtils.equals("BigDecimal", sType)) {
            return new BigDecimal(element.getTextTrim());
        } else if (StringUtils.equals("Array", sType)) {
            List<Element> subElements = element.elements();
            if (subElements == null)
                return null;
            List<Object> objs = new ArrayList<>(subElements.size());
            for (Element subE : subElements) {
                if (StringUtils.equals("item", element.getName())) {
                    objs.add(
                        elementToObject(subE));
                }
            }
            return objs;
        } else /*if (StringUtils.equals("Object", sType)) */{
            Map<String, Object> objectMap = new HashMap<>();
            List<Element> subElements = element.elements();
            if (subElements == null || subElements.isEmpty())
                return element.getTextTrim();
            for (Element subE : subElements) {
                objectMap.put(element.getName(),
                    elementToObject(subE));
            }
            return objectMap;
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> elementToJSONObject(Element element) {
        Object obj = elementToObject(element);
        if (obj instanceof Map)
            return (Map<String, Object>) obj;
        return null;
    }

    public static Map<String, Object> xmlStringToJSONObject(String xmlString) {
        try {
            Document doc = DocumentHelper.parseText(xmlString);
            return elementToJSONObject(doc.getRootElement());
        } catch (DocumentException e) {
            logger.error(e.getMessage(), e);//logger.error(e.getMessage(), e);
            return null;
        }
    }

    public static Object xmlStringToObject(String xmlString) {
        try {
            Document doc = DocumentHelper.parseText(xmlString);
            return elementToObject(doc.getRootElement());
        } catch (DocumentException e) {
            logger.error(e.getMessage(), e);//logger.error(e.getMessage(), e);
            return null;
        }
    }
}
