/*
 * @(#) XmlUtils.java
 * Created Date: 2011-11-8
 *
 * Copyright (c) Centit Co., Ltd
 *
 * This software is the confidential and proprietary information of
 * Centit Co., Ltd. ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in accordance
 * with the terms of the license agreement you entered into with
 * Centit Co., Ltd.
 */
package com.centit.support.xml;

import org.apache.commons.text.StringEscapeUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * XML公用操作类
 *
 * @author <a href="mailto:ljy@centit.com">ljy</a>
 * @version $Rev$ <br>
 * $Id$
 */
@SuppressWarnings("unused")
public abstract class XmlUtils {

    private static final Logger logger = LoggerFactory.getLogger(XmlUtils.class);

    private XmlUtils() {
        throw new IllegalAccessError("Utility class");
    }

    public static Document string2xml(String xmlStr) {

//        SAXReader saxReader = new SAXReader();
        Document xmlDoc = null;
        try {

//            InputStream in = new ByteArrayInputStream(xmlStr.getBytes());
//            InputStreamReader isReader = new InputStreamReader(in, "GBK");
//            xmlDoc = saxReader.read(isReader);
            xmlDoc = DocumentHelper.parseText(xmlStr);
        } catch (DocumentException e) {
            logger.error(e.getMessage(), e);
        }
//        } catch (UnsupportedEncodingException e) {
//            logger.error(e.getMessage(),e.getCause());
//        }

        return xmlDoc;
    }

    /*
     * 替换字符串中特殊字符
     */
    public static String encodeString(String strData) {
        return StringEscapeUtils.escapeXml11(strData);
        /* if (strData == null)
         {
             return "";
         }

         StringBuilder xmlData = new StringBuilder();
         //byte[] bf = strData.getBytes();
         //int sl = bf.length;
         for(int i=0 ;i<strData.length();i++){
             switch(strData.charAt(i)){
             case '&':
                 xmlData.append("&amp;");
                 break;
             case '<':
                 xmlData.append("&lt;");
                 break;
             case '>':
                 xmlData.append("&gt;");
                 break;
             case '"':
                 xmlData.append("&quot;");
                 break;
             case '\'':
                 xmlData.append("&apos;");
                 break;
             default:
                 xmlData.append(strData.charAt(i));
                 break;
             }
         }
         return strData;  */
    }

    /*
     * 还原字符串中特殊字符
     */
    public static String decodeString(String xmlData) {
        return StringEscapeUtils.unescapeXml(xmlData);
        /*String strData = xmlData.replaceAll("&lt;", "<");
         strData = strData.replaceAll( "&gt;", ">");
         strData = strData.replaceAll("&apos;", "'");
         strData = strData.replaceAll("&quot;", "\"");
         strData = strData.replaceAll( "&amp;", "&");
         return strData;  */
    }

/*    public static Document mergeXMLDocumet(final Document xmlDoc1,final Document xmlDoc2){
        Document xmlDoc = DocumentHelper.createDocument(xmlDoc1.getRootElement());
        Element element= xmlDoc.getRootElement();
        Element element2= xmlDoc2.getRootElement();
        if(element2!=null){
            List<Attribute> attrs = element2.attributes();
            if(attrs != null){
                element.setAttributes(attrs);
            }
            List<Element> elements = element2.elements();
            if(elements != null){
                for(Element ele : elements)
                    element.add(ele);
            }
        }
        return xmlDoc;
    }*/
}
