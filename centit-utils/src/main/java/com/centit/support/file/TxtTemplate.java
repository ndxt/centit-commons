package com.centit.support.file;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/*
 * 基于模板的文本内容核对：比对两个文件，模板中用占位符标记的可变区域自动跳过
 * 占位符格式：{{字段名}} 或 ${字段名}
 */
public class TxtTemplate {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{.+?}}|\\$\\{.+?}");

    /*
     * 核对结果
     */
    public static class CompareResult {
        private boolean match;
        private String message;
        private int totalLines;
        private int diffLines;
        private int skippedLines;
        private final List<String> differences;

        public boolean isMatch() { return match; }
        public String getMessage() { return message; }
        public int getTotalLines() { return totalLines; }
        public int getDiffLines() { return diffLines; }
        public int getSkippedLines() { return skippedLines; }
        public List<String> getDifferences() { return differences; }

        public CompareResult(boolean match, String message) {
            this.match = match;
            this.message = message;
            this.differences = new ArrayList<>();
        }

        @Override
        public String toString() {
            if (match) return "[一致] " + message + "（跳过可变行" + skippedLines + "行）";
            StringBuilder sb = new StringBuilder();
            sb.append("[不一致] ").append(message);
            sb.append("，总计").append(totalLines).append("行，差异").append(diffLines).append("行，跳过可变").append(skippedLines).append("行");
            int show = Math.min(differences.size(), 30);
            for (int i = 0; i < show; i++) {
                sb.append("\n  ").append(differences.get(i));
            }
            if (differences.size() > 30) {
                sb.append("\n  ... 共 ").append(differences.size()).append(" 处差异");
            }
            return sb.toString();
        }
    }

    /*
     * 用模板文件核对实际文件
     * 模板中 {{xxx}} 或 ${xxx} 标记的行视为可变区域，跳过比对
     */
    public static CompareResult compareWithTemplate(File templateFile, File actualFile) {
        return compareWithTemplate(templateFile, actualFile, true);
    }

    public static CompareResult compareWithTemplate(File templateFile, File actualFile, boolean ignoreWhitespace) {
        if (templateFile == null || actualFile == null) {
            return new CompareResult(false, "文件参数为空");
        }
        if (!templateFile.exists() || !actualFile.exists()) {
            return new CompareResult(false, "文件不存在");
        }

        List<String> templateLines = readLines(templateFile);
        List<String> actualLines = readLines(actualFile);

        if (templateLines == null) return new CompareResult(false, "模板文件读取失败");
        if (actualLines == null) return new CompareResult(false, "待核文件读取失败");

        return compare(templateLines, actualLines, ignoreWhitespace);
    }

    /*
     * 用模板字符串核对实际文件
     */
    public static CompareResult compareWithTemplate(String templateContent, File actualFile) {
        return compareWithTemplate(templateContent, actualFile, true);
    }

    public static CompareResult compareWithTemplate(String templateContent, File actualFile, boolean ignoreWhitespace) {
        if (templateContent == null || actualFile == null || !actualFile.exists()) {
            return new CompareResult(false, "参数无效");
        }

        List<String> templateLines = Arrays.asList(templateContent.split("\\r?\\n"));
        List<String> actualLines = readLines(actualFile);
        if (actualLines == null) return new CompareResult(false, "文件读取失败");

        return compare(templateLines, actualLines, ignoreWhitespace);
    }

    /*
     * 用模板字符串核对字符串
     */
    public static CompareResult compareWithTemplate(String templateContent, String actualContent) {
        return compareWithTemplate(templateContent, actualContent, true);
    }

    public static CompareResult compareWithTemplate(String templateContent, String actualContent, boolean ignoreWhitespace) {
        List<String> templateLines = templateContent == null ? new ArrayList<>() : Arrays.asList(templateContent.split("\\r?\\n"));
        List<String> actualLines = actualContent == null ? new ArrayList<>() : Arrays.asList(actualContent.split("\\r?\\n"));
        return compare(templateLines, actualLines, ignoreWhitespace);
    }

    /*
     * 自定义占位符正则，核对文件
     */
    public static CompareResult compareWithPattern(File templateFile, File actualFile, String placeholderRegex) {
        return compareWithPattern(templateFile, actualFile, placeholderRegex, true);
    }

    public static CompareResult compareWithPattern(File templateFile, File actualFile, String placeholderRegex, boolean ignoreWhitespace) {
        if (templateFile == null || actualFile == null || !templateFile.exists() || !actualFile.exists()) {
            return new CompareResult(false, "文件不存在");
        }

        Pattern customPattern = Pattern.compile(placeholderRegex);
        List<String> templateLines = readLines(templateFile);
        List<String> actualLines = readLines(actualFile);
        if (templateLines == null || actualLines == null) {
            return new CompareResult(false, "文件读取失败");
        }

        return compare(templateLines, actualLines, ignoreWhitespace, customPattern);
    }

    /*
     * 核心比较逻辑（使用默认占位符 {{}}/${}）
     */
    private static CompareResult compare(List<String> templateLines, List<String> actualLines, boolean ignoreWhitespace) {
        return compare(templateLines, actualLines, ignoreWhitespace, PLACEHOLDER);
    }

    private static CompareResult compare(List<String> templateLines, List<String> actualLines, boolean ignoreWhitespace, Pattern pattern) {
        CompareResult result = new CompareResult(true, "内容一致");
        int maxLen = Math.max(templateLines.size(), actualLines.size());
        result.totalLines = maxLen;
        int diffCount = 0;
        int skipCount = 0;

        for (int i = 0; i < maxLen; i++) {
            String tLine = i < templateLines.size() ? templateLines.get(i) : null;
            String aLine = i < actualLines.size() ? actualLines.get(i) : null;

            // 模板行包含占位符，跳过该行
            if (tLine != null && pattern.matcher(tLine).find()) {
                skipCount++;
                continue;
            }

            String c1 = ignoreWhitespace ? normalize(tLine) : tLine;
            String c2 = ignoreWhitespace ? normalize(aLine) : aLine;

            if (!Objects.equals(c1, c2)) {
                diffCount++;
                if (result.differences.size() < 50) {
                    String d1 = tLine == null ? "(无)" : truncate(tLine, 100);
                    String d2 = aLine == null ? "(无)" : truncate(aLine, 100);
                    result.differences.add("第" + (i + 1) + "行: 模板[" + d1 + "] != 实际[" + d2 + "]");
                }
            }
        }

        result.skippedLines = skipCount;

        if (diffCount > 0) {
            result.match = false;
            result.diffLines = diffCount;
            result.message = "共" + maxLen + "行，" + diffCount + "行不同，跳过可变" + skipCount + "行";
        } else {
            result.message = "内容一致（跳过可变" + skipCount + "行）";
        }

        return result;
    }

    private static String normalize(String line) {
        if (line == null) return null;
        return line.replaceAll("\\s+", " ").trim();
    }

    private static String truncate(String s, int max) {
        if (s == null) return "(无)";
        if (s.length() <= max) return s;
        return s.substring(0, max) + "...";
    }

    private static List<String> readLines(File file) {
        try {
            String encoding = detectEncoding(file);
            return Files.readAllLines(file.toPath(), Charset.forName(encoding));
        } catch (Exception e) {
            return null;
        }
    }

    private static String detectEncoding(File file) {
        try {
            byte[] head = new byte[Math.min(3, (int) file.length())];
            try (FileInputStream fis = new FileInputStream(file)) {
                fis.read(head);
            }
            if (head.length >= 3 && (head[0] & 0xFF) == 0xEF && (head[1] & 0xFF) == 0xBB && (head[2] & 0xFF) == 0xBF) {
                return "UTF-8";
            }
            byte[] all = Files.readAllBytes(file.toPath());
            String test = new String(all, StandardCharsets.UTF_8);
            if (Arrays.equals(all, test.getBytes(StandardCharsets.UTF_8))) {
                return "UTF-8";
            }
        } catch (Exception ignored) {}
        return "GBK";
    }
}
