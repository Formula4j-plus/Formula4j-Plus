package com.formula.calculator.core.strict;

import com.formula.calculator.api.StrictFormulaException;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.formula.calculator.api.StrictFormulaException.Code.*;

/** 一次严格执行的数据快照、类型转换及资源上限。 */
final class StrictValues {
    final MathContext mathContext;
    private final Map<String, ?> data;
    private final Map<String, String> mapping;
    private int depth;

    StrictValues(Map<String, ?> data, Map<String, String> mapping, MathContext mathContext) {
        this.data = data == null ? Collections.emptyMap() : new HashMap<>(data);
        this.mapping = mapping == null ? Collections.emptyMap() : new HashMap<>(mapping);
        this.mathContext = mathContext;
    }

    void enter(ExpressionParser.Node node) {
        if (depth >= 128) {
            throw error(LIMIT_EXCEEDED, "求值深度超过 128", node);
        }
        depth++;
    }

    void leave() { depth--; }

    Object field(ExpressionParser.Node node) {
        String key = mapping.containsKey(node.symbol) ? mapping.get(node.symbol) : node.symbol;
        if (!data.containsKey(key)) throw error(MISSING_FIELD, "字段不存在: " + node.symbol, node);
        Object value = data.get(key);
        if (value instanceof Number) return number(value, node);
        if (value == null || value instanceof Boolean) return value;
        if (value instanceof String) return checked(value, node);
        throw error(TYPE_ERROR, "字段仅支持数字、文本、布尔值或 null", node);
    }

    BigDecimal number(Object value, ExpressionParser.Node node) {
        if (!(value instanceof Number)) throw error(TYPE_ERROR, "需要数字，文本不自动转成数字", node);
        try {
            String text = value.toString();
            if (text.length() > 1100) throw error(LIMIT_EXCEEDED, "数字长度超过上限", node);
            return (BigDecimal) checked(value instanceof BigDecimal ? value : new BigDecimal(text), node);
        } catch (NumberFormatException failure) {
            throw error(NUMERIC_ERROR, "非法数字或非有限数值", node);
        }
    }

    boolean bool(Object value, ExpressionParser.Node node) {
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof BigDecimal) return ((BigDecimal) value).signum() != 0;
        throw error(TYPE_ERROR, "条件需要布尔值或数字", node);
    }

    String text(Object value, ExpressionParser.Node node) {
        if (!(value instanceof String)) throw error(TYPE_ERROR, "需要文本", node);
        return (String) value;
    }

    String display(Object value, ExpressionParser.Node node) {
        if (value == null) return "";
        if (value instanceof BigDecimal) return ((BigDecimal) value).stripTrailingZeros().toPlainString();
        if (value instanceof Boolean) return (Boolean) value ? "TRUE" : "FALSE";
        return text(value, node);
    }

    int integer(Object value, int minimum, int maximum, ExpressionParser.Node node) {
        try {
            int result = number(value, node).intValueExact();
            if (result < minimum || result > maximum) throw new ArithmeticException();
            return result;
        } catch (ArithmeticException failure) {
            throw error(NUMERIC_ERROR, "整数参数必须在 " + minimum + " 至 " + maximum + " 之间", node);
        }
    }

    Object checked(Object value, ExpressionParser.Node node) {
        if (value instanceof BigDecimal) {
            BigDecimal number = (BigDecimal) value;
            if (number.precision() > 1100 || Math.abs((long) number.scale()) > 1000) {
                throw error(LIMIT_EXCEEDED, "数字精度或数量级超过上限", node);
            }
        } else if (value instanceof String && ((String) value).length() > 16384) {
            throw error(LIMIT_EXCEEDED, "文本长度超过 16384", node);
        }
        return value;
    }

    static StrictFormulaException error(StrictFormulaException.Code code, String message, ExpressionParser.Node node) {
        return new StrictFormulaException(code, message, node.start, node.end, node.symbol);
    }
}
