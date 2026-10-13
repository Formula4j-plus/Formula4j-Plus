package com.formula.calculator.functions.math;

import com.formula.calculator.registry.FunctionRegistry;
import com.formula.calculator.FunctionName;
import com.formula.calculator.utils.FormulaParamUtils;
import com.formula.calculator.utils.FormulaStringUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static com.formula.calculator.FunctionName.*;
import static com.formula.calculator.utils.FormulaMathUtils.*;
import static com.formula.calculator.utils.FormulaStringUtils.fromRoman;

/** 数学函数实现；沿用现有公式计算与容错规则，注册顺序与旧流程一致。 */
public final class MathFunctions {
    private MathFunctions() { }
    /** 将数学类别的实际实现注册到不可变分发表。 */
    public static FunctionRegistry createRegistry() {
        FunctionRegistry registry = new FunctionRegistry();

        // PI - 圆周率
        registry.register(PI.getName(), params -> {
            return String.valueOf(Math.PI);
        });

        // ABS - 绝对值
        registry.register(ABS.getName(), params -> {
            try {
                double value = Double.parseDouble(params.trim());
                return String.valueOf(Math.abs(value));
            } catch (Exception e) {
                return params;
            }
        });

        // SQRT - 平方根
        registry.register(SQRT.getName(), params -> {
            try {
                double value = Double.parseDouble(params.trim());
                return String.valueOf(Math.sqrt(value));
            } catch (Exception e) {
                return params;
            }
        });

        // SQRTPI - 返回(数字*PI)的平方根
        registry.register(SQRTPI.getName(), params -> {
            try {
                double value = Double.parseDouble(params.trim());
                return String.valueOf(Math.sqrt(value * Math.PI));
            } catch (Exception e) {
                return params;
            }
        });

        // SUMSQ - 平方和
        registry.register(SUMSQ.getName(), params -> {
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
        registry.register(PRODUCT.getName(), params -> {
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
        registry.register(QUOTIENT.getName(), params -> {
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
        registry.register(GCD.getName(), params -> {
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
        registry.register(LCM.getName(), params -> {
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
        registry.register(FACT.getName(), params -> {
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
        registry.register(FACTDOUBLE.getName(), params -> {
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
        registry.register(COMBIN.getName(), params -> {
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
        registry.register(PERMUT.getName(), params -> {
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
        registry.register(DEGREES.getName(), params -> {
            try {
                double radians = Double.parseDouble(params.trim());
                return String.valueOf(Math.toDegrees(radians));
            } catch (Exception e) {
                return params;
            }
        });

        // RADIANS - 角度转弧度
        registry.register(RADIANS.getName(), params -> {
            try {
                double degrees = Double.parseDouble(params.trim());
                return String.valueOf(Math.toRadians(degrees));
            } catch (Exception e) {
                return params;
            }
        });

        // POWER
        registry.register(POWER.getName(), params -> {
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
        registry.register(LOG.getName(), params -> {
            try {
                double value = Double.parseDouble(params.trim());
                return String.valueOf(Math.log10(value));
            } catch (Exception e) {
                return params;
            }
        });

        // LN
        registry.register(LN.getName(), params -> {
            try {
                double value = Double.parseDouble(params.trim());
                return String.valueOf(Math.log(value));
            } catch (Exception e) {
                return params;
            }
        });

        // EXP
        registry.register(EXP.getName(), params -> {
            try {
                double value = Double.parseDouble(params.trim());
                return String.valueOf(Math.exp(value));
            } catch (Exception e) {
                return params;
            }
        });

        // CEILING
        registry.register(CEILING.getName(), params -> {
            try {
                double value = Double.parseDouble(params.trim());
                return String.valueOf(Math.ceil(value));
            } catch (Exception e) {
                return params;
            }
        });

        // CEILING.MATH - 向上取整到指定倍数
        registry.register(CEILING_MATH.getName(), params -> {
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
        registry.register(FLOOR.getName(), params -> {
            try {
                double value = Double.parseDouble(params.trim());
                return String.valueOf(Math.floor(value));
            } catch (Exception e) {
                return params;
            }
        });

        // FLOOR.MATH - 向下取整到指定倍数
        registry.register(FLOOR_MATH.getName(), params -> {
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
        registry.register(MROUND.getName(), params -> {
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
        registry.register(MOD.getName(), params -> {
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
        registry.register(SIN.getName(), params -> {
            try {
                double value = Double.parseDouble(params.trim());
                return String.valueOf(Math.sin(value));
            } catch (Exception e) {
                return params;
            }
        });

        // COS
        registry.register(COS.getName(), params -> {
            try {
                double value = Double.parseDouble(params.trim());
                return String.valueOf(Math.cos(value));
            } catch (Exception e) {
                return params;
            }
        });

        // TAN
        registry.register(TAN.getName(), params -> {
            try {
                double value = Double.parseDouble(params.trim());
                return String.valueOf(Math.tan(value));
            } catch (Exception e) {
                return params;
            }
        });

        // ASIN
        registry.register(ASIN.getName(), params -> {
            try {
                double value = Double.parseDouble(params.trim());
                return String.valueOf(Math.asin(value));
            } catch (Exception e) {
                return params;
            }
        });

        // ACOS
        registry.register(ACOS.getName(), params -> {
            try {
                double value = Double.parseDouble(params.trim());
                return String.valueOf(Math.acos(value));
            } catch (Exception e) {
                return params;
            }
        });

        // ATAN
        registry.register(ATAN.getName(), params -> {
            try {
                double value = Double.parseDouble(params.trim());
                return String.valueOf(Math.atan(value));
            } catch (Exception e) {
                return params;
            }
        });

        // ATAN2 - 返回两个参数的反正切值
        registry.register(ATAN2.getName(), params -> {
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
        registry.register(SINH.getName(), params -> {
            try {
                double value = Double.parseDouble(params.trim());
                return String.valueOf(Math.sinh(value));
            } catch (Exception e) {
                return params;
            }
        });

        // COSH - 双曲余弦
        registry.register(COSH.getName(), params -> {
            try {
                double value = Double.parseDouble(params.trim());
                return String.valueOf(Math.cosh(value));
            } catch (Exception e) {
                return params;
            }
        });

        // TANH - 双曲正切
        registry.register(TANH.getName(), params -> {
            try {
                double value = Double.parseDouble(params.trim());
                return String.valueOf(Math.tanh(value));
            } catch (Exception e) {
                return params;
            }
        });

        // ASINH - 反双曲正弦
        registry.register(ASINH.getName(), params -> {
            try {
                double value = Double.parseDouble(params.trim());
                return String.valueOf(Math.log(value + Math.sqrt(value * value + 1)));
            } catch (Exception e) {
                return params;
            }
        });

        // ACOSH - 反双曲余弦
        registry.register(ACOSH.getName(), params -> {
            try {
                double value = Double.parseDouble(params.trim());
                return String.valueOf(Math.log(value + Math.sqrt(value * value - 1)));
            } catch (Exception e) {
                return params;
            }
        });

        // ATANH - 反双曲正切
        registry.register(ATANH.getName(), params -> {
            try {
                double value = Double.parseDouble(params.trim());
                return String.valueOf(0.5 * Math.log((1 + value) / (1 - value)));
            } catch (Exception e) {
                return params;
            }
        });

        // INT - 向下取整到整数
        registry.register(INT.getName(), params -> {
            try {
                double value = Double.parseDouble(params.trim());
                return String.valueOf((int) Math.floor(value));
            } catch (Exception e) {
                return params;
            }
        });

        // TRUNC - 截断小数部分
        registry.register(TRUNC.getName(), params -> {
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
        registry.register(SIGN.getName(), params -> {
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
        registry.register(RAND.getName(), params -> {
            return String.valueOf(Math.random());
        });

        // RANDBETWEEN - 返回指定范围内的随机整数
        registry.register(RANDBETWEEN.getName(), params -> {
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
        registry.register(EVEN.getName(), params -> {
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
        registry.register(ODD.getName(), params -> {
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
        registry.register(ISEVEN.getName(), params -> {
            try {
                double value = Double.parseDouble(params.trim());
                int intValue = (int) value;
                return (intValue % 2 == 0) ? "1" : "0";
            } catch (Exception e) {
                return "0";
            }
        });

        // ISODD - 判断是否为奇数
        registry.register(ISODD.getName(), params -> {
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
        registry.register(LARGE.getName(), params -> {
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
        registry.register(SMALL.getName(), params -> {
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
        registry.register(GEOMEAN.getName(), params -> {
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
        registry.register(HARMEAN.getName(), params -> {
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
        registry.register(LOG10.getName(), params -> {
            try {
                double value = Double.parseDouble(params.trim());
                return String.valueOf(Math.log10(value));
            } catch (Exception e) {
                return params;
            }
        });

        // LOG2 - 以2为底的对数
        registry.register(LOG2.getName(), params -> {
            try {
                double value = Double.parseDouble(params.trim());
                return String.valueOf(Math.log(value) / Math.log(2));
            } catch (Exception e) {
                return params;
            }
        });

        // ACOT - 反余切
        registry.register(FunctionName.ACOT.getName(), params -> {
            try {
                double value = Double.parseDouble(params.trim());
                return String.valueOf(Math.PI / 2 - Math.atan(value));
            } catch (Exception e) {
                return params;
            }
        });

        // ACOTH - 反双曲余切
        registry.register(FunctionName.ACOTH.getName(), params -> {
            try {
                double value = Double.parseDouble(params.trim());
                if (Math.abs(value) <= 1) return "0";
                return String.valueOf(0.5 * Math.log((value + 1) / (value - 1)));
            } catch (Exception e) {
                return params;
            }
        });

        // BASE - 转换为指定进制
        registry.register(FunctionName.BASE.getName(), params -> {
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
        registry.register(FunctionName.ROMAN.getName(), params -> {
            try {
                int number = Integer.parseInt(params.trim());
                if (number < 1 || number > 3999) return "\"\"";
                return "\"" + FormulaStringUtils.toRoman(number) + "\"";
            } catch (Exception e) {
                return "\"\"";
            }
        });

        // ARABIC - 罗马数字转阿拉伯数字（简化实现）
        registry.register(FunctionName.ARABIC.getName(), params -> {
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
        registry.register(FunctionName.SEC.getName(), params -> {
            try {
                double value = Double.parseDouble(params.trim());
                return String.valueOf(1.0 / Math.cos(value));
            } catch (Exception e) {
                return params;
            }
        });

        // SECH - 双曲正割
        registry.register(FunctionName.SECH.getName(), params -> {
            try {
                double value = Double.parseDouble(params.trim());
                return String.valueOf(1.0 / Math.cosh(value));
            } catch (Exception e) {
                return params;
            }
        });

        // CSC - 余割
        registry.register(FunctionName.CSC.getName(), params -> {
            try {
                double value = Double.parseDouble(params.trim());
                return String.valueOf(1.0 / Math.sin(value));
            } catch (Exception e) {
                return params;
            }
        });

        // CSCH - 双曲余割
        registry.register(FunctionName.CSCH.getName(), params -> {
            try {
                double value = Double.parseDouble(params.trim());
                return String.valueOf(1.0 / Math.sinh(value));
            } catch (Exception e) {
                return params;
            }
        });

        // COT - 余切
        registry.register(FunctionName.COT.getName(), params -> {
            try {
                double value = Double.parseDouble(params.trim());
                return String.valueOf(1.0 / Math.tan(value));
            } catch (Exception e) {
                return params;
            }
        });

        // COTH - 双曲余切
        registry.register(FunctionName.COTH.getName(), params -> {
            try {
                double value = Double.parseDouble(params.trim());
                return String.valueOf(1.0 / Math.tanh(value));
            } catch (Exception e) {
                return params;
            }
        });

        // CEILING.PRECISE - 精确向上取整
        registry.register(FunctionName.CEILING_PRECISE.getName(), params -> {
            String[] parts = FormulaParamUtils.splitFunctionParams(params, 2);
            try {
                double number = Double.parseDouble(parts[0].trim());
                double significance = parts.length > 1 ? Double.parseDouble(parts[1].trim()) : 1.0;
                if (significance == 0) return String.valueOf(number);
                double ceilingResult = Math.ceil(number / significance) * significance;
                return String.valueOf(ceilingResult);
            } catch (Exception e) {
                return parts[0];
            }
        });

        // FLOOR.PRECISE - 精确向下取整
        registry.register(FunctionName.FLOOR_PRECISE.getName(), params -> {
            String[] parts = FormulaParamUtils.splitFunctionParams(params, 2);
            try {
                double number = Double.parseDouble(parts[0].trim());
                double significance = parts.length > 1 ? Double.parseDouble(parts[1].trim()) : 1.0;
                if (significance == 0) return String.valueOf(number);
                double floorResult = Math.floor(number / significance) * significance;
                return String.valueOf(floorResult);
            } catch (Exception e) {
                return parts[0];
            }
        });

        // ISO.CEILING - ISO向上取整
        registry.register(FunctionName.ISO_CEILING.getName(), params -> {
            String[] parts = FormulaParamUtils.splitFunctionParams(params, 2);
            try {
                double number = Double.parseDouble(parts[0].trim());
                double significance = parts.length > 1 ? Double.parseDouble(parts[1].trim()) : 1.0;
                if (significance == 0) return String.valueOf(number);
                double ceilingResult = Math.ceil(number / significance) * significance;
                return String.valueOf(ceilingResult);
            } catch (Exception e) {
                return parts[0];
            }
        });

        // MUNIT - 返回单位矩阵（简化实现）
        registry.register(FunctionName.MUNIT.getName(), params -> {
            try {
                int dimension = Integer.parseInt(params.trim());
                if (dimension <= 0) return "0";
                // 简化实现：返回1（单位矩阵的对角线元素）
                return "1";
            } catch (Exception e) {
                return "0";
            }
        });

        // MULTINOMIAL - 多项系数
        registry.register(FunctionName.MULTINOMIAL.getName(), params -> {
            try {
                String[] parts = FormulaParamUtils.splitFunctionParams(params, -1);
                int sum = 0;
                double numerator = 1;
                for (String part : parts) {
                    int n = (int) Double.parseDouble(part.trim());
                    if (n < 0) return "0";
                    sum += n;
                    numerator *= factorial(n);
                }
                return String.valueOf(factorial(sum) / numerator);
            } catch (Exception e) {
                return "0";
            }
        });

        return registry.freeze();
    }

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
}
