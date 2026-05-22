package com.centit.support.xml;

import com.centit.support.algorithm.*;
import com.centit.support.common.JavaBeanField;
import com.centit.support.common.JavaBeanMetaData;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
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
        if(flattenArray && paraentElement != null){
            for (Object obj : values) {
                if (obj != null) {
                    paraentElement.add(createXMLElementFromObject(null, elementName, obj,
                        addTypeAttr, fieldAsKeyAttr, true, hasSerialized));
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
            if (obj != null) {
                Pair<String, Object> keyAndValue = extraKeyAndValue(obj);
                Element entry = createXMLElementFromObject(element, keyAndValue.getKey(), keyAndValue.getValue(),
                    addTypeAttr, fieldAsKeyAttr, flattenArray, hasSerialized);
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

    @SuppressWarnings("unchecked")
    public static Element createXMLElementFromObject(Element paraentElement, String elementName, Object object, boolean addTypeAttr, boolean fieldAsKeyAttr, boolean flattenArray, HashSet<Object> hasSerialized) {
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
                if (jo.getValue() != null) {
                    String keyName = StringBaseOpt.objectToString(jo.getKey());
                    Element entry = createXMLElementFromObject(element, keyName, jo.getValue(), addTypeAttr, fieldAsKeyAttr, flattenArray, hasSerialized);
                    if(entry!=null) {
                        element.add(entry);
                    }
                }
            }
            return element;
        }

        if (object instanceof Collection) {
            return createArrayXMLElement(paraentElement, elementName, (Collection<Object>) object, addTypeAttr, fieldAsKeyAttr, flattenArray, hasSerialized);
        } else if (object instanceof Object[]) {
            return createArrayXMLElement(paraentElement, elementName, CollectionsOpt.arrayToList((Object[]) object), addTypeAttr, fieldAsKeyAttr, flattenArray, hasSerialized);
        }

        if (ReflectionOpt.isScalarType(object.getClass())) {
            return createXMLElement(elementName, "String", object, addTypeAttr, fieldAsKeyAttr);
        } else {
            if(hasSerialized.contains(object)){
                return createXMLElement(elementName, "recursion", object.getClass().getName(), addTypeAttr, fieldAsKeyAttr);
            }
            hasSerialized.add(object);
            JavaBeanMetaData jbm = JavaBeanMetaData.createBeanMetaDataFromType(object.getClass());
            Map<String, JavaBeanField> fields = jbm.getFileds();
            if (fields == null)
                return createXMLElement(elementName, "String", object, addTypeAttr, fieldAsKeyAttr);

            Element element = createElement(elementName, fieldAsKeyAttr);
            if(addTypeAttr) {
                element.addAttribute("type", "Object");
                element.addAttribute("class", object.getClass().getName());
            }
            for (Map.Entry<String, JavaBeanField> field : fields.entrySet()) {
                Object obj = field.getValue().getObjectFieldValue(object);
                if (obj != null) {
                    Element entry = createXMLElementFromObject(element, field.getKey(), obj, addTypeAttr, fieldAsKeyAttr, flattenArray, hasSerialized);
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
        HashSet<Object> hasSerialized = new HashSet<>();
        Element element = createXMLElementFromObject(null, rootName, object, addTypeAttr, fieldAsKeyAttr, flattenArray, hasSerialized);
        return element.asXML();
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
        HashSet<Object> hasSerialized = new HashSet<>();
        Element element = createXMLElementFromObject(null,nameSpacePrefix+":"+rootName,
            object, addTypeAttr, fieldAsKeyAttr, flattenArray, hasSerialized);
        element.add(new Namespace(nameSpacePrefix, namespace));
        return element.asXML();
    }

    public static String objectToXMLString(String rootName, Object object) {
        return objectToXMLString(rootName, object, true, true, false);
    }

    public static String objectToXMLString(Object object) {
        return objectToXMLString("object", object,true, true, false);
    }

    @SuppressWarnings("unchecked")
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
                if (StringUtils.equals(XML_ARRAY_ITEM_TAG, subE.getName())) {
                    objs.add(
                        elementToObject(subE));
                } else {
                    objs.add(CollectionsOpt.createHashMap(subE.getName(),
                        elementToObject(subE)));
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
