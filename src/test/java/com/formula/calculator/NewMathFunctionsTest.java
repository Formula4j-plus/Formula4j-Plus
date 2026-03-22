package com.formula.calculator;

import com.alibaba.fastjson.JSONObject;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 测试新增的数学函数
 */
public class NewMathFunctionsTest {
    
    @Test
    public void testEVEN() {
        JSONObject data = new JSONObject();
        
        // EVEN(3.2) 应该返回 4
        String formula = "EVEN(3.2)";
        Object result = FormulaCalculator.calculate(formula, data);
        assertEquals(4.0, ((Number) result).doubleValue(), 0.0001);
        System.out.println("✓ EVEN函数测试通过: " + result);
        
        // EVEN(4) 应该返回 4
        formula = "EVEN(4)";
        result = FormulaCalculator.calculate(formula, data);
        assertEquals(4.0, ((Number) result).doubleValue(), 0.0001);
    }
    
    @Test
    public void testODD() {
        JSONObject data = new JSONObject();
        
        // ODD(3.2) 应该返回 5
        String formula = "ODD(3.2)";
        Object result = FormulaCalculator.calculate(formula, data);
        assertEquals(5.0, ((Number) result).doubleValue(), 0.0001);
        System.out.println("✓ ODD函数测试通过: " + result);
        
        // ODD(3) 应该返回 3
        formula = "ODD(3)";
        result = FormulaCalculator.calculate(formula, data);
        assertEquals(3.0, ((Number) result).doubleValue(), 0.0001);
    }
    
    @Test
    public void testISEVEN() {
        JSONObject data = new JSONObject();
        
        // ISEVEN(4) 应该返回 1
        String formula = "ISEVEN(4)";
        Object result = FormulaCalculator.calculate(formula, data);
        assertEquals(1.0, ((Number) result).doubleValue(), 0.0001);
        System.out.println("✓ ISEVEN函数测试通过: " + result);
        
        // ISEVEN(3) 应该返回 0
        formula = "ISEVEN(3)";
        result = FormulaCalculator.calculate(formula, data);
        assertEquals(0.0, ((Number) result).doubleValue(), 0.0001);
    }
    
    @Test
    public void testISODD() {
        JSONObject data = new JSONObject();
        
        // ISODD(3) 应该返回 1
        String formula = "ISODD(3)";
        Object result = FormulaCalculator.calculate(formula, data);
        assertEquals(1.0, ((Number) result).doubleValue(), 0.0001);
        System.out.println("✓ ISODD函数测试通过: " + result);
        
        // ISODD(4) 应该返回 0
        formula = "ISODD(4)";
        result = FormulaCalculator.calculate(formula, data);
        assertEquals(0.0, ((Number) result).doubleValue(), 0.0001);
    }
    
    @Test
    public void testLARGE() {
        JSONObject data = new JSONObject();
        
        // LARGE(1,2,3,4,5, 2) 应该返回第2大的值，即4
        String formula = "LARGE(1,2,3,4,5, 2)";
        Object result = FormulaCalculator.calculate(formula, data);
        assertEquals(4.0, ((Number) result).doubleValue(), 0.0001);
        System.out.println("✓ LARGE函数测试通过: " + result);
    }
    
    @Test
    public void testSMALL() {
        JSONObject data = new JSONObject();
        
        // SMALL(1,2,3,4,5, 2) 应该返回第2小的值，即2
        String formula = "SMALL(1,2,3,4,5, 2)";
        Object result = FormulaCalculator.calculate(formula, data);
        assertEquals(2.0, ((Number) result).doubleValue(), 0.0001);
        System.out.println("✓ SMALL函数测试通过: " + result);
    }
    
    @Test
    public void testGEOMEAN() {
        JSONObject data = new JSONObject();
        
        // GEOMEAN(2, 8) = sqrt(2*8) = sqrt(16) = 4
        String formula = "GEOMEAN(2, 8)";
        Object result = FormulaCalculator.calculate(formula, data);
        assertEquals(4.0, ((Number) result).doubleValue(), 0.0001);
        System.out.println("✓ GEOMEAN函数测试通过: " + result);
    }
    
    @Test
    public void testHARMEAN() {
        JSONObject data = new JSONObject();
        
        // HARMEAN(2, 8) = 2 / (1/2 + 1/8) = 2 / (4/8 + 1/8) = 2 / (5/8) = 16/5 = 3.2
        String formula = "HARMEAN(2, 8)";
        Object result = FormulaCalculator.calculate(formula, data);
        assertEquals(3.2, ((Number) result).doubleValue(), 0.1);
        System.out.println("✓ HARMEAN函数测试通过: " + result);
    }
    
    @Test
    public void testLOG10() {
        JSONObject data = new JSONObject();
        
        // LOG10(100) = 2
        String formula = "LOG10(100)";
        Object result = FormulaCalculator.calculate(formula, data);
        assertEquals(2.0, ((Number) result).doubleValue(), 0.0001);
        System.out.println("✓ LOG10函数测试通过: " + result);
    }
    
    @Test
    public void testLOG2() {
        JSONObject data = new JSONObject();
        
        // LOG2(8) = 3
        String formula = "LOG2(8)";
        Object result = FormulaCalculator.calculate(formula, data);
        assertEquals(3.0, ((Number) result).doubleValue(), 0.0001);
        System.out.println("✓ LOG2函数测试通过: " + result);
    }
    
    @Test
    public void testRAND() {
        JSONObject data = new JSONObject();
        
        // RAND() 应该返回0到1之间的随机数
        String formula = "RAND()";
        Object result = FormulaCalculator.calculate(formula, data);
        double value = ((Number) result).doubleValue();
        assertTrue(value >= 0 && value < 1);
        System.out.println("✓ RAND函数测试通过: " + result);
    }
    
    @Test
    public void testRANDBETWEEN() {
        JSONObject data = new JSONObject();
        
        // RANDBETWEEN(1, 100) 应该返回1到100之间的随机整数
        String formula = "RANDBETWEEN(1, 100)";
        Object result = FormulaCalculator.calculate(formula, data);
        double value = ((Number) result).doubleValue();
        assertTrue(value >= 1 && value <= 100);
        System.out.println("✓ RANDBETWEEN函数测试通过: " + result);
    }
    
    @Test
    public void testQUOTIENT() {
        JSONObject data = new JSONObject();
        
        // QUOTIENT(10, 3) = 3 (整数除法)
        String formula = "QUOTIENT(10, 3)";
        Object result = FormulaCalculator.calculate(formula, data);
        assertEquals(3.0, ((Number) result).doubleValue(), 0.0001);
        System.out.println("✓ QUOTIENT函数测试通过: " + result);
    }
}




