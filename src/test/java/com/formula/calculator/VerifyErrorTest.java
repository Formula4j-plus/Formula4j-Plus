package com.formula.calculator;

import com.alibaba.fastjson.JSONObject;

/**
 * VerifyError修复验证测试
 * 
 * 用于验证混淆后的jar包不会出现VerifyError
 */
public class VerifyErrorTest {
    
    public static void main(String[] args) {
        System.out.println("========== VerifyError修复验证 ==========");
        
        try {
            // 测试基本功能
            testBasicFunction();
            
            // 测试自定义函数
            testCustomFunction();
            
            // 测试复杂公式
            testComplexFormula();
            
            System.out.println("\n✅ 所有测试通过！VerifyError已修复。");
        } catch (VerifyError e) {
            System.err.println("\n❌ VerifyError仍然存在:");
            e.printStackTrace();
            System.exit(1);
        } catch (Exception e) {
            System.err.println("\n❌ 其他错误:");
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private static void testBasicFunction() {
        System.out.println("\n[测试1] 基本函数");
        
        String formula = "SUM(1, 2, 3)";
        JSONObject data = new JSONObject();
        Object result = FormulaCalculator.calculate(formula, data);
        System.out.println("公式: " + formula);
        System.out.println("结果: " + result);
        assert result != null : "结果不能为null";
    }
    
    private static void testCustomFunction() {
        System.out.println("\n[测试2] 自定义函数");
        
        // 注册自定义函数
        FormulaCalculator.registerCustomFunction("MYTEST", (params, data, fieldMapping) -> {
            return "100";
        });
        
        String formula = "MYTEST()";
        JSONObject data = new JSONObject();
        Object result = FormulaCalculator.calculate(formula, data);
        System.out.println("公式: " + formula);
        System.out.println("结果: " + result);
        assert result != null : "结果不能为null";
    }
    
    private static void testComplexFormula() {
        System.out.println("\n[测试3] 复杂公式");
        
        String formula = "SUM(1,2,3) + ABS(-5) + SQRT(16)";
        JSONObject data = new JSONObject();
        Object result = FormulaCalculator.calculate(formula, data);
        System.out.println("公式: " + formula);
        System.out.println("结果: " + result);
        assert result != null : "结果不能为null";
    }
}

