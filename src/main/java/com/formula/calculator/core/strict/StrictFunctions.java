package com.formula.calculator.core.strict;

import com.formula.calculator.api.FunctionDescriptor;
import com.formula.calculator.api.StrictFormulaException;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.formula.calculator.api.StrictFormulaException.Code.*;

/** 严格函数的小范围实现；参数保持原生类型，条件函数显式控制求值。 */
final class StrictFunctions {
    private static final Map<String, FunctionDescriptor> FUNCTIONS;
    private static final List<FunctionDescriptor> DESCRIPTORS;

    static {
        Map<String, FunctionDescriptor> functions = new LinkedHashMap<>();
        add(functions, "IF", 3, 3, "condition, any, any", "any", true);
        add(functions, "IFERROR", 2, 2, "any, any", "any", true);
        add(functions, "AND", 1, Integer.MAX_VALUE, "condition...", "boolean", true);
        add(functions, "OR", 1, Integer.MAX_VALUE, "condition...", "boolean", true);
        add(functions, "NOT", 1, 1, "condition", "boolean", false);
        for (String name : new String[] {"SUM", "AVERAGE", "MIN", "MAX"}) {
            add(functions, name, 1, Integer.MAX_VALUE, "number...", "number", false);
        }
        for (String name : new String[] {"ABS", "SQRT"}) add(functions, name, 1, 1, "number", "number", false);
        for (String name : new String[] {"ROUND", "ROUNDUP", "ROUNDDOWN"}) {
            add(functions, name, 2, 2, "number, integer[-1000,1000]", "number", false);
        }
        add(functions, "POWER", 2, 2, "number, integer[-1000,1000]", "number", false);
        add(functions, "MOD", 2, 2, "number, number", "number", false);
        add(functions, "CONCATENATE", 1, Integer.MAX_VALUE, "scalar...", "string", false);
        for (String name : new String[] {"UPPER", "LOWER", "TRIM"}) {
            add(functions, name, 1, 1, "string", "string", false);
        }
        add(functions, "LEN", 1, 1, "string", "number", false);
        for (String name : new String[] {"LEFT", "RIGHT"}) {
            add(functions, name, 1, 2, "string, optional integer[0,16384]", "string", false);
        }
        add(functions, "MID", 3, 3, "string, integer[1,16384], integer[0,16384]", "string", false);
        for (String name : new String[] {"ISNUMBER", "ISTEXT", "ISBLANK"}) {
            add(functions, name, 1, 1, "any", "boolean", false);
        }
        add(functions, "VALUE", 1, 1, "number or decimal string", "number", false);
        FUNCTIONS = Collections.unmodifiableMap(functions);
        DESCRIPTORS = Collections.unmodifiableList(new ArrayList<>(functions.values()));
    }

    private StrictFunctions() { }

    private static void add(Map<String, FunctionDescriptor> functions, String name, int minimum, int maximum,
                            String parameters, String result, boolean lazy) {
        functions.put(name, new FunctionDescriptor(name, minimum, maximum, parameters, result, lazy));
    }

    static FunctionDescriptor find(String name) { return FUNCTIONS.get(name); }
    static List<FunctionDescriptor> descriptors() { return DESCRIPTORS; }

    /** IF、IFERROR、AND、OR 在取参数值前决定是否执行对应节点。 */
    static Object evaluate(ExpressionParser.Node node, StrictValues values) {
        switch (node.symbol) {
            case "IF":
                boolean condition = values.bool(node.arguments.get(0).evaluate(values), node);
                return node.arguments.get(condition ? 1 : 2).evaluate(values);
            case "IFERROR":
                try {
                    return node.arguments.get(0).evaluate(values);
                } catch (StrictFormulaException failure) {
                    // 资源上限不能被公式中的容错函数绕过。
                    if (failure.getCode() == LIMIT_EXCEEDED) throw failure;
                    return node.arguments.get(1).evaluate(values);
                }
            case "AND": case "OR":
                boolean and = node.symbol.equals("AND");
                for (ExpressionParser.Node argument : node.arguments) {
                    boolean value = values.bool(argument.evaluate(values), node);
                    if (value != and) return value;
                }
                return and;
            default:
                List<Object> arguments = new ArrayList<>(node.arguments.size());
                for (ExpressionParser.Node argument : node.arguments) arguments.add(argument.evaluate(values));
                return eager(node, values, arguments);
        }
    }

