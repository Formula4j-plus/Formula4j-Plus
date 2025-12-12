package com.formula.calculator;

import com.alibaba.fastjson.JSONObject;
import com.formula.calculator.model.FormulaData;
import com.formula.calculator.model.FieldMark;
import com.formula.calculator.utils.FormulaParamUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 自定义函数使用示例
 * 
 * 本示例展示如何：
 * 1. 在Java中定义自定义函数
 * 2. 注册自定义函数
 * 3. 在公式中使用自定义函数
 * 4. 对应的JavaScript函数实现（用于前端）
 */
public class CustomFunctionExample {
    
    public static void main(String[] args) {
        // ============================================
        // 示例1: 简单的自定义函数 - 计算折扣价格
        // ============================================
        example1_DiscountPrice();
        
        // ============================================
        // 示例2: 带多个参数的自定义函数 - 计算税费
        // ============================================
        example2_TaxCalculation();
        
        // ============================================
        // 示例3: 使用字段映射的自定义函数 - 计算总价
        // ============================================
        example3_TotalPrice();
        
        // ============================================
        // 示例4: 复杂的自定义函数 - 计算运费
        // ============================================
        example4_ShippingFee();
    }
    
    /**
     * 示例1: 简单的自定义函数 - 计算折扣价格
     * 
     * 功能：根据原价和折扣率计算折扣后的价格
     * 公式：DISCOUNT_PRICE(原价, 折扣率)
     * 
     * JavaScript对应实现：
     * function DISCOUNT_PRICE(price, discount) {
     *     return price * (1 - discount);
     * }
     */
    private static void example1_DiscountPrice() {
        System.out.println("\n========== 示例1: 折扣价格计算 ==========");
        
        // 1. 注册自定义函数
        FormulaCalculator.registerCustomFunction("DISCOUNT_PRICE", (params, data, fieldMapping) -> {
            try {
                // 解析参数（参数格式：原价,折扣率）
                String[] parts = FormulaParamUtils.splitFunctionParams(params, 2);
                if (parts.length < 2) {
                    return "0";
                }
                
                // 获取参数值
                double price = Double.parseDouble(parts[0].trim());
                double discount = Double.parseDouble(parts[1].trim());
                
                // 计算折扣价格：原价 * (1 - 折扣率)
                double result = price * (1 - discount);
                
                return String.valueOf(result);
            } catch (Exception e) {
                return "0";
            }
        });
        
        // 2. 使用自定义函数
        String formula = "DISCOUNT_PRICE(100, 0.2)"; // 原价100，折扣率20%
        JSONObject data = new JSONObject();
        
        Object result = FormulaCalculator.calculate(formula, data);
        System.out.println("公式: " + formula);
        System.out.println("结果: " + result); // 应该输出: 80.0
        
        // 3. 在复杂公式中使用
        String complexFormula = "DISCOUNT_PRICE(100, 0.2) + DISCOUNT_PRICE(200, 0.15)";
        Object complexResult = FormulaCalculator.calculate(complexFormula, data);
        System.out.println("复杂公式: " + complexFormula);
        System.out.println("结果: " + complexResult); // 应该输出: 270.0
        
        // 4. 与字段结合使用
        FormulaData formulaData = new FormulaData();
        formulaData.setTxt("DISCOUNT_PRICE(原价, 折扣率)");
        
        List<FieldMark> marks = new ArrayList<>();
        marks.add(new FieldMark("price", "原价"));
        marks.add(new FieldMark("discount", "折扣率"));
        formulaData.setMarks(marks);
        
        JSONObject fieldData = new JSONObject();
        fieldData.put("price", 150);
        fieldData.put("discount", 0.3);
        
        Object fieldResult = FormulaCalculator.calculate(formulaData, fieldData);
        System.out.println("使用字段: DISCOUNT_PRICE(原价, 折扣率)");
        System.out.println("结果: " + fieldResult); // 应该输出: 105.0
    }
    
