package com.formula.calculator;

import com.alibaba.fastjson.JSONObject;
import com.formula.calculator.model.FieldMark;
import com.formula.calculator.model.FormulaData;
import com.formula.calculator.utils.*;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.formula.calculator.FunctionName.*;
import static com.formula.calculator.utils.FormulaMathUtils.*;
import static com.formula.calculator.utils.FormulaStatisticsUtils.*;
import static com.formula.calculator.utils.FormulaDateUtils.*;
import static com.formula.calculator.utils.FormulaStringUtils.*;
import static com.formula.calculator.utils.FormulaParamUtils.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * 公式计算引擎
 * 支持Formula.js语法，用于后端验证前端计算结果
 * 支持常用、逻辑、统计、数学等函数
 * 
 * @author Formula Calculator
 * @version 2.0.0
 */
public class FormulaCalculator {
    
    private static final Logger logger = LoggerFactory.getLogger(FormulaCalculator.class);
    
    // 匹配字段引用，如 ${fieldName} 或 [fieldName] 或直接字段名
    private static final Pattern FIELD_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}|\\[([^\\]]+)\\]|([a-zA-Z_][a-zA-Z0-9_\\u4e00-\\u9fa5]*)");
    
    // 自定义函数注册表
    private static final Map<String, CustomFunction> customFunctions = new HashMap<>();
    
    // 静态初始化：注册TESTABC自定义函数
    static {
        registerCustomFunction(TESTABC.getName(), (params, data, fieldMapping) -> {
            // TESTABC自定义函数实现
            // 这里可以根据实际需求实现
            return params; // 示例：返回参数本身
        });
    }
    
    /**
     * 计算公式（使用FormulaData结构）
     * 
     * @param formulaData 公式数据结构，包含txt和marks
     * @param data JSONObject包含所有字段值（使用enCode作为key）
     * @return 计算结果
     * @throws FormulaException 公式计算异常
     */
    public static Object calculate(FormulaData formulaData, JSONObject data) throws FormulaException {
        if (formulaData == null || formulaData.getTxt() == null || formulaData.getTxt().trim().isEmpty()) {
            return null;
        }
        
        // 构建字段映射：menuId -> enCode
        Map<String, String> fieldMapping = formulaData.buildFieldMapping();
        
        return calculate(formulaData.getTxt(), data, fieldMapping);
    }
    
    /**
     * 计算公式（兼容旧版本）
     * 
     * @param formula 公式表达式
     * @param data JSONObject包含所有字段值
     * @return 计算结果
     * @throws FormulaException 公式计算异常
     */
    public static Object calculate(String formula, JSONObject data) throws FormulaException {
        return calculate(formula, data, null);
    }
    
    /**
     * 计算公式（支持字段映射）
     * 
     * @param formula 公式表达式
     * @param data JSONObject包含所有字段值
     * @param fieldMapping 字段映射：显示名称 -> enCode
     * @return 计算结果
     * @throws FormulaException 公式计算异常
     */
    public static Object calculate(String formula, JSONObject data, Map<String, String> fieldMapping) throws FormulaException {
        if (formula == null || formula.trim().isEmpty()) {
            return null;
        }
        
        try {
            // 先处理CONCATENATE函数（需要在replaceFields之前处理，以保留字符串值）
            String processedFormula = processConcatenateFunction(formula, data, fieldMapping);
            
            // 替换字段引用为实际值（支持字段映射）
            processedFormula = replaceFields(processedFormula, data, fieldMapping);
            
            // 处理所有函数（按优先级处理）
            processedFormula = processAllFunctions(processedFormula, data, fieldMapping);
            
            // 检查结果是否是字符串字面量（用引号包裹）
            String trimmed = processedFormula.trim();
            if (trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
                // 返回字符串（移除引号）
                return trimmed.substring(1, trimmed.length() - 1);
            }
            
            // 检查是否包含字符串拼接（有字符串字面量参与+运算）
            if (containsStringConcatenation(processedFormula)) {
                // 进行字符串拼接
                logger.debug("检测到字符串拼接，表达式: {}", processedFormula);
                return evaluateStringConcatenation(processedFormula);
            }
            
            // 使用exp4j计算表达式
            return evaluateExpression(processedFormula);
            
        } catch (Exception e) {
            logger.error("公式计算失败: formula={}, error={}", formula, e.getMessage(), e);
            throw new FormulaException("公式计算失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 替换公式中的字段引用为实际值（支持字段映射）
     */
    private static String replaceFields(String formula, JSONObject data, Map<String, String> fieldMapping) {
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
    
    /**
     * 获取字段的enCode
     */
    private static String getEnCode(String fieldName, Map<String, String> fieldMapping) {
        if (fieldMapping != null && fieldMapping.containsKey(fieldName)) {
            return fieldMapping.get(fieldName);
        }
        return fieldName; // 如果没有映射，直接使用原名称
    }
    
    /**
     * 处理所有函数
     */
    private static String processAllFunctions(String formula, JSONObject data, Map<String, String> fieldMapping) {
        String result = formula;
        boolean changed = true;
        int maxIterations = 50; // 防止无限循环
        int iterations = 0;
        
        // 循环处理，直到没有更多函数需要处理
        while (changed && iterations < maxIterations) {
            changed = false;
            iterations++;
            String before = result;
            
            // 1. 处理文本函数（需要先处理，因为可能在条件中使用）
            result = processTextFunctions(result, data, fieldMapping);
            // 2. 处理日期时间函数
            result = processDateTimeFunctions(result, data, fieldMapping);
            // 3. 处理信息函数
            result = processInformationFunctions(result, data, fieldMapping);
            // 4. 处理统计函数（SUM等需要先处理，因为可能在IF函数中）
            result = processStatisticalFunctions(result, data, fieldMapping);
            // 5. 处理数学函数（包括PI、SQRT等）
            result = processMathFunctions(result);
            // 6. 处理常用函数（SUM、AVERAGE等）
            result = processCommonFunctions(result, data, fieldMapping);
            // 7. 处理查找函数
            result = processLookupFunctions(result, data, fieldMapping);
            // 8. 处理财务函数
            result = processFinancialFunctions(result, data, fieldMapping);
            // 9. 处理工程函数
            result = processEngineeringFunctions(result, data, fieldMapping);
            // 10. 处理分布函数
            result = processDistributionFunctions(result, data, fieldMapping);
            // 11. 处理逻辑函数（IF函数最后处理，因为它的参数可能包含其他函数）
            result = processLogicalFunctions(result, data, fieldMapping);
            // 12. 处理自定义函数
            result = processCustomFunctions(result, data, fieldMapping);
            // 13. 再次处理数学函数（处理嵌套情况，如SQRTPI中的PI）
            result = processMathFunctions(result);
            
            if (!result.equals(before)) {
                changed = true;
            }
        }
        
        return result;
    }
    
    /**
     * 处理常用函数
     */
    private static String processCommonFunctions(String formula, JSONObject data, Map<String, String> fieldMapping) {
        String result = formula;
        
        // SUM
        result = processFunction(result, SUM.getName(), params -> {
            List<Double> values = parseValues(params, data, fieldMapping);
            return String.valueOf(values.stream().mapToDouble(Double::doubleValue).sum());
        });
        
        // AVERAGE
        result = processFunction(result, AVERAGE.getName(), params -> {
            List<Double> values = parseValues(params, data, fieldMapping);
            if (values.isEmpty()) return "0";
            return String.valueOf(values.stream().mapToDouble(Double::doubleValue).average().orElse(0));
        });
        
        // MAX
        result = processFunction(result, MAX.getName(), params -> {
            List<Double> values = parseValues(params, data, fieldMapping);
            return String.valueOf(values.stream().mapToDouble(Double::doubleValue).max().orElse(0));
        });
        
        // MIN
        result = processFunction(result, MIN.getName(), params -> {
            List<Double> values = parseValues(params, data, fieldMapping);
            return String.valueOf(values.stream().mapToDouble(Double::doubleValue).min().orElse(0));
        });
        
        // COUNT
        result = processFunction(result, COUNT.getName(), params -> {
            List<Double> values = parseValues(params, data, fieldMapping);
            return String.valueOf(values.size());
        });
        
        // COUNTIF
        result = processFunction(result, COUNTIF.getName(), params -> {
            String[] parts = FormulaParamUtils.splitFunctionParams(params, 2);
            if (parts.length < 2) return "0";
            List<Double> values = parseValues(parts[0], data, fieldMapping);
            String condition = parts[1].trim();
            long count = values.stream().filter(v -> evaluateCondition(v.toString() + condition)).count();
            return String.valueOf(count);
        });
        
        // SUMIF
        result = processFunction(result, SUMIF.getName(), params -> {
            String[] parts = FormulaParamUtils.splitFunctionParams(params, 3);
            if (parts.length < 2) return "0";
            // 简化处理：SUMIF(range, criteria, [sum_range])
            return "0"; // 需要更复杂的实现
        });
        
        // IF
        result = processIfFunction(result, data, fieldMapping);
        
        // ROUND
        result = processRoundFunction(result);
        
        // ROUNDUP
        result = processFunction(result, ROUNDUP.getName(), params -> {
            String[] parts = FormulaParamUtils.splitFunctionParams(params, 2);
            if (parts.length < 2) return parts[0];
            try {
                double value = Double.parseDouble(parts[0].trim());
                int decimals = Integer.parseInt(parts[1].trim());
                BigDecimal bd = BigDecimal.valueOf(value);
                bd = bd.setScale(decimals, RoundingMode.CEILING);
                return String.valueOf(bd.doubleValue());
            } catch (Exception e) {
                return parts[0];
            }
        });
        
        // ROUNDDOWN
        result = processFunction(result, ROUNDDOWN.getName(), params -> {
            String[] parts = FormulaParamUtils.splitFunctionParams(params, 2);
            if (parts.length < 2) return parts[0];
            try {
                double value = Double.parseDouble(parts[0].trim());
                int decimals = Integer.parseInt(parts[1].trim());
                BigDecimal bd = BigDecimal.valueOf(value);
                bd = bd.setScale(decimals, RoundingMode.FLOOR);
                return String.valueOf(bd.doubleValue());
            } catch (Exception e) {
                return parts[0];
            }
        });
        
        // CONCATENATE - 连接字符串（字符串连接）
        result = processFunction(result, FunctionName.CONCATENATE.getName(), params -> {
            String[] parts = FormulaParamUtils.splitFunctionParams(params, -1);
            StringBuilder sb = new StringBuilder();
            for (String part : parts) {
                part = part.trim();
                String valueStr = null;
                
                // 先尝试从原始数据中获取值（保留字符串类型）
                // 检查是否是字段名（通过fieldMapping查找）
                String enCode = getEnCode(part, fieldMapping);
                Object rawValue = data.get(enCode);
                
                if (rawValue != null) {
                    // 如果找到了原始值，直接使用（保留字符串类型）
                    // 如果是字符串类型，直接使用；如果是数值类型，转换为字符串（不保留小数点）
                    if (rawValue instanceof String) {
                        valueStr = (String) rawValue;
                    } else if (rawValue instanceof Number) {
                        // 如果是整数，不显示小数点；如果是小数，显示小数
                        double num = ((Number) rawValue).doubleValue();
                        if (num == (long) num) {
                            valueStr = String.valueOf((long) num);
                        } else {
                            valueStr = String.valueOf(num);
                        }
                    } else {
                        valueStr = rawValue.toString();
                    }
                } else {
                    // 如果没有找到，尝试作为已替换的值或字面量处理
                    // 移除引号（如果有）
                    if (part.startsWith("\"") && part.endsWith("\"")) {
                        valueStr = part.substring(1, part.length() - 1);
                    } else {
                        // 尝试作为数值解析，然后转换为字符串（不保留小数点）
                        try {
                            double num = Double.parseDouble(part);
                            if (num == (long) num) {
                                valueStr = String.valueOf((long) num);
                            } else {
                                valueStr = String.valueOf(num);
                            }
                        } catch (NumberFormatException e) {
                            valueStr = part;
                        }
                    }
                }
                
                sb.append(valueStr);
            }
            // 返回拼接后的字符串（用引号包裹，以便后续识别为字符串字面量）
            return "\"" + sb.toString() + "\"";
        });
        
        // DATE - 返回特定日期（简化实现，返回日期的时间戳或天数）
        result = processFunction(result, DATE.getName(), params -> {
            String[] parts = FormulaParamUtils.splitFunctionParams(params, 3);
            if (parts.length < 3) return "0";
            try {
                int year = Integer.parseInt(parts[0].trim());
                int month = Integer.parseInt(parts[1].trim());
                int day = Integer.parseInt(parts[2].trim());
                // 使用Calendar计算日期
                Calendar cal = Calendar.getInstance();
                cal.set(year, month - 1, day); // month从0开始
                // 返回时间戳（毫秒）或天数（从1900-01-01开始）
                long days = (cal.getTimeInMillis() - new GregorianCalendar(1900, 0, 1).getTimeInMillis()) / (1000L * 60 * 60 * 24);
                return String.valueOf(days);
            } catch (Exception e) {
                logger.warn("DATE函数计算失败: {}", e.getMessage());
                return "0";
            }
        });
        
        return result;
    }
    
    /**
     * 处理逻辑函数
     */
    private static String processLogicalFunctions(String formula, JSONObject data, Map<String, String> fieldMapping) {
        String result = formula;
        
        // AND
        result = processFunction(result, AND.getName(), params -> {
            String[] parts = FormulaParamUtils.splitFunctionParams(params, -1);
            for (String part : parts) {
                part = replaceFields(part.trim(), data, fieldMapping);
                if (!evaluateCondition(part)) {
                    return "0";
                }
            }
            return "1";
        });
        
        // OR
        result = processFunction(result, OR.getName(), params -> {
            String[] parts = FormulaParamUtils.splitFunctionParams(params, -1);
            for (String part : parts) {
                part = replaceFields(part.trim(), data, fieldMapping);
                if (evaluateCondition(part)) {
                    return "1";
                }
            }
            return "0";
        });
        
        // NOT
        result = processFunction(result, NOT.getName(), params -> {
            params = replaceFields(params.trim(), data, fieldMapping);
            return evaluateCondition(params) ? "0" : "1";
        });
        
        // XOR
        result = processFunction(result, XOR.getName(), params -> {
            String[] parts = FormulaParamUtils.splitFunctionParams(params, 2);
            if (parts.length < 2) return "0";
            boolean a = evaluateCondition(replaceFields(parts[0].trim(), data, fieldMapping));
            boolean b = evaluateCondition(replaceFields(parts[1].trim(), data, fieldMapping));
            return (a ^ b) ? "1" : "0";
        });
        
        // IF
        result = processIfFunction(result, data, fieldMapping);
        
        // IFS (多条件IF)
        result = processFunction(result, IFS.getName(), params -> {
            String[] parts = FormulaParamUtils.splitFunctionParams(params, -1);
            if (parts.length < 2 || parts.length % 2 != 0) return "0";
            for (int i = 0; i < parts.length; i += 2) {
                String condition = replaceFields(parts[i].trim(), data, fieldMapping);
                if (evaluateCondition(condition)) {
                    return replaceFields(parts[i + 1].trim(), data, fieldMapping);
                }
            }
            return "0";
        });
        
        // SWITCH - 多值选择
        result = processFunction(result, SWITCH.getName(), params -> {
            String[] parts = FormulaParamUtils.splitFunctionParams(params, -1);
            if (parts.length < 3) return "0";
            String expression = replaceFields(parts[0].trim(), data, fieldMapping);
            // 默认值在最后（如果参数个数为奇数）
            String defaultValue = parts.length % 2 == 0 ? parts[parts.length - 1].trim() : "0";
            if (parts.length % 2 == 0) {
                defaultValue = parts[parts.length - 1].trim();
            }
            
            // 遍历value-result对
            for (int i = 1; i < parts.length - (parts.length % 2 == 0 ? 1 : 0); i += 2) {
                String value = replaceFields(parts[i].trim(), data, fieldMapping);
                if (expression.equals(value)) {
                    return replaceFields(parts[i + 1].trim(), data, fieldMapping);
                }
            }
            return replaceFields(defaultValue, data, fieldMapping);
        });
        
        // IFERROR - 如果表达式错误，返回指定值
        result = processFunction(result, IFERROR.getName(), params -> {
            String[] parts = FormulaParamUtils.splitFunctionParams(params, 2);
            if (parts.length < 2) return parts[0];
            try {
                String expression = replaceFields(parts[0].trim(), data, fieldMapping);
                // 尝试计算表达式
                try {
                    Object calcResult = evaluateExpression(expression);
                    return String.valueOf(calcResult);
                } catch (Exception e) {
                    // 如果计算失败，返回错误值
                    return replaceFields(parts[1].trim(), data, fieldMapping);
                }
            } catch (Exception e) {
                return replaceFields(parts[1].trim(), data, fieldMapping);
            }
        });
        
        // IFNA - 如果值为#N/A，返回指定值
        result = processFunction(result, IFNA.getName(), params -> {
            String[] parts = FormulaParamUtils.splitFunctionParams(params, 2);
            if (parts.length < 2) return parts[0];
            try {
                String expression = replaceFields(parts[0].trim(), data, fieldMapping);
                // 检查是否为#N/A或null
                if (expression.equalsIgnoreCase("#N/A") || expression.equalsIgnoreCase("N/A") || expression.equals("null")) {
                    return replaceFields(parts[1].trim(), data, fieldMapping);
                }
                return expression;
            } catch (Exception e) {
                return replaceFields(parts[1].trim(), data, fieldMapping);
            }
        });
        
        return result;
    }
    
    /**
     * 处理统计函数
     */
    private static String processStatisticalFunctions(String formula, JSONObject data, Map<String, String> fieldMapping) {
        String result = formula;
        
        // STDEV (标准差)
        result = processFunction(result, STDEV.getName(), params -> {
            List<Double> values = parseValues(params, data, fieldMapping);
            if (values.size() < 2) return "0";
            double avg = values.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            double variance = values.stream().mapToDouble(v -> Math.pow(v - avg, 2)).sum() / (values.size() - 1);
            return String.valueOf(Math.sqrt(variance));
        });
        
        // STDEVP (总体标准差)
        result = processFunction(result, STDEVP.getName(), params -> {
            List<Double> values = parseValues(params, data, fieldMapping);
            if (values.isEmpty()) return "0";
            double avg = values.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            double variance = values.stream().mapToDouble(v -> Math.pow(v - avg, 2)).sum() / values.size();
            return String.valueOf(Math.sqrt(variance));
        });
        
        // VAR (方差)
        result = processFunction(result, VAR.getName(), params -> {
            List<Double> values = parseValues(params, data, fieldMapping);
            if (values.size() < 2) return "0";
            double avg = values.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            double variance = values.stream().mapToDouble(v -> Math.pow(v - avg, 2)).sum() / (values.size() - 1);
            return String.valueOf(variance);
        });
        
        // VARP (总体方差)
        result = processFunction(result, VARP.getName(), params -> {
            List<Double> values = parseValues(params, data, fieldMapping);
            if (values.isEmpty()) return "0";
            double avg = values.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            double variance = values.stream().mapToDouble(v -> Math.pow(v - avg, 2)).sum() / values.size();
            return String.valueOf(variance);
        });
        
        // MEDIAN (中位数)
        result = processFunction(result, MEDIAN.getName(), params -> {
            List<Double> values = parseValues(params, data, fieldMapping);
            if (values.isEmpty()) return "0";
            Collections.sort(values);
            int size = values.size();
            if (size % 2 == 0) {
                return String.valueOf((values.get(size / 2 - 1) + values.get(size / 2)) / 2);
            } else {
                return String.valueOf(values.get(size / 2));
            }
        });
        
        // MODE (众数)
        result = processFunction(result, MODE.getName(), params -> {
            List<Double> values = parseValues(params, data, fieldMapping);
            if (values.isEmpty()) return "0";
            Map<Double, Long> freq = new HashMap<>();
            values.forEach(v -> freq.put(v, freq.getOrDefault(v, 0L) + 1));
            return String.valueOf(freq.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(0.0));
        });
        
        // AVEDEV (平均绝对偏差)
        result = processFunction(result, AVEDEV.getName(), params -> {
            List<Double> values = parseValues(params, data, fieldMapping);
            if (values.isEmpty()) return "0";
            double avg = values.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            double sumDev = values.stream().mapToDouble(v -> Math.abs(v - avg)).sum();
            return String.valueOf(sumDev / values.size());
        });
        
        // AVERAGEIF - 条件平均值
        result = processFunction(result, FunctionName.AVERAGEIF.getName(), params -> {
            String[] parts = FormulaParamUtils.splitFunctionParams(params, 3);
            if (parts.length < 2) return "0";
            List<Double> values = parseValues(parts[0], data, fieldMapping);
            String condition = parts[1].trim();
            List<Double> filtered = new ArrayList<>();
            for (Double v : values) {
                if (evaluateCondition(v.toString() + condition)) {
                    filtered.add(v);
                }
            }
            if (filtered.isEmpty()) return "0";
            return String.valueOf(filtered.stream().mapToDouble(Double::doubleValue).average().orElse(0));
        });
        
        // AVERAGEIFS - 多条件平均值
        result = processFunction(result, FunctionName.AVERAGEIFS.getName(), params -> {
            // 简化实现
            return "0";
        });
        
        // COUNTBLANK - 统计空白单元格
        result = processFunction(result, FunctionName.COUNTBLANK.getName(), params -> {
            List<Double> values = parseValues(params, data, fieldMapping);
            long count = values.stream().filter(v -> v == 0 || Double.isNaN(v)).count();
            return String.valueOf(count);
        });
        
        // COUNTA - 统计非空单元格
        result = processFunction(result, FunctionName.COUNTA.getName(), params -> {
            List<Double> values = parseValues(params, data, fieldMapping);
            return String.valueOf(values.size());
        });
        
        // COUNTIFS - 多条件计数
        result = processFunction(result, FunctionName.COUNTIFS.getName(), params -> {
            // 简化实现
            return "0";
        });
        
        // SUMIFS - 多条件求和
        result = processFunction(result, FunctionName.SUMIFS.getName(), params -> {
            // 简化实现
            return "0";
        });
        
        // RANK - 排名
        result = processFunction(result, FunctionName.RANK.getName(), params -> {
            String[] parts = FormulaParamUtils.splitFunctionParams(params, 3);
            if (parts.length < 2) return "0";
            try {
                double number = Double.parseDouble(parts[0].trim());
                List<Double> values = parseValues(parts[1], data, fieldMapping);
                final int order; // 0=降序, 1=升序
                if (parts.length > 2) {
                    order = Integer.parseInt(parts[2].trim());
                } else {
                    order = 0;
                }
                final int finalOrder = order;
                values.sort((a, b) -> finalOrder == 0 ? Double.compare(b, a) : Double.compare(a, b));
                int rank = 1;
                for (Double v : values) {
                    if (v.equals(number)) {
                        return String.valueOf(rank);
                    }
                    rank++;
                }
                return "0";
            } catch (Exception e) {
                return "0";
            }
        });
        
        // PERCENTILE - 百分位数
        result = processFunction(result, FunctionName.PERCENTILE.getName(), params -> {
            String[] parts = FormulaParamUtils.splitFunctionParams(params, 2);
            if (parts.length < 2) return "0";
            try {
                List<Double> values = parseValues(parts[0], data, fieldMapping);
                double k = Double.parseDouble(parts[1].trim());
                if (k < 0 || k > 1) return "0";
                values.sort(Double::compareTo);
                double index = k * (values.size() - 1);
                int lower = (int) Math.floor(index);
                int upper = (int) Math.ceil(index);
                if (lower == upper) {
                    return String.valueOf(values.get(lower));
                }
                double weight = index - lower;
                return String.valueOf(values.get(lower) * (1 - weight) + values.get(upper) * weight);
            } catch (Exception e) {
                return "0";
            }
        });
        
        // QUARTILE - 四分位数
        result = processFunction(result, FunctionName.QUARTILE.getName(), params -> {
            String[] parts = FormulaParamUtils.splitFunctionParams(params, 2);
            if (parts.length < 2) return "0";
            try {
                List<Double> values = parseValues(parts[0], data, fieldMapping);
                int quart = Integer.parseInt(parts[1].trim());
                if (quart < 0 || quart > 4) return "0";
                double k = quart * 0.25;
                return processPercentile(values, k);
            } catch (Exception e) {
                return "0";
            }
        });
        
        return result;
    }
    
    /**
     * 计算百分位数
     */
    private static String processPercentile(List<Double> values, double k) {
        if (values.isEmpty()) return "0";
        values.sort(Double::compareTo);
        double index = k * (values.size() - 1);
        int lower = (int) Math.floor(index);
        int upper = (int) Math.ceil(index);
        if (lower == upper) {
            return String.valueOf(values.get(lower));
        }
        double weight = index - lower;
        return String.valueOf(values.get(lower) * (1 - weight) + values.get(upper) * weight);
    }
    
    /**
     * 处理数学函数
     */
    private static String processMathFunctions(String formula) {
        String result = formula;
        
        // PI - 圆周率
        result = processFunction(result, PI.getName(), params -> {
            return String.valueOf(Math.PI);
        });
        
        // ABS - 绝对值
        result = processFunction(result, ABS.getName(), params -> {
            try {
                double value = Double.parseDouble(params.trim());
                return String.valueOf(Math.abs(value));
            } catch (Exception e) {
                return params;
            }
        });
        
        // SQRT - 平方根
        result = processFunction(result, SQRT.getName(), params -> {
            try {
                double value = Double.parseDouble(params.trim());
                return String.valueOf(Math.sqrt(value));
            } catch (Exception e) {
                return params;
            }
        });
        
        // SQRTPI - 返回(数字*PI)的平方根
        result = processFunction(result, SQRTPI.getName(), params -> {
            try {
                double value = Double.parseDouble(params.trim());
                return String.valueOf(Math.sqrt(value * Math.PI));
            } catch (Exception e) {
                return params;
            }
        });
        
        // SUMSQ - 平方和
        result = processFunction(result, SUMSQ.getName(), params -> {
            String[] parts = FormulaParamUtils.splitFunctionParams(params, -1);
            double sum = 0;
            for (String part : parts) {
                try {
                    double value = Double.parseDouble(part.trim());
                    sum += value * value;
                } catch (Exception e) {
                    // 忽略无法解析的值
                }
            }
            return String.valueOf(sum);
        });
        
        // PRODUCT - 乘积
        result = processFunction(result, PRODUCT.getName(), params -> {
            String[] parts = FormulaParamUtils.splitFunctionParams(params, -1);
            double product = 1;
            for (String part : parts) {
                try {
                    double value = Double.parseDouble(part.trim());
                    product *= value;
                } catch (Exception e) {
                    // 忽略无法解析的值
                }
            }
            return String.valueOf(product);
        });
        
        // QUOTIENT - 商（整数除法）
        result = processFunction(result, QUOTIENT.getName(), params -> {
            String[] parts = FormulaParamUtils.splitFunctionParams(params, 2);
            if (parts.length < 2) return "0";
            try {
                double numerator = Double.parseDouble(parts[0].trim());
                double denominator = Double.parseDouble(parts[1].trim());
                if (denominator == 0) return "0";
                return String.valueOf((int)(numerator / denominator));
            } catch (Exception e) {
                return "0";
            }
        });
        
        // GCD - 最大公约数
        result = processFunction(result, GCD.getName(), params -> {
            String[] parts = FormulaParamUtils.splitFunctionParams(params, -1);
            if (parts.length == 0) return "0";
            try {
                long[] numbers = new long[parts.length];
                for (int i = 0; i < parts.length; i++) {
                    numbers[i] = (long) Double.parseDouble(parts[i].trim());
                }
                long gcd = numbers[0];
                for (int i = 1; i < numbers.length; i++) {
                    gcd = gcd(gcd, numbers[i]);
                }
                return String.valueOf(gcd);
            } catch (Exception e) {
                return "0";
            }
        });
        
        // LCM - 最小公倍数
        result = processFunction(result, LCM.getName(), params -> {
            String[] parts = FormulaParamUtils.splitFunctionParams(params, -1);
            if (parts.length == 0) return "0";
            try {
                long[] numbers = new long[parts.length];
                for (int i = 0; i < parts.length; i++) {
                    numbers[i] = (long) Double.parseDouble(parts[i].trim());
                }
                long lcm = numbers[0];
                for (int i = 1; i < numbers.length; i++) {
                    lcm = lcm(lcm, numbers[i]);
                }
                return String.valueOf(lcm);
            } catch (Exception e) {
                return "0";
            }
        });
        
        // FACT - 阶乘
        result = processFunction(result, FACT.getName(), params -> {
            try {
                int n = (int) Double.parseDouble(params.trim());
                if (n < 0) return "0";
                if (n > 170) return String.valueOf(Double.POSITIVE_INFINITY); // 避免溢出
                long fact = 1;
                for (int i = 2; i <= n; i++) {
                    fact *= i;
                }
                return String.valueOf(fact);
            } catch (Exception e) {
                return "0";
            }
        });
        
        // FACTDOUBLE - 双阶乘
        result = processFunction(result, FACTDOUBLE.getName(), params -> {
            try {
                int n = (int) Double.parseDouble(params.trim());
                if (n < 0) return "0";
                long fact = 1;
                for (int i = n; i > 0; i -= 2) {
                    fact *= i;
                }
                return String.valueOf(fact);
            } catch (Exception e) {
                return "0";
            }
        });
        
        // COMBIN - 组合数 C(n,k)
        result = processFunction(result, COMBIN.getName(), params -> {
            String[] parts = FormulaParamUtils.splitFunctionParams(params, 2);
            if (parts.length < 2) return "0";
            try {
                int n = (int) Double.parseDouble(parts[0].trim());
                int k = (int) Double.parseDouble(parts[1].trim());
                if (n < 0 || k < 0 || k > n) return "0";
                if (k > n - k) k = n - k; // 优化
                long combinResult = 1;
                for (int i = 0; i < k; i++) {
                    combinResult = combinResult * (n - i) / (i + 1);
                }
                return String.valueOf(combinResult);
            } catch (Exception e) {
                return "0";
            }
        });
        
        // PERMUT - 排列数 P(n,k)
        result = processFunction(result, PERMUT.getName(), params -> {
            String[] parts = FormulaParamUtils.splitFunctionParams(params, 2);
            if (parts.length < 2) return "0";
            try {
                int n = (int) Double.parseDouble(parts[0].trim());
                int k = (int) Double.parseDouble(parts[1].trim());
                if (n < 0 || k < 0 || k > n) return "0";
                long permutResult = 1;
                for (int i = 0; i < k; i++) {
                    permutResult *= (n - i);
                }
                return String.valueOf(permutResult);
            } catch (Exception e) {
                return "0";
            }
        });
        
        // DEGREES - 弧度转角度
        result = processFunction(result, DEGREES.getName(), params -> {
            try {
                double radians = Double.parseDouble(params.trim());
                return String.valueOf(Math.toDegrees(radians));
            } catch (Exception e) {
                return params;
            }
        });
        
        // RADIANS - 角度转弧度
        result = processFunction(result, RADIANS.getName(), params -> {
            try {
                double degrees = Double.parseDouble(params.trim());
                return String.valueOf(Math.toRadians(degrees));
            } catch (Exception e) {
                return params;
            }
        });
        
        // POWER
        result = processFunction(result, POWER.getName(), params -> {
            String[] parts = FormulaParamUtils.splitFunctionParams(params, 2);
            if (parts.length < 2) return parts[0];
            try {
                double base = Double.parseDouble(parts[0].trim());
                double exponent = Double.parseDouble(parts[1].trim());
                return String.valueOf(Math.pow(base, exponent));
            } catch (Exception e) {
                return parts[0];
            }
        });
        
        // LOG
        result = processFunction(result, LOG.getName(), params -> {
            try {
                double value = Double.parseDouble(params.trim());
                return String.valueOf(Math.log10(value));
            } catch (Exception e) {
                return params;
            }
        });
        
        // LN
        result = processFunction(result, LN.getName(), params -> {
            try {
                double value = Double.parseDouble(params.trim());
                return String.valueOf(Math.log(value));
            } catch (Exception e) {
                return params;
            }
        });
        
        // EXP
        result = processFunction(result, EXP.getName(), params -> {
            try {
                double value = Double.parseDouble(params.trim());
                return String.valueOf(Math.exp(value));
            } catch (Exception e) {
                return params;
            }
        });
        
        // CEILING
        result = processFunction(result, CEILING.getName(), params -> {
            try {
                double value = Double.parseDouble(params.trim());
                return String.valueOf(Math.ceil(value));
            } catch (Exception e) {
                return params;
            }
        });
        
        // CEILING.MATH - 向上取整到指定倍数
        result = processFunction(result, CEILING_MATH.getName(), params -> {
            String[] parts = FormulaParamUtils.splitFunctionParams(params, 3);
            try {
                double number = Double.parseDouble(parts[0].trim());
                double significance = parts.length > 1 ? Double.parseDouble(parts[1].trim()) : 1.0;
                int mode = parts.length > 2 ? Integer.parseInt(parts[2].trim()) : 0;
                if (significance == 0) return String.valueOf(number);
                double ceilingResult = Math.ceil(number / significance) * significance;
                return String.valueOf(ceilingResult);
            } catch (Exception e) {
                return parts[0];
            }
        });
        
        // FLOOR
        result = processFunction(result, FLOOR.getName(), params -> {
            try {
                double value = Double.parseDouble(params.trim());
                return String.valueOf(Math.floor(value));
            } catch (Exception e) {
                return params;
            }
        });
        
        // FLOOR.MATH - 向下取整到指定倍数
        result = processFunction(result, FLOOR_MATH.getName(), params -> {
            String[] parts = FormulaParamUtils.splitFunctionParams(params, 3);
            try {
                double number = Double.parseDouble(parts[0].trim());
                double significance = parts.length > 1 ? Double.parseDouble(parts[1].trim()) : 1.0;
                int mode = parts.length > 2 ? Integer.parseInt(parts[2].trim()) : 0;
                if (significance == 0) return String.valueOf(number);
                double floorResult = Math.floor(number / significance) * significance;
                return String.valueOf(floorResult);
            } catch (Exception e) {
                return parts[0];
            }
        });
        
        // MROUND - 四舍五入到指定倍数
        result = processFunction(result, MROUND.getName(), params -> {
            String[] parts = FormulaParamUtils.splitFunctionParams(params, 2);
            if (parts.length < 2) return parts[0];
            try {
                double number = Double.parseDouble(parts[0].trim());
                double multiple = Double.parseDouble(parts[1].trim());
                if (multiple == 0) return "0";
                double rounded = Math.round(number / multiple) * multiple;
                return String.valueOf(rounded);
            } catch (Exception e) {
                return parts[0];
            }
        });
        
        // MOD
        result = processFunction(result, MOD.getName(), params -> {
            String[] parts = FormulaParamUtils.splitFunctionParams(params, 2);
            if (parts.length < 2) return parts[0];
            try {
                double dividend = Double.parseDouble(parts[0].trim());
                double divisor = Double.parseDouble(parts[1].trim());
                return String.valueOf(dividend % divisor);
            } catch (Exception e) {
                return parts[0];
            }
        });
        
        // SIN
        result = processFunction(result, SIN.getName(), params -> {
            try {
                double value = Double.parseDouble(params.trim());
                return String.valueOf(Math.sin(value));
            } catch (Exception e) {
                return params;
            }
        });
        
        // COS
        result = processFunction(result, COS.getName(), params -> {
            try {
                double value = Double.parseDouble(params.trim());
                return String.valueOf(Math.cos(value));
            } catch (Exception e) {
                return params;
            }
        });
        
        // TAN
        result = processFunction(result, TAN.getName(), params -> {
            try {
                double value = Double.parseDouble(params.trim());
                return String.valueOf(Math.tan(value));
            } catch (Exception e) {
                return params;
            }
        });
        
        // ASIN
        result = processFunction(result, ASIN.getName(), params -> {
            try {
                double value = Double.parseDouble(params.trim());
                return String.valueOf(Math.asin(value));
            } catch (Exception e) {
                return params;
            }
        });
        
        // ACOS
        result = processFunction(result, ACOS.getName(), params -> {
            try {
                double value = Double.parseDouble(params.trim());
                return String.valueOf(Math.acos(value));
            } catch (Exception e) {
                return params;
            }
        });
        
        // ATAN
        result = processFunction(result, ATAN.getName(), params -> {
            try {
                double value = Double.parseDouble(params.trim());
                return String.valueOf(Math.atan(value));
            } catch (Exception e) {
                return params;
            }
        });
        
        // ATAN2 - 返回两个参数的反正切值
        result = processFunction(result, ATAN2.getName(), params -> {
            String[] parts = FormulaParamUtils.splitFunctionParams(params, 2);
            if (parts.length < 2) return "0";
            try {
                double y = Double.parseDouble(parts[0].trim());
                double x = Double.parseDouble(parts[1].trim());
                return String.valueOf(Math.atan2(y, x));
            } catch (Exception e) {
                return "0";
            }
        });
        
        // SINH - 双曲正弦
        result = processFunction(result, SINH.getName(), params -> {
            try {
                double value = Double.parseDouble(params.trim());
                return String.valueOf(Math.sinh(value));
            } catch (Exception e) {
                return params;
            }
        });
        
        // COSH - 双曲余弦
        result = processFunction(result, COSH.getName(), params -> {
            try {
                double value = Double.parseDouble(params.trim());
                return String.valueOf(Math.cosh(value));
            } catch (Exception e) {
                return params;
            }
        });
        
        // TANH - 双曲正切
        result = processFunction(result, TANH.getName(), params -> {
            try {
                double value = Double.parseDouble(params.trim());
                return String.valueOf(Math.tanh(value));
            } catch (Exception e) {
                return params;
            }
        });
        
        // ASINH - 反双曲正弦
        result = processFunction(result, ASINH.getName(), params -> {
            try {
                double value = Double.parseDouble(params.trim());
                return String.valueOf(Math.log(value + Math.sqrt(value * value + 1)));
            } catch (Exception e) {
                return params;
            }
        });
        
        // ACOSH - 反双曲余弦
        result = processFunction(result, ACOSH.getName(), params -> {
            try {
                double value = Double.parseDouble(params.trim());
                return String.valueOf(Math.log(value + Math.sqrt(value * value - 1)));
            } catch (Exception e) {
                return params;
            }
        });
        
        // ATANH - 反双曲正切
        result = processFunction(result, ATANH.getName(), params -> {
            try {
                double value = Double.parseDouble(params.trim());
                return String.valueOf(0.5 * Math.log((1 + value) / (1 - value)));
            } catch (Exception e) {
                return params;
            }
        });
        
        // INT - 向下取整到整数
        result = processFunction(result, INT.getName(), params -> {
            try {
                double value = Double.parseDouble(params.trim());
                return String.valueOf((int) Math.floor(value));
            } catch (Exception e) {
                return params;
            }
        });
        
        // TRUNC - 截断小数部分
        result = processFunction(result, TRUNC.getName(), params -> {
            String[] parts = FormulaParamUtils.splitFunctionParams(params, 2);
            try {
                double value = Double.parseDouble(parts[0].trim());
                int decimals = parts.length > 1 ? Integer.parseInt(parts[1].trim()) : 0;
                BigDecimal bd = BigDecimal.valueOf(value);
                bd = bd.setScale(decimals, RoundingMode.DOWN);
                return String.valueOf(bd.doubleValue());
            } catch (Exception e) {
                return parts[0];
            }
        });
        
        // SIGN - 返回数字的符号
        result = processFunction(result, SIGN.getName(), params -> {
            try {
                double value = Double.parseDouble(params.trim());
                if (value > 0) return "1";
                if (value < 0) return "-1";
                return "0";
            } catch (Exception e) {
                return params;
            }
        });
        
        // RAND - 返回0到1之间的随机数
        result = processFunction(result, RAND.getName(), params -> {
            return String.valueOf(Math.random());
        });
        
        // RANDBETWEEN - 返回指定范围内的随机整数
        result = processFunction(result, RANDBETWEEN.getName(), params -> {
            String[] parts = FormulaParamUtils.splitFunctionParams(params, 2);
            if (parts.length < 2) return "0";
            try {
                int bottom = Integer.parseInt(parts[0].trim());
                int top = Integer.parseInt(parts[1].trim());
                int randResult = bottom + (int)(Math.random() * (top - bottom + 1));
                return String.valueOf(randResult);
            } catch (Exception e) {
                return "0";
            }
        });
        
        // EVEN - 向上取整到最接近的偶数
        result = processFunction(result, EVEN.getName(), params -> {
            try {
                double value = Double.parseDouble(params.trim());
                int intValue = (int) Math.ceil(value);
                if (intValue % 2 == 0) {
                    return String.valueOf(intValue);
                } else {
                    return String.valueOf(intValue + 1);
                }
            } catch (Exception e) {
                return params;
            }
        });
        
        // ODD - 向上取整到最接近的奇数
        result = processFunction(result, ODD.getName(), params -> {
            try {
                double value = Double.parseDouble(params.trim());
                int intValue = (int) Math.ceil(value);
                if (intValue % 2 == 1) {
                    return String.valueOf(intValue);
                } else {
                    return String.valueOf(intValue + 1);
                }
            } catch (Exception e) {
                return params;
            }
        });
        
        // ISEVEN - 判断是否为偶数
        result = processFunction(result, ISEVEN.getName(), params -> {
            try {
                double value = Double.parseDouble(params.trim());
                int intValue = (int) value;
                return (intValue % 2 == 0) ? "1" : "0";
            } catch (Exception e) {
                return "0";
            }
        });
        
        // ISODD - 判断是否为奇数
        result = processFunction(result, ISODD.getName(), params -> {
            try {
                double value = Double.parseDouble(params.trim());
                int intValue = (int) value;
                return (intValue % 2 == 1) ? "1" : "0";
            } catch (Exception e) {
                return "0";
            }
        });
        
        // LARGE - 返回第K大的值
        // LARGE(array, k) - 参数格式：值1,值2,...,值n, k
        result = processFunction(result, LARGE.getName(), params -> {
            try {
                String[] allParts = splitFunctionParams(params, -1);
                if (allParts.length < 2) return "0";
                
                // 最后一个参数是k，前面的都是值
                int k = Integer.parseInt(allParts[allParts.length - 1].trim());
                List<Double> values = new ArrayList<>();
                
                for (int i = 0; i < allParts.length - 1; i++) {
                    try {
                        values.add(Double.parseDouble(allParts[i].trim()));
                    } catch (Exception e) {
                        // 忽略无法解析的值
                    }
                }
                
                if (values.isEmpty() || k < 1 || k > values.size()) return "0";
                Collections.sort(values, Collections.reverseOrder());
                return String.valueOf(values.get(k - 1));
            } catch (Exception e) {
                return "0";
            }
        });
        
        // SMALL - 返回第K小的值
        // SMALL(array, k) - 参数格式：值1,值2,...,值n, k
        result = processFunction(result, SMALL.getName(), params -> {
            try {
                String[] allParts = splitFunctionParams(params, -1);
                if (allParts.length < 2) return "0";
                
                // 最后一个参数是k，前面的都是值
                int k = Integer.parseInt(allParts[allParts.length - 1].trim());
                List<Double> values = new ArrayList<>();
                
                for (int i = 0; i < allParts.length - 1; i++) {
                    try {
                        values.add(Double.parseDouble(allParts[i].trim()));
                    } catch (Exception e) {
                        // 忽略无法解析的值
                    }
                }
                
                if (values.isEmpty() || k < 1 || k > values.size()) return "0";
                Collections.sort(values);
                return String.valueOf(values.get(k - 1));
            } catch (Exception e) {
                return "0";
            }
        });
        
        // GEOMEAN - 几何平均数
        result = processFunction(result, GEOMEAN.getName(), params -> {
            String[] parts = FormulaParamUtils.splitFunctionParams(params, -1);
            if (parts.length == 0) return "0";
            try {
                double product = 1.0;
                int count = 0;
                for (String part : parts) {
                    try {
                        double value = Double.parseDouble(part.trim());
                        if (value > 0) {
                            product *= value;
                            count++;
                        }
                    } catch (Exception e) {
                        // 忽略无法解析的值
                    }
                }
                if (count == 0) return "0";
                return String.valueOf(Math.pow(product, 1.0 / count));
            } catch (Exception e) {
                return "0";
            }
        });
        
        // HARMEAN - 调和平均数
        result = processFunction(result, HARMEAN.getName(), params -> {
            String[] parts = FormulaParamUtils.splitFunctionParams(params, -1);
            if (parts.length == 0) return "0";
            try {
                double sum = 0.0;
                int count = 0;
                for (String part : parts) {
                    try {
                        double value = Double.parseDouble(part.trim());
                        if (value > 0) {
                            sum += 1.0 / value;
                            count++;
                        }
                    } catch (Exception e) {
                        // 忽略无法解析的值
                    }
                }
                if (count == 0 || sum == 0) return "0";
                return String.valueOf(count / sum);
            } catch (Exception e) {
                return "0";
            }
        });
        
        // LOG10 - 以10为底的对数（与LOG相同，但更明确）
        result = processFunction(result, LOG10.getName(), params -> {
            try {
                double value = Double.parseDouble(params.trim());
                return String.valueOf(Math.log10(value));
            } catch (Exception e) {
                return params;
            }
        });
        
        // LOG2 - 以2为底的对数
        result = processFunction(result, LOG2.getName(), params -> {
            try {
                double value = Double.parseDouble(params.trim());
                return String.valueOf(Math.log(value) / Math.log(2));
            } catch (Exception e) {
                return params;
            }
        });
        
        // ACOT - 反余切
        result = processFunction(result, FunctionName.ACOT.getName(), params -> {
            try {
                double value = Double.parseDouble(params.trim());
                return String.valueOf(Math.PI / 2 - Math.atan(value));
            } catch (Exception e) {
                return params;
            }
        });
        
        // ACOTH - 反双曲余切
        result = processFunction(result, FunctionName.ACOTH.getName(), params -> {
            try {
                double value = Double.parseDouble(params.trim());
                if (Math.abs(value) <= 1) return "0";
                return String.valueOf(0.5 * Math.log((value + 1) / (value - 1)));
            } catch (Exception e) {
                return params;
            }
        });
        
        // BASE - 转换为指定进制
        result = processFunction(result, FunctionName.BASE.getName(), params -> {
            String[] parts = FormulaParamUtils.splitFunctionParams(params, 2);
            if (parts.length < 2) return "0";
            try {
                long number = Long.parseLong(parts[0].trim());
                int radix = Integer.parseInt(parts[1].trim());
                if (radix < 2 || radix > 36) return "0";
                return "\"" + Long.toString(number, radix).toUpperCase() + "\"";
            } catch (Exception e) {
                return "0";
            }
        });
        
        // ROMAN - 阿拉伯数字转罗马数字（简化实现）
        result = processFunction(result, FunctionName.ROMAN.getName(), params -> {
            try {
                int number = Integer.parseInt(params.trim());
                if (number < 1 || number > 3999) return "\"\"";
                return "\"" + FormulaStringUtils.toRoman(number) + "\"";
            } catch (Exception e) {
                return "\"\"";
            }
        });
        
        // ARABIC - 罗马数字转阿拉伯数字（简化实现）
        result = processFunction(result, FunctionName.ARABIC.getName(), params -> {
            String roman = params.trim();
            if (roman.startsWith("\"") && roman.endsWith("\"")) {
                roman = roman.substring(1, roman.length() - 1);
            }
            try {
                return String.valueOf(fromRoman(roman.toUpperCase()));
            } catch (Exception e) {
                return "0";
            }
        });
        
        // SEC - 正割
        result = processFunction(result, FunctionName.SEC.getName(), params -> {
            try {
                double value = Double.parseDouble(params.trim());
                return String.valueOf(1.0 / Math.cos(value));
            } catch (Exception e) {
                return params;
            }
        });
        
        // SECH - 双曲正割
        result = processFunction(result, FunctionName.SECH.getName(), params -> {
            try {
                double value = Double.parseDouble(params.trim());
                return String.valueOf(1.0 / Math.cosh(value));
            } catch (Exception e) {
                return params;
            }
        });
        
        // CSC - 余割
        result = processFunction(result, FunctionName.CSC.getName(), params -> {
            try {
                double value = Double.parseDouble(params.trim());
                return String.valueOf(1.0 / Math.sin(value));
            } catch (Exception e) {
                return params;
            }
        });
        
        // CSCH - 双曲余割
        result = processFunction(result, FunctionName.CSCH.getName(), params -> {
            try {
                double value = Double.parseDouble(params.trim());
                return String.valueOf(1.0 / Math.sinh(value));
            } catch (Exception e) {
                return params;
            }
        });
        
        // COT - 余切
        result = processFunction(result, FunctionName.COT.getName(), params -> {
            try {
                double value = Double.parseDouble(params.trim());
                return String.valueOf(1.0 / Math.tan(value));
            } catch (Exception e) {
                return params;
            }
        });
        
        // COTH - 双曲余切
        result = processFunction(result, FunctionName.COTH.getName(), params -> {
            try {
                double value = Double.parseDouble(params.trim());
                return String.valueOf(1.0 / Math.tanh(value));
            } catch (Exception e) {
                return params;
            }
        });
        
        return result;
    }
    
    
    /**
     * 处理文本函数
     */
    private static String processTextFunctions(String formula, JSONObject data, Map<String, String> fieldMapping) {
        String result = formula;
        
        // UPPER - 转换为大写
        result = processFunction(result, FunctionName.UPPER.getName(), params -> {
            String text = getStringValue(params, data, fieldMapping);
            return "\"" + text.toUpperCase() + "\"";
        });
        
        // LOWER - 转换为小写
        result = processFunction(result, FunctionName.LOWER.getName(), params -> {
            String text = getStringValue(params, data, fieldMapping);
            return "\"" + text.toLowerCase() + "\"";
        });
        
        // PROPER - 首字母大写
        result = processFunction(result, FunctionName.PROPER.getName(), params -> {
            String text = getStringValue(params, data, fieldMapping);
            StringBuilder sb = new StringBuilder();
            boolean capitalizeNext = true;
            for (char c : text.toCharArray()) {
                if (Character.isWhitespace(c)) {
                    sb.append(c);
                    capitalizeNext = true;
                } else if (capitalizeNext) {
                    sb.append(Character.toUpperCase(c));
                    capitalizeNext = false;
                } else {
                    sb.append(Character.toLowerCase(c));
                }
            }
            return "\"" + sb.toString() + "\"";
        });
        
        // LEN - 字符串长度
        result = processFunction(result, FunctionName.LEN.getName(), params -> {
            String text = getStringValue(params, data, fieldMapping);
            return String.valueOf(text.length());
        });
        
        // LEFT - 从左提取字符
        result = processFunction(result, FunctionName.LEFT.getName(), params -> {
            String[] parts = FormulaParamUtils.splitFunctionParams(params, 2);
            if (parts.length < 1) return "\"\"";
            String text = getStringValue(parts[0], data, fieldMapping);
            int num = 1;
            if (parts.length > 1) {
                try {
                    num = Integer.parseInt(parts[1].trim());
                } catch (Exception e) {
                    num = 1;
                }
            }
            if (num < 0) num = 0;
            if (num > text.length()) num = text.length();
            return "\"" + text.substring(0, num) + "\"";
        });
        
        // RIGHT - 从右提取字符
        result = processFunction(result, FunctionName.RIGHT.getName(), params -> {
            String[] parts = FormulaParamUtils.splitFunctionParams(params, 2);
            if (parts.length < 1) return "\"\"";
            String text = getStringValue(parts[0], data, fieldMapping);
            int num = 1;
            if (parts.length > 1) {
                try {
                    num = Integer.parseInt(parts[1].trim());
                } catch (Exception e) {
                    num = 1;
                }
            }
            if (num < 0) num = 0;
            if (num > text.length()) num = text.length();
            return "\"" + text.substring(text.length() - num) + "\"";
        });
        
        // MID - 从中间提取字符
        result = processFunction(result, FunctionName.MID.getName(), params -> {
            String[] parts = FormulaParamUtils.splitFunctionParams(params, 3);
            if (parts.length < 3) return "\"\"";
            String text = getStringValue(parts[0], data, fieldMapping);
            try {
                int start = Integer.parseInt(parts[1].trim()) - 1; // Excel从1开始
                int length = Integer.parseInt(parts[2].trim());
                if (start < 0) start = 0;
                if (start >= text.length()) return "\"\"";
                int end = Math.min(start + length, text.length());
                return "\"" + text.substring(start, end) + "\"";
            } catch (Exception e) {
                return "\"\"";
            }
        });
        
        // TRIM - 删除空格
        result = processFunction(result, FunctionName.TRIM.getName(), params -> {
            String text = getStringValue(params, data, fieldMapping);
            text = text.trim().replaceAll("\\s+", " ");
            return "\"" + text + "\"";
        });
        
        // FIND - 查找文本位置（区分大小写）
        result = processFunction(result, FunctionName.FIND.getName(), params -> {
            String[] parts = FormulaParamUtils.splitFunctionParams(params, 3);
            if (parts.length < 2) return "0";
            String findText = getStringValue(parts[0], data, fieldMapping);
            String withinText = getStringValue(parts[1], data, fieldMapping);
            int startNum = 1;
            if (parts.length > 2) {
                try {
                    startNum = Integer.parseInt(parts[2].trim());
                } catch (Exception e) {
                    startNum = 1;
                }
            }
            int pos = withinText.indexOf(findText, startNum - 1);
            return String.valueOf(pos == -1 ? 0 : pos + 1);
        });
        
        // SEARCH - 查找文本位置（不区分大小写）
        result = processFunction(result, FunctionName.SEARCH.getName(), params -> {
            String[] parts = FormulaParamUtils.splitFunctionParams(params, 3);
            if (parts.length < 2) return "0";
            String findText = getStringValue(parts[0], data, fieldMapping);
            String withinText = getStringValue(parts[1], data, fieldMapping);
            int startNum = 1;
            if (parts.length > 2) {
                try {
                    startNum = Integer.parseInt(parts[2].trim());
                } catch (Exception e) {
                    startNum = 1;
                }
            }
            int pos = withinText.toLowerCase().indexOf(findText.toLowerCase(), startNum - 1);
            return String.valueOf(pos == -1 ? 0 : pos + 1);
        });
        
        // REPLACE - 替换文本
        result = processFunction(result, FunctionName.REPLACE.getName(), params -> {
            String[] parts = FormulaParamUtils.splitFunctionParams(params, 4);
            if (parts.length < 4) return "\"\"";
            String oldText = getStringValue(parts[0], data, fieldMapping);
            try {
                int startNum = Integer.parseInt(parts[1].trim()) - 1;
                int numChars = Integer.parseInt(parts[2].trim());
                String newText = getStringValue(parts[3], data, fieldMapping);
                if (startNum < 0 || startNum >= oldText.length()) return "\"" + oldText + "\"";
                int endNum = Math.min(startNum + numChars, oldText.length());
                String resultStr = oldText.substring(0, startNum) + newText + oldText.substring(endNum);
                return "\"" + resultStr + "\"";
            } catch (Exception e) {
                return "\"" + oldText + "\"";
            }
        });
        
        // SUBSTITUTE - 替换指定文本
        result = processFunction(result, FunctionName.SUBSTITUTE.getName(), params -> {
            String[] parts = FormulaParamUtils.splitFunctionParams(params, 4);
            if (parts.length < 3) return "\"\"";
            String text = getStringValue(parts[0], data, fieldMapping);
            String oldText = getStringValue(parts[1], data, fieldMapping);
            String newText = getStringValue(parts[2], data, fieldMapping);
            int instanceNum = -1; // 替换所有
            if (parts.length > 3) {
                try {
                    instanceNum = Integer.parseInt(parts[3].trim());
                } catch (Exception e) {
                    instanceNum = -1;
                }
            }
            if (instanceNum == -1) {
                text = text.replace(oldText, newText);
            } else if (instanceNum > 0) {
                int count = 0;
                int index = 0;
                while ((index = text.indexOf(oldText, index)) != -1) {
                    count++;
                    if (count == instanceNum) {
                        text = text.substring(0, index) + newText + text.substring(index + oldText.length());
                        break;
                    }
                    index += oldText.length();
                }
            }
            return "\"" + text + "\"";
        });
        
        // REPT - 重复文本
        result = processFunction(result, FunctionName.REPT.getName(), params -> {
            String[] parts = FormulaParamUtils.splitFunctionParams(params, 2);
            if (parts.length < 2) return "\"\"";
            String text = getStringValue(parts[0], data, fieldMapping);
            try {
                int times = Integer.parseInt(parts[1].trim());
                if (times < 0) return "\"\"";
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < times; i++) {
                    sb.append(text);
                }
                return "\"" + sb.toString() + "\"";
            } catch (Exception e) {
                return "\"\"";
            }
        });
        
        // EXACT - 比较两个文本是否完全相同
        result = processFunction(result, FunctionName.EXACT.getName(), params -> {
            String[] parts = FormulaParamUtils.splitFunctionParams(params, 2);
            if (parts.length < 2) return "0";
            String text1 = getStringValue(parts[0], data, fieldMapping);
            String text2 = getStringValue(parts[1], data, fieldMapping);
            return text1.equals(text2) ? "1" : "0";
        });
        
        // VALUE - 将文本转换为数字
        result = processFunction(result, FunctionName.VALUE.getName(), params -> {
            String text = getStringValue(params, data, fieldMapping);
            try {
                return String.valueOf(Double.parseDouble(text));
            } catch (Exception e) {
                return "0";
            }
        });
        
        // CHAR - 返回字符代码对应的字符
        result = processFunction(result, FunctionName.CHAR.getName(), params -> {
            try {
                int code = Integer.parseInt(params.trim());
                if (code < 1 || code > 255) return "\"\"";
                return "\"" + (char) code + "\"";
            } catch (Exception e) {
                return "\"\"";
            }
        });
        
        // CODE - 返回字符的代码值
        result = processFunction(result, FunctionName.CODE.getName(), params -> {
            String text = getStringValue(params, data, fieldMapping);
            if (text.isEmpty()) return "0";
            return String.valueOf((int) text.charAt(0));
        });
        
        // TEXT - 将数值转换为文本格式（简化实现）
        result = processFunction(result, FunctionName.TEXT.getName(), params -> {
            String[] parts = FormulaParamUtils.splitFunctionParams(params, 2);
            if (parts.length < 1) return "\"\"";
            String value = parts[0].trim();
            String format = "0";
            if (parts.length > 1) {
                format = parts[1].trim();
                if (format.startsWith("\"") && format.endsWith("\"")) {
                    format = format.substring(1, format.length() - 1);
                }
            }
            try {
                double num = Double.parseDouble(value);
                int decimals = 0;
                if (format.contains(".")) {
                    String[] formatParts = format.split("\\.");
                    if (formatParts.length > 1) {
                        decimals = formatParts[1].replaceAll("[^0#]", "").length();
                    }
                }
                if (decimals == 0) {
                    return "\"" + String.format("%.0f", num) + "\"";
                } else {
                    return "\"" + String.format("%." + decimals + "f", num) + "\"";
                }
            } catch (Exception e) {
                return "\"" + value + "\"";
            }
        });
        
        // CONCAT - 连接文本（类似CONCATENATE，但更现代）
        result = processFunction(result, FunctionName.CONCAT.getName(), params -> {
            String[] parts = FormulaParamUtils.splitFunctionParams(params, -1);
            StringBuilder sb = new StringBuilder();
            for (String part : parts) {
                sb.append(getStringValue(part, data, fieldMapping));
            }
            return "\"" + sb.toString() + "\"";
        });
        
        // TEXTJOIN - 使用分隔符连接文本
        result = processFunction(result, FunctionName.TEXTJOIN.getName(), params -> {
            String[] parts = FormulaParamUtils.splitFunctionParams(params, -1);
            if (parts.length < 3) return "\"\"";
            String delimiter = getStringValue(parts[0], data, fieldMapping);
            boolean ignoreEmpty = false;
            try {
                ignoreEmpty = Integer.parseInt(parts[1].trim()) != 0;
            } catch (Exception e) {
                // 默认不忽略
            }
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (int i = 2; i < parts.length; i++) {
                String part = getStringValue(parts[i], data, fieldMapping);
                if (ignoreEmpty && part.isEmpty()) continue;
                if (!first) sb.append(delimiter);
                sb.append(part);
                first = false;
            }
            return "\"" + sb.toString() + "\"";
        });
        
        // CLEAN - 删除不可打印字符
        result = processFunction(result, FunctionName.CLEAN.getName(), params -> {
            String text = getStringValue(params, data, fieldMapping);
            text = text.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]", "");
            return "\"" + text + "\"";
        });
        
        // T - 返回文本值，非文本返回空字符串
        result = processFunction(result, FunctionName.T.getName(), params -> {
            String value = getStringValue(params, data, fieldMapping);
            try {
                Double.parseDouble(value);
                return "\"\""; // 是数字，返回空
            } catch (NumberFormatException e) {
                return "\"" + value + "\""; // 是文本，返回文本
            }
        });
        
        // FIXED - 将数字格式化为文本，使用固定小数位数
        result = processFunction(result, FunctionName.FIXED.getName(), params -> {
            String[] parts = FormulaParamUtils.splitFunctionParams(params, 3);
            if (parts.length < 1) return "\"\"";
            try {
                double number = Double.parseDouble(parts[0].trim());
                int decimals = 2;
                boolean noCommas = false;
                if (parts.length > 1) {
                    decimals = Integer.parseInt(parts[1].trim());
                }
                if (parts.length > 2) {
                    noCommas = Integer.parseInt(parts[2].trim()) != 0;
                }
                java.text.DecimalFormat df;
                StringBuilder pattern = new StringBuilder();
                for (int i = 0; i < Math.max(0, decimals); i++) {
                    pattern.append("0");
                }
                if (noCommas) {
                    df = new java.text.DecimalFormat("0." + pattern.toString());
                } else {
                    df = new java.text.DecimalFormat("#,##0." + pattern.toString());
                }
                return "\"" + df.format(number) + "\"";
            } catch (Exception e) {
                return "\"\"";
            }
        });
        
        // DOLLAR - 将数字格式化为货币格式
        result = processFunction(result, FunctionName.DOLLAR.getName(), params -> {
            String[] parts = FormulaParamUtils.splitFunctionParams(params, 2);
            if (parts.length < 1) return "\"\"";
            try {
                double number = Double.parseDouble(parts[0].trim());
                int decimals = 2;
                if (parts.length > 1) {
                    decimals = Integer.parseInt(parts[1].trim());
                }
                StringBuilder pattern = new StringBuilder();
                for (int i = 0; i < Math.max(0, decimals); i++) {
                    pattern.append("0");
                }
                java.text.DecimalFormat df = new java.text.DecimalFormat("$#,##0." + pattern.toString());
                return "\"" + df.format(number) + "\"";
            } catch (Exception e) {
                return "\"\"";
            }
        });
        
        // NUMBERVALUE - 将文本转换为数字
        result = processFunction(result, FunctionName.NUMBERVALUE.getName(), params -> {
            String[] parts = FormulaParamUtils.splitFunctionParams(params, 3);
            if (parts.length < 1) return "0";
            try {
                String text = getStringValue(parts[0], data, fieldMapping);
                String decimalSeparator = ".";
                String groupSeparator = ",";
                if (parts.length > 1) {
                    decimalSeparator = getStringValue(parts[1], data, fieldMapping);
                }
                if (parts.length > 2) {
                    groupSeparator = getStringValue(parts[2], data, fieldMapping);
                }
                text = text.replace(groupSeparator, "");
                text = text.replace(decimalSeparator, ".");
                return String.valueOf(Double.parseDouble(text));
            } catch (Exception e) {
                return "0";
            }
        });
        
        return result;
    }
    
    /**
     * 处理日期时间函数
     */
    private static String processDateTimeFunctions(String formula, JSONObject data, Map<String, String> fieldMapping) {
        String result = formula;
        
        // TODAY - 返回当前日期
        result = processFunction(result, FunctionName.TODAY.getName(), params -> {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            long days = (cal.getTimeInMillis() - new GregorianCalendar(1900, 0, 1).getTimeInMillis()) / (1000L * 60 * 60 * 24);
            return String.valueOf(days);
        });
        
        // NOW - 返回当前日期和时间
        result = processFunction(result, FunctionName.NOW.getName(), params -> {
            Calendar cal = Calendar.getInstance();
            long days = (cal.getTimeInMillis() - new GregorianCalendar(1900, 0, 1).getTimeInMillis()) / (1000L * 60 * 60 * 24);
            double time = (cal.get(Calendar.HOUR_OF_DAY) * 3600 + cal.get(Calendar.MINUTE) * 60 + cal.get(Calendar.SECOND)) / 86400.0;
            return String.valueOf(days + time);
        });
        
        // YEAR - 返回年份
        result = processFunction(result, FunctionName.YEAR.getName(), params -> {
            try {
                double serial = Double.parseDouble(params.trim());
                Calendar cal = FormulaDateUtils.dateFromSerial(serial);
                return String.valueOf(cal.get(Calendar.YEAR));
            } catch (Exception e) {
                return "0";
            }
        });
        
        // MONTH - 返回月份
        result = processFunction(result, FunctionName.MONTH.getName(), params -> {
            try {
                double serial = Double.parseDouble(params.trim());
                Calendar cal = FormulaDateUtils.dateFromSerial(serial);
                return String.valueOf(cal.get(Calendar.MONTH) + 1);
            } catch (Exception e) {
                return "0";
            }
        });
        
        // DAY - 返回日期
        result = processFunction(result, FunctionName.DAY.getName(), params -> {
            try {
                double serial = Double.parseDouble(params.trim());
                Calendar cal = FormulaDateUtils.dateFromSerial(serial);
                return String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
            } catch (Exception e) {
                return "0";
            }
        });
        
        // HOUR - 返回小时
        result = processFunction(result, FunctionName.HOUR.getName(), params -> {
            try {
                double serial = Double.parseDouble(params.trim());
                Calendar cal = dateTimeFromSerial(serial);
                return String.valueOf(cal.get(Calendar.HOUR_OF_DAY));
            } catch (Exception e) {
                return "0";
            }
        });
        
        // MINUTE - 返回分钟
        result = processFunction(result, FunctionName.MINUTE.getName(), params -> {
            try {
                double serial = Double.parseDouble(params.trim());
                Calendar cal = dateTimeFromSerial(serial);
                return String.valueOf(cal.get(Calendar.MINUTE));
            } catch (Exception e) {
                return "0";
            }
        });
        
        // SECOND - 返回秒数
        result = processFunction(result, FunctionName.SECOND.getName(), params -> {
            try {
                double serial = Double.parseDouble(params.trim());
                Calendar cal = dateTimeFromSerial(serial);
                return String.valueOf(cal.get(Calendar.SECOND));
            } catch (Exception e) {
                return "0";
            }
        });
        
        // WEEKDAY - 返回星期几
        result = processFunction(result, FunctionName.WEEKDAY.getName(), params -> {
            String[] parts = FormulaParamUtils.splitFunctionParams(params, 2);
            if (parts.length < 1) return "0";
            try {
                double serial = Double.parseDouble(parts[0].trim());
                Calendar cal = FormulaDateUtils.dateFromSerial(serial);
                int returnType = 1; // 默认：1=Sunday, 2=Monday, ..., 7=Saturday
                if (parts.length > 1) {
                    returnType = Integer.parseInt(parts[1].trim());
                }
                int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
                // Calendar: 1=Sunday, 2=Monday, ..., 7=Saturday
                if (returnType == 1) {
                    return String.valueOf(dayOfWeek);
                } else if (returnType == 2) {
                    // 1=Monday, 2=Tuesday, ..., 7=Sunday
                    return String.valueOf(dayOfWeek == 1 ? 7 : dayOfWeek - 1);
                } else if (returnType == 3) {
                    // 0=Monday, 1=Tuesday, ..., 6=Sunday
                    return String.valueOf(dayOfWeek == 1 ? 6 : dayOfWeek - 2);
                }
                return String.valueOf(dayOfWeek);
            } catch (Exception e) {
                return "0";
            }
        });
        
        // WEEKNUM - 返回周数
        result = processFunction(result, FunctionName.WEEKNUM.getName(), params -> {
            String[] parts = FormulaParamUtils.splitFunctionParams(params, 2);
            if (parts.length < 1) return "0";
            try {
                double serial = Double.parseDouble(parts[0].trim());
                Calendar cal = FormulaDateUtils.dateFromSerial(serial);
                int returnType = 1; // 默认：周从周日开始
                if (parts.length > 1) {
                    returnType = Integer.parseInt(parts[1].trim());
                }
                Calendar yearStart = (Calendar) cal.clone();
                yearStart.set(Calendar.MONTH, Calendar.JANUARY);
                yearStart.set(Calendar.DAY_OF_MONTH, 1);
                if (returnType == 1) {
                    yearStart.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                } else {
                    yearStart.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                }
                long diff = cal.getTimeInMillis() - yearStart.getTimeInMillis();
                int weekNum = (int) (diff / (7L * 24 * 60 * 60 * 1000)) + 1;
                return String.valueOf(weekNum);
            } catch (Exception e) {
                return "0";
            }
        });
        
        // DAYS - 返回两个日期之间的天数
        result = processFunction(result, FunctionName.DAYS.getName(), params -> {
            String[] parts = FormulaParamUtils.splitFunctionParams(params, 2);
            if (parts.length < 2) return "0";
            try {
                double serial1 = Double.parseDouble(parts[0].trim());
                double serial2 = Double.parseDouble(parts[1].trim());
                return String.valueOf((long)(serial2 - serial1));
            } catch (Exception e) {
                return "0";
            }
        });
        
        // WORKDAY - 返回工作日日期
        result = processFunction(result, FunctionName.WORKDAY.getName(), params -> {
            String[] parts = FormulaParamUtils.splitFunctionParams(params, 3);
            if (parts.length < 2) return "0";
            try {
                double startDate = Double.parseDouble(parts[0].trim());
                int days = Integer.parseInt(parts[1].trim());
                Calendar cal = dateFromSerial(startDate);
                // 简化实现：不考虑节假日
                int addedDays = 0;
                while (addedDays < Math.abs(days)) {
                    cal.add(Calendar.DAY_OF_MONTH, days > 0 ? 1 : -1);
                    int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
                    if (dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY) {
                        addedDays++;
                    }
                }
                long resultDays = (cal.getTimeInMillis() - new GregorianCalendar(1900, 0, 1).getTimeInMillis()) / (1000L * 60 * 60 * 24);
                return String.valueOf(resultDays);
            } catch (Exception e) {
                return "0";
            }
        });
        
        // DAYS360 - 按360天一年计算两个日期之间的天数
        result = processFunction(result, FunctionName.DAYS360.getName(), params -> {
            String[] parts = FormulaParamUtils.splitFunctionParams(params, 3);
            if (parts.length < 2) return "0";
            try {
                double startDate = Double.parseDouble(parts[0].trim());
                double endDate = Double.parseDouble(parts[1].trim());
                boolean method = false;
                if (parts.length > 2) {
                    method = Integer.parseInt(parts[2].trim()) != 0;
                }
                Calendar cal1 = FormulaDateUtils.dateFromSerial(startDate);
                Calendar cal2 = FormulaDateUtils.dateFromSerial(endDate);
                int year1 = cal1.get(Calendar.YEAR);
                int month1 = cal1.get(Calendar.MONTH) + 1;
                int day1 = cal1.get(Calendar.DAY_OF_MONTH);
                int year2 = cal2.get(Calendar.YEAR);
                int month2 = cal2.get(Calendar.MONTH) + 1;
                int day2 = cal2.get(Calendar.DAY_OF_MONTH);
                int days = (year2 - year1) * 360 + (month2 - month1) * 30 + (day2 - day1);
                return String.valueOf(days);
            } catch (Exception e) {
                return "0";
            }
        });
        
        // WORKDAY_INTL - 返回工作日（自定义周末）
        result = processFunction(result, FunctionName.WORKDAY_INTL.getName(), params -> {
            String[] parts = FormulaParamUtils.splitFunctionParams(params, 4);
            if (parts.length < 2) return "0";
            try {
                double startDate = Double.parseDouble(parts[0].trim());
                int days = Integer.parseInt(parts[1].trim());
                Calendar cal = FormulaDateUtils.dateFromSerial(startDate);
                return String.valueOf(FormulaDateUtils.dateToSerial(FormulaDateUtils.workday(cal, days)));
            } catch (Exception e) {
                return "0";
            }
        });
        
        // NETWORKDAYS_INTL - 返回工作日数（自定义周末）
        result = processFunction(result, FunctionName.NETWORKDAYS_INTL.getName(), params -> {
            String[] parts = FormulaParamUtils.splitFunctionParams(params, 4);
            if (parts.length < 2) return "0";
            try {
                double startDate = Double.parseDouble(parts[0].trim());
                double endDate = Double.parseDouble(parts[1].trim());
                Calendar cal1 = FormulaDateUtils.dateFromSerial(startDate);
                Calendar cal2 = FormulaDateUtils.dateFromSerial(endDate);
                return String.valueOf(FormulaDateUtils.networkDays(cal1, cal2));
            } catch (Exception e) {
                return "0";
            }
        });
        
        // DATEDIF - 计算两个日期之间的差值
        result = processFunction(result, FunctionName.DATEDIF.getName(), params -> {
            String[] parts = FormulaParamUtils.splitFunctionParams(params, 3);
            if (parts.length < 3) return "0";
            try {
                double startDate = Double.parseDouble(parts[0].trim());
                double endDate = Double.parseDouble(parts[1].trim());
                String unit = getStringValue(parts[2], data, fieldMapping).toUpperCase();
                Calendar cal1 = FormulaDateUtils.dateFromSerial(startDate);
                Calendar cal2 = FormulaDateUtils.dateFromSerial(endDate);
                if (cal2.before(cal1)) return "0";
                
                int year1 = cal1.get(Calendar.YEAR);
                int month1 = cal1.get(Calendar.MONTH);
                int day1 = cal1.get(Calendar.DAY_OF_MONTH);
                int year2 = cal2.get(Calendar.YEAR);
                int month2 = cal2.get(Calendar.MONTH);
                int day2 = cal2.get(Calendar.DAY_OF_MONTH);
                
                switch (unit) {
                    case "Y":
                        int years = year2 - year1;
                        if (month2 < month1 || (month2 == month1 && day2 < day1)) years--;
                        return String.valueOf(years);
                    case "M":
                        int months = (year2 - year1) * 12 + (month2 - month1);
                        if (day2 < day1) months--;
                        return String.valueOf(months);
                    case "D":
                        return String.valueOf((cal2.getTimeInMillis() - cal1.getTimeInMillis()) / (1000L * 60 * 60 * 24));
                    case "MD":
                        return String.valueOf(day2 - day1);
                    case "YM":
                        int monthsOnly = month2 - month1;
                        if (day2 < day1) monthsOnly--;
                        return String.valueOf(monthsOnly < 0 ? monthsOnly + 12 : monthsOnly);
                    case "YD":
                        Calendar temp = (Calendar) cal1.clone();
                        temp.set(Calendar.YEAR, year2);
                        long days = (cal2.getTimeInMillis() - temp.getTimeInMillis()) / (1000L * 60 * 60 * 24);
                        return String.valueOf(days);
                    default:
                        return "0";
                }
            } catch (Exception e) {
                return "0";
            }
        });
        
        return result;
    }
    
    /**
     * 处理信息函数
     */
    private static String processInformationFunctions(String formula, JSONObject data, Map<String, String> fieldMapping) {
        String result = formula;
        
        // ISBLANK - 检查是否为空
        result = processFunction(result, FunctionName.ISBLANK.getName(), params -> {
            String value = params.trim();
            return (value.isEmpty() || value.equals("0") || value.equals("\"\"")) ? "1" : "0";
        });
        
        // ISNUMBER - 检查是否为数字
        result = processFunction(result, FunctionName.ISNUMBER.getName(), params -> {
            String value = params.trim();
            try {
                Double.parseDouble(value);
                return "1";
            } catch (Exception e) {
                return "0";
            }
        });
        
        // ISTEXT - 检查是否为文本
        result = processFunction(result, FunctionName.ISTEXT.getName(), params -> {
            String value = params.trim();
            if (value.startsWith("\"") && value.endsWith("\"")) {
                return "1";
            }
            try {
                Double.parseDouble(value);
                return "0";
            } catch (Exception e) {
                return "1";
            }
        });
        
        // ISLOGICAL - 检查是否为逻辑值
        result = processFunction(result, FunctionName.ISLOGICAL.getName(), params -> {
            String value = params.trim();
            return (value.equals("0") || value.equals("1") || value.equalsIgnoreCase("TRUE") || value.equalsIgnoreCase("FALSE")) ? "1" : "0";
        });
        
        // ISERROR - 检查是否为错误
        result = processFunction(result, FunctionName.ISERROR.getName(), params -> {
            String value = params.trim();
            return (value.startsWith("#") || value.contains("ERROR")) ? "1" : "0";
        });
        
        // ISNA - 检查是否为 #N/A
        result = processFunction(result, FunctionName.ISNA.getName(), params -> {
            String value = params.trim();
            return (value.equals("#N/A") || value.equalsIgnoreCase("N/A")) ? "1" : "0";
        });
        
        // ISERR - 检查是否为错误（除#N/A）
        result = processFunction(result, FunctionName.ISERR.getName(), params -> {
            String value = params.trim();
            if (value.equals("#N/A") || value.equalsIgnoreCase("N/A")) return "0";
            return (value.startsWith("#") || value.contains("ERROR")) ? "1" : "0";
        });
        
        // TRUE - 返回 TRUE
        result = processFunction(result, FunctionName.TRUE.getName(), params -> {
            return "1";
        });
        
        // FALSE - 返回 FALSE
        result = processFunction(result, FunctionName.FALSE.getName(), params -> {
            return "0";
        });
        
        // TYPE - 返回值的类型
        result = processFunction(result, FunctionName.TYPE.getName(), params -> {
            String value = params.trim();
            if (value.startsWith("\"") && value.endsWith("\"")) {
                return "2"; // 文本
            }
            try {
                Double.parseDouble(value);
                return "1"; // 数字
            } catch (Exception e) {
                return "2"; // 文本
            }
        });
        
        // ISFORMULA - 检查是否为公式（简化实现）
        result = processFunction(result, FunctionName.ISFORMULA.getName(), params -> {
            return "0";
        });
        
        // ISREF - 检查是否为引用（简化实现）
        result = processFunction(result, FunctionName.ISREF.getName(), params -> {
            return "0";
        });
        
        // CELL - 返回单元格信息（简化实现）
        result = processFunction(result, FunctionName.CELL.getName(), params -> {
            String[] parts = FormulaParamUtils.splitFunctionParams(params, 2);
            if (parts.length < 1) return "\"\"";
            String infoType = getStringValue(parts[0], data, fieldMapping).toUpperCase();
            switch (infoType) {
                case "ADDRESS":
                    return "\"$A$1\"";
                case "ROW":
                    return "1";
                case "COL":
                case "COLUMN":
                    return "1";
                case "CONTENTS":
                    return parts.length > 1 ? "\"" + getStringValue(parts[1], data, fieldMapping) + "\"" : "\"\"";
                default:
                    return "\"\"";
            }
        });
        
        // INFO - 返回当前环境信息（简化实现）
        result = processFunction(result, FunctionName.INFO.getName(), params -> {
            String infoType = getStringValue(params, data, fieldMapping).toUpperCase();
            switch (infoType) {
                case "DIRECTORY":
                    return "\"" + System.getProperty("user.dir") + "\"";
                case "OSVERSION":
                    return "\"" + System.getProperty("os.version") + "\"";
                case "RELEASE":
                    return "\"" + System.getProperty("java.version") + "\"";
                case "SYSTEM":
                    return "\"pcdos\"";
                default:
                    return "\"\"";
            }
        });
        
        // ERROR_TYPE - 返回错误类型（简化实现）
        result = processFunction(result, FunctionName.ERROR_TYPE.getName(), params -> {
            String errorValue = getStringValue(params, data, fieldMapping);
            if (errorValue.contains("#NULL!")) return "1";
            if (errorValue.contains("#DIV/0!")) return "2";
            if (errorValue.contains("#VALUE!")) return "3";
            if (errorValue.contains("#REF!")) return "4";
            if (errorValue.contains("#NAME?")) return "5";
            if (errorValue.contains("#NUM!")) return "6";
            if (errorValue.contains("#N/A")) return "7";
            return "#N/A";
        });
        
        // NA - 返回#N/A错误
        result = processFunction(result, FunctionName.NA.getName(), params -> {
            return "#N/A";
        });
        
        // N - 将值转换为数字
        result = processFunction(result, FunctionName.N.getName(), params -> {
            String value = params.trim();
            try {
                if (value.startsWith("\"") && value.endsWith("\"")) {
                    return "0";
                }
                return String.valueOf(Double.parseDouble(value));
            } catch (NumberFormatException e) {
                try {
                    double dateValue = Double.parseDouble(value);
                    return String.valueOf(dateValue);
                } catch (Exception ex) {
                    return "0";
                }
            }
        });
        
        return result;
    }
    
    /**
     * 从序列号获取日期时间
     */
    private static Calendar dateTimeFromSerial(double serial) {
        Calendar base = FormulaDateUtils.dateFromSerial(serial);
        double time = serial - (long) serial;
        int seconds = (int) (time * 86400);
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;
        base.set(Calendar.HOUR_OF_DAY, hours);
        base.set(Calendar.MINUTE, minutes);
        base.set(Calendar.SECOND, secs);
        return base;
    }
    
    /**
     * 获取字符串值（处理字段引用和字面量）
     */
    private static String getStringValue(String param, JSONObject data, Map<String, String> fieldMapping) {
        String text = param.trim();
        // 先尝试从原始数据中获取值
        String enCode = getEnCode(text, fieldMapping);
        Object rawValue = data.get(enCode);
        
        if (rawValue != null) {
            return rawValue.toString();
        }
        
        // 移除引号（如果有）
        if (text.startsWith("\"") && text.endsWith("\"")) {
            return text.substring(1, text.length() - 1);
        }
        
        return text;
    }
    
    /**
     * 处理查找函数
     */
    private static String processLookupFunctions(String formula, JSONObject data, Map<String, String> fieldMapping) {
        String result = formula;
        
        // CHOOSE - 从值列表中选择
        result = processFunction(result, FunctionName.CHOOSE.getName(), params -> {
            String[] parts = FormulaParamUtils.splitFunctionParams(params, -1);
            if (parts.length < 2) return "0";
            try {
                int index = Integer.parseInt(parts[0].trim());
                if (index < 1 || index >= parts.length) return "0";
                return parts[index].trim();
            } catch (Exception e) {
                return "0";
            }
        });
        
        // ROW - 返回行号（简化实现，返回1）
        result = processFunction(result, FunctionName.ROW.getName(), params -> {
            return "1";
        });
        
        // COLUMN - 返回列号（简化实现，返回1）
        result = processFunction(result, FunctionName.COLUMN.getName(), params -> {
            return "1";
        });
        
        // ROWS - 返回行数（简化实现）
        result = processFunction(result, FunctionName.ROWS.getName(), params -> {
            String[] parts = FormulaParamUtils.splitFunctionParams(params, -1);
            return String.valueOf(parts.length);
        });
        
        // COLUMNS - 返回列数（简化实现）
        result = processFunction(result, FunctionName.COLUMNS.getName(), params -> {
            String[] parts = FormulaParamUtils.splitFunctionParams(params, -1);
            return String.valueOf(parts.length);
        });
        
        // VLOOKUP - 垂直查找（简化实现）
        result = processFunction(result, FunctionName.VLOOKUP.getName(), params -> {
            // 简化实现：VLOOKUP(lookup_value, table_array, col_index_num, [range_lookup])
            String[] parts = FormulaParamUtils.splitFunctionParams(params, 4);
            if (parts.length < 3) return "0";
            try {
                String lookupValue = parts[0].trim();
                // 简化：假设table_array是逗号分隔的值列表
                String[] tableArray = splitFunctionParams(parts[1], -1);
                int colIndex = Integer.parseInt(parts[2].trim());
                boolean rangeLookup = true;
                if (parts.length > 3) {
                    rangeLookup = Integer.parseInt(parts[3].trim()) != 0;
                }
                // 简化实现：只查找第一个匹配
                for (String row : tableArray) {
                    String[] rowValues = splitFunctionParams(row, -1);
                    if (rowValues.length > 0 && rowValues[0].trim().equals(lookupValue)) {
                        if (colIndex > 0 && colIndex <= rowValues.length) {
                            return rowValues[colIndex - 1].trim();
                        }
                    }
                }
                return "0";
            } catch (Exception e) {
                return "0";
            }
        });
        
        // HLOOKUP - 水平查找（简化实现）
        result = processFunction(result, FunctionName.HLOOKUP.getName(), params -> {
            // 简化实现：类似VLOOKUP
            return "0";
        });
        
        // INDEX - 返回指定位置的值（简化实现）
        result = processFunction(result, FunctionName.INDEX.getName(), params -> {
            String[] parts = FormulaParamUtils.splitFunctionParams(params, 3);
            if (parts.length < 2) return "0";
            try {
                String[] array = splitFunctionParams(parts[0], -1);
                int row = Integer.parseInt(parts[1].trim());
                int col = 1;
                if (parts.length > 2) {
                    col = Integer.parseInt(parts[2].trim());
                }
                // 简化实现：假设是一维数组
                int index = (row - 1);
                if (index >= 0 && index < array.length) {
                    return array[index].trim();
                }
                return "0";
            } catch (Exception e) {
                return "0";
            }
        });
        
        // MATCH - 返回匹配位置（简化实现）
        result = processFunction(result, FunctionName.MATCH.getName(), params -> {
            String[] parts = FormulaParamUtils.splitFunctionParams(params, 3);
            if (parts.length < 2) return "0";
            try {
                String lookupValue = parts[0].trim();
                String[] lookupArray = splitFunctionParams(parts[1], -1);
                int matchType = 1; // 1=小于等于, 0=精确匹配, -1=大于等于
                if (parts.length > 2) {
                    matchType = Integer.parseInt(parts[2].trim());
                }
                for (int i = 0; i < lookupArray.length; i++) {
                    if (matchType == 0 && lookupArray[i].trim().equals(lookupValue)) {
                        return String.valueOf(i + 1);
                    }
                }
                return "0";
            } catch (Exception e) {
                return "0";
            }
        });
        
        return result;
    }
    
    /**
     * 处理自定义函数
     */
    private static String processCustomFunctions(String formula, JSONObject data, Map<String, String> fieldMapping) {
        String result = formula;
        
        for (Map.Entry<String, CustomFunction> entry : customFunctions.entrySet()) {
            String funcName = entry.getKey();
            CustomFunction func = entry.getValue();
            
            result = processFunction(result, funcName, params -> {
                return func.execute(params, data, fieldMapping);
            });
        }
        
        return result;
    }
    
    /**
     * 注册自定义函数
     */
    public static void registerCustomFunction(String name, CustomFunction function) {
        customFunctions.put(name.toUpperCase(), function);
    }
    
    /**
     * 移除自定义函数
     */
    public static void unregisterCustomFunction(String name) {
        customFunctions.remove(name.toUpperCase());
    }
    
    /**
     * 通用函数处理
     */
    private static String processFunction(String formula, String funcName, java.util.function.Function<String, String> processor) {
        // 使用单词边界确保函数名完整匹配，避免部分匹配（如ISEVEN匹配到IS）
        Pattern pattern = Pattern.compile("(?<![a-zA-Z0-9_])" + Pattern.quote(funcName) + "\\s*\\(([^)]*)\\)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(formula);
        StringBuffer sb = new StringBuffer();
        
        while (matcher.find()) {
            try {
                String params = matcher.group(1);
                String result = processor.apply(params);
                matcher.appendReplacement(sb, result);
            } catch (Exception e) {
                logger.warn("函数 {} 计算失败: {}", funcName, e.getMessage());
                matcher.appendReplacement(sb, matcher.group(0));
            }
        }
        matcher.appendTail(sb);
        
        return sb.toString();
    }
    
    /**
     * 处理CONCATENATE函数（在replaceFields之前处理，以保留字符串值）
     * 支持嵌套处理
     */
    private static String processConcatenateFunction(String formula, JSONObject data, Map<String, String> fieldMapping) {
        String result = formula;
        boolean changed = true;
        int maxIterations = 50; // 防止无限循环
        int iterations = 0;
        
        // 循环处理，直到没有更多CONCATENATE函数需要处理
        while (changed && iterations < maxIterations) {
            changed = false;
            iterations++;
            String before = result;
            
            Pattern pattern = Pattern.compile("(?<![a-zA-Z0-9_])" + Pattern.quote(FunctionName.CONCATENATE.getName()) + "\\s*\\(([^)]*)\\)", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(result);
            StringBuffer sb = new StringBuffer();
            
            while (matcher.find()) {
                try {
                    String params = matcher.group(1);
                    String[] parts = FormulaParamUtils.splitFunctionParams(params, -1);
                    StringBuilder concatSb = new StringBuilder();
                    
                    for (String part : parts) {
                        part = part.trim();
                        String valueStr = null;
                        
                        // 先尝试从原始数据中获取值（保留字符串类型）
                        // 检查是否是字段名（通过fieldMapping查找）
                        String enCode = getEnCode(part, fieldMapping);
                        Object rawValue = data.get(enCode);
                        
                        if (rawValue != null) {
                            // 如果找到了原始值，直接使用（保留字符串类型）
                            if (rawValue instanceof String) {
                                valueStr = (String) rawValue;
                            } else if (rawValue instanceof Number) {
                                // 如果是整数，不显示小数点；如果是小数，显示小数
                                double num = ((Number) rawValue).doubleValue();
                                if (num == (long) num) {
                                    valueStr = String.valueOf((long) num);
                                } else {
                                    valueStr = String.valueOf(num);
                                }
                            } else {
                                valueStr = rawValue.toString();
                            }
                        } else {
                            // 如果没有找到，尝试作为字面量处理
                            // 移除引号（如果有）
                            if (part.startsWith("\"") && part.endsWith("\"")) {
                                valueStr = part.substring(1, part.length() - 1);
                            } else {
                                // 尝试作为数值解析，然后转换为字符串
                                try {
                                    double num = Double.parseDouble(part);
                                    if (num == (long) num) {
                                        valueStr = String.valueOf((long) num);
                                    } else {
                                        valueStr = String.valueOf(num);
                                    }
                                } catch (NumberFormatException e) {
                                    valueStr = part;
                                }
                            }
                        }
                        
                        concatSb.append(valueStr);
                    }
                    
                    // 返回拼接后的字符串（用引号包裹）
                    matcher.appendReplacement(sb, "\"" + concatSb.toString() + "\"");
                    changed = true;
                } catch (Exception e) {
                    logger.warn("CONCATENATE函数计算失败: {}", e.getMessage());
                    matcher.appendReplacement(sb, matcher.group(0));
                }
            }
            matcher.appendTail(sb);
            result = sb.toString();
        }
        
        return result;
    }
    
    /**
     * 处理IF函数（支持嵌套函数）
     */
    private static String processIfFunction(String formula, JSONObject data, Map<String, String> fieldMapping) {
        // 从最外层开始处理IF函数，避免嵌套问题
        Pattern pattern = Pattern.compile("IF\\s*\\(", Pattern.CASE_INSENSITIVE);
        String result = formula;
        boolean changed = true;
        int maxIterations = 100; // 防止无限循环
        int iterations = 0;
        
        while (changed && iterations < maxIterations) {
            changed = false;
            iterations++;
            Matcher matcher = pattern.matcher(result);
            StringBuffer sb = new StringBuffer();
            
            while (matcher.find()) {
                int pos = matcher.end(); // 跳过 IF函数名和左括号
                
                // 找到匹配的右括号，处理嵌套括号
                int depth = 1;
                int paramStart = pos;
                int comma1Pos = -1;
                int comma2Pos = -1;
                
                while (pos < result.length() && depth > 0) {
                    char c = result.charAt(pos);
                    if (c == '(') {
                        depth++;
                    } else if (c == ')') {
                        depth--;
                    } else if (c == ',' && depth == 1) {
                        if (comma1Pos == -1) {
                            comma1Pos = pos;
                        } else if (comma2Pos == -1) {
                            comma2Pos = pos;
                        }
                    }
                    pos++;
                }
                
                if (depth == 0 && comma1Pos != -1 && comma2Pos != -1) {
                    String condition = result.substring(paramStart, comma1Pos).trim();
                    String trueValue = result.substring(comma1Pos + 1, comma2Pos).trim();
                    String falseValue = result.substring(comma2Pos + 1, pos - 1).trim();
                    
                    // 先替换条件中的字段引用
                    condition = replaceFields(condition, data, fieldMapping);
                    
                    // 检查trueValue和falseValue是否是字符串字面量（用引号包裹）
                    // 如果是，不需要replaceFields，直接使用
                    boolean trueValueIsString = trueValue.trim().startsWith("\"") && trueValue.trim().endsWith("\"");
                    boolean falseValueIsString = falseValue.trim().startsWith("\"") && falseValue.trim().endsWith("\"");
                    
                    if (!trueValueIsString) {
                        trueValue = replaceFields(trueValue, data, fieldMapping);
                    }
                    if (!falseValueIsString) {
                        falseValue = replaceFields(falseValue, data, fieldMapping);
                    }
                    
                    // 计算条件
                    boolean conditionResult = FormulaParamUtils.evaluateCondition(condition);
                    String selectedValue = conditionResult ? trueValue : falseValue;
                    
                    // 替换IF函数调用
                    matcher.appendReplacement(sb, selectedValue);
                    changed = true;
                } else {
                    // 格式不正确，保留原样
                    matcher.appendReplacement(sb, matcher.group(0));
                }
            }
            matcher.appendTail(sb);
            result = sb.toString();
        }
        
        return result;
    }
    
    /**
     * 处理ROUND函数
     */
    private static String processRoundFunction(String formula) {
        Pattern pattern = Pattern.compile("ROUND\\s*\\(([^,]+),\\s*([^)]+)\\)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(formula);
        StringBuffer sb = new StringBuffer();
        
        while (matcher.find()) {
            try {
                String valueStr = matcher.group(1).trim();
                String decimalsStr = matcher.group(2).trim();
                
                double value = Double.parseDouble(valueStr);
                int decimals = Integer.parseInt(decimalsStr);
                
                BigDecimal bd = BigDecimal.valueOf(value);
                bd = bd.setScale(decimals, RoundingMode.HALF_UP);
                
                matcher.appendReplacement(sb, String.valueOf(bd.doubleValue()));
            } catch (Exception e) {
                logger.warn("ROUND函数计算失败: {}", e.getMessage());
                matcher.appendReplacement(sb, matcher.group(0));
            }
        }
        matcher.appendTail(sb);
        
        return sb.toString();
    }
    
    /**
     * 解析参数值列表（支持字段映射）
     */
    private static List<Double> parseValues(String params, JSONObject data, Map<String, String> fieldMapping) {
        List<Double> values = new ArrayList<>();
        String[] parts = splitFunctionParams(params, -1);
        
        for (String part : parts) {
            part = part.trim();
            
            // 先替换字段引用
            part = replaceFields(part, data, fieldMapping);
            
            try {
                // 尝试作为数值解析
                double value = Double.parseDouble(part);
                values.add(value);
            } catch (NumberFormatException e) {
                logger.warn("无法解析为数值: {}", part);
                values.add(0.0);
            }
        }
        
        return values;
    }
    
    /**
     * 分割函数参数（处理嵌套括号）
     */
    private static String[] splitFunctionParams(String params, int expectedCount) {
        List<String> result = new ArrayList<>();
        int depth = 0;
        StringBuilder current = new StringBuilder();
        
        for (char c : params.toCharArray()) {
            if (c == '(') {
                depth++;
                current.append(c);
            } else if (c == ')') {
                depth--;
                current.append(c);
            } else if (c == ',' && depth == 0) {
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
     * 计算条件表达式
     */
    private static boolean evaluateCondition(String condition) {
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
            } else {
                // 尝试作为布尔值或数值判断
                double value = Double.parseDouble(condition);
                return value != 0;
            }
        } catch (Exception e) {
            logger.warn("条件表达式计算失败: {}, error: {}", condition, e.getMessage());
            return false;
        }
    }
    
    /**
     * 检查表达式是否包含字符串拼接（有字符串字面量参与+运算）
     */
    private static boolean containsStringConcatenation(String expression) {
        // 检查是否包含用引号包裹的字符串字面量
        Pattern stringPattern = Pattern.compile("\"[^\"]*\"");
        return stringPattern.matcher(expression).find();
    }
    
    /**
     * 执行字符串拼接
     * 例如："abc" + 123 + "def" -> "abc123def"
     * 支持：CONCATENATE(字段1,字段2)+'==='+SUM(字段1,字段2)
     */
    private static String evaluateStringConcatenation(String expression) {
        try {
            logger.debug("开始字符串拼接，表达式: {}", expression);
            StringBuilder result = new StringBuilder();
            
            // 按+号分割，但要考虑字符串字面量中的+号
            List<String> parts = splitByPlusOperator(expression);
            logger.debug("分割后的部分: {}", parts);
            
            // 处理每个部分
            for (String part : parts) {
                part = part.trim();
                if (part.isEmpty()) {
                    continue;
                }
                
                if (part.startsWith("\"") && part.endsWith("\"")) {
                    // 字符串字面量，移除引号
                    result.append(part.substring(1, part.length() - 1));
                } else {
                    // 尝试作为数值解析
                    String cleaned = part.trim();
                    boolean parsed = false;
                    try {
                        double num = Double.parseDouble(cleaned);
                        // 如果是整数，不显示小数点
                        if (num == (long) num) {
                            result.append((long) num);
                        } else {
                            result.append(num);
                        }
                        parsed = true;
                    } catch (NumberFormatException e) {
                        // 不是纯数值，继续处理
                    }
                    
                    if (!parsed) {
                        // 无法解析为数值，尝试作为表达式计算
                        try {
                            Object calcResult = evaluateExpression(cleaned);
                            if (calcResult instanceof Number) {
                                double num = ((Number) calcResult).doubleValue();
                                if (num == (long) num) {
                                    result.append((long) num);
                                } else {
                                    result.append(num);
                                }
                            } else {
                                result.append(calcResult.toString());
                            }
                        } catch (Exception ex) {
                            // 计算失败，作为字符串添加（移除可能的引号）
                            String finalCleaned = cleaned;
                            if (finalCleaned.startsWith("\"") && finalCleaned.endsWith("\"")) {
                                finalCleaned = finalCleaned.substring(1, finalCleaned.length() - 1);
                            }
                            result.append(finalCleaned);
                        }
                    }
                }
            }
            
            return result.toString();
        } catch (Exception e) {
            logger.error("字符串拼接失败: expression={}, error={}", expression, e.getMessage());
            throw new FormulaException("字符串拼接失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 按+运算符分割表达式，但保留字符串字面量中的+号
     */
    private static List<String> splitByPlusOperator(String expression) {
        List<String> parts = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inString = false;
        boolean escaped = false;
        
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            
            if (escaped) {
                current.append(c);
                escaped = false;
                continue;
            }
            
            if (c == '\\') {
                escaped = true;
                current.append(c);
                continue;
            }
            
            if (c == '"') {
                inString = !inString;
                current.append(c);
                continue;
            }
            
            if (c == '+' && !inString) {
                // 在字符串外遇到+号，分割
                String part = current.toString().trim();
                if (part.length() > 0) {
                    parts.add(part);
                }
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        
        // 添加最后一部分
        String part = current.toString().trim();
        if (part.length() > 0) {
            parts.add(part);
        }
        
        logger.debug("splitByPlusOperator 输入: {}, 输出: {}", expression, parts);
        return parts;
    }
    
    /**
     * 使用exp4j计算表达式
     */
    private static Object evaluateExpression(String expression) {
        try {
            // 清理表达式，移除多余空格
            expression = expression.trim().replaceAll("\\s+", " ");
            
            // 使用exp4j计算
            Expression exp = new ExpressionBuilder(expression)
                    .build();
            
            double result = exp.evaluate();
            
            // 如果是整数，返回整数类型
            if (result == (long) result) {
                return (long) result;
            }
            
            return result;
        } catch (Exception e) {
            logger.error("表达式计算失败: expression={}, error={}", expression, e.getMessage());
            throw new FormulaException("表达式计算失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 转换为数值字符串
     */
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
            logger.warn("无法转换为数值: {}, 使用0替代", value);
            return "0";
        }
    }
    
    
    /**
     * 自定义函数接口
     */
    @FunctionalInterface
    public interface CustomFunction {
        String execute(String params, JSONObject data, Map<String, String> fieldMapping);
    }
}
