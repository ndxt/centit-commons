package com.centit.support.xml;

import com.centit.support.algorithm.*;
import com.centit.support.common.JavaBeanField;
import com.centit.support.common.JavaBeanMetaData;
import org.apache.commons.lang3.tuple.Pair;
import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by codefan on 17-6-30.
 */
@SuppressWarnings("unused")
public abstract class XMLObject {

    protected static final Logger logger = LoggerFactory.getLogger(XMLObject.class);
    public static final String XML_ARRAY_ITEM_TAG = "__item";

    private XMLObject() {
        throw new IllegalAccessError("Utility class");
    }

    private static Element createElement(String elementName, boolean fieldAsKeyAttr) {
        String keyEntName = fieldAsKeyAttr? "entry" : elementName;
        Element element = DocumentHelper.createElement(keyEntName);
        if(fieldAsKeyAttr) {
            element.addAttribute("key", elementName);
        }
        return element;
    }
    public static Element createXMLElement(String elementName, String valueType, Object value, boolean addTypeAttr, boolean fieldAsKeyAttr) {
        Element element = createElement(elementName, fieldAsKeyAttr);
        if(addTypeAttr) {
            element.addAttribute("type", valueType);
        }
        element.setText(StringBaseOpt.objectToString(value));
        return element;
    }

    public static Element createArrayXMLElement(Element paraentElement, String elementName, Collection<Object> values, boolean addTypeAttr, boolean fieldAsKeyAttr, boolean flattenArray, HashSet<Object> hasSerialized) {
        return createArrayXMLElement(paraentElement, elementName, values, addTypeAttr, fieldAsKeyAttr,
            flattenArray, false, true, hasSerialized);
    }

    /**
     *
     * @param paraentElement 父节点
     * @param elementName 节点名称
     * @param values 数组
     * @param addTypeAttr 是否添加类型属性
     * @param fieldAsKeyAttr 是否将字段名作为key属性
     * @param flattenArray 是否扁平化数组
     * @param ignoreNullValue 是否忽略 null 值，true 时数组中的 null 项不生成标签
     * @param prettyFormat 是否格式化输出，单个属性一行，复合属性或对象标签单独一行，便于阅读
     * @param hasSerialized 已序列化对象缓存，防止循环引用
     * @return xml元素
     */
    public static Element createArrayXMLElement(Element paraentElement, String elementName, Collection<Object> values, boolean addTypeAttr, boolean fieldAsKeyAttr, boolean flattenArray, boolean ignoreNullValue, boolean prettyFormat, HashSet<Object> hasSerialized) {
        if(flattenArray && paraentElement != null){
            for (Object obj : values) {
                if (obj != null || !ignoreNullValue) {
                    paraentElement.add(createXMLElementFromObject(null, elementName, obj,
                        addTypeAttr, fieldAsKeyAttr, true, ignoreNullValue, prettyFormat, hasSerialized));
                }
            }
            return null;
        }
        Element element = createElement(elementName, fieldAsKeyAttr);
        element.addAttribute("type", "Array");
        if(values.isEmpty()){
            return element;
        }
        if(addTypeAttr) {
            element.addAttribute("class", values.iterator().next().getClass().getName());
        }
        for (Object obj : values) {
            if (obj != null || !ignoreNullValue) {
                Pair<String, Object> keyAndValue = extraKeyAndValue(obj);
                Element entry = createXMLElementFromObject(element, keyAndValue.getKey(), keyAndValue.getValue(),
                    addTypeAttr, fieldAsKeyAttr, flattenArray, ignoreNullValue, prettyFormat, hasSerialized);
                if(entry!=null) {
                    element.add(entry);
                }
            }
        }
        return element;
    }

    @SuppressWarnings("unchecked")
    private static Pair<String, Object> extraKeyAndValue(Object obj){
        if(obj instanceof Map){
            Map<Object, Object> mapObj = (Map<Object, Object>)obj;
            if(mapObj.size()==1){
                Map.Entry<Object, Object> ent = mapObj.entrySet().iterator().next();
                return Pair.of(StringBaseOpt.objectToString(ent.getKey()), ent.getValue());
            }
        }
        return Pair.of(XML_ARRAY_ITEM_TAG,  obj);
    }

    public static Element createXMLElementFromObject(Element paraentElement, String elementName, Object object, boolean addTypeAttr, boolean fieldAsKeyAttr, boolean flattenArray, HashSet<Object> hasSerialized) {
        return createXMLElementFromObject(paraentElement, elementName, object, addTypeAttr, fieldAsKeyAttr,
            flattenArray, false, true, hasSerialized);
    }

