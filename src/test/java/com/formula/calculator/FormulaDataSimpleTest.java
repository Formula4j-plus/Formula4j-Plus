package com.formula.calculator;

import com.alibaba.fastjson.JSONObject;
import com.formula.calculator.model.FormulaData;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 简单测试：验证是否符合用户需求
 * 需求：{txt:"公式(字段1,field2)",marks:[{enCode:"字段key",menuId:"字段名称",form:{ch:0,line:0,stick:null}}]}
 * 要把jsonobject里的数据替换到公式里然后进行计算
 */
public class FormulaDataSimpleTest {
    
    @Test
    public void testBasicRequirement() {
        // 模拟前端数据结构
        String jsonStr = "{\n" +
            "  \"txt\": \"字段1 + 字段2\",\n" +
            "  \"marks\": [\n" +
            "    {\"enCode\": \"field1\", \"menuId\": \"字段1\", \"form\": {\"ch\": 0, \"line\": 0, \"stick\": null}},\n" +
            "    {\"enCode\": \"field2\", \"menuId\": \"字段2\", \"form\": {\"ch\": 0, \"line\": 0, \"stick\": null}}\n" +
            "  ]\n" +
            "}";
        
        // 解析FormulaData
        JSONObject json = JSONObject.parseObject(jsonStr);
        FormulaData formulaData = FormulaData.fromJSON(json);
        
        // 准备数据（JSONObject包含所有数据，key使用enCode）
        JSONObject data = new JSONObject();
        data.put("field1", 10);  // enCode对应的值
        data.put("field2", 20);  // enCode对应的值
        
        // 计算公式：字段1 + 字段2 -> 应该替换为 10 + 20 = 30
        Object result = FormulaCalculator.calculate(formulaData, data);
        
        // 验证结果
        assertEquals(30.0, ((Number) result).doubleValue(), 0.0001);
        System.out.println("✓ 基本需求测试通过：字段替换和计算正确");
    }
    
    @Test
    public void testWithFunction() {
        // 测试带函数的公式
        String jsonStr = "{\n" +
            "  \"txt\": \"SUM(字段1,字段2)\",\n" +
            "  \"marks\": [\n" +
            "    {\"enCode\": \"a\", \"menuId\": \"字段1\", \"form\": {\"ch\": 0, \"line\": 0, \"stick\": null}},\n" +
            "    {\"enCode\": \"b\", \"menuId\": \"字段2\", \"form\": {\"ch\": 0, \"line\": 0, \"stick\": null}}\n" +
            "  ]\n" +
            "}";
        
        JSONObject json = JSONObject.parseObject(jsonStr);
        FormulaData formulaData = FormulaData.fromJSON(json);
        
        JSONObject data = new JSONObject();
        data.put("a", 15);
        data.put("b", 25);
        
        // SUM(字段1,字段2) -> SUM(15, 25) = 40
        Object result = FormulaCalculator.calculate(formulaData, data);
        assertEquals(40.0, ((Number) result).doubleValue(), 0.0001);
        System.out.println("✓ 函数测试通过：SUM函数计算正确");
    }
}

