package com.formula.calculator;

import com.alibaba.fastjson.JSONObject;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 测试新添加的数学函数
 */
public class MathFunctionsTest {
    
    @Test
    public void testPI() {
        String formula = "PI()";
        JSONObject data = new JSONObject();
        Object result = FormulaCalculator.calculate(formula, data);
        assertEquals(Math.PI, ((Number) result).doubleValue(), 0.0001);
        System.out.println("✓ PI函数测试通过: " + result);
    }
    
    @Test
    public void testSUMSQ() {
        // SUMSQ(2, 3, 4) = 4 + 9 + 16 = 29
        String formula = "SUMSQ(2, 3, 4)";
        JSONObject data = new JSONObject();
        Object result = FormulaCalculator.calculate(formula, data);
        assertEquals(29.0, ((Number) result).doubleValue(), 0.0001);
        System.out.println("✓ SUMSQ函数测试通过: " + result);
    }
    
    @Test
    public void testPRODUCT() {
        // PRODUCT(2, 3, 4) = 24
        String formula = "PRODUCT(2, 3, 4)";
        JSONObject data = new JSONObject();
        Object result = FormulaCalculator.calculate(formula, data);
        assertEquals(24.0, ((Number) result).doubleValue(), 0.0001);
        System.out.println("✓ PRODUCT函数测试通过: " + result);
    }
    
    @Test
    public void testGCD() {
        // GCD(12, 18) = 6
        String formula = "GCD(12, 18)";
        JSONObject data = new JSONObject();
        Object result = FormulaCalculator.calculate(formula, data);
        assertEquals(6.0, ((Number) result).doubleValue(), 0.0001);
        System.out.println("✓ GCD函数测试通过: " + result);
    }
    
    @Test
    public void testLCM() {
        // LCM(12, 18) = 36
        String formula = "LCM(12, 18)";
        JSONObject data = new JSONObject();
        Object result = FormulaCalculator.calculate(formula, data);
        assertEquals(36.0, ((Number) result).doubleValue(), 0.0001);
        System.out.println("✓ LCM函数测试通过: " + result);
    }
    
    @Test
    public void testFACT() {
        // FACT(5) = 120
        String formula = "FACT(5)";
        JSONObject data = new JSONObject();
        Object result = FormulaCalculator.calculate(formula, data);
        assertEquals(120.0, ((Number) result).doubleValue(), 0.0001);
        System.out.println("✓ FACT函数测试通过: " + result);
    }
    
    @Test
    public void testCOMBIN() {
        // COMBIN(5, 2) = 10
        String formula = "COMBIN(5, 2)";
        JSONObject data = new JSONObject();
        Object result = FormulaCalculator.calculate(formula, data);
        assertEquals(10.0, ((Number) result).doubleValue(), 0.0001);
        System.out.println("✓ COMBIN函数测试通过: " + result);
    }
    
    @Test
    public void testPERMUT() {
        // PERMUT(5, 2) = 20
        String formula = "PERMUT(5, 2)";
        JSONObject data = new JSONObject();
        Object result = FormulaCalculator.calculate(formula, data);
        assertEquals(20.0, ((Number) result).doubleValue(), 0.0001);
        System.out.println("✓ PERMUT函数测试通过: " + result);
    }
    
    @Test
    public void testDEGREES() {
        // DEGREES(PI()) = 180
        String formula = "DEGREES(PI())";
        JSONObject data = new JSONObject();
        Object result = FormulaCalculator.calculate(formula, data);
        assertEquals(180.0, ((Number) result).doubleValue(), 0.1);
        System.out.println("✓ DEGREES函数测试通过: " + result);
    }
    
    @Test
    public void testRADIANS() {
        // RADIANS(180) = PI
        String formula = "RADIANS(180)";
        JSONObject data = new JSONObject();
        Object result = FormulaCalculator.calculate(formula, data);
        assertEquals(Math.PI, ((Number) result).doubleValue(), 0.0001);
        System.out.println("✓ RADIANS函数测试通过: " + result);
    }
    
    @Test
    public void testSQRTPI() {
        // SQRTPI(4) = sqrt(4*PI)
        String formula = "SQRTPI(4)";
        JSONObject data = new JSONObject();
        Object result = FormulaCalculator.calculate(formula, data);
        assertEquals(Math.sqrt(4 * Math.PI), ((Number) result).doubleValue(), 0.0001);
        System.out.println("✓ SQRTPI函数测试通过: " + result);
    }
}