    /**
     *
     * @param paraentElement 父节点
     * @param elementName 节点名称
     * @param object 对象
     * @param addTypeAttr 是否添加类型属性
     * @param fieldAsKeyAttr 是否将字段名作为key属性
     * @param flattenArray 是否扁平化数组
     * @param ignoreNullValue 是否忽略 null 值，true 时值为 null 的字段不生成标签；false 时输出空标签（addTypeAttr 为 true 时带 type="Null" 属性）
     * @param prettyFormat 是否格式化输出，单个属性一行，复合属性或对象标签单独一行，便于阅读
     * @param hasSerialized 已序列化对象缓存，防止循环引用
     * @return xml元素
     */
    @SuppressWarnings("unchecked")
    public static Element createXMLElementFromObject(Element paraentElement, String elementName, Object object, boolean addTypeAttr, boolean fieldAsKeyAttr, boolean flattenArray, boolean ignoreNullValue, boolean prettyFormat, HashSet<Object> hasSerialized) {
        if (object == null) {
            Element element = createElement(elementName, fieldAsKeyAttr);
            if(addTypeAttr) {
                element.addAttribute("type", "Null");
            }
            return element;
        }
        if (object instanceof String) {
            return createXMLElement(elementName, "String", object, addTypeAttr, fieldAsKeyAttr);
        }
        if (object instanceof Long) {
            return createXMLElement(elementName, "Long", object, addTypeAttr, fieldAsKeyAttr);
        }
        if (object instanceof BigDecimal) {
            return createXMLElement(elementName, "BigDecimal", object, addTypeAttr, fieldAsKeyAttr);
        }
        if (object instanceof Boolean) {
            return createXMLElement(elementName, "Boolean", object, addTypeAttr, fieldAsKeyAttr);
        }
        if (object instanceof Integer) {
            return createXMLElement(elementName, "Integer", object, addTypeAttr, fieldAsKeyAttr);
        }
        if (object instanceof Number) {
            return createXMLElement(elementName, "Number", object, addTypeAttr, fieldAsKeyAttr);
        }
        if (object instanceof Date) {
            return createXMLElement(elementName, "Date", object, addTypeAttr, fieldAsKeyAttr);
        }
        if (object instanceof Map) {
            Element element = createElement(elementName, fieldAsKeyAttr);
            if(addTypeAttr) {
                element.addAttribute("type", "Object");
                element.addAttribute("class", object.getClass().getName());
            }
            for (Map.Entry<Object, Object> jo : ((Map<Object, Object>) object).entrySet()) {
                if (jo.getValue() != null || !ignoreNullValue) {
                    String keyName = StringBaseOpt.objectToString(jo.getKey());
                    Element entry = createXMLElementFromObject(element, keyName, jo.getValue(), addTypeAttr, fieldAsKeyAttr, flattenArray, ignoreNullValue, prettyFormat, hasSerialized);
                    if(entry!=null) {
                        element.add(entry);
                    }
                }
            }
            return element;
        }

        if (object instanceof Collection) {
            return createArrayXMLElement(paraentElement, elementName, (Collection<Object>) object, addTypeAttr, fieldAsKeyAttr, flattenArray, ignoreNullValue, prettyFormat, hasSerialized);
        } else if (object instanceof Object[]) {
            return createArrayXMLElement(paraentElement, elementName, CollectionsOpt.arrayToList((Object[]) object), addTypeAttr, fieldAsKeyAttr, flattenArray, ignoreNullValue, prettyFormat, hasSerialized);
        }

        if (ReflectionOpt.isScalarType(object.getClass())) {
            return createXMLElement(elementName, "String", object, addTypeAttr, fieldAsKeyAttr);
        } else {
            if(hasSerialized.contains(object)){
                return createXMLElement(elementName, "recursion", object.getClass().getName(), addTypeAttr, fieldAsKeyAttr);
            }
            hasSerialized.add(object);
            JavaBeanMetaData jbm = JavaBeanMetaData.createBeanMetaDataFromType(object.getClass());
            Map<String, JavaBeanField> fields = jbm.getFields();
            if (fields == null)
                return createXMLElement(elementName, "String", object, addTypeAttr, fieldAsKeyAttr);

            Element element = createElement(elementName, fieldAsKeyAttr);
            if(addTypeAttr) {
                element.addAttribute("type", "Object");
                element.addAttribute("class", object.getClass().getName());
            }
            for (Map.Entry<String, JavaBeanField> field : fields.entrySet()) {
                Object obj = field.getValue().getObjectFieldValue(object);
                if (obj != null || !ignoreNullValue) {
                    Element entry = createXMLElementFromObject(element, field.getKey(), obj, addTypeAttr, fieldAsKeyAttr, flattenArray, ignoreNullValue, prettyFormat, hasSerialized);
                    if(entry!=null) {
                        element.add(entry);
                    }
                }
            }
            return element;
        }
    }

