package com.centit.support.test.utils;

import com.centit.support.compiler.ConstDefine;
import com.centit.support.compiler.EmbedFunc;
import com.centit.support.compiler.VariableFormula;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * 验证 EmbedFunc.regexmatchgroups 的实际行为。
 * 核心用例直接调用 EmbedFunc.runFuncWithObject（绕过表达式解析，可靠验证函数实现）；
 * 另含一例通过 VariableFormula 表达式调用，验证端到端可用。
 */
public class TestRegexMatchGroups {

    @Test
    public void multiMatchWithNames() {
        Object r = EmbedFunc.runFuncWithObject(
            Arrays.asList("(\\w+)=(\\w+)", "a=1&b=2", "key", "value"),
            ConstDefine.FUNC_REG_MATCH_GROUPS);
        assertNotNull(r);
        assertTrue("多匹配应返回 List", r instanceof List);
        List<?> list = (List<?>) r;
        assertEquals(2, list.size());
        assertEquals("a", ((Map<?, ?>) list.get(0)).get("key"));
        assertEquals("1", ((Map<?, ?>) list.get(0)).get("value"));
        assertEquals("b", ((Map<?, ?>) list.get(1)).get("key"));
        assertEquals("2", ((Map<?, ?>) list.get(1)).get("value"));
    }

    @Test
    public void singleMatchReturnsMap() {
        Object r = EmbedFunc.runFuncWithObject(
            Arrays.asList("(\\d{4})-(\\d{2})-(\\d{2})", "2026-06-18", "year", "month", "day"),
            ConstDefine.FUNC_REG_MATCH_GROUPS);
        assertNotNull(r);
        assertTrue("单匹配应返回 Map", r instanceof Map);
        Map<?, ?> m = (Map<?, ?>) r;
        assertEquals("2026", m.get("year"));
        assertEquals("06", m.get("month"));
        assertEquals("18", m.get("day"));
    }

    @Test
    public void defaultGroupNamesWhenNoNames() {
        // 不传 names，分组用默认名 g1/g2
        Object r = EmbedFunc.runFuncWithObject(
            Arrays.asList("(\\w+)=(\\w+)", "a=1"),
            ConstDefine.FUNC_REG_MATCH_GROUPS);
        assertNotNull(r);
        assertTrue(r instanceof Map);
        Map<?, ?> m = (Map<?, ?>) r;
        assertEquals("a", m.get("g1"));
        assertEquals("1", m.get("g2"));
    }

    @Test
    public void noMatchReturnsNull() {
        Object r = EmbedFunc.runFuncWithObject(
            Arrays.asList("\\d+", "abc"),
            ConstDefine.FUNC_REG_MATCH_GROUPS);
        assertNull("无匹配应返回 null", r);
    }

    @Test
    public void noCaptureGroupUsesWholeMatch() {
        // 无捕获组，整匹配放入 name1
        Object r = EmbedFunc.runFuncWithObject(
            Arrays.asList("\\w+", "hello world", "word"),
            ConstDefine.FUNC_REG_MATCH_GROUPS);
        assertNotNull(r);
        assertTrue(r instanceof List);
        List<?> list = (List<?>) r;
        assertEquals(2, list.size());
        assertEquals("hello", ((Map<?, ?>) list.get(0)).get("word"));
        assertEquals("world", ((Map<?, ?>) list.get(1)).get("word"));
    }

    @Test
    public void namesAsList() {
        // names 以 List 整体传入，验证 flatOperands 展平
        Object r = EmbedFunc.runFuncWithObject(
            Arrays.asList("(\\w+)=(\\w+)", "a=1&b=2", Arrays.asList("key", "value")),
            ConstDefine.FUNC_REG_MATCH_GROUPS);
        assertNotNull(r);
        assertTrue("List 整体传入也应返回 List", r instanceof List);
        List<?> list = (List<?>) r;
        assertEquals(2, list.size());
        assertEquals("a", ((Map<?, ?>) list.get(0)).get("key"));
        assertEquals("1", ((Map<?, ?>) list.get(0)).get("value"));
    }

    /**
     * 通过 VariableFormula 表达式调用。
     * 注意：VariableFormula 读入表达式时会再转义一次——表达式里每两个 \\ 对应正则里的一个 \。
     * 故 Java 代码 "\\\\w"（4个反斜杠）→ 表达式字符串 "\\w"（2个）→ 解析为正则 "\w"（1个）。
     */
    @Test
    public void viaFormulaExpression() {
        Object r = VariableFormula.calculate(
            "regexmatchgroups('(\\\\w+)=(\\\\w+)', 'a=1&b=2', 'key', 'value')");
        assertNotNull(r);
        assertTrue(r instanceof List);
        List<?> list = (List<?>) r;
        assertEquals(2, list.size());
        assertEquals("a", ((Map<?, ?>) list.get(0)).get("key"));
        assertEquals("1", ((Map<?, ?>) list.get(0)).get("value"));
    }
}
