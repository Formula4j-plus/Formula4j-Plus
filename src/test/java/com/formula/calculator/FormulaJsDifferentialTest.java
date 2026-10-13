package com.formula.calculator;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.formula.calculator.api.StrictFormulaException;
import com.formula.calculator.core.strict.StrictFormulaEngine;
import org.junit.Test;

import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import static org.junit.Assert.*;

/** 离线 Java 契约测试与 CI 差分报告导出；期望值来自已审核并固定的真实运行结果。 */
public class FormulaJsDifferentialTest {
    @Test
    public void verifiesRecordedJavaResultsAndExportsOracleInput() throws Exception {
        JSONObject corpus;
        try (InputStream input = getClass().getResourceAsStream("/compatibility/formulajs-4.6.1.json")) {
            assertNotNull("差分语料必须存在", input);
            corpus = JSON.parseObject(input, JSONObject.class);
        }
        StrictFormulaEngine strict = new StrictFormulaEngine();
        Map<String, Object> results = new LinkedHashMap<>();
        for (Object item : corpus.getJSONArray("cases")) {
            JSONObject fixture = (JSONObject) item;
            String id = fixture.getString("id");
            String formula = fixture.getString("formula");
            JSONObject data = fixture.getJSONObject("data");
            Map<String, Object> engines = new LinkedHashMap<>();
            engines.put("legacy", capture(() -> FormulaCalculator.calculate(formula,
                    data == null ? new JSONObject() : data)));
            engines.put("strict", capture(() -> strict.execute(formula, data)));
            results.put(id, engines);
            JSONObject recorded = fixture.getJSONObject("javaExpected");
            assertNotNull(id + " 缺少已审核 Java 期望值", recorded);
            assertEquals(id + " 的 Java 行为发生变化，需要审核兼容契约", recorded,
                    JSON.parseObject(JSON.toJSONString(engines)));
        }
        assertEquals("语料不允许重复 id", corpus.getJSONArray("cases").size(), results.size());
        String output = System.getProperty("formula.differential.output");
        if (output != null) {
            Map<String, Object> report = new LinkedHashMap<>();
            report.put("formulajsVersion", corpus.getString("formulajsVersion"));
            report.put("cases", results);
            report.put("strictFunctions", strict.getFunctions());
            Files.write(Paths.get(output), JSON.toJSONString(report, true).getBytes(StandardCharsets.UTF_8));
        }
    }

    private static Map<String, Object> capture(Supplier<Object> calculation) {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            Object value = calculation.get();
            if (value == null) result.put("type", "null");
            else if (value instanceof Number) {
                result.put("type", "number");
                result.put("value", new BigDecimal(value.toString()).stripTrailingZeros().toPlainString());
            } else if (value instanceof Boolean) {
                result.put("type", "boolean");
                result.put("value", value);
            } else if (value instanceof String) {
                result.put("type", "string");
                result.put("value", value);
            } else {
                throw new AssertionError("语料出现未建模的类型: " + value.getClass());
            }
        } catch (RuntimeException failure) {
            result.put("type", "error");
            result.put("code", failure instanceof StrictFormulaException
                    ? ((StrictFormulaException) failure).getCode().name() : "LEGACY_ERROR");
        }
        return result;
    }
}