    /**
     *
     * @param rootName 跟节点名称
     * @param object 对象
     * @param addTypeAttr 是否添加类型属性
     * @param fieldAsKeyAttr 是否将字段名作为key属性
     * @param flattenArray 是否扁平化数组
     * @return xml字符串
     */
    public static String objectToXMLString(String rootName, Object object, boolean addTypeAttr, boolean fieldAsKeyAttr, boolean flattenArray) {
        return objectToXMLString(rootName, object, addTypeAttr, fieldAsKeyAttr, flattenArray, false, true);
    }

    /**
     *
     * @param rootName 跟节点名称
     * @param object 对象
     * @param addTypeAttr 是否添加类型属性
     * @param fieldAsKeyAttr 是否将字段名作为key属性
     * @param flattenArray 是否扁平化数组
     * @param ignoreNullValue 是否忽略 null 值，true 时值为 null 的字段不生成标签；false 时输出空标签（addTypeAttr 为 true 时带 type="Null" 属性）
     * @param prettyFormat 是否格式化输出，单个属性一行，复合属性或对象标签单独一行，便于阅读
     * @return xml字符串
     */
    public static String objectToXMLString(String rootName, Object object, boolean addTypeAttr, boolean fieldAsKeyAttr, boolean flattenArray, boolean ignoreNullValue, boolean prettyFormat) {
        HashSet<Object> hasSerialized = new HashSet<>();
        Element element = createXMLElementFromObject(null, rootName, object, addTypeAttr, fieldAsKeyAttr,
            flattenArray, ignoreNullValue, prettyFormat, hasSerialized);
        return elementToXMLString(element, prettyFormat);
    }

    /**
     *
     * @param rootName 跟节点名称
     * @param nameSpacePrefix 命名空间前缀
     * @param namespace 命名空间
     * @param object 对象
     * @param addTypeAttr 是否添加类型属性
     * @param fieldAsKeyAttr 是否将字段名作为key属性
     * @param flattenArray 是否扁平化数组
     * @return xml字符串
     */
    public static String objectToXMLString(String rootName, String nameSpacePrefix, String namespace, Object object, boolean addTypeAttr, boolean fieldAsKeyAttr, boolean flattenArray) {
        return objectToXMLString(rootName, nameSpacePrefix, namespace, object, addTypeAttr, fieldAsKeyAttr,
            flattenArray, false, true);
    }

    /**
     *
     * @param rootName 跟节点名称
     * @param nameSpacePrefix 命名空间前缀
     * @param namespace 命名空间
     * @param object 对象
     * @param addTypeAttr 是否添加类型属性
     * @param fieldAsKeyAttr 是否将字段名作为key属性
     * @param flattenArray 是否扁平化数组
     * @param ignoreNullValue 是否忽略 null 值，true 时值为 null 的字段不生成标签；false 时输出空标签（addTypeAttr 为 true 时带 type="Null" 属性）
     * @param prettyFormat 是否格式化输出，单个属性一行，复合属性或对象标签单独一行，便于阅读
     * @return xml字符串
     */
    public static String objectToXMLString(String rootName, String nameSpacePrefix, String namespace, Object object, boolean addTypeAttr, boolean fieldAsKeyAttr, boolean flattenArray, boolean ignoreNullValue, boolean prettyFormat) {
        HashSet<Object> hasSerialized = new HashSet<>();
        Element element = createXMLElementFromObject(null,nameSpacePrefix+":"+rootName,
            object, addTypeAttr, fieldAsKeyAttr, flattenArray, ignoreNullValue, prettyFormat, hasSerialized);
        element.add(new Namespace(nameSpacePrefix, namespace));
        return elementToXMLString(element, prettyFormat);
    }

    /**
     * 将 xml 元素转换为字符串，支持格式化输出
     * @param element xml元素
     * @param prettyFormat 是否格式化输出，true 时单个属性一行，复合属性或对象标签单独一行
     * @return xml字符串
     */
    private static String elementToXMLString(Element element, boolean prettyFormat) {
        if (!prettyFormat) {
            return element.asXML();
        }
        OutputFormat format = OutputFormat.createPrettyPrint();
        StringWriter stringWriter = new StringWriter();
        try {
            XMLWriter xmlWriter = new XMLWriter(stringWriter, format);
            xmlWriter.write(element);
            xmlWriter.close();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return element.asXML();
        }
        // XMLWriter 写独立的 Element 时会在首部输出一个换行，这里去掉首尾空白
        return stringWriter.toString().trim();
    }

