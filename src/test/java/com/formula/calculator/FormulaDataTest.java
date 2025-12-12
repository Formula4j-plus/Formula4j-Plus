package com.formula.calculator;

import com.alibaba.fastjson.JSONObject;
import com.formula.calculator.model.FormulaData;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 测试FormulaData数据结构是否符合前端需求
 */
public class FormulaDataTest {
    
    @Test
    public void testFormulaDataStructure() {
        // 模拟前端传来的数据结构
        String jsonStr = "{\n" +
            "  \"txt\": \"SUM(字段1,字段2)\",\n" +
            "  \"marks\": [\n" +
            "    {\"enCode\": \"field1\", \"menuId\": \"字段1\", \"form\": {\"ch\": 0, \"line\": 0, \"stick\": null}},\n" +
            "    {\"enCode\": \"field2\", \"menuId\": \"字段2\", \"form\": {\"ch\": 0, \"line\": 0, \"stick\": null}}\n" +
            "  ]\n" +
            "}";
        
        // 解析为FormulaData
        JSONObject json = JSONObject.parseObject(jsonStr);
        FormulaData formulaData = FormulaData.fromJSON(json);
        
        // 验证解析结果
        assertNotNull(formulaData);
        assertEquals("SUM(字段1,字段2)", formulaData.getTxt());
        assertEquals(2, formulaData.getMarks().size());
        assertEquals("field1", formulaData.getMarks().get(0).getEnCode());
        assertEquals("字段1", formulaData.getMarks().get(0).getMenuId());
        assertEquals("field2", formulaData.getMarks().get(1).getEnCode());
        assertEquals("字段2", formulaData.getMarks().get(1).getMenuId());
        
        // 准备数据（使用enCode作为key）
        JSONObject data = new JSONObject();
        data.put("field1", 10);  // enCode
        data.put("field2", 20);  // enCode
        
        // 计算公式
        Object result = FormulaCalculator.calculate(formulaData, data);
        
        // 验证结果：SUM(字段1,字段2) = SUM(10, 20) = 30
        assertEquals(30.0, ((Number) result).doubleValue(), 0.0001);
    }
    
    @Test
    public void testFieldReplacement() {
        // 测试字段替换逻辑
        String jsonStr = "{\n" +
            "  \"txt\": \"CONCATENATE(字段1,字段2)+===+SUM(字段12,字段2)\",\n" +
            "  \"marks\": [\n" +
            "    {\"enCode\": \"input_demo1\", \"menuId\": \"字段1\", \"form\": {\"ch\": 0, \"line\": 0, \"stick\": null}},\n" +
            "    {\"enCode\": \"input_demo2\", \"menuId\": \"字段2\", \"form\": {\"ch\": 0, \"line\": 0, \"stick\": null}}\n" +
//            "    {\"enCode\": \"c\", \"menuId\": \"字段3\", \"form\": {\"ch\": 0, \"line\": 0, \"stick\": null}}\n" +
            "  ]\n" +
            "}";
        
        JSONObject json = JSONObject.parseObject(jsonStr);
        FormulaData formulaData = FormulaData.fromJSON(json);
        
        // 准备数据
        JSONObject data = new JSONObject();
        data.put("input_demo1", 10);  // 字段1的值
        data.put("input_demo2", 12);  // 字段2的值
//        data.put("c", 3);   // 字段3的值
        
        // 计算：字段1 + 字段2 * 字段3 = 10 + 20 * 3 = 10 + 60 = 70
        Object result = FormulaCalculator.calculate(formulaData, data);
        System.out.println(result);

      //  assertEquals(70.0, ((Number) result).doubleValue(), 0.0001);
    }
    
    @Test
    public void testComplexFormula() {
        // 测试复杂公式
        String jsonStr = "{\n" +
            "  \"txt\": \"IF(字段1 >= 100, SUM(字段1,字段2), 字段1 * 字段2)\",\n" +
            "  \"marks\": [\n" +
            "    {\"enCode\": \"x\", \"menuId\": \"字段1\", \"form\": {\"ch\": 0, \"line\": 0, \"stick\": null}},\n" +
            "    {\"enCode\": \"y\", \"menuId\": \"字段2\", \"form\": {\"ch\": 0, \"line\": 0, \"stick\": null}}\n" +
            "  ]\n" +
            "}";
        
        JSONObject json = JSONObject.parseObject(jsonStr);
        FormulaData formulaData = FormulaData.fromJSON(json);
        
        // 测试1：字段1 >= 100，应该返回 SUM(字段1,字段2)
        JSONObject data1 = new JSONObject();
        data1.put("x", 150);
        data1.put("y", 50);
        Object result1 = FormulaCalculator.calculate(formulaData, data1);
        assertEquals(200.0, ((Number) result1).doubleValue(), 0.0001); // SUM(150, 50) = 200
        
        // 测试2：字段1 < 100，应该返回 字段1 * 字段2
        JSONObject data2 = new JSONObject();
        data2.put("x", 80);
        data2.put("y", 50);
        Object result2 = FormulaCalculator.calculate(formulaData, data2);
        assertEquals(4000.0, ((Number) result2).doubleValue(), 0.0001); // 80 * 50 = 4000
    }
    
    @Test
    public void testDirectFieldName() {
        // 测试直接字段名（不带${}或[]）
        String jsonStr = "{\n" +
            "  \"txt\": \"字段1 + field2\",\n" +
            "  \"marks\": [\n" +
            "    {\"enCode\": \"f1\", \"menuId\": \"字段1\", \"form\": {\"ch\": 0, \"line\": 0, \"stick\": null}},\n" +
            "    {\"enCode\": \"f2\", \"menuId\": \"field2\", \"form\": {\"ch\": 0, \"line\": 0, \"stick\": null}}\n" +
            "  ]\n" +
            "}";
        
        JSONObject json = JSONObject.parseObject(jsonStr);
        FormulaData formulaData = FormulaData.fromJSON(json);
        
        JSONObject data = new JSONObject();
        data.put("f1", 15);
        data.put("f2", 25);
        
        Object result = FormulaCalculator.calculate(formulaData, data);
        assertEquals(40.0, ((Number) result).doubleValue(), 0.0001);
    }
}

