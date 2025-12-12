package com.formula.calculator;

import com.alibaba.fastjson.JSONObject;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 自定义脚本函数测试
 */
public class CustomScriptFunctionTest {
    
    @Test
    public void testJavaScriptFunction() {
        // 注册JavaScript自定义函数
        String jsScript = "var parts = params.split(',');" +
                         "var sum = 0;" +
                         "for (var i = 0; i < parts.length; i++) {" +
                         "    sum += parseFloat(parts[i]);" +
                         "}" +
                         "sum.toString();";
        
        FormulaCalculator.registerCustomFunction("MYJSSUM", "js", jsScript);
        
        // 测试JavaScript函数
        String formula = "MYJSSUM(10, 20, 30)";
        JSONObject data = new JSONObject();
        
        Object result = FormulaCalculator.calculate(formula, data, null);
        System.out.println("✓ JavaScript自定义函数测试通过: " + result);
        
        // 清理
        FormulaCalculator.unregisterCustomFunction("MYJSSUM");
    }
    
    @Test
    public void testJavaScriptWithData() {
        // 注册JavaScript函数，使用JSONObject数据
        String jsScript = "var field1 = data.get('field1');" +
                         "var field2 = data.get('field2');" +
                         "(field1 * field2).toString();";
        
        FormulaCalculator.registerCustomFunction("MULTIPLY", "js", jsScript);
        
        // 测试
        String formula = "MULTIPLY()";
        JSONObject data = new JSONObject();
        data.put("field1", 5);
        data.put("field2", 10);
        
        Object result = FormulaCalculator.calculate(formula, data, null);
        System.out.println("✓ JavaScript使用数据测试通过: " + result);
        assertEquals("50.0", result.toString());
        
        // 清理
        FormulaCalculator.unregisterCustomFunction("MULTIPLY");
    }
    
    @Test
    public void testTraditionalCustomFunction() {
        // 使用传统方式注册自定义函数
        FormulaCalculator.registerCustomFunction("DOUBLE", (params, data, fieldMapping) -> {
            double value = Double.parseDouble(params);
            return String.valueOf(value * 2);
        });
        
        // 测试
        String formula = "DOUBLE(25)";
        JSONObject data = new JSONObject();
        
        Object result = FormulaCalculator.calculate(formula, data, null);
        System.out.println("✓ 传统自定义函数测试通过: " + result);
        assertEquals(50.0, ((Number) result).doubleValue(), 0.0001);
        
        // 清理
        FormulaCalculator.unregisterCustomFunction("DOUBLE");
    }
}

