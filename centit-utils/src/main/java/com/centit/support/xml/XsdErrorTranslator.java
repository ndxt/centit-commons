package com.centit.support.xml;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * XSD验证错误翻译器
 * 将XML Schema验证的英文错误消息翻译为友好的中文提示
 */
public class XsdErrorTranslator {
    
    // 常见错误模式匹配
    private static final Pattern CVc_COMPLEX_TYPE_2_4 = Pattern.compile(
        "cvc-complex-type\\.2\\.4[a-z]?:.*element '([^']+)'.*",
        Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern CVc_COMPLEX_TYPE_3_2_1 = Pattern.compile(
        "cvc-complex-type\\.3\\.2\\.1:.*attribute '([^']+)'.*element '([^']+)'.*",
        Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern CVc_COMPLEX_TYPE_3_2_2 = Pattern.compile(
        "cvc-complex-type\\.3\\.2\\.2:.*attribute '([^']+)'.*",
        Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern CVc_ELEMENT_MINIMAX_OCCURS = Pattern.compile(
        "cvc-complex-type\\.2\\.3:.*element '([^']+)'.*",
        Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern CVc_DATATYPE_VALID_1_2_3 = Pattern.compile(
        "cvc-datatype-valid\\.1\\.2\\.3:.*'([^']+)'.*'([^']+)'.*",
        Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern CVc_TYPE_3_1_3 = Pattern.compile(
        "cvc-type\\.3\\.1\\.3:.*element '([^']+)'.*",
        Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern CVc_ELT_CONTENT_VALID = Pattern.compile(
        "cvc-elt\\.3\\.2\\.2:.*element '([^']+)'.*",
        Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern SRC_ELEMENT_3_1 = Pattern.compile(
        "src-element\\.3\\.1:.*element '([^']+)'.*",
        Pattern.CASE_INSENSITIVE
    );
    
    /**
     * 翻译XSD验证错误消息
     * @param originalMessage 原始错误消息
     * @return 翻译后的中文错误消息
     */
    public static String translate(String originalMessage) {
        if (originalMessage == null || originalMessage.isEmpty()) {
            return originalMessage;
        }
        
        // 尝试匹配并翻译常见错误类型
        String translated = translateByPattern(originalMessage);
        if (translated != null) {
            return translated;
        }
        
        // 通用翻译规则
        return translateGeneral(originalMessage);
    }
    
    /**
     * 基于正则模式匹配翻译
     */
    private static String translateByPattern(String message) {
        // cvc-complex-type.2.4.a: 元素缺失
        Matcher matcher = CVc_COMPLEX_TYPE_2_4.matcher(message);
        if (matcher.find()) {
            String elementName = matcher.group(1);
            return "元素 '" + extractLocalName(elementName) + "' 的内容不完整，缺少必需的子元素";
        }
        
        // cvc-complex-type.3.2.1: 缺少必需属性
        matcher = CVc_COMPLEX_TYPE_3_2_1.matcher(message);
        if (matcher.find()) {
            String attrName = matcher.group(1);
            String elementName = matcher.group(2);
            return "元素 '" + extractLocalName(elementName) + "' 缺少必需的属性 '" + 
                   extractLocalName(attrName) + "'";
        }
        
        // cvc-complex-type.3.2.2: 出现未声明的属性
        matcher = CVc_COMPLEX_TYPE_3_2_2.matcher(message);
        if (matcher.find()) {
            String attrName = matcher.group(1);
            return "属性 '" + extractLocalName(attrName) + "' 未在元素中声明，不允许使用";
        }
        
        // cvc-complex-type.2.3: 元素出现次数不符合要求
        matcher = CVc_ELEMENT_MINIMAX_OCCURS.matcher(message);
        if (matcher.find()) {
            String elementName = matcher.group(1);
            return "元素 '" + extractLocalName(elementName) + "' 的出现次数不符合schema定义的要求";
        }
        
        // cvc-datatype-valid.1.2.3: 数据类型验证失败
        matcher = CVc_DATATYPE_VALID_1_2_3.matcher(message);
        if (matcher.find()) {
            String value = matcher.group(1);
            String dataType = matcher.group(2);
            return "值 '" + value + "' 不符合数据类型 '" + dataType + "' 的格式要求";
        }
        
        // cvc-type.3.1.3: 元素值为空但不应为空
        matcher = CVc_TYPE_3_1_3.matcher(message);
        if (matcher.find()) {
            String elementName = matcher.group(1);
            return "元素 '" + extractLocalName(elementName) + "' 的值不能为空";
        }
        
        // cvc-elt.3.2.2: 元素内容验证失败
        matcher = CVc_ELT_CONTENT_VALID.matcher(message);
        if (matcher.find()) {
            String elementName = matcher.group(1);
            return "元素 '" + extractLocalName(elementName) + "' 的内容不符合schema定义";
        }
        
        // src-element.3.1: 元素未定义
        matcher = SRC_ELEMENT_3_1.matcher(message);
        if (matcher.find()) {
            String elementName = matcher.group(1);
            return "元素 '" + extractLocalName(elementName) + "' 在schema中未定义";
        }
        
        return null;
    }
    
    /**
     * 通用翻译规则
     */
    private static String translateGeneral(String message) {
        String lowerMessage = message.toLowerCase();
        
        // 常见关键字翻译
        if (lowerMessage.contains("not a valid")) {
            return message.replaceAll("(?i)not a valid", "不是一个有效的")
                         .replaceAll("(?i)value", "值");
        }
        
        if (lowerMessage.contains("is not complete")) {
            return message.replaceAll("(?i)is not complete", "不完整")
                         .replaceAll("(?i)one of", "其中之一");
        }
        
        if (lowerMessage.contains("missing child element")) {
            return message.replaceAll("(?i)missing child element", "缺少子元素");
        }
        
        if (lowerMessage.contains("required attribute")) {
            return message.replaceAll("(?i)required attribute", "必需的属性");
        }
        
        if (lowerMessage.contains("unexpected element")) {
            return message.replaceAll("(?i)unexpected element", "意外的元素");
        }
        
        if (lowerMessage.contains("invalid content")) {
            return message.replaceAll("(?i)invalid content", "无效的内容");
        }
        
        // 如果无法识别，返回原消息
        return message;
    }
    
    /**
     * 从带命名空间的名称中提取本地名称
     * 例如: {http://example.com}localName -> localName
     */
    private static String extractLocalName(String qualifiedName) {
        if (qualifiedName == null) {
            return "";
        }
        int braceIndex = qualifiedName.lastIndexOf('}');
        if (braceIndex >= 0 && braceIndex < qualifiedName.length() - 1) {
            return qualifiedName.substring(braceIndex + 1);
        }
        int colonIndex = qualifiedName.lastIndexOf(':');
        if (colonIndex >= 0 && colonIndex < qualifiedName.length() - 1) {
            return qualifiedName.substring(colonIndex + 1);
        }
        return qualifiedName;
    }
}