    /**
     * 示例2: 带多个参数的自定义函数 - 计算税费
     * 
     * 功能：根据金额、税率和是否含税计算税费或总价
     * 公式：TAX_CALC(金额, 税率, 是否含税)
     * 是否含税: 1表示含税，0表示不含税
     * 
     * JavaScript对应实现：
     * function TAX_CALC(amount, taxRate, isInclusive) {
     *     if (isInclusive === 1) {
     *         // 含税：税费 = 金额 - 金额 / (1 + 税率)
     *         return amount - amount / (1 + taxRate);
     *     } else {
     *         // 不含税：税费 = 金额 * 税率
     *         return amount * taxRate;
     *     }
     * }
     */
    private static void example2_TaxCalculation() {
        System.out.println("\n========== 示例2: 税费计算 ==========");
        
        // 1. 注册自定义函数
        FormulaCalculator.registerCustomFunction("TAX_CALC", (params, data, fieldMapping) -> {
            try {
                String[] parts = FormulaParamUtils.splitFunctionParams(params, 3);
                if (parts.length < 3) {
                    return "0";
                }
                
                double amount = Double.parseDouble(parts[0].trim());
                double taxRate = Double.parseDouble(parts[1].trim());
                int isInclusive = Integer.parseInt(parts[2].trim());
                
                double tax;
                if (isInclusive == 1) {
                    // 含税：税费 = 金额 - 金额 / (1 + 税率)
                    tax = amount - amount / (1 + taxRate);
                } else {
                    // 不含税：税费 = 金额 * 税率
                    tax = amount * taxRate;
                }
                
                return String.valueOf(tax);
            } catch (Exception e) {
                return "0";
            }
        });
        
        // 2. 测试含税情况
        String formula1 = "TAX_CALC(110, 0.1, 1)"; // 含税金额110，税率10%
        JSONObject data = new JSONObject();
        Object result1 = FormulaCalculator.calculate(formula1, data);
        System.out.println("含税公式: " + formula1);
        System.out.println("税费: " + result1); // 应该输出: 10.0
        
        // 3. 测试不含税情况
        String formula2 = "TAX_CALC(100, 0.1, 0)"; // 不含税金额100，税率10%
        Object result2 = FormulaCalculator.calculate(formula2, data);
        System.out.println("不含税公式: " + formula2);
        System.out.println("税费: " + result2); // 应该输出: 10.0
    }
    
    /**
     * 示例3: 使用字段映射的自定义函数 - 计算总价
     * 
     * 功能：根据单价、数量和折扣计算总价
     * 公式：TOTAL_PRICE(单价, 数量, 折扣)
     * 
     * JavaScript对应实现：
     * function TOTAL_PRICE(unitPrice, quantity, discount) {
     *     return unitPrice * quantity * (1 - discount);
     * }
     */
    private static void example3_TotalPrice() {
        System.out.println("\n========== 示例3: 总价计算（使用字段映射） ==========");
        
        // 1. 注册自定义函数
        FormulaCalculator.registerCustomFunction("TOTAL_PRICE", (params, data, fieldMapping) -> {
            try {
                String[] parts = FormulaParamUtils.splitFunctionParams(params, 3);
                if (parts.length < 3) {
                    return "0";
                }
                
                // 解析参数，支持字段引用
                // 获取字段值：先尝试从data中获取，如果不存在则解析为数值
                double unitPrice = parseDoubleValue(parts[0].trim(), data, fieldMapping);
                double quantity = parseDoubleValue(parts[1].trim(), data, fieldMapping);
                double discount = parseDoubleValue(parts[2].trim(), data, fieldMapping);
                
                // 计算总价：单价 * 数量 * (1 - 折扣)
                double total = unitPrice * quantity * (1 - discount);
                
                return String.valueOf(total);
            } catch (Exception e) {
                return "0";
            }
        });
        
        // 2. 使用FormulaData和字段映射
        FormulaData formulaData = new FormulaData();
        formulaData.setTxt("TOTAL_PRICE(单价, 数量, 折扣率)");
        
        List<FieldMark> marks = new ArrayList<>();
        marks.add(new FieldMark("unitPrice", "单价"));
        marks.add(new FieldMark("quantity", "数量"));
        marks.add(new FieldMark("discount", "折扣率"));
        formulaData.setMarks(marks);
        
        JSONObject fieldData = new JSONObject();
        fieldData.put("unitPrice", 50);
        fieldData.put("quantity", 10);
        fieldData.put("discount", 0.15);
        
        Object result = FormulaCalculator.calculate(formulaData, fieldData);
        System.out.println("公式: TOTAL_PRICE(单价, 数量, 折扣率)");
        System.out.println("数据: 单价=50, 数量=10, 折扣率=0.15");
        System.out.println("结果: " + result); // 应该输出: 425.0
    }
    
