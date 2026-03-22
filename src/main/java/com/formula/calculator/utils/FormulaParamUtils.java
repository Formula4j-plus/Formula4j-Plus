package com.formula.calculator.utils;

import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 公式计算参数工具类
 * 提供参数解析相关的辅助函数
 * 
 * @author WanShen
 */
public class FormulaParamUtils {
    
    /**
     * 分割函数参数（处理嵌套括号）
     */
    public static String[] splitFunctionParams(String params, int expectedCount) {
        List<String> result = new ArrayList<>();
        int depth = 0;
        StringBuilder current = new StringBuilder();
        boolean inString = false;
        boolean escaped = false;
        
        for (char c : params.toCharArray()) {
            if (escaped) {
                current.append(c);
                escaped = false;
            } else if (c == '\\') {
                current.append(c);
                escaped = true;
            } else if (c == '"') {
                current.append(c);
                inString = !inString;
            } else if (!inString && c == '(') {
                depth++;
                current.append(c);
            } else if (!inString && c == ')') {
                depth--;
                current.append(c);
            } else if (!inString && c == ',' && depth == 0) {
                result.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        
        if (current.length() > 0) {
            result.add(current.toString());
        }
        
        return result.toArray(new String[0]);
    }
    
    /**
     * 解析参数值列表（支持字段映射）
     */
    public static List<Double> parseValues(String params, JSONObject data, 
                                          Map<String, String> fieldMapping,
                                          Function<String, String> fieldReplacer) {
        List<Double> values = new ArrayList<>();
        String[] parts = splitFunctionParams(params, -1);
        
        for (String part : parts) {
            part = part.trim();
            
            // 先替换字段引用
            if (fieldReplacer != null) {
                part = fieldReplacer.apply(part);
            }
            
            try {
                // 尝试作为数值解析
                double value = Double.parseDouble(part);
                values.add(value);
            } catch (NumberFormatException e) {
                values.add(0.0);
            }
        }
        
        return values;
    }
    
    /**
     * 计算条件表达式
     */
    public static boolean evaluateCondition(String condition) {
        try {
            condition = condition.trim();
            
            // 支持常见的比较运算符
            if (condition.contains(">=")) {
                String[] parts = condition.split(">=", 2);
                double left = Double.parseDouble(parts[0].trim());
                double right = Double.parseDouble(parts[1].trim());
                return left >= right;
            } else if (condition.contains("<=")) {
                String[] parts = condition.split("<=", 2);
                double left = Double.parseDouble(parts[0].trim());
                double right = Double.parseDouble(parts[1].trim());
                return left <= right;
            } else if (condition.contains("!=")) {
                String[] parts = condition.split("!=", 2);
                double left = Double.parseDouble(parts[0].trim());
                double right = Double.parseDouble(parts[1].trim());
                return left != right;
            } else if (condition.contains("==")) {
                String[] parts = condition.split("==", 2);
                double left = Double.parseDouble(parts[0].trim());
                double right = Double.parseDouble(parts[1].trim());
                return left == right;
            } else if (condition.contains(">")) {
                String[] parts = condition.split(">", 2);
                double left = Double.parseDouble(parts[0].trim());
                double right = Double.parseDouble(parts[1].trim());
                return left > right;
            } else if (condition.contains("<")) {
                String[] parts = condition.split("<", 2);
                double left = Double.parseDouble(parts[0].trim());
                double right = Double.parseDouble(parts[1].trim());
                return left < right;
            }
            
            // 默认尝试作为布尔值解析
            return Boolean.parseBoolean(condition);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 转换为double
     */
    public static double convertToDouble(Object value) {
        if (value == null) {
            return 0.0;
        }
        
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        
        if (value instanceof String) {
            String str = ((String) value).trim();
            if (str.isEmpty()) {
                return 0.0;
            }
            try {
                return Double.parseDouble(str);
            } catch (NumberFormatException e) {
                // 无法解析为数字，返回0（计算类函数）
                return 0.0;
            }
        }
        
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            // 无法解析为数字，返回0（计算类函数）
            return 0.0;
        }
    }
    
    /**
     * 转换为字符串（拼接类函数使用）
     * 保留原始值，不转换为0
     */
    public static String convertToString(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof String) {
            return (String) value;
        }
        if (value instanceof Number) {
            // 如果是整数，不显示小数点；如果是小数，显示小数
            double num = ((Number) value).doubleValue();
            if (num == (long) num) {
                return String.valueOf((long) num);
            } else {
                return String.valueOf(num);
            }
        }
        return value.toString();
    }
}

