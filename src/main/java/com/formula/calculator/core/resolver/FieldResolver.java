package com.formula.calculator.core.resolver;

import com.alibaba.fastjson.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static com.formula.calculator.utils.FormulaParamUtils.convertToDouble;

/** 字段引用与展示名称解析；当前阶段保留旧转换规则与替换顺序。 */
public final class FieldResolver {
    private FieldResolver() { }
    /** 解析 ${field}、[field] 和映射名称，缺失与非法数值仍按 0 处理。 */
    public static String resolve(String formula, JSONObject data, Map<String, String> fieldMapping) {
        String result = formula;

        // 先处理 ${fieldName} 和 [fieldName] 格式
        Pattern pattern1 = Pattern.compile("\\$\\{([^}]+)\\}|\\[([^\\]]+)\\]");
        Matcher matcher1 = pattern1.matcher(formula);
        StringBuffer sb1 = new StringBuffer();

        while (matcher1.find()) {
            String fieldName = matcher1.group(1) != null ? matcher1.group(1) : matcher1.group(2);
            String enCode = getEnCode(fieldName, fieldMapping);
            Object value = data.get(enCode);
            String valueStr = convertToNumericString(value);
            matcher1.appendReplacement(sb1, valueStr);
        }
        matcher1.appendTail(sb1);
        result = sb1.toString();

        // 处理直接字段名（在函数参数中）
        // 注意：需要小心处理，避免替换函数名和已替换的值
        if (fieldMapping != null && !fieldMapping.isEmpty()) {
            // 按长度降序排序，先替换长的字段名，避免部分匹配问题
            List<Map.Entry<String, String>> sortedEntries = new ArrayList<>(fieldMapping.entrySet());
            sortedEntries.sort((a, b) -> Integer.compare(b.getKey().length(), a.getKey().length()));

            for (Map.Entry<String, String> entry : sortedEntries) {
                String menuId = entry.getKey();
                String enCode = entry.getValue();

                // 使用单词边界匹配，避免替换函数名和已替换的数值
                // 匹配：字段名前后不是字母、数字、下划线、中文字符
                Pattern fieldPattern = Pattern.compile("(?<![\\w\\u4e00-\\u9fa5])" + Pattern.quote(menuId) + "(?![\\w\\u4e00-\\u9fa5])");
                Matcher fieldMatcher = fieldPattern.matcher(result);
                StringBuffer sb2 = new StringBuffer();

                while (fieldMatcher.find()) {
                    // 检查是否在函数名位置（函数名后应该有左括号）
                    int end = fieldMatcher.end();
                    if (end < result.length() && result.charAt(end) == '(') {
                        // 这是函数名，不替换
                        fieldMatcher.appendReplacement(sb2, fieldMatcher.group(0));
                        continue;
                    }

                    Object value = data.get(enCode);
                    String valueStr = convertToNumericString(value);
                    fieldMatcher.appendReplacement(sb2, valueStr);
                }
                fieldMatcher.appendTail(sb2);
                result = sb2.toString();
            }
        }

        return result;
    }

    public static String getEnCode(String fieldName, Map<String, String> fieldMapping) {
        if (fieldMapping != null && fieldMapping.containsKey(fieldName)) {
            return fieldMapping.get(fieldName);
        }
        return fieldName; // 如果没有映射，直接使用原名称
    }

    private static String convertToNumericString(Object value) {
        if (value == null) {
            return "0";
        }

        if (value instanceof Number) {
            return value.toString();
        }

        try {
            return String.valueOf(convertToDouble(value));
        } catch (Exception e) {
            return "0";
        }
    }
}
