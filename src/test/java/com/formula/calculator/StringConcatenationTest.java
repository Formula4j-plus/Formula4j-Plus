package com.formula.calculator;

import com.alibaba.fastjson.JSONObject;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 字符串拼接测试
 */
public class StringConcatenationTest {
    
    @Test
    public void testStringConcatenation() {
        // 测试：CONCATENATE(字段1,字段2)+'==='+SUM(字段1,字段2)
        String formula = "CONCATENATE(字段1,字段2)+\"===\"+SUM(字段1,字段2)";
        
        JSONObject data = new JSONObject();
        data.put("field1", "Hello");
        data.put("field2", "World");
        data.put("field3", 10);
        data.put("field4", 20);
        
        Map<String, String> fieldMapping = new HashMap<>();
        fieldMapping.put("字段1", "field1");
        fieldMapping.put("字段2", "field2");
        fieldMapping.put("字段3", "field3");
        fieldMapping.put("字段4", "field4");
        
        Object result = FormulaCalculator.calculate(formula, data, fieldMapping);
        
        // 期望结果：HelloWorld===0（因为字段1和字段2不是数字，SUM返回0）
        System.out.println("字符串拼接结果: " + result);
        assertTrue(result instanceof String);
        assertTrue(((String) result).contains("HelloWorld"));
        assertTrue(((String) result).contains("==="));
    }
    
    @Test
    public void testStringConcatenationWithNumbers() {
        // 测试：CONCATENATE(字段3,字段4)+'==='+SUM(字段3,字段4)
        String formula = "CONCATENATE(字段3,字段4)+\"===\"+SUM(字段3,字段4)";
        
        JSONObject data = new JSONObject();
        data.put("field3", 10);
        data.put("field4", 20);
        
        Map<String, String> fieldMapping = new HashMap<>();
        fieldMapping.put("字段3", "field3");
        fieldMapping.put("字段4", "field4");
        
        Object result = FormulaCalculator.calculate(formula, data, fieldMapping);
        
        // 期望结果：1020===30
        System.out.println("字符串拼接结果（数字）: " + result);
        assertTrue(result instanceof String);
        String resultStr = (String) result;
        assertTrue(resultStr.contains("==="));
        // 应该包含1020和30
        assertTrue(resultStr.contains("1020") || resultStr.contains("10") && resultStr.contains("20"));
        assertTrue(resultStr.contains("30"));
    }
    
    @Test
    public void testNonNumericValueInSum() {
        // 测试：SUM(字段1,字段2) 其中字段1和字段2不是数字
        String formula = "SUM(字段1,字段2)";
        
        JSONObject data = new JSONObject();
        data.put("field1", "Hello");
        data.put("field2", "World");
        
        Map<String, String> fieldMapping = new HashMap<>();
        fieldMapping.put("字段1", "field1");
        fieldMapping.put("字段2", "field2");
        
        Object result = FormulaCalculator.calculate(formula, data, fieldMapping);
        
        // 期望结果：0（非数字值在SUM中返回0）
        System.out.println("SUM非数字值结果: " + result);
        assertEquals(0.0, ((Number) result).doubleValue(), 0.0001);
    }
    
    @Test
    public void testMixedStringAndNumberConcatenation() {
        // 测试：字符串字面量 + 数值
        String formula = "\"Hello\"+123+\"World\""; // 注意：这里使用半角双引号
        
        JSONObject data = new JSONObject();
        
        Object result = FormulaCalculator.calculate(formula, data, null);
        
        // 期望结果：Hello123World
        System.out.println("混合字符串拼接结果: " + result);
        assertEquals("Hello123World", result);
    }
}

