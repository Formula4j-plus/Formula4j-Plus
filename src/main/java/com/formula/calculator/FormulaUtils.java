package com.formula.calculator;

import com.alibaba.fastjson.JSONObject;
import com.formula.calculator.model.FormulaData;

/**
 * 公式计算工具类
 * 提供便捷的静态方法
 * 
 * @author WanShen
 * @version 2.0.0
 */
public class FormulaUtils {
    
    /**
     * 计算公式（便捷方法）
     * 
     * @param formula 公式表达式
     * @param data JSONObject数据
     * @return 计算结果
     */
    public static Object calculate(String formula, JSONObject data) {
        return FormulaCalculator.calculate(formula, data);
    }
    
    /**
     * 计算公式（使用FormulaData结构）
     * 
     * @param formulaData 公式数据结构
     * @param data JSONObject数据（使用enCode作为key）
     * @return 计算结果
     */
    public static Object calculate(FormulaData formulaData, JSONObject data) {
        return FormulaCalculator.calculate(formulaData, data);
    }
    
    /**
     * 计算公式并返回double类型
     * 
     * @param formula 公式表达式
     * @param data JSONObject数据
     * @return double计算结果
     */
    public static double calculateDouble(String formula, JSONObject data) {
        Object result = FormulaCalculator.calculate(formula, data);
        if (result == null) {
            return 0;
        }
        if (result instanceof Number) {
            return ((Number) result).doubleValue();
        }
        return Double.parseDouble(result.toString());
    }
    
    /**
     * 计算公式并返回double类型（使用FormulaData）
     * 
     * @param formulaData 公式数据结构
     * @param data JSONObject数据
     * @return double计算结果
     */
    public static double calculateDouble(FormulaData formulaData, JSONObject data) {
        Object result = FormulaCalculator.calculate(formulaData, data);
        if (result == null) {
            return 0;
        }
        if (result instanceof Number) {
            return ((Number) result).doubleValue();
        }
        return Double.parseDouble(result.toString());
    }
    
    /**
     * 计算公式并返回long类型
     * 
     * @param formula 公式表达式
     * @param data JSONObject数据
     * @return long计算结果
     */
    public static long calculateLong(String formula, JSONObject data) {
        Object result = FormulaCalculator.calculate(formula, data);
        if (result == null) {
            return 0;
        }
        if (result instanceof Number) {
            return ((Number) result).longValue();
        }
        return Long.parseLong(result.toString());
    }
    
    /**
     * 计算公式并返回long类型（使用FormulaData）
     * 
     * @param formulaData 公式数据结构
     * @param data JSONObject数据
     * @return long计算结果
     */
    public static long calculateLong(FormulaData formulaData, JSONObject data) {
        Object result = FormulaCalculator.calculate(formulaData, data);
        if (result == null) {
            return 0;
        }
        if (result instanceof Number) {
            return ((Number) result).longValue();
        }
        return Long.parseLong(result.toString());
    }
    
    /**
     * 验证前端计算结果是否正确
     * 
     * @param formula 公式表达式
     * @param data JSONObject数据
     * @param frontendResult 前端计算结果
     * @param tolerance 允许的误差范围（默认0.0001）
     * @return 是否一致
     */
    public static boolean verify(String formula, JSONObject data, Object frontendResult, double tolerance) {
        try {
            Object backendResult = FormulaCalculator.calculate(formula, data);
            
            if (backendResult == null && frontendResult == null) {
                return true;
            }
            
            if (backendResult == null || frontendResult == null) {
                return false;
            }
            
            double backend = convertToDouble(backendResult);
            double frontend = convertToDouble(frontendResult);
            
            return Math.abs(backend - frontend) <= tolerance;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 验证前端计算结果是否正确（使用FormulaData）
     * 
     * @param formulaData 公式数据结构
     * @param data JSONObject数据
     * @param frontendResult 前端计算结果
     * @param tolerance 允许的误差范围
     * @return 是否一致
     */
    public static boolean verify(FormulaData formulaData, JSONObject data, Object frontendResult, double tolerance) {
        try {
            Object backendResult = FormulaCalculator.calculate(formulaData, data);
            
            if (backendResult == null && frontendResult == null) {
                return true;
            }
            
            if (backendResult == null || frontendResult == null) {
                return false;
            }
            
            double backend = convertToDouble(backendResult);
            double frontend = convertToDouble(frontendResult);
            
            return Math.abs(backend - frontend) <= tolerance;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 验证前端计算结果是否正确（默认误差0.0001）
     */
    public static boolean verify(String formula, JSONObject data, Object frontendResult) {
        return verify(formula, data, frontendResult, 0.0001);
    }
    
    /**
     * 验证前端计算结果是否正确（使用FormulaData，默认误差0.0001）
     */
    public static boolean verify(FormulaData formulaData, JSONObject data, Object frontendResult) {
        return verify(formulaData, data, frontendResult, 0.0001);
    }
    
    /**
     * 转换为double
     */
    private static double convertToDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return Double.parseDouble(value.toString());
    }
}
