package com.formula.calculator.support;

import com.formula.calculator.api.VerificationResult;

import java.math.BigDecimal;
import java.util.function.Supplier;

import static com.formula.calculator.api.VerificationResult.Status.*;

/** 新增的类型化校验策略；旧 verify 方法仍保留数值转换规则。 */
public final class ResultVerifier {
    private ResultVerifier() { }

    /** 数值允许数字字符串和绝对误差，文本精确比较，布尔值仅与布尔值比较。 */
    public static VerificationResult verify(Supplier<Object> calculation, Object frontend, double tolerance) {
        if (!Double.isFinite(tolerance) || tolerance < 0) {
            return new VerificationResult(INVALID_COMPARISON, null, frontend, "误差必须是有限的非负数");
        }
        final Object backend;
        try {
            backend = calculation.get();
        } catch (RuntimeException error) {
            return new VerificationResult(EVALUATION_ERROR, null, frontend, error.getMessage());
        }
        boolean match;
        if (backend == null || frontend == null) {
            match = backend == frontend;
        } else if (backend instanceof Number) {
            if (!(frontend instanceof Number) && !(frontend instanceof String)) {
                return new VerificationResult(INVALID_COMPARISON, backend, frontend, "数值结果需要可解析的数字");
            }
            try {
                // 十进制比较避免比较环节将大整数再次压缩到 double；不改变计算内核精度。
                BigDecimal left = new BigDecimal(backend.toString());
                BigDecimal right = new BigDecimal(frontend.toString().trim());
                match = left.subtract(right).abs().compareTo(BigDecimal.valueOf(tolerance)) <= 0;
            } catch (NumberFormatException error) {
                return new VerificationResult(INVALID_COMPARISON, backend, frontend, "不支持非法数字或非有限数值");
            }
        } else if (backend instanceof String || backend instanceof Boolean) {
            match = backend.equals(frontend);
        } else {
            return new VerificationResult(INVALID_COMPARISON, backend, frontend, "不支持的结果类型");
        }
        return new VerificationResult(match ? MATCH : MISMATCH, backend, frontend,
                match ? "结果一致" : "结果不一致");
    }
}
