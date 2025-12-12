package com.formula.calculator;

import com.alibaba.fastjson.JSONObject;
import com.formula.calculator.model.FieldMark;
import com.formula.calculator.model.FormulaData;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试FormulaData支持不同的字段名结构
 * 验证：可以使用 formula/fields 等自定义字段名，而不仅仅是 txt/marks
 */
public class FormulaDataCustomStructureTest {
    
    @Test
    public void testDefaultStructure() {
        // 测试默认结构（向后兼容）
        String jsonStr = "{\n" +
            "  \"txt\": \"SUM(字段1,字段2)\",\n" +
            "  \"marks\": [\n" +
            "    {\"enCode\": \"field1\", \"menuId\": \"字段1\"},\n" +
            "    {\"enCode\": \"field2\", \"menuId\": \"字段2\"}\n" +
            "  ]\n" +
            "}";
        
        JSONObject json = JSONObject.parseObject(jsonStr);
        FormulaData formulaData = FormulaData.fromJSON(json);
        
        assertEquals("SUM(字段1,字段2)", formulaData.getTxt());
        assertEquals(2, formulaData.getMarks().size());
        assertEquals("field1", formulaData.getMarks().get(0).getEnCode());
        assertEquals("字段1", formulaData.getMarks().get(0).getMenuId());
        
        System.out.println("✓ 默认结构测试通过");
    }
    
    @Test
    public void testCustomFieldNames() {
        // 测试自定义字段名：formula 和 fields
        String jsonStr = "{\n" +
            "  \"formula\": \"SUM(单价,数量)\",\n" +
            "  \"fields\": [\n" +
            "    {\"enCode\": \"price\", \"menuId\": \"单价\"},\n" +
            "    {\"enCode\": \"quantity\", \"menuId\": \"数量\"}\n" +
            "  ]\n" +
            "}";
        
        JSONObject json = JSONObject.parseObject(jsonStr);
        FormulaData formulaData = FormulaData.fromJSON(json, "formula", "fields");
        
        assertEquals("SUM(单价,数量)", formulaData.getTxt());
        assertEquals(2, formulaData.getMarks().size());
        assertEquals("price", formulaData.getMarks().get(0).getEnCode());
        assertEquals("单价", formulaData.getMarks().get(0).getMenuId());
        assertEquals("quantity", formulaData.getMarks().get(1).getEnCode());
        assertEquals("数量", formulaData.getMarks().get(1).getMenuId());
        
        // 验证可以正常计算
        JSONObject data = new JSONObject();
        data.put("price", 100);
        data.put("quantity", 5);
        
        Object result = FormulaCalculator.calculate(formulaData, data);
        assertEquals(105.0, ((Number) result).doubleValue(), 0.0001);
        
        System.out.println("✓ 自定义字段名测试通过");
    }
    
    @Test
    public void testExpressionAndFieldMarks() {
        // 测试 expression 和 fieldMarks 字段名
        String jsonStr = "{\n" +
            "  \"expression\": \"字段1 * 字段2\",\n" +
            "  \"fieldMarks\": [\n" +
            "    {\"enCode\": \"f1\", \"menuId\": \"字段1\"},\n" +
            "    {\"enCode\": \"f2\", \"menuId\": \"字段2\"}\n" +
            "  ]\n" +
            "}";
        
        JSONObject json = JSONObject.parseObject(jsonStr);
        FormulaData formulaData = FormulaData.fromJSON(json, "expression", "fieldMarks");
        
        assertEquals("字段1 * 字段2", formulaData.getTxt());
        assertEquals(2, formulaData.getMarks().size());
        
        // 验证可以正常计算
        JSONObject data = new JSONObject();
        data.put("f1", 10);
        data.put("f2", 20);
        
        Object result = FormulaCalculator.calculate(formulaData, data);
        assertEquals(200.0, ((Number) result).doubleValue(), 0.0001);
        
        System.out.println("✓ expression/fieldMarks 字段名测试通过");
    }
    
