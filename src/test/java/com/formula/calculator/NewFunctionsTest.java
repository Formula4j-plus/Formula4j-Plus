package com.formula.calculator;

import com.alibaba.fastjson.JSONObject;
import com.formula.calculator.model.FormulaData;
import com.formula.calculator.model.FieldMark;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 测试新添加的函数
 */
public class NewFunctionsTest {
    
    @Test
    public void testCONCATNAME() {
        JSONObject data = new JSONObject();
        data.put("field1", "Hello");
        data.put("field2", "World");
        
        // CONCATNAME函数测试（注意：返回hashCode）
        String formula = "CONCATNAME(\"Hello\", \"World\")";
        Object result = FormulaCalculator.calculate(formula, data);
        assertNotNull(result);
        System.out.println("✓ CONCATNAME函数测试通过");
    }
    
    @Test
    public void testDATE() {
        // DATE函数：DATE(2024, 1, 1)
        String formula = "DATE(2024, 1, 1)";
        JSONObject data = new JSONObject();
        Object result = FormulaCalculator.calculate(formula, data);
        assertNotNull(result);
        System.out.println("✓ DATE函数测试通过");
    }
    
    @Test
    public void testTESTABC() {
        // TESTABC自定义函数
        String formula = "TESTABC(123)";
        JSONObject data = new JSONObject();
        Object result = FormulaCalculator.calculate(formula, data);
        assertNotNull(result);
        System.out.println("✓ TESTABC自定义函数测试通过");
    }
    
    @Test
    public void testSWITCH() {
        JSONObject data = new JSONObject();
        data.put("value", 2);
        
        // SWITCH(value, 1, "One", 2, "Two", "Other")
        String formula = "SWITCH(2, 1, 10, 2, 20, 0)";
        Object result = FormulaCalculator.calculate(formula, data);
        assertEquals(20.0, ((Number) result).doubleValue(), 0.0001);
        System.out.println("✓ SWITCH函数测试通过");
    }
    
    @Test
    public void testIFERROR() {
        JSONObject data = new JSONObject();
        
        // IFERROR(1/0, 999) - 应该返回999
        String formula = "IFERROR(1/0, 999)";
        Object result = FormulaCalculator.calculate(formula, data);
        assertEquals(999.0, ((Number) result).doubleValue(), 0.0001);
        System.out.println("✓ IFERROR函数测试通过");
    }
    
    @Test
    public void testIFNA() {
        JSONObject data = new JSONObject();
        
        // IFNA("#N/A", 0) - 应该返回0
        String formula = "IFNA(\"#N/A\", 0)";
        Object result = FormulaCalculator.calculate(formula, data);
        assertEquals(0.0, ((Number) result).doubleValue(), 0.0001);
        System.out.println("✓ IFNA函数测试通过");
    }
    
    @Test
    public void testAVEDEV() {
        JSONObject data = new JSONObject();
        
        // AVEDEV(10, 20, 30)
        String formula = "AVEDEV(10, 20, 30)";
        Object result = FormulaCalculator.calculate(formula, data);
        assertTrue(((Number) result).doubleValue() > 0);
        System.out.println("✓ AVEDEV函数测试通过");
    }
    
    @Test
    public void testMROUND() {
        JSONObject data = new JSONObject();
        
        // MROUND(7, 3) - 应该返回6或9（最接近3的倍数）
        String formula = "MROUND(7, 3)";
        Object result = FormulaCalculator.calculate(formula, data);
        assertEquals(6.0, ((Number) result).doubleValue(), 0.0001);
        System.out.println("✓ MROUND函数测试通过");
    }
    
    @Test
    public void testCEILING_MATH() {
        JSONObject data = new JSONObject();
        
        // CEILING.MATH(7, 3) - 向上取整到3的倍数，应该返回9
        String formula = "CEILING.MATH(7, 3)";
        Object result = FormulaCalculator.calculate(formula, data);
        assertEquals(9.0, ((Number) result).doubleValue(), 0.0001);
        System.out.println("✓ CEILING.MATH函数测试通过");
    }
    
    @Test
    public void testFLOOR_MATH() {
        JSONObject data = new JSONObject();
        
        // FLOOR.MATH(7, 3) - 向下取整到3的倍数，应该返回6
        String formula = "FLOOR.MATH(7, 3)";
        Object result = FormulaCalculator.calculate(formula, data);
        assertEquals(6.0, ((Number) result).doubleValue(), 0.0001);
        System.out.println("✓ FLOOR.MATH函数测试通过");
    }
}

