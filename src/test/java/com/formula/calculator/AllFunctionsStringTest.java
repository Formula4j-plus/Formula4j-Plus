package com.formula.calculator;

import com.alibaba.fastjson.JSONObject;
import com.formula.calculator.model.FormulaData;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 测试所有函数是否正确处理字符串返回值
 */
public class AllFunctionsStringTest {
    
    @Test
    public void testCONCATENATE() {
        // 测试CONCATENATE函数返回字符串
        String jsonStr = "{\n" +
            "  \"txt\": \"CONCATENATE(字段1,字段2)\",\n" +
            "  \"marks\": [\n" +
            "    {\"enCode\": \"f1\", \"menuId\": \"字段1\", \"form\": {\"ch\": 0, \"line\": 0, \"stick\": null}},\n" +
            "    {\"enCode\": \"f2\", \"menuId\": \"字段2\", \"form\": {\"ch\": 0, \"line\": 0, \"stick\": null}}\n" +
            "  ]\n" +
            "}";
        
        JSONObject json = JSONObject.parseObject(jsonStr);
        FormulaData formulaData = FormulaData.fromJSON(json);
        
        JSONObject data = new JSONObject();
        data.put("f1", "Hello");
        data.put("f2", "World");
        
        Object result = FormulaCalculator.calculate(formulaData, data);
        assertEquals("HelloWorld", result);
        assertTrue(result instanceof String);
        System.out.println("✓ CONCATENATE返回字符串测试通过: " + result);
    }
    
    @Test
    public void testIFWithCONCATENATE() {
        // 测试IF函数返回CONCATENATE的结果
        String jsonStr = "{\n" +
            "  \"txt\": \"IF(字段1 > 0, CONCATENATE(字段1,字段2), \\\"default\\\")\",\n" +
            "  \"marks\": [\n" +
            "    {\"enCode\": \"f1\", \"menuId\": \"字段1\", \"form\": {\"ch\": 0, \"line\": 0, \"stick\": null}},\n" +
            "    {\"enCode\": \"f2\", \"menuId\": \"字段2\", \"form\": {\"ch\": 0, \"line\": 0, \"stick\": null}}\n" +
            "  ]\n" +
            "}";
        
        JSONObject json = JSONObject.parseObject(jsonStr);
        FormulaData formulaData = FormulaData.fromJSON(json);
        
        JSONObject data = new JSONObject();
        data.put("f1", "123");
        data.put("f2", "456");
        
        Object result = FormulaCalculator.calculate(formulaData, data);
        assertEquals("123456", result);
        assertTrue(result instanceof String);
        System.out.println("✓ IF返回CONCATENATE结果测试通过: " + result);
    }
    
    @Test
    public void testSWITCHWithString() {
        // 测试SWITCH函数返回字符串
        String jsonStr = "{\n" +
            "  \"txt\": \"SWITCH(字段1, 1, CONCATENATE(字段2,字段3), 2, \\\"two\\\", \\\"default\\\")\",\n" +
            "  \"marks\": [\n" +
            "    {\"enCode\": \"f1\", \"menuId\": \"字段1\", \"form\": {\"ch\": 0, \"line\": 0, \"stick\": null}},\n" +
            "    {\"enCode\": \"f2\", \"menuId\": \"字段2\", \"form\": {\"ch\": 0, \"line\": 0, \"stick\": null}},\n" +
            "    {\"enCode\": \"f3\", \"menuId\": \"字段3\", \"form\": {\"ch\": 0, \"line\": 0, \"stick\": null}}\n" +
            "  ]\n" +
            "}";
        
        JSONObject json = JSONObject.parseObject(jsonStr);
        FormulaData formulaData = FormulaData.fromJSON(json);
        
        JSONObject data = new JSONObject();
        data.put("f1", 1);
        data.put("f2", "Hello");
        data.put("f3", "World");
        
        Object result = FormulaCalculator.calculate(formulaData, data);
        assertEquals("HelloWorld", result);
        assertTrue(result instanceof String);
        System.out.println("✓ SWITCH返回CONCATENATE结果测试通过: " + result);
    }
    