    @Test
    public void testWithFieldMapping() {
        // 测试使用字段名映射配置
        String jsonStr = "{\n" +
            "  \"formulaText\": \"AVERAGE(分数1,分数2,分数3)\",\n" +
            "  \"fieldList\": [\n" +
            "    {\"enCode\": \"score1\", \"menuId\": \"分数1\"},\n" +
            "    {\"enCode\": \"score2\", \"menuId\": \"分数2\"},\n" +
            "    {\"enCode\": \"score3\", \"menuId\": \"分数3\"}\n" +
            "  ]\n" +
            "}";
        
        JSONObject json = JSONObject.parseObject(jsonStr);
        
        // 使用字段名映射
        Map<String, String> fieldMapping = new HashMap<>();
        fieldMapping.put("formula", "formulaText");
        fieldMapping.put("marks", "fieldList");
        
        FormulaData formulaData = FormulaData.fromJSON(json, fieldMapping);
        
        assertEquals("AVERAGE(分数1,分数2,分数3)", formulaData.getTxt());
        assertEquals(3, formulaData.getMarks().size());
        
        // 验证可以正常计算
        JSONObject data = new JSONObject();
        data.put("score1", 80);
        data.put("score2", 90);
        data.put("score3", 100);
        
        Object result = FormulaCalculator.calculate(formulaData, data);
        assertEquals(90.0, ((Number) result).doubleValue(), 0.0001);
        
        System.out.println("✓ 字段名映射配置测试通过");
    }
    
    @Test
    public void testAutoFallback() {
        // 测试自动回退机制：如果指定字段不存在，自动尝试常见字段名
        String jsonStr = "{\n" +
            "  \"formula\": \"MAX(值1,值2)\",\n" +
            "  \"marks\": [\n" +
            "    {\"enCode\": \"v1\", \"menuId\": \"值1\"},\n" +
            "    {\"enCode\": \"v2\", \"menuId\": \"值2\"}\n" +
            "  ]\n" +
            "}";
        
        JSONObject json = JSONObject.parseObject(jsonStr);
        
        // 指定不存在的字段名，应该自动回退到 formula 和 marks
        FormulaData formulaData = FormulaData.fromJSON(json, "nonExistent", "alsoNonExistent");
        
        // 由于自动回退，应该能找到 formula 和 marks
        assertEquals("MAX(值1,值2)", formulaData.getTxt());
        assertEquals(2, formulaData.getMarks().size());
        
        System.out.println("✓ 自动回退机制测试通过");
    }
    
    @Test
    public void testMixedStructures() {
        // 测试混合结构：不同的字段名组合
        String[] formulaKeys = {"txt", "formula", "expression"};
        String[] marksKeys = {"marks", "fields", "fieldMarks"};
        
        for (String formulaKey : formulaKeys) {
            for (String marksKey : marksKeys) {
                String jsonStr = String.format("{\n" +
                    "  \"%s\": \"字段1 + 字段2\",\n" +
                    "  \"%s\": [\n" +
                    "    {\"enCode\": \"f1\", \"menuId\": \"字段1\"},\n" +
                    "    {\"enCode\": \"f2\", \"menuId\": \"字段2\"}\n" +
                    "  ]\n" +
                    "}", formulaKey, marksKey);
                
                JSONObject json = JSONObject.parseObject(jsonStr);
                FormulaData formulaData = FormulaData.fromJSON(json, formulaKey, marksKey);
                
                assertEquals("字段1 + 字段2", formulaData.getTxt());
                assertEquals(2, formulaData.getMarks().size());
                
                // 验证计算
                JSONObject data = new JSONObject();
                data.put("f1", 15);
                data.put("f2", 25);
                
                Object result = FormulaCalculator.calculate(formulaData, data);
                assertEquals(40.0, ((Number) result).doubleValue(), 0.0001);
            }
        }
        
        System.out.println("✓ 混合结构测试通过");
    }
}

