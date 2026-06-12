package com.centit.support.xml;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * XSD验证错误翻译器
 * 将XML Schema验证的英文错误消息翻译为友好的中文提示
 */
public class XsdErrorTranslator {

    // 常见错误模式匹配
    private static final Pattern CVC_COMPLEX_TYPE_2_4 = Pattern.compile(
        "cvc-complex-type\\.2\\.4[a-z]?:.*element '([^']+)'.*",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern CVC_COMPLEX_TYPE_3_2_1 = Pattern.compile(
        "cvc-complex-type\\.3\\.2\\.1:.*attribute '([^']+)'.*element '([^']+)'.*",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern CVC_COMPLEX_TYPE_3_2_2 = Pattern.compile(
        "cvc-complex-type\\.3\\.2\\.2:.*attribute '([^']+)'.*",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern CVC_ELEMENT_MINIMAX_OCCURS = Pattern.compile(
        "cvc-complex-type\\.2\\.3:.*element '([^']+)'.*",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern CVC_DATATYPE_VALID_1_2_3 = Pattern.compile(
        "cvc-datatype-valid\\.1\\.2\\.3:.*'([^']+)'.*'([^']+)'.*",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern CVC_TYPE_3_1_3 = Pattern.compile(
        "cvc-type\\.3\\.1\\.3:.*element '([^']+)'.*",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern CVC_ELT_CONTENT_VALID = Pattern.compile(
        "cvc-elt\\.3\\.2\\.2:.*element '([^']+)'.*",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern SRC_ELEMENT_3_1 = Pattern.compile(
        "src-element\\.3\\.1:.*element '([^']+)'.*",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern CVC_DATATYPE_VALID_1_2_1 = Pattern.compile(
        "cvc-datatype-valid\\.1\\.2\\.1:\\s*'([^']*)'\\s+is\\s+.+?\\s+for\\s+'([^']+)'",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern CVC_ENUMERATION_VALID = Pattern.compile(
        "cvc-enumeration-valid:\\s*Value\\s+'([^']+)'\\s+is\\s+not\\s+facet-valid\\s+with\\s+respect\\s+to\\s+enumeration\\s+'\\[([^\\]]+)\\]'",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern CVC_IDENTITY_CONSTRAINT_4_1 = Pattern.compile(
        "cvc-identity-constraint\\.4\\.1:\\s*Duplicate\\s+unique\\s+value\\s+\\[([^\\]]+)\\]\\s+found\\s+for\\s+identity\\s+constraint\\s+\"([^\"]+)\"\\s+of\\s+element\\s+\"([^\"]+)\"",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern CVC_MIN_LENGTH_VALID = Pattern.compile(
        "cvc-minLength-valid:\\s*Value\\s+'([^']*)'\\s+with\\s+length\\s*=\\s*'?(\\d+)'?\\s+is\\s+not\\s+facet-valid\\s+with\\s+respect\\s+to\\s+minLength\\s+'?(\\d+)'?\\s+for\\s+type\\s+'([^']+)'",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern CVC_LENGTH_VALID = Pattern.compile(
        "cvc-length-valid:\\s*Value\\s+'([^']*)'\\s+with\\s+length\\s*=\\s*'?(\\d+)'?\\s+is\\s+not\\s+facet-valid\\s+with\\s+respect\\s+to\\s+length\\s+'?(\\d+)'?\\s+for\\s+type\\s+'([^']+)'",
        Pattern.CASE_INSENSITIVE
    );

    // XML 特殊字符相关错误模式
    private static final Pattern INVALID_CHARACTER_ENTITY = Pattern.compile(
        "(?:The entity|Entity) \".*?\" was referenced, but not declared",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern INVALID_XML_CHARACTER = Pattern.compile(
        "An invalid XML character \\(Unicode: (0x[0-9a-f]+)\\) (?:was found|in element)",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern INVALID_CHARACTER_IN_ELEMENT = Pattern.compile(
        "Invalid character (?:\\(Unicode: 0x([0-9a-f]+)\\))? (?:was found|in) the element content",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern ILLEGAL_CHARACTER = Pattern.compile(
        "(?:Character|char) (?:0x([0-9a-f]+)|\"([^\"]+)\") is (?:an invalid|not a valid) XML character",
        Pattern.CASE_INSENSITIVE
    );

    /**
     * 允许的字符正则表达式
     * 包含：中文、英文字母、数字、CJK标点符号、空白字符、常用键盘标点符号
     * 键盘标点：;:"'()[]{}?!@#$%^&*~`|<>=+_-
     */
    private static final Pattern ALLOWED_CHARS_PATTERN = Pattern.compile(
        "^[\\p{IsHan}a-zA-Z0-9\\u3000-\\u303F\\s;:'\"()\\[\\]{}!@#$%^&*~`|<>=+_,./\\\\-]+$"
    );

    /**
     * 检测内容中是否包含特殊字符
     * @param content 要检测的内容
     * @return 如果存在特殊字符，返回特殊字符的描述；否则返回 null
     */
    public static String detectInvalidCharacters(String content) {
        if (content == null || content.isEmpty()) {
            return null;
        }

        // 使用正则表达式检查是否只包含允许的字符
        if (ALLOWED_CHARS_PATTERN.matcher(content).matches()) {
            return null;
        }

        // 如果包含不允许的字符，找出所有特殊字符
        StringBuilder specialChars = new StringBuilder();
        StringBuilder positions = new StringBuilder();

        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            String charStr = String.valueOf(c);

            // 检查字符是否匹配允许的模式
            if (!ALLOWED_CHARS_PATTERN.matcher(charStr).matches()) {
                if (specialChars.length() > 0) {
                    specialChars.append(", ");
                    positions.append(", ");
                }
                specialChars.append(String.format("'%c' (Unicode: 0x%04X)", c, (int) c));
                positions.append(i);
            }
        }

        if (specialChars.length() > 0) {
            return "检测到特殊字符: " + specialChars + "，位置: " + positions;
        }
        return null;
    }

    /**
     * 检查字符是否是 XML 1.0 中的非法字符
     * XML 1.0 规范允许：#x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD]
     * 注意：char 类型是 16 位，最大值为 0xFFFF，所以不需要检查 0x10000-0x10FFFF 范围
     */
    @SuppressWarnings("unused")
    private static boolean isInvalidXmlChar(char c) {
        return !(
            c == 0x9 || c == 0xA || c == 0xD ||  // Tab, LF, CR
            (c >= 0x20 && c <= 0xD7FF) ||
            (c >= 0xE000 && c <= 0xFFFD)
        );
    }

    /**
     * 检测需要转义的 XML 特殊字符
     * @param content 要检测的内容
     * @return 如果存在未转义的特殊字符，返回描述；否则返回 null
     */
    public static String detectUnescapedSpecialChars(String content) {
        if (content == null || content.isEmpty()) {
            return null;
        }

        StringBuilder warnings = new StringBuilder();

        // 检查未转义的 & 符号（但排除合法的实体引用）
        int ampIndex = 0;
        while ((ampIndex = content.indexOf('&', ampIndex)) != -1) {
            // 检查是否是合法的实体引用
            if (!isValidEntityReference(content, ampIndex)) {
                if (warnings.length() > 0) {
                    warnings.append(", ");
                }
                warnings.append("发现未转义的 '&' 符号在位置 ").append(ampIndex);
            }
            ampIndex++;
        }

        // 检查未转义的 < 符号
        int ltIndex = 0;
        while ((ltIndex = content.indexOf('<', ltIndex)) != -1) {
            if (!isValidTagStart(content, ltIndex)) {
                if (warnings.length() > 0) {
                    warnings.append(", ");
                }
                warnings.append("发现未转义的 '<' 符号在位置 ").append(ltIndex);
            }
            ltIndex++;
        }

        // 检查未转义的 ]]> 序列（在 CDATA 中会导致问题）
        int cdataEndIndex = content.indexOf("]]>");
        if (cdataEndIndex != -1 && !content.contains("<![CDATA[")) {
            if (warnings.length() > 0) {
                warnings.append(", ");
            }
            warnings.append("发现未转义的 ']]>' 序列在位置 ").append(cdataEndIndex);
        }

        return warnings.length() > 0 ? warnings.toString() : null;
    }

    /**
     * 检查 & 符号是否是合法的实体引用
     */
    private static boolean isValidEntityReference(String content, int ampIndex) {
        int endIndex = content.indexOf(';', ampIndex);
        if (endIndex == -1 || endIndex - ampIndex > 10) {
            return false;
        }

        String entity = content.substring(ampIndex + 1, endIndex);
        // 检查是否是预定义实体或数字字符引用
        return entity.matches("amp|lt|gt|quot|apos|#\\d+|#x[0-9a-fA-F]+");
    }

    /**
     * 检查 < 符号是否是合法的标签开始
     */
    private static boolean isValidTagStart(String content, int ltIndex) {
        if (ltIndex + 1 >= content.length()) {
            return false;
        }

        char nextChar = content.charAt(ltIndex + 1);
        // 允许的标签开始字符：字母、下划线、问号（XML声明）、感叹号（注释/CDATA）
        return Character.isLetter(nextChar) || nextChar == '_' || nextChar == '?' ||
               nextChar == '!' || nextChar == '/';
    }

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
            Matcher matcher24a = CVC_COMPLEX_TYPE_2_4.matcher(message);
            if (matcher24a.find()) {
                String elementName = matcher24a.group(1);
                return "元素 '" + extractLocalName(elementName) + "' 的内容不完整，缺少必需的子元素";
            }
        }

        // cvc-complex-type.3.2.1: 缺少必需属性
        matcher = CVC_COMPLEX_TYPE_3_2_1.matcher(message);
        if (matcher.find()) {
            String attrName = matcher.group(1);
            String elementName = matcher.group(2);
            return "元素 '" + extractLocalName(elementName) + "' 缺少必需的属性 '" +
                   extractLocalName(attrName) + "'";
        }

        // cvc-complex-type.3.2.2: 出现未声明的属性
        matcher = CVC_COMPLEX_TYPE_3_2_2.matcher(message);
        if (matcher.find()) {
            String attrName = matcher.group(1);
            return "属性 '" + extractLocalName(attrName) + "' 未在元素中声明，不允许使用";
        }

        // cvc-complex-type.2.3: 元素出现次数不符合要求
        matcher = CVC_ELEMENT_MINIMAX_OCCURS.matcher(message);
        if (matcher.find()) {
            String elementName = matcher.group(1);
            return "元素 '" + extractLocalName(elementName) + "' 的出现次数不符合schema定义的要求";
        }

        // cvc-datatype-valid.1.2.3: 数据类型验证失败
        matcher = CVC_DATATYPE_VALID_1_2_3.matcher(message);
        if (matcher.find()) {
            String value = matcher.group(1);
            String dataType = matcher.group(2);
            return "值 '" + value + "' 不符合数据类型 '" + dataType + "' 的格式要求";
        }

        // cvc-type.3.1.3: 元素值为空但不应为空
        matcher = CVC_TYPE_3_1_3.matcher(message);
        if (matcher.find()) {
            String elementName = matcher.group(1);
            return "元素 '" + extractLocalName(elementName) + "' 的值不能为空";
        }

        // cvc-elt.3.2.2: 元素内容验证失败
        matcher = CVC_ELT_CONTENT_VALID.matcher(message);
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
        matcher = CVC_DATATYPE_VALID_1_2_1.matcher(message);
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
        matcher = CVC_ENUMERATION_VALID.matcher(message);
        if (matcher.find()) {
            String invalidValue = matcher.group(1);
            String validValues = matcher.group(2);
            return "值 '" + invalidValue + "' 不在允许的枚举值范围 [" + validValues + "] 内";
        }

        // cvc-identity-constraint.4.1: 唯一性约束冲突
        matcher = CVC_IDENTITY_CONSTRAINT_4_1.matcher(message);
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
        matcher = CVC_MIN_LENGTH_VALID.matcher(message);
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
        matcher = CVC_LENGTH_VALID.matcher(message);
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


        // 特殊字符相关错误翻译

        // 无效的 XML 字符
        matcher = INVALID_XML_CHARACTER.matcher(message);
        if (matcher.find()) {
            String charCode = matcher.group(1);
            return "XML 中包含非法字符 (Unicode: " + charCode + ")，该字符不符合 XML 1.0 规范";
        }

        // 元素内容中的无效字符
        matcher = INVALID_CHARACTER_IN_ELEMENT.matcher(message);
        if (matcher.find()) {
            String charCode = matcher.group(1);
            if (charCode != null) {
                return "元素内容中包含非法字符 (Unicode: 0x" + charCode + ")";
            }
            return "元素内容中包含非法字符，请检查是否包含控制字符或其他无效字符";
        }

        // 非法 XML 字符
        matcher = ILLEGAL_CHARACTER.matcher(message);
        if (matcher.find()) {
            String charCode = matcher.group(1);
            String charLiteral = matcher.group(2);
            if (charCode != null) {
                return "字符 0x" + charCode + " 不是合法的 XML 字符";
            }
            if (charLiteral != null) {
                return "字符 '" + charLiteral + "' 不是合法的 XML 字符";
            }
            return "包含非法的 XML 字符";
        }

        // 实体引用错误
        matcher = INVALID_CHARACTER_ENTITY.matcher(message);
        if (matcher.find()) {
            return "XML 中引用了未声明的实体，请检查实体引用是否正确或需要在 DTD 中声明";
        }

        // 通用特殊字符错误检测
        String lowerMessage = message.toLowerCase();
        if (lowerMessage.contains("invalid character") || lowerMessage.contains("illegal character")) {
            if (lowerMessage.contains("unicode")) {
                return "XML 中包含非法字符，该字符不符合 XML 规范要求";
            }
            return "XML 中包含无效字符，请检查内容";
        }

        if (lowerMessage.contains("entity") && lowerMessage.contains("not declared")) {
            return "XML 中引用了未声明的实体，请检查实体引用";
        }

        return null;
    }

    /**
     * 通用翻译规则
     */
    private static String translateGeneral(String message) {
        String lowerMessage = message.toLowerCase();


        // 特殊字符相关错误优先处理
        if (lowerMessage.contains("invalid character") || lowerMessage.contains("illegal character")) {
            return message.replaceAll("(?i)invalid character", "非法字符")
                         .replaceAll("(?i)illegal character", "非法字符")
                         .replaceAll("(?i)was found", "被发现")
                         .replaceAll("(?i)in the element", "在元素中");
        }

        if (lowerMessage.contains("entity") && lowerMessage.contains("not declared")) {
            return message.replaceAll("(?i)entity", "实体")
                         .replaceAll("(?i)not declared", "未声明")
                         .replaceAll("(?i)was referenced", "被引用");
        }

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
     * 例如: {@code {http://example.com}localName} -> localName
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
