package com.formula.calculator;

import com.alibaba.fastjson.JSONObject;
import org.junit.Test;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * 迁移前固定的行为契约；预期值不依赖新旧入口互相比较。
 */
public class CompatibilityContractTest {
    @Test
    public void preservesResultTypesAndFieldConversions() {
        JSONObject data = new JSONObject();
        data.put("price", "12.5");
        data.put("invalid", "abc");
        data.put("empty", null);
        assertNull(FormulaCalculator.calculate(" ", data));
        assertEquals(Long.valueOf(3), FormulaCalculator.calculate("1+2", data));
        assertEquals(Double.valueOf(2.5), FormulaCalculator.calculate("5/2", data));
        assertEquals("hello", FormulaCalculator.calculate("\"hello\"", data));
        assertEquals(Long.valueOf(25), FormulaCalculator.calculate("${price}*2", data));
        assertEquals(Long.valueOf(0), FormulaCalculator.calculate("${missing}+[empty]+${invalid}", data));
        assertEquals(Long.valueOf(25), FormulaCalculator.calculate("单价*2", data,
                Collections.singletonMap("单价", "price")));
    }

    @Test
    public void preservesRepresentativeBusinessResults() {
        JSONObject data = new JSONObject();
        assertEquals(Long.valueOf(29), FormulaCalculator.calculate("ABS(-10)+SUM(5,10)+SQRT(16)", data));
        assertEquals(Long.valueOf(8), FormulaCalculator.calculate("POWER(2,3)", data));
        assertEquals(Long.valueOf(1), FormulaCalculator.calculate("IF(2>1,1,0)", data));
        assertEquals("Hello123World", FormulaCalculator.calculate("CONCATENATE(\"Hello\",123,\"World\")", data));
        assertTrue(FormulaUtils.verify("0.1+0.2", data, 0.3));
        // 旧 verify 仅做数字比较，即便两边文本相同也返回 false。
        assertFalse(FormulaUtils.verify("\"hello\"", data, "hello"));
        assertFalse(FormulaUtils.verify("UNKNOWN_FUNCTION(1)", data, 0));
    }

    @Test(expected = FormulaException.class)
    public void preservesUnknownFunctionFailure() {
        FormulaCalculator.calculate("UNKNOWN_FUNCTION(1)", new JSONObject());
    }

    @Test
    public void preservesCustomFunctionLifecycle() {
        AtomicInteger calls = new AtomicInteger();
        FormulaCalculator.registerCustomFunction("CONTRACTFUNC", (params, data, mapping) -> {
            calls.incrementAndGet();
            return "7";
        });
        try {
            assertEquals(Long.valueOf(8), FormulaCalculator.calculate("CONTRACTFUNC()+1", new JSONObject()));
            assertEquals(1, calls.get());
        } finally {
            FormulaCalculator.unregisterCustomFunction("CONTRACTFUNC");
        }
    }
}
