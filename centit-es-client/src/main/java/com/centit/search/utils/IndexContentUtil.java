package com.centit.search.utils;

import java.util.Map;

public abstract class IndexContentUtil {
    public static final int MAX_CONTENT_LENGTH = 32000;
    public static String truncateContent(String content){
        //elastic search 中的text字段最大长度为32766， 所以这里限制一下长度，如果长度大于 MAX_CONTENT_LENGTH
        // 1. 删除字符串中 没有意义的符号 、连续的空格 、连续的换行 和 非现实字符
        // 2. 如果仍然超过 MAX_CONTENT_LENGTH 则截断掉
        if (content == null || content.length() <= MAX_CONTENT_LENGTH) {
            return content;
        }
        // 删除无意义符号、连续空格、连续换行和非显示字符
        String cleaned = content
            .replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "") // 删除控制字符，保留回车、换行、制表符
            .replaceAll("\\s+", " ") // 将连续空白字符替换为单个空格
            .replaceAll("[\r\n]+", "\n") // 将连续换行替换为单个换行
            .replaceAll("[\\p{So}\\p{Sk}\\p{Sm}\\p{Sc}&&[^\\p{L}\\p{N}\\p{P}\\s]]", "") // 删除特殊符号，保留字母、数字、标点、空格
            .trim();

        // 如果长度超过限制则截断
        if (cleaned.length() > MAX_CONTENT_LENGTH) {
            return cleaned.substring(0, MAX_CONTENT_LENGTH);
        }
        return cleaned;
    }

    public static void truncateIndexObject(Map<String, Object> indexedObject){
        //检查这个Map indexedObject 中的value 是不是字符串，如果是字符串，调用 truncateContent 截断
        if (indexedObject == null) {
            return;
        }
        for (Map.Entry<String, Object> entry : indexedObject.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof String) {
                String truncated = truncateContent((String) value);
                entry.setValue(truncated);
            }
        }
    }
}