    public static String objectToXMLString(String rootName, Object object) {
        return objectToXMLString(rootName, object, true, false, true);
    }

    public static String objectToXMLString(Object object) {
        return objectToXMLString("object", object,true, false, true);
    }

    @SuppressWarnings("unchecked")
    public static Object elementToObject(Element element) {
        //Map<String, Object> objectMap = new HashMap<>();
        Attribute attr = element.attribute("type");
        String sType = attr == null ? null :attr.getValue();
        if ("Date".equals(sType)) {
            return DatetimeOpt.smartPraseDate(element.getTextTrim());
        } else if ("Long".equals(sType)) {
            return NumberBaseOpt.castObjectToLong(element.getTextTrim());
        } else if ("Integer".equals(sType)) {
            return NumberBaseOpt.castObjectToInteger(element.getTextTrim());
        } else if ("Number".equals(sType)) {
            return NumberBaseOpt.castObjectToDouble(element.getTextTrim());
        } else if ("Boolean".equals(sType)) {
            return StringRegularOpt.isTrue(element.getTextTrim());
        } else if ("BigDecimal".equals(sType)) {
            return new BigDecimal(element.getTextTrim());
        } else if ("Null".equals(sType)) {
            return null;
        } else if ("Array".equals(sType)) {
            List<Element> subElements = element.elements();
            if (subElements == null)
                return null;
            List<Object> objs = new ArrayList<>(subElements.size());
            for (Element subE : subElements) {
                String keyName = subE.getName();
                Attribute keyAttr = subE.attribute("key");
                if(keyAttr != null) {
                    keyName = keyAttr.getValue();
                }
                if (XML_ARRAY_ITEM_TAG.equals(keyName)) {
                    objs.add(elementToObject(subE));
                } else {
                    objs.add(CollectionsOpt.createHashMap(keyName,
                        elementToObject(subE)));
                }
            }
            return objs;
        } else /*if ("Object".equals(sType)) */{
            List<Element> subElements = element.elements();
            if (subElements == null || subElements.isEmpty())
                return element.getTextTrim();
            Map<String, Object> objectMap = new HashMap<>();
            for (Element subE : subElements) {
                String keyName = subE.getName();
                Attribute keyAttr = subE.attribute("key");
                if(keyAttr != null) {
                    keyName = keyAttr.getValue();
                }
                Object obj = elementToObject(subE);
                if(objectMap.containsKey(keyName)){
                    Object oldObj = objectMap.get(keyName);
                    if(oldObj instanceof List){
                        ((List<Object>)oldObj).add(obj);
                    }else{
                        objectMap.put(keyName,
                            CollectionsOpt.createList(oldObj, obj));
                    }
                }else {
                    objectMap.put(keyName, obj);
                }
            }
            return objectMap;
        }
    }

    public static Document parseXmlStreamIgnoreDtd(InputStream xmlStream) throws DocumentException{
            SAXReader builder = new SAXReader(false);
            builder.setValidation(false);
            builder.setEntityResolver((publicId, systemId) -> new InputSource(
                new ByteArrayInputStream(
                    "<?xml version='1.0' encoding='UTF-8'?>".getBytes()))
            );
            //Attribute attr;
            return builder.read(xmlStream);
    }

    public static Document parseXmlTextIgnoreDtd(String xmlString) throws DocumentException{
        SAXReader builder = new SAXReader(false);
        builder.setValidation(false);
        builder.setEntityResolver((publicId, systemId) -> new InputSource(
            new ByteArrayInputStream(
                "<?xml version='1.0' encoding='UTF-8'?>".getBytes()))
        );
        InputSource source = new InputSource(new StringReader(xmlString));
        return builder.read(source);
    }

    public static Object xmlStringToObject(String xmlString) {
        try {
            Document doc = parseXmlTextIgnoreDtd(xmlString);
            return elementToObject(doc.getRootElement());
        } catch (DocumentException e) {
            logger.error(e.getMessage(), e);//logger.error(e.getMessage(), e);
            return null;
        }
    }

    public static Object xmlStreamToObject(InputStream xmlStream) {
        try {
            Document doc = parseXmlStreamIgnoreDtd(xmlStream);
            return elementToObject(doc.getRootElement());
        } catch (DocumentException e) {
            logger.error(e.getMessage(), e);//logger.error(e.getMessage(), e);
            return null;
        }
    }
}
