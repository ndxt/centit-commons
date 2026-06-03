package com.centit.search.utils;

import java.util.Map;

public abstract class IndexContentUtil {
    public static final int MAX_CONTENT_LENGTH = 32000;
    public static String truncateContent(String content){
        //根据 content 创建一个不超过 MAX_CONTENT_LENGTH 长度的语义摘要，主要处理中文
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

        if (cleaned.length() <= MAX_CONTENT_LENGTH) {
            return cleaned;
        }
        // 语义摘要：提取关键信息
        String[] sentences = cleaned.split("[。！？\\.]");
        StringBuilder summary = new StringBuilder();
        // 优先选择包含关键词的句子
        String[] keywords = {"重要", "关键", "主要", "核心", "总结", "结论", "目标", "问题", "解决", "方案", "结果", "思路",
            "分析", "讨论", "建议", "首先", "第一", "重点", "关注", "注意", "中心", "领导", "资质", "材料", "保障", "文件"};
        // 第一轮：选择包含关键词的句子
        for (String sentence : sentences) {
            String trimmedSentence = sentence.trim();
            if (trimmedSentence.length() < 10) continue;

            boolean hasKeyword = false;
            for (String keyword : keywords) {
                if (trimmedSentence.contains(keyword)) {
                    hasKeyword = true;
                    break;
                }
            }
            if (hasKeyword) {
                String testSummary = summary.isEmpty() ? trimmedSentence : summary + "。" + trimmedSentence;
                if (testSummary.length() > MAX_CONTENT_LENGTH) {
                    break;
                }
                if (!summary.isEmpty()) {
                    summary.append("。");
                }
                summary.append(trimmedSentence);
            }
        }
        // 第二轮：如果摘要还有空间，添加其他较长的句子
        if (summary.length() < MAX_CONTENT_LENGTH * 0.7) {
            for (String sentence : sentences) {
                String trimmedSentence = sentence.trim();
                if (trimmedSentence.length() < 20) continue;
                // 跳过已经添加的句子
                if (summary.toString().contains(trimmedSentence)) continue;
                String testSummary = summary.isEmpty() ? trimmedSentence : summary + "。" + trimmedSentence;
                if (testSummary.length() > MAX_CONTENT_LENGTH) {
                    break;
                }
                if (!summary.isEmpty()) {
                    summary.append("。");
                }
                summary.append(trimmedSentence);
            }
        }
        // 如果没有找到合适的句子，取前面部分
        if (summary.isEmpty()) {
            return cleaned.substring(0, MAX_CONTENT_LENGTH);
        }
        return summary.toString();
    }

    public static void truncateIndexObject(Map<String, Object> indexedObject){
        //检查这个Map indexedObject 中的value 是不是字符串，如果是字符串，调用 truncateContent 截断
        if (indexedObject == null) {
            return;
        }
        for (Map.Entry<String, Object> entry : indexedObject.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof String strValue) {
                String truncated = truncateContent(strValue);
                entry.setValue(truncated);
            }
        }
    }
}