    @Test
    public void testIFSWithString() {
        // 测试IFS函数返回字符串
        String jsonStr = "{\n" +
            "  \"txt\": \"IFS(字段1 > 10, CONCATENATE(字段2,字段3), 字段1 > 5, \\\"medium\\\", \\\"small\\\")\",\n" +
            "  \"marks\": [\n" +
            "    {\"enCode\": \"f1\", \"menuId\": \"字段1\", \"form\": {\"ch\": 0, \"line\": 0, \"stick\": null}},\n" +
            "    {\"enCode\": \"f2\", \"menuId\": \"字段2\", \"form\": {\"ch\": 0, \"line\": 0, \"stick\": null}},\n" +
            "    {\"enCode\": \"f3\", \"menuId\": \"字段3\", \"form\": {\"ch\": 0, \"line\": 0, \"stick\": null}}\n" +
            "  ]\n" +
            "}";
        
        JSONObject json = JSONObject.parseObject(jsonStr);
        FormulaData formulaData = FormulaData.fromJSON(json);
        
        JSONObject data = new JSONObject();
        data.put("f1", 15);
        data.put("f2", "Hello");
        data.put("f3", "World");
        
        Object result = FormulaCalculator.calculate(formulaData, data);
        assertEquals("HelloWorld", result);
        assertTrue(result instanceof String);
        System.out.println("✓ IFS返回CONCATENATE结果测试通过: " + result);
    }
    
    @Test
    public void testCONCATENATEWithNumbers() {
        // 测试CONCATENATE函数处理数字
        String jsonStr = "{\n" +
            "  \"txt\": \"CONCATENATE(字段1,字段2)\",\n" +
            "  \"marks\": [\n" +
            "    {\"enCode\": \"f1\", \"menuId\": \"字段1\", \"form\": {\"ch\": 0, \"line\": 0, \"stick\": null}},\n" +
            "    {\"enCode\": \"f2\", \"menuId\": \"字段2\", \"form\": {\"ch\": 0, \"line\": 0, \"stick\": null}}\n" +
            "  ]\n" +
            "}";
        
        JSONObject json = JSONObject.parseObject(jsonStr);
        FormulaData formulaData = FormulaData.fromJSON(json);
        
        JSONObject data = new JSONObject();
        data.put("f1", 123);
        data.put("f2", 456);
        
        Object result = FormulaCalculator.calculate(formulaData, data);
        assertEquals("123456", result);
        assertTrue(result instanceof String);
        System.out.println("✓ CONCATENATE处理数字测试通过: " + result);
    }
    
    @Test
    public void testCONCATENATEWithMixedTypes() {
        // 测试CONCATENATE函数处理混合类型
        String jsonStr = "{\n" +
            "  \"txt\": \"CONCATENATE(字段1,字段2,字段3)\",\n" +
            "  \"marks\": [\n" +
            "    {\"enCode\": \"f1\", \"menuId\": \"字段1\", \"form\": {\"ch\": 0, \"line\": 0, \"stick\": null}},\n" +
            "    {\"enCode\": \"f2\", \"menuId\": \"字段2\", \"form\": {\"ch\": 0, \"line\": 0, \"stick\": null}},\n" +
            "    {\"enCode\": \"f3\", \"menuId\": \"字段3\", \"form\": {\"ch\": 0, \"line\": 0, \"stick\": null}}\n" +
            "  ]\n" +
            "}";
        
        JSONObject json = JSONObject.parseObject(jsonStr);
        FormulaData formulaData = FormulaData.fromJSON(json);
        
        JSONObject data = new JSONObject();
        data.put("f1", "Hello");
        data.put("f2", 123);
        data.put("f3", "World");
        
        Object result = FormulaCalculator.calculate(formulaData, data);
        assertEquals("Hello123World", result);
        assertTrue(result instanceof String);
        System.out.println("✓ CONCATENATE处理混合类型测试通过: " + result);
    }
}

