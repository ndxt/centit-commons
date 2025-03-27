package com.centit.support.network;

import com.centit.support.xml.XMLObject;
import org.apache.commons.lang3.StringUtils;
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
        List<Element> schemas = portTypes.elements( "schema");
        for(Element schema : schemas) {
            for (Element typeElm : schema.elements("element")) {
                if (typeElm.attribute("name").getValue().equals(typeName)) {
                    return typeElm;
                }
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

    public static Element findOperationElement(Element rootElement, String typeName) {
        List<Element> portTypes = rootElement.elements( "portType");
        for (Element portType : portTypes) {
            for (Element operation : portType.elements( "operation")) {
                Attribute nameAttr = operation.attribute("name");
                if (nameAttr != null && typeName.equals(nameAttr.getValue())){
                    return operation;
                }
            }
        }
        return null;
    }

    public static Element findElementByName(Element rootElement, String element , String nameValue) {
        List<Element> messages = rootElement.elements( element);
        for (Element message : messages){
            Attribute nameAttr = message.attribute("name");
            if (nameAttr != null && nameValue.equals(nameAttr.getValue())){
                return message;
            }
        }
        return null;
    }

    public static Map<String, String> getSoapActionParams(Element rootElement, String actionName) {
        Map<String, String> params = new HashMap<>();
        //默认类型和方法名一致
        Element operationElem = findTypeElement(rootElement, actionName);
        if(operationElem!=null){
            return mapElementType(operationElem);
        }
        // operation → input → message → part → element
        operationElem = findOperationElement(rootElement, actionName);
        if(operationElem == null) return params;
        Element tempElm = operationElem.element("input");
        if(tempElm == null) return params;
        String msgName = tempElm.attribute("message").getValue();
        if(StringUtils.isBlank(msgName)) return params;
        int p = msgName.indexOf(":");
        if(p>=0){
            msgName = msgName.substring(p+1);
        }
        String typeName = msgName;
        tempElm = findElementByName(rootElement, "message", msgName);
        if(tempElm != null) {
            Element partElm = findElementByName(tempElm, "part", "parameters");

            if (partElm == null) {
                partElm = tempElm.element("part");
            }
            if (partElm != null) {
                typeName = partElm.attribute("element").getValue();
                p = typeName.indexOf(":");
                if (p >= 0) {
                    typeName = typeName.substring(p + 1);
                }
            }
        }
        operationElem = findTypeElement(rootElement, typeName);
        if(operationElem == null) return params;
        return mapElementType(operationElem);
    }

    public static String buildSoapXml(String soapNameSpace, String actionName, Object requestBody){
        String xmlBoday = XMLObject.objectToXMLString("act:"+actionName, requestBody, false, false);
        StringBuilder sb = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
        sb.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" ")
            .append("xmlns:act=\"").append(soapNameSpace).append("\" >")
                .append("<soapenv:Header/> ")
                .append("<soapenv:Body>")
                    .append(xmlBoday)
                .append("</soapenv:Body>")
            .append("</soapenv:Envelope>");
        return sb.toString();
    }

}