    /**
     * 示例4: 复杂的自定义函数 - 计算运费
     * 
     * 功能：根据重量、距离和快递类型计算运费
     * 公式：SHIPPING_FEE(重量, 距离, 快递类型)
     * 快递类型: 1=标准快递, 2=加急快递, 3=特快专递
     * 
     * JavaScript对应实现：
     * function SHIPPING_FEE(weight, distance, expressType) {
     *     var basePrice = 10; // 基础运费
     *     var weightPrice = weight * 2; // 重量费用：每公斤2元
     *     var distancePrice = distance * 0.5; // 距离费用：每公里0.5元
     *     
     *     var typeMultiplier = 1;
     *     if (expressType === 2) typeMultiplier = 1.5; // 加急
     *     if (expressType === 3) typeMultiplier = 2; // 特快
     *     
     *     return (basePrice + weightPrice + distancePrice) * typeMultiplier;
     * }
     */
    private static void example4_ShippingFee() {
        System.out.println("\n========== 示例4: 运费计算（复杂逻辑） ==========");
        
        // 1. 注册自定义函数
        FormulaCalculator.registerCustomFunction("SHIPPING_FEE", (params, data, fieldMapping) -> {
            try {
                String[] parts = FormulaParamUtils.splitFunctionParams(params, 3);
                if (parts.length < 3) {
                    return "0";
                }
                
                double weight = Double.parseDouble(parts[0].trim());
                double distance = Double.parseDouble(parts[1].trim());
                int expressType = Integer.parseInt(parts[2].trim());
                
                // 基础运费
                double basePrice = 10;
                // 重量费用：每公斤2元
                double weightPrice = weight * 2;
                // 距离费用：每公里0.5元
                double distancePrice = distance * 0.5;
                
                // 快递类型倍数
                double typeMultiplier = 1.0;
                if (expressType == 2) {
                    typeMultiplier = 1.5; // 加急快递
                } else if (expressType == 3) {
                    typeMultiplier = 2.0; // 特快专递
                }
                
                // 计算总运费
                double totalFee = (basePrice + weightPrice + distancePrice) * typeMultiplier;
                
                return String.valueOf(totalFee);
            } catch (Exception e) {
                return "0";
            }
        });
        
        // 2. 测试标准快递
        String formula1 = "SHIPPING_FEE(5, 100, 1)"; // 5公斤，100公里，标准快递
        JSONObject data = new JSONObject();
        Object result1 = FormulaCalculator.calculate(formula1, data);
        System.out.println("标准快递: " + formula1);
        System.out.println("运费: " + result1); // 应该输出: 70.0
        
        // 3. 测试加急快递
        String formula2 = "SHIPPING_FEE(5, 100, 2)"; // 5公斤，100公里，加急快递
        Object result2 = FormulaCalculator.calculate(formula2, data);
        System.out.println("加急快递: " + formula2);
        System.out.println("运费: " + result2); // 应该输出: 105.0
        
        // 4. 测试特快专递
        String formula3 = "SHIPPING_FEE(5, 100, 3)"; // 5公斤，100公里，特快专递
        Object result3 = FormulaCalculator.calculate(formula3, data);
        System.out.println("特快专递: " + formula3);
        System.out.println("运费: " + result3); // 应该输出: 140.0
        
        // 5. 与其他函数组合使用
        String complexFormula = "SHIPPING_FEE(5, 100, 1) + SHIPPING_FEE(3, 50, 2)";
        Object complexResult = FormulaCalculator.calculate(complexFormula, data);
        System.out.println("组合公式: " + complexFormula);
        System.out.println("总运费: " + complexResult);
    }
    
    /**
     * 辅助方法：解析数值（支持字段映射）
     */
    private static double parseDoubleValue(String param, JSONObject data, Map<String, String> fieldMapping) {
        try {
            // 先尝试从data中获取字段值
            if (fieldMapping != null && fieldMapping.containsKey(param)) {
                String enCode = fieldMapping.get(param);
                if (data.containsKey(enCode)) {
                    Object value = data.get(enCode);
                    if (value instanceof Number) {
                        return ((Number) value).doubleValue();
                    } else if (value instanceof String) {
                        return Double.parseDouble((String) value);
                    }
                }
            }
            
            // 如果不是字段引用，直接解析为数值
            return Double.parseDouble(param.trim());
        } catch (Exception e) {
            return 0.0;
        }
    }
}