    private static Object eager(ExpressionParser.Node node, StrictValues values, List<Object> args) {
        Object first = args.get(0);
        switch (node.symbol) {
            case "NOT": return !values.bool(first, node);
            case "ISNUMBER": return first instanceof BigDecimal;
            case "ISTEXT": return first instanceof String;
            case "ISBLANK": return first == null;
            case "VALUE":
                if (first instanceof BigDecimal) return first;
                String numericText = values.text(first, node).trim();
                if (numericText.length() > 1100) throw StrictValues.error(LIMIT_EXCEEDED, "数字文本过长", node);
                try {
                    return new BigDecimal(numericText);
                } catch (NumberFormatException failure) {
                    throw StrictValues.error(NUMERIC_ERROR, "VALUE 需要十进制数字文本", node);
                }
            case "SUM": case "AVERAGE": case "MIN": case "MAX":
                BigDecimal result = values.number(first, node);
                for (int i = 1; i < args.size(); i++) {
                    BigDecimal next = values.number(args.get(i), node);
                    if (node.symbol.equals("MIN")) result = result.min(next);
                    else if (node.symbol.equals("MAX")) result = result.max(next);
                    else result = result.add(next, values.mathContext);
                }
                return node.symbol.equals("AVERAGE")
                        ? result.divide(BigDecimal.valueOf(args.size()), values.mathContext) : result;
            case "ABS": return values.number(first, node).abs(values.mathContext);
            case "POWER": return power(values.number(first, node), values.number(args.get(1), node), node, values);
            case "MOD": return mod(values.number(first, node), values.number(args.get(1), node), node, values);
            case "SQRT": return sqrt(values.number(first, node), node, values);
            case "ROUND": case "ROUNDUP": case "ROUNDDOWN":
                int digits = values.integer(args.get(1), -1000, 1000, node);
                RoundingMode rounding = node.symbol.equals("ROUND") ? RoundingMode.HALF_UP
                        : node.symbol.equals("ROUNDUP") ? RoundingMode.UP : RoundingMode.DOWN;
                return values.number(first, node).setScale(digits, rounding);
            case "CONCATENATE":
                StringBuilder joined = new StringBuilder();
                for (Object value : args) {
                    joined.append(values.display(value, node));
                    if (joined.length() > 16384) throw StrictValues.error(LIMIT_EXCEEDED, "拼接文本过长", node);
                }
                return joined.toString();
            case "LEN": return BigDecimal.valueOf(values.text(first, node).length());
            case "UPPER": return values.text(first, node).toUpperCase(Locale.ROOT);
            case "LOWER": return values.text(first, node).toLowerCase(Locale.ROOT);
            case "TRIM": return values.text(first, node).replaceAll("^ +| +$", "").replaceAll(" +", " ");
            case "LEFT": case "RIGHT":
                String text = values.text(first, node);
                int count = args.size() == 1 ? 1 : values.integer(args.get(1), 0, 16384, node);
                count = Math.min(count, text.length());
                return node.symbol.equals("LEFT") ? text.substring(0, count) : text.substring(text.length() - count);
            case "MID":
                String source = values.text(first, node);
                int start = Math.min(values.integer(args.get(1), 1, 16384, node) - 1, source.length());
                int length = values.integer(args.get(2), 0, 16384, node);
                return source.substring(start, Math.min(start + length, source.length()));
            default: throw new IllegalStateException("函数实现缺失: " + node.symbol);
        }
    }

    /** 整数幂使用 BigDecimal；非整数幂不退回 double。 */
    static BigDecimal power(BigDecimal base, BigDecimal exponent, ExpressionParser.Node node, StrictValues values) {
        int power = values.integer(exponent, -1000, 1000, node);
        if (base.signum() == 0 && power < 0) throw StrictValues.error(DIVISION_BY_ZERO, "零不能取负数幂", node);
        return base.pow(power, values.mathContext);
    }

    /** MOD 的结果符号与除数一致，区别于 Java remainder。 */
    static BigDecimal mod(BigDecimal number, BigDecimal divisor, ExpressionParser.Node node, StrictValues values) {
        if (divisor.signum() == 0) throw StrictValues.error(DIVISION_BY_ZERO, "MOD 除数不能为零", node);
        BigDecimal remainder = number.remainder(divisor);
        if (remainder.signum() != 0 && remainder.signum() != divisor.signum()) remainder = remainder.add(divisor);
        return remainder.round(values.mathContext);
    }

    /** Java 8 的十进制平方根；以额外精度牛顿迭代，最后按配置舍入。 */
    private static BigDecimal sqrt(BigDecimal number, ExpressionParser.Node node, StrictValues values) {
        if (number.signum() < 0) throw StrictValues.error(NUMERIC_ERROR, "SQRT 参数不能为负数", node);
        if (number.signum() == 0) return BigDecimal.ZERO;
        MathContext working = new MathContext(values.mathContext.getPrecision() + 8, RoundingMode.HALF_EVEN);
        int magnitude = number.precision() - number.scale();
        BigDecimal estimate = BigDecimal.ONE.scaleByPowerOfTen(Math.floorDiv(magnitude, 2));
        BigDecimal two = BigDecimal.valueOf(2);
        for (int i = 0; i < working.getPrecision() * 2 + 16; i++) {
            BigDecimal next = estimate.add(number.divide(estimate, working)).divide(two, working);
            if (next.compareTo(estimate) == 0) return next.round(values.mathContext);
            estimate = next;
        }
        return estimate.round(values.mathContext);
    }
}
