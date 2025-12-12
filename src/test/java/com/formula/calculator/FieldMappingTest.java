package com.formula.calculator;

import com.alibaba.fastjson.JSONObject;
import com.formula.calculator.FormulaCalculator;
import com.formula.calculator.model.FieldMark;
import com.formula.calculator.model.FormulaData;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 测试字段映射逻辑
 * 验证：公式中使用menuId，通过marks找到enCode，用enCode去JSONObject中查找值
 */
public class FieldMappingTest {
    
    @Test
    public void testFieldMappingLogic() {
        // 前端传来的公式JSON
        String jsonStr = "{\n" +
            "  \"txt\": \"字段1 + 字段2\",\n" +
            "  \"marks\": [\n" +
            "    {\"enCode\": \"字段key1\", \"menuId\": \"字段1\", \"form\": {\"ch\": 0, \"line\": 0, \"stick\": null}},\n" +
            "    {\"enCode\": \"字段key2\", \"menuId\": \"字段2\", \"form\": {\"ch\": 0, \"line\": 0, \"stick\": null}}\n" +
            "  ]\n" +
            "}";
        
        // 解析FormulaData
        JSONObject json = JSONObject.parseObject(jsonStr);
        FormulaData formulaData = FormulaData.fromJSON(json);
        
        // JSONObject数据：key是enCode，value是字段值
        JSONObject data = new JSONObject();
        data.put("字段key1", 10);  // enCode作为key
        data.put("字段key2", 20);  // enCode作为key
        
        // 计算公式：公式中使用menuId（字段1、字段2），应该映射到enCode（字段key1、字段key2）
        // 然后用enCode去JSONObject中查找值
        Object result = FormulaCalculator.calculate(formulaData, data);
        
        // 验证：字段1(menuId) -> 字段key1(enCode) -> 10, 字段2(menuId) -> 字段key2(enCode) -> 20
        // 结果应该是 10 + 20 = 30
        assertEquals(30.0, ((Number) result).doubleValue(), 0.0001);
        System.out.println("✓ 字段映射逻辑测试通过: " + result);
    }
    
    @Test
    public void testFieldMappingWithFunction() {
        // 测试函数中的字段映射
        String jsonStr = "{\n" +
            "  \"txt\": \"SUM(单价,数量)\",\n" +
            "  \"marks\": [\n" +
            "    {\"enCode\": \"price\", \"menuId\": \"单价\", \"form\": {\"ch\": 0, \"line\": 0, \"stick\": null}},\n" +
            "    {\"enCode\": \"quantity\", \"menuId\": \"数量\", \"form\": {\"ch\": 0, \"line\": 0, \"stick\": null}}\n" +
            "  ]\n" +
            "}";
        
        JSONObject json = JSONObject.parseObject(jsonStr);
        FormulaData formulaData = FormulaData.fromJSON(json);
        
        // JSONObject使用enCode作为key
        JSONObject data = new JSONObject();
        data.put("price", 100);      // enCode: price
        data.put("quantity", 5);     // enCode: quantity
        
        // 公式中使用menuId（单价、数量），应该映射到enCode（price、quantity）
        Object result = FormulaCalculator.calculate(formulaData, data);
        
        // SUM(单价,数量) -> SUM(price,quantity) -> SUM(100,5) = 105
        assertEquals(105.0, ((Number) result).doubleValue(), 0.0001);
        System.out.println("✓ 函数中字段映射测试通过: " + result);
    }
    
    @Test
    public void testEnCodeAsKey() {
        // 验证：JSONObject的key必须是enCode，不是menuId
        FormulaData formulaData = new FormulaData();
        formulaData.setTxt("字段1 + 字段2");
        
        List<FieldMark> marks = new ArrayList<>();
        marks.add(new FieldMark("enCode1", "字段1"));  // enCode: enCode1, menuId: 字段1
        marks.add(new FieldMark("enCode2", "字段2"));  // enCode: enCode2, menuId: 字段2
        formulaData.setMarks(marks);
        
        // 正确：使用enCode作为key
        JSONObject data1 = new JSONObject();
        data1.put("enCode1", 10);  // ✅ 使用enCode
        data1.put("enCode2", 20);  // ✅ 使用enCode
        Object result1 = FormulaCalculator.calculate(formulaData, data1);
        assertEquals(30.0, ((Number) result1).doubleValue(), 0.0001);
        
        // 错误：使用menuId作为key（应该找不到值）
        JSONObject data2 = new JSONObject();
        data2.put("字段1", 10);  // ❌ 使用menuId，应该找不到
        data2.put("字段2", 20);  // ❌ 使用menuId，应该找不到
        Object result2 = FormulaCalculator.calculate(formulaData, data2);
        // 找不到值时应该返回0，所以结果是0+0=0
        assertEquals(0.0, ((Number) result2).doubleValue(), 0.0001);
        
        System.out.println("✓ enCode作为key验证通过");
    }
}

