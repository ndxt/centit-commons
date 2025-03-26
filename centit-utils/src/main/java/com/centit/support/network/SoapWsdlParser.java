package com.centit.support.network;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public abstract class SoapWsdlParser {

    protected static final Logger logger = LoggerFactory.getLogger(SoapWsdlParser.class);
    public static Element findTypeElement(Element rootElement, String typeName) {
        Element portTypes = rootElement.element( "types");
        if(portTypes==null)  return null;
        Element typeElement = portTypes.element( "schema");
        if(typeElement==null)  return null;
        for( Element schema : typeElement.elements("element")){
            if( schema.attribute("name").getValue().equals(typeName)){
                return schema;
            }
        }
        return null;
    }

    public static Map<String, String> mapElementType(Element operationElem) {
        Map<String, String> stringMap = new HashMap<>();
        Element element = operationElem.element("complexType");
        if( element == null) return stringMap;
        element = element.element("sequence");
        if( element == null) return stringMap;
        for( Element elem : element.elements("element")){
            String name = elem.attribute("name").getValue();
            String type = elem.attribute("type").getValue();
            stringMap.put(name, type);
        }
        return stringMap;
    }

    public static String getSoapNameSpace(Element rootElement) {
        Attribute attr = rootElement.attribute("targetNamespace");
        if(attr!=null)
            return attr.getValue();
        return "missing!";
    }

    public static List<String> getSoapActionList(Element rootElement) {
        List<String> methods = new ArrayList<>();
        List<Element> portTypes = rootElement.elements( "portType");
        for (Element portType : portTypes) {
            for (Element operation : portType.elements( "operation")) {
                Attribute nameAttr = operation.attribute("name");
                if (nameAttr != null) {
                    methods.add(nameAttr.getValue());
                }
            }
        }
        return methods;
    }

    public static Map<String, String> getSoapActionParams(Element rootElement, String actionName) {
        Map<String, String> params = new HashMap<>();
        Element portTypes = rootElement.element( "types");
        Element operationElem = findTypeElement(portTypes, actionName);
        if(operationElem==null){
            return params;
        }
        return mapElementType(operationElem);
    }
}
