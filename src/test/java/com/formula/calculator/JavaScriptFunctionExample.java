package com.formula.calculator;

import com.alibaba.fastjson.JSONObject;

/**
 * JavaScript脚本自定义函数示例
 * 
 * 展示如何使用JavaScript脚本注册自定义函数
 */
public class JavaScriptFunctionExample {
    
    public static void main(String[] args) {
        // ============================================
        // 示例1: 简单的JavaScript函数 - 求和
        // ============================================
        example1_SimpleSum();
        
        // ============================================
        // 示例2: 带条件的JavaScript函数
        // ============================================
        example2_ConditionalFunction();
        
        // ============================================
        // 示例3: 复杂的JavaScript函数
        // ============================================
        example3_ComplexFunction();
    }
    
    /**
     * 示例1: 简单的JavaScript函数 - 求和
     * 
     * JavaScript脚本格式：
     * - 函数表达式: "function(a, b) { return a + b; }"
     * - 箭头函数: "(a, b) => a + b"
     */
    private static void example1_SimpleSum() {
        System.out.println("\n========== 示例1: JavaScript求和函数 ==========");
        
        // 方式1: 使用函数表达式
        String jsScript1 = "function(a, b) { return a + b; }";
        FormulaCalculator.registerCustomFunction("MYJSSUM", "js", jsScript1);
        
        // 使用自定义函数
        String formula1 = "MYJSSUM(10, 20)";
        JSONObject data = new JSONObject();
        Object result1 = FormulaCalculator.calculate(formula1, data);
        System.out.println("公式: " + formula1);
        System.out.println("结果: " + result1); // 应该输出: 30.0
        
        // 方式2: 使用箭头函数（如果JavaScript引擎支持）
        try {
            String jsScript2 = "(a, b) => a + b";
            FormulaCalculator.registerCustomFunction("MYJSSUM2", "js", jsScript2);
            
            String formula2 = "MYJSSUM2(15, 25)";
            Object result2 = FormulaCalculator.calculate(formula2, data);
            System.out.println("公式: " + formula2);
            System.out.println("结果: " + result2); // 应该输出: 40.0
        } catch (Exception e) {
            System.out.println("箭头函数可能不被支持: " + e.getMessage());
        }
        
        // 方式3: 多个参数
        String jsScript3 = "function(a, b, c) { return a + b + c; }";
        FormulaCalculator.registerCustomFunction("MYJSSUM3", "js", jsScript3);
        
        String formula3 = "MYJSSUM3(10, 20, 30)";
        Object result3 = FormulaCalculator.calculate(formula3, data);
        System.out.println("公式: " + formula3);
        System.out.println("结果: " + result3); // 应该输出: 60.0
    }
    
    /**
     * 示例2: 带条件的JavaScript函数
     * 
     * 功能：如果第一个参数大于第二个参数，返回它们的差，否则返回它们的和
     */
    private static void example2_ConditionalFunction() {
        System.out.println("\n========== 示例2: 带条件的JavaScript函数 ==========");
        
        String jsScript = "function(a, b) { " +
                         "  if (a > b) { " +
                         "    return a - b; " +
                         "  } else { " +
                         "    return a + b; " +
                         "  } " +
                         "}";
        
        FormulaCalculator.registerCustomFunction("MYJSCOND", "js", jsScript);
        
        // 测试 a > b 的情况
        String formula1 = "MYJSCOND(10, 5)";
        JSONObject data = new JSONObject();
        Object result1 = FormulaCalculator.calculate(formula1, data);
        System.out.println("公式: " + formula1 + " (a > b)");
        System.out.println("结果: " + result1); // 应该输出: 5.0
        
        // 测试 a <= b 的情况
        String formula2 = "MYJSCOND(5, 10)";
        Object result2 = FormulaCalculator.calculate(formula2, data);
        System.out.println("公式: " + formula2 + " (a <= b)");
        System.out.println("结果: " + result2); // 应该输出: 15.0
    }
    
    /**
     * 示例3: 复杂的JavaScript函数
     * 
     * 功能：计算折扣后的总价
     * 公式：DISCOUNT_TOTAL(单价, 数量, 折扣率)
     */
    private static void example3_ComplexFunction() {
        System.out.println("\n========== 示例3: 复杂的JavaScript函数 ==========");
        
        String jsScript = "function(price, quantity, discount) { " +
                         "  var total = price * quantity; " +
                         "  var discountAmount = total * discount; " +
                         "  return total - discountAmount; " +
                         "}";
        
        FormulaCalculator.registerCustomFunction("DISCOUNT_TOTAL", "js", jsScript);
        
        // 使用自定义函数
        String formula = "DISCOUNT_TOTAL(100, 5, 0.2)"; // 单价100，数量5，折扣率20%
        JSONObject data = new JSONObject();
        Object result = FormulaCalculator.calculate(formula, data);
        System.out.println("公式: " + formula);
        System.out.println("结果: " + result); // 应该输出: 400.0 (100*5*(1-0.2))
        
        // 与其他函数组合使用
        String complexFormula = "DISCOUNT_TOTAL(100, 5, 0.2) + DISCOUNT_TOTAL(50, 3, 0.1)";
        Object complexResult = FormulaCalculator.calculate(complexFormula, data);
        System.out.println("复杂公式: " + complexFormula);
        System.out.println("结果: " + complexResult); // 应该输出: 535.0
    }
}

