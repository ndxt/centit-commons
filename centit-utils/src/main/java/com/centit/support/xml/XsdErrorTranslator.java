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
    
    private static final Pattern CVc_DATATYPE_VALID_1_2_1 = Pattern.compile(
        "cvc-datatype-valid\\.1\\.2\\.1:\\s*'([^']*)'\\s+is\\s+.+?\\s+for\\s+'([^']+)'",
        Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern CVc_ENUMERATION_VALID = Pattern.compile(
        "cvc-enumeration-valid:\\s*Value\\s+'([^']+)'\\s+is\\s+not\\s+facet-valid\\s+with\\s+respect\\s+to\\s+enumeration\\s+'\\[([^\\]]+)\\]'",
        Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern CVc_IDENTITY_CONSTRAINT_4_1 = Pattern.compile(
        "cvc-identity-constraint\\.4\\.1:\\s*Duplicate\\s+unique\\s+value\\s+\\[([^\\]]+)\\]\\s+found\\s+for\\s+identity\\s+constraint\\s+\"([^\"]+)\"\\s+of\\s+element\\s+\"([^\"]+)\"",
        Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern CVc_MIN_LENGTH_VALID = Pattern.compile(
        "cvc-minLength-valid:\\s*Value\\s+'([^']*)'\\s+with\\s+length\\s*=\\s*'?(\\d+)'?\\s+is\\s+not\\s+facet-valid\\s+with\\s+respect\\s+to\\s+minLength\\s+'?(\\d+)'?\\s+for\\s+type\\s+'([^']+)'",
        Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern CVc_LENGTH_VALID = Pattern.compile(
        "cvc-length-valid:\\s*Value\\s+'([^']*)'\\s+with\\s+length\\s*=\\s*'?(\\d+)'?\\s+is\\s+not\\s+facet-valid\\s+with\\s+respect\\s+to\\s+length\\s+'?(\\d+)'?\\s+for\\s+type\\s+'([^']+)'",
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
        Matcher matcher;
        // cvc-complex-type.2.4.a: 元素缺失或无效内容
        if (message.startsWith("cvc-complex-type.2.4.a")) {
            // 匹配: 无效的内容 was found starting with element 'X'. One of '{Y}' is expected.
            Pattern pattern24a = Pattern.compile(
                "(?:无效的内容|Invalid content).*(?:starting with element|element) '([^']+)'[^']*One of '\\{([^}]+)\\}' is expected",
                Pattern.CASE_INSENSITIVE
            );
            Matcher m = pattern24a.matcher(message);
            if (m.find()) {
                String elementName = m.group(1);
                String expectedElements = m.group(2);
                return "元素 '" + extractLocalName(elementName) + "' 处发现无效内容，期望的元素是: " + expectedElements;
            }
            // 简化匹配: 只提取元素名
            Matcher matcher24a = CVc_COMPLEX_TYPE_2_4.matcher(message);
            if (matcher24a.find()) {
                String elementName = matcher24a.group(1);
                return "元素 '" + extractLocalName(elementName) + "' 的内容不完整，缺少必需的子元素";
            }
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
        
        // cvc-datatype-valid.1.2.1: 数据类型格式无效(如空值)
        matcher = CVc_DATATYPE_VALID_1_2_1.matcher(message);
        if (matcher.find()) {
            String value = matcher.group(1);
            String dataType = matcher.group(2);
            String chineseDataType = translateDataType(dataType);
            if (value == null || value.isEmpty()) {
                return "元素 '" + extractLocalName(dataType) + "' 的值不能为空";
            } else {
                return "值 '" + value + "' 不是有效的" + chineseDataType + "格式";
            }
        }
        
        // cvc-enumeration-valid: 枚举值验证失败
        matcher = CVc_ENUMERATION_VALID.matcher(message);
        if (matcher.find()) {
            String invalidValue = matcher.group(1);
            String validValues = matcher.group(2);
            return "值 '" + invalidValue + "' 不在允许的枚举值范围 [" + validValues + "] 内";
        }
        
        // cvc-identity-constraint.4.1: 唯一性约束冲突
        matcher = CVc_IDENTITY_CONSTRAINT_4_1.matcher(message);
        if (matcher.find()) {
            String duplicateValue = matcher.group(1);
            String constraintName = matcher.group(2);
            String elementName = matcher.group(3);
            return "元素 '" + extractLocalName(elementName) + "' 的唯一性约束 '" + constraintName + 
                   "' 发现重复值 [" + duplicateValue + "]";
        }
        
        // cvc-complex-type.2.4.b: 元素内容不完整(带期望元素列表)
        if (message.startsWith("cvc-complex-type.2.4.b")) {
            // 匹配: The content of element 'X' is not complete. One of '{Y}' is expected.
            Pattern pattern24b = Pattern.compile(
                "The content of element '([^']+)'[^']*One of '\\{([^}]+)\\}' is expected",
                Pattern.CASE_INSENSITIVE
            );
            Matcher m = pattern24b.matcher(message);
            if (m.find()) {
                String elementName = m.group(1);
                String expectedElements = m.group(2);
                return "元素 '" + extractLocalName(elementName) + "' 的内容不完整，缺少必需的子元素: " + expectedElements;
            }
            // 如果无法解析详细信息,返回通用消息
            Pattern pattern24bSimple = Pattern.compile(
                "The content of element '([^']+)'[^不]*不完整",
                Pattern.CASE_INSENSITIVE
            );
            m = pattern24bSimple.matcher(message);
            if (m.find()) {
                String elementName = m.group(1);
                return "元素 '" + extractLocalName(elementName) + "' 的内容不完整";
            }
        }
        
        // cvc-minLength-valid: 最小长度验证失败
        matcher = CVc_MIN_LENGTH_VALID.matcher(message);
        if (matcher.find()) {
            String value = matcher.group(1);
            String actualLength = matcher.group(2);
            String minLength = matcher.group(3);
            String typeName = matcher.group(4);
            if (value == null || value.isEmpty() || "0".equals(actualLength)) {
                return "元素 '" + extractLocalName(typeName) + "' 的值不能为空，至少需要" + minLength + "个字符";
            } else {
                return "值 '" + value + "' 的长度(" + actualLength + ")小于要求的最小长度(" + minLength + ")";
            }
        }
        
        // cvc-length-valid: 固定长度验证失败
        matcher = CVc_LENGTH_VALID.matcher(message);
        if (matcher.find()) {
            String value = matcher.group(1);
            String actualLength = matcher.group(2);
            String requiredLength = matcher.group(3);
            String typeName = matcher.group(4);
            if (value == null || value.isEmpty() || "0".equals(actualLength)) {
                return "元素 '" + extractLocalName(typeName) + "' 的值不能为空，需要" + requiredLength + "个字符";
            } else {
                return "值 '" + value + "' 的长度(" + actualLength + ")不符合要求的长度(" + requiredLength + ")";
            }
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
    
    /**
     * 翻译数据类型名称为中文
     */
    private static String translateDataType(String dataType) {
        if (dataType == null || dataType.isEmpty()) {
            return "数据";
        }
        
        String lowerType = dataType.toLowerCase();
        switch (lowerType) {
            case "date":
                return "日期";
            case "datetime":
            case "date-time":
                return "日期时间";
            case "time":
                return "时间";
            case "integer":
            case "int":
                return "整数";
            case "decimal":
            case "double":
            case "float":
                return "数字";
            case "string":
                return "字符串";
            case "boolean":
                return "布尔值";
            default:
                return dataType;
        }
    }
}
