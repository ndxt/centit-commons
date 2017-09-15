package com.centit.support.xml;

import com.centit.support.algorithm.*;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by codefan on 17-6-30.
 */
@SuppressWarnings("unused")
public abstract class XMLObject {

    private XMLObject() {
        throw new IllegalAccessError("Utility class");
    }

    protected static final Logger logger = LoggerFactory.getLogger(XMLObject.class);

    public static Element createXMLElement(String elementName , String valueType, Object value){
        Element element = DocumentHelper.createElement(elementName);
        element.addAttribute("type",valueType);
        element.setText(StringBaseOpt.objectToString(value));
        return element;
    }
  
    @SuppressWarnings("unchecked")
    public static Element createXMLElementFromObject(String elementName , Object object){

        if(object instanceof String){
            return createXMLElement(elementName ,"String", object);
        }
        
        if(object instanceof Long){
            return createXMLElement(elementName ,"Long", object);
        }
        
        if(object instanceof BigDecimal){
            return createXMLElement(elementName ,"BigDecimal", object);
        }
        
        if(object instanceof Boolean){
            return createXMLElement(elementName ,"Boolean", object);
        }
        
        if(object instanceof Integer){
            return createXMLElement(elementName ,"Integer", object);
        }
        if(object instanceof Number){
            return createXMLElement(elementName ,"Number", object);
        }
        if(object instanceof Date){
            return createXMLElement(elementName ,"Date", object);
        }
        if(object instanceof Map){
            Element element = DocumentHelper.createElement(elementName);
            element.addAttribute("type","Object");
            for(Map.Entry<Object,Object> jo : ((Map<Object,Object>)object).entrySet()){
                if(jo.getValue()!=null)
                    element.add(createXMLElementFromObject(
                            StringBaseOpt.objectToString(jo.getKey()), jo.getValue()));
            }
            return element;
        }
        
        if(object instanceof Object[]){
            Element element = DocumentHelper.createElement(elementName);
            element.addAttribute("type","Array");
            for(Object obj: (Object[]) object){
                if(obj!=null) {
                    element.add(createXMLElementFromObject("item", obj));
                }
            }
            return element;
        }else if(object instanceof Collection){
            Element element = DocumentHelper.createElement(elementName);
            element.addAttribute("type","Array");
            for(Object obj: (Collection<?>) object){
                if(obj!=null) {
                    element.add(createXMLElementFromObject("item", obj));
                }
            }
            return element;
        }
        
        if(ReflectionOpt.isScalarType(object.getClass())){
             return createXMLElement(elementName ,"String", object);
        }else{
            Field[] fields = ReflectionOpt.getFields(object);
            if(fields==null)
                return createXMLElement(elementName ,"String", object);

            Element element = DocumentHelper.createElement(elementName);
            element.addAttribute("type","Object");
            for(Field field: fields){
                Object obj = ReflectionOpt.getFieldValue( object,   field);
                if(obj!=null)
                    element.add(createXMLElementFromObject(field.getName(), obj));
             
            }
            return element;
        }
    }
    
    public static String jsonObjectToXMLString(Map<String, Object> json){
        Element element = createXMLElementFromObject("object",json);
        return element.asXML();
        //return DocumentHelper.createDocument(element).asXML();
    }
        
    public static String objectToXMLString(Object object){
        Element element = createXMLElementFromObject("object",object);
        return element.asXML();
        //return DocumentHelper.createDocument(element).asXML();
    }
    
    public static Object elementToObject(Element element ){
        //Map<String, Object> objectMap = new HashMap<>();
        Attribute attr = element.attribute("type");
        String stype = attr==null?null:element.attribute("type").getValue();
        if(StringUtils.equals("Date", stype)){
            return DatetimeOpt.smartPraseDate(element.getTextTrim());
        }else if(StringUtils.equals("Long", stype)){
            return NumberBaseOpt.castObjectToLong(element.getTextTrim());
        }else if(StringUtils.equals("Integer", stype)){
            return NumberBaseOpt.castObjectToInteger(element.getTextTrim());
        }else if(StringUtils.equals("Number", stype)){
            return NumberBaseOpt.castObjectToDouble(element.getTextTrim());
        }else if(StringUtils.equals("Boolean", stype)){
            return StringRegularOpt.isTrue(element.getTextTrim());
        }else if(StringUtils.equals("BigDecimal", stype)){
            return new BigDecimal(element.getTextTrim());
        }else if(StringUtils.equals("Array", stype)){
            List<Element> subElements = element.elements();
            if(subElements==null)
                return null;
            List<Object> objs = new ArrayList<>(subElements.size());
            for(Element subE : subElements ){
                if(StringUtils.equals("item",element.getName())) {
                    objs.add(
                            elementToObject(subE));
                }
            }
            return objs;
        }else if(StringUtils.equals("Object", stype)){
            Map<String, Object> objectMap = new HashMap<>();
            List<Element> subElements = element.elements();
            if(subElements==null)
                return null;
            for(Element subE : subElements ){
                objectMap.put(element.getName(),
                        elementToObject( subE ));
            }
            return objectMap;
        }else {
            return element.getTextTrim();
        }
    }
    
    @SuppressWarnings("unchecked")
    public static Map<String, Object> elementToJSONObject(Element element ){
        Object obj = elementToObject(element);
        if(obj instanceof Map)
            return (Map<String, Object> ) obj;
        return null;
    }
    
    public static Map<String, Object> xmlStringToJSONObject(String xmlString){
        try {
            Document doc = DocumentHelper.parseText(xmlString);
            return elementToJSONObject(doc.getRootElement());
        } catch (DocumentException e) {
            logger.error(e.getMessage(),e);//e.printStackTrace();
            return null;
        }
    }
    
    public static Object xmlStringToObject(String xmlString){
        try {
            Document doc = DocumentHelper.parseText(xmlString);
            return elementToObject(doc.getRootElement());
        } catch (DocumentException e) {
            logger.error(e.getMessage(),e);//e.printStackTrace();
            return null;
        }
    }
}