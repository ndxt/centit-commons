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

    public static Element createXMLElement(String elementName, String valueType, Object value, boolean addAttr) {
        Element element = DocumentHelper.createElement(elementName);
        if(addAttr) {
            element.addAttribute("type", valueType);
        }
        element.setText(StringBaseOpt.objectToString(value));
        return element;
    }

    @SuppressWarnings("unchecked")
    public static Element createXMLElementFromObject(String elementName, Object object, boolean addAttr, boolean fieldAsKeyAttr, HashSet<Object> hasSerialized) {
        if (object instanceof String) {
            return createXMLElement(elementName, "String", object, addAttr);
        }
        if (object instanceof Long) {
            return createXMLElement(elementName, "Long", object, addAttr);
        }
        if (object instanceof BigDecimal) {
            return createXMLElement(elementName, "BigDecimal", object, addAttr);
        }
        if (object instanceof Boolean) {
            return createXMLElement(elementName, "Boolean", object, addAttr);
        }
        if (object instanceof Integer) {
            return createXMLElement(elementName, "Integer", object, addAttr);
        }
        if (object instanceof Number) {
            return createXMLElement(elementName, "Number", object, addAttr);
        }
        if (object instanceof Date) {
            return createXMLElement(elementName, "Date", object, addAttr);
        }
        if (object instanceof Map) {
            Element element = DocumentHelper.createElement(elementName);
            if(addAttr) {
                element.addAttribute("type", "Object");
                element.addAttribute("class", object.getClass().getName());
            }
            for (Map.Entry<Object, Object> jo : ((Map<Object, Object>) object).entrySet()) {
                if (jo.getValue() != null) {
                    String keyName = StringBaseOpt.objectToString(jo.getKey());
                    String keyEntName = fieldAsKeyAttr? "entry" : keyName;
                    Element entry = createXMLElementFromObject(keyEntName, jo.getValue(), addAttr, fieldAsKeyAttr, hasSerialized);
                    if(fieldAsKeyAttr) {
                        entry.addAttribute("key", keyName);
                    }
                    element.add(entry);
                }
            }
            return element;
        }

        if (object instanceof Object[]) {
            Element element = DocumentHelper.createElement(elementName);
            element.addAttribute("type", "Array");
            if(addAttr) {
                element.addAttribute("class", object.getClass().getName());
            }
            for (Object obj : (Object[]) object) {
                if (obj != null) {
                    element.add(createXMLElementFromObject("item", obj, addAttr, fieldAsKeyAttr, hasSerialized));
                }
            }
            return element;
        } else if (object instanceof Collection) {
            Element element = DocumentHelper.createElement(elementName);
            element.addAttribute("type", "Array");
            if(addAttr) {
                element.addAttribute("class", object.getClass().getName());
            }
            for (Object obj : (Collection<?>) object) {
                if (obj != null) {
                    element.add(createXMLElementFromObject("item", obj, addAttr, fieldAsKeyAttr, hasSerialized));
                }
            }
            return element;
        }

        if (ReflectionOpt.isScalarType(object.getClass())) {
            return createXMLElement(elementName, "String", object, addAttr);
        } else {
            if(hasSerialized.contains(object)){
                return createXMLElement(elementName, "recursion", object.getClass().getName(), addAttr);
            }
            hasSerialized.add(object);
            JavaBeanMetaData jbm = JavaBeanMetaData.createBeanMetaDataFromType(object.getClass());
            Map<String, JavaBeanField> fields = jbm.getFileds();
            if (fields == null)
                return createXMLElement(elementName, "String", object, addAttr);

            Element element = DocumentHelper.createElement(elementName);
            if(addAttr) {
                element.addAttribute("type", "Object");
                element.addAttribute("class", object.getClass().getName());
            }
            for (Map.Entry<String, JavaBeanField> field : fields.entrySet()) {
                Object obj = field.getValue().getObjectFieldValue(object);
                if (obj != null) {
                    String keyEntName = fieldAsKeyAttr? "entry" : field.getKey();
                    Element entry = createXMLElementFromObject(keyEntName, obj, addAttr, fieldAsKeyAttr, hasSerialized);
                    if(fieldAsKeyAttr) {
                        entry.addAttribute("key", field.getKey());
                    }
                    element.add(entry);
                }
            }
            return element;
        }
    }

    public static String objectToXMLString(String rootName, Object object, boolean addAttr, boolean fieldAsKeyAttr) {
        HashSet<Object> hasSerialized = new HashSet<>();
        Element element = createXMLElementFromObject(rootName, object, addAttr, fieldAsKeyAttr, hasSerialized);
        return element.asXML();
    }

    public static String objectToXMLString(String rootName, String nameSpacePrefix, String namespace, Object object, boolean addAttr, boolean fieldAsKeyAttr) {
        HashSet<Object> hasSerialized = new HashSet<>();
        Element element = createXMLElementFromObject(nameSpacePrefix+":"+rootName,
            object, addAttr, fieldAsKeyAttr, hasSerialized);
        element.add(new Namespace(nameSpacePrefix, namespace));
        return element.asXML();
    }

    public static String objectToXMLString(String rootName, Object object) {
        return objectToXMLString(rootName, object, true, true);
    }

    public static String objectToXMLString(Object object) {
        return objectToXMLString("object", object);
    }

    public static Object elementToObject(Element element) {
        //Map<String, Object> objectMap = new HashMap<>();
        Attribute attr = element.attribute("type");
        String sType = attr == null ? null :attr.getValue();
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
            List<Element> subElements = element.elements();
            if (subElements == null || subElements.isEmpty())
                return element.getTextTrim();
            Map<String, Object> objectMap = new HashMap<>();
            for (Element subE : subElements) {
                String keyName = subE.getName();
                Attribute keyAttr = subE.attribute("key");
                if(attr != null) {
                    keyName = attr.getValue();
                }
                objectMap.put(keyName,
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
