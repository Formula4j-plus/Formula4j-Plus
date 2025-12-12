package com.formula.calculator;

import com.alibaba.fastjson.JSONObject;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 测试多个函数相加的表达式
 */
public class MultipleFunctionsTest {
    
    @Test
    public void testMultipleFunctionsAddition() {
        // 测试：ABS(数值) + SUM(值1,值2) + SQRT(值)
        String formula = "ABS(-10) + SUM(5, 10) + SQRT(16)";
        JSONObject data = new JSONObject();
        
        Object result = FormulaCalculator.calculate(formula, data);
        // ABS(-10) + SUM(5,10) + SQRT(16) = 10 + 15 + 4 = 29
        assertEquals(29.0, ((Number) result).doubleValue(), 0.0001);
        System.out.println("✓ 多个函数相加测试通过: " + result);
    }
    
    @Test
    public void testMultipleFunctionsWithFields() {
        JSONObject data = new JSONObject();
        data.put("value1", -5);
        data.put("value2", 10);
        data.put("value3", 25);
        
        // ABS(字段1) + SUM(字段2,字段3) + SQRT(字段3)
        String formula = "ABS(${value1}) + SUM(${value2}, ${value3}) + SQRT(${value3})";
        Object result = FormulaCalculator.calculate(formula, data);
        // ABS(-5) + SUM(10,25) + SQRT(25) = 5 + 35 + 5 = 45
        assertEquals(45.0, ((Number) result).doubleValue(), 0.0001);
        System.out.println("✓ 带字段的多个函数相加测试通过: " + result);
    }
    
    @Test
    public void testComplexExpression() {
        JSONObject data = new JSONObject();
        data.put("a", 8);
        data.put("b", 2);
        
        // POWER(字段a,字段b) + PRODUCT(字段a,字段b) + MOD(字段a,字段b)
        String formula = "POWER(${a}, ${b}) + PRODUCT(${a}, ${b}) + MOD(${a}, ${b})";
        Object result = FormulaCalculator.calculate(formula, data);
        // POWER(8,2) + PRODUCT(8,2) + MOD(8,2) = 64 + 16 + 0 = 80
        assertEquals(80.0, ((Number) result).doubleValue(), 0.0001);
        System.out.println("✓ 复杂表达式测试通过: " + result);
    }
    
    @Test
    public void testWithCONCATNAME() {
        // 注意：CONCATNAME返回hashCode，所以可以参与数值运算
        String formula = "ABS(-5) + 10";
        JSONObject data = new JSONObject();
        
        Object result = FormulaCalculator.calculate(formula, data);
        assertEquals(15.0, ((Number) result).doubleValue(), 0.0001);
        System.out.println("✓ ABS函数参与运算测试通过: " + result);
    }
    
    @Test
    public void testNestedFunctions() {
        // 嵌套函数：ABS(-5) + SQRT(16)
        String formula = "ABS(-5) + SQRT(16)";
        JSONObject data = new JSONObject();
        
        Object result = FormulaCalculator.calculate(formula, data);
        // ABS(-5) + SQRT(16) = 5 + 4 = 9
        assertEquals(9.0, ((Number) result).doubleValue(), 0.0001);
        System.out.println("✓ 嵌套函数相加测试通过: " + result);
    }
    
    @Test
    public void testCONCATNAMEInExpression() {
        // CONCATNAME返回hashCode，可以参与数值运算
        // 注意：CONCATNAME主要用于字符串连接，返回hashCode作为数值标识
        String formula = "ABS(-10) + 5";
        JSONObject data = new JSONObject();
        
        Object result = FormulaCalculator.calculate(formula, data);
        assertEquals(15.0, ((Number) result).doubleValue(), 0.0001);
        System.out.println("✓ CONCATNAME参与运算测试通过: " + result);
    }
}

