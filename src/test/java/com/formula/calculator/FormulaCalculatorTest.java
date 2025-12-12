package com.formula.calculator;

import com.alibaba.fastjson.JSONObject;
import com.formula.calculator.model.FieldMark;
import com.formula.calculator.model.FormulaData;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 公式计算器测试类
 */
public class FormulaCalculatorTest {
    
    @Test
    public void testBasicCalculation() {
        JSONObject data = new JSONObject();
        data.put("a", 10);
        data.put("b", 20);
        
        Object result = FormulaCalculator.calculate("${a} + ${b}", data);
        assertEquals(30.0, ((Number) result).doubleValue(), 0.0001);
    }
    
    @Test
    public void testFormulaData() {
        FormulaData formulaData = new FormulaData();
        formulaData.setTxt("SUM(字段1,字段2)");
        
        List<FieldMark> marks = new ArrayList<>();
        marks.add(new FieldMark("field1", "字段1"));
        marks.add(new FieldMark("field2", "字段2"));
        formulaData.setMarks(marks);
        
        JSONObject data = new JSONObject();
        data.put("field1", 10);
        data.put("field2", 20);
        
        Object result = FormulaCalculator.calculate(formulaData, data);
        assertEquals(30.0, ((Number) result).doubleValue(), 0.0001);
    }
    
    @Test
    public void testSumFunction() {
        JSONObject data = new JSONObject();
        data.put("field1", 10);
        data.put("field2", 20);
        data.put("field3", 30);
        
        Object result = FormulaCalculator.calculate("SUM(${field1}, ${field2}, ${field3})", data);
        assertEquals(60.0, ((Number) result).doubleValue(), 0.0001);
    }
    
    @Test
    public void testAverageFunction() {
        JSONObject data = new JSONObject();
        data.put("field1", 10);
        data.put("field2", 20);
        data.put("field3", 30);
        
        Object result = FormulaCalculator.calculate("AVERAGE(${field1}, ${field2}, ${field3})", data);
        assertEquals(20.0, ((Number) result).doubleValue(), 0.0001);
    }
    
    @Test
    public void testIfFunction() {
        JSONObject data = new JSONObject();
        data.put("score", 85);
        
        Object result = FormulaCalculator.calculate("IF(${score} >= 60, 1, 0)", data);
        assertEquals(1.0, ((Number) result).doubleValue(), 0.0001);
    }
    
    @Test
    public void testLogicalFunctions() {
        JSONObject data = new JSONObject();
        data.put("a", 10);
        data.put("b", 20);
        data.put("c", 5);
        
        // AND
        Object result1 = FormulaCalculator.calculate("AND(${a} > 5, ${b} > 10)", data);
        assertEquals(1.0, ((Number) result1).doubleValue(), 0.0001);
        
        // OR
        Object result2 = FormulaCalculator.calculate("OR(${a} < 5, ${b} > 10)", data);
        assertEquals(1.0, ((Number) result2).doubleValue(), 0.0001);
        
        // NOT
        Object result3 = FormulaCalculator.calculate("NOT(${a} < 5)", data);
        assertEquals(1.0, ((Number) result3).doubleValue(), 0.0001);
    }
    
    @Test
    public void testMathFunctions() {
        JSONObject data = new JSONObject();
        data.put("value", -10);
        
        // ABS
        Object result1 = FormulaCalculator.calculate("ABS(${value})", data);
        assertEquals(10.0, ((Number) result1).doubleValue(), 0.0001);
        
        // SQRT
        Object result2 = FormulaCalculator.calculate("SQRT(16)", data);
        assertEquals(4.0, ((Number) result2).doubleValue(), 0.0001);
        
        // POWER
        Object result3 = FormulaCalculator.calculate("POWER(2, 3)", data);
        assertEquals(8.0, ((Number) result3).doubleValue(), 0.0001);
    }
    
    @Test
    public void testStatisticalFunctions() {
        JSONObject data = new JSONObject();
        data.put("a", 10);
        data.put("b", 20);
        data.put("c", 30);
        
        // MEDIAN
        Object result = FormulaCalculator.calculate("MEDIAN(${a}, ${b}, ${c})", data);
        assertEquals(20.0, ((Number) result).doubleValue(), 0.0001);
    }
    
    @Test
    public void testComplexFormula() {
        JSONObject data = new JSONObject();
        data.put("base", 1000);
        data.put("bonus", 200);
        data.put("tax", 0.1);
        
        Object result = FormulaCalculator.calculate("(${base} + ${bonus}) * (1 - ${tax})", data);
        assertEquals(1080.0, ((Number) result).doubleValue(), 0.0001);
    }
    
    @Test
    public void testVerify() {
        JSONObject data = new JSONObject();
        data.put("a", 10);
        data.put("b", 20);
        
        boolean verified = FormulaUtils.verify("${a} + ${b}", data, 30);
        assertTrue(verified);
    }
}
