package com.formula.calculator.core.strict;

import com.formula.calculator.api.FormulaContext;
import com.formula.calculator.api.FormulaEngine;
import com.formula.calculator.api.FunctionDescriptor;
import com.formula.calculator.api.VerificationResult;
import com.formula.calculator.support.ResultVerifier;

import java.math.MathContext;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** 显式启用的严格引擎：类型化语法树、十进制运算、结构化错误和有界编译缓存。 */
public final class StrictFormulaEngine implements FormulaEngine {
    private final MathContext mathContext;
    private final int cacheCapacity;
    private final Map<String, CompiledFormula> cache = new LinkedHashMap<>(16, 0.75f, true);

    /** 默认 DECIMAL128（34 位有效数字、HALF_EVEN），缓存最多 256 个公式。 */
    public StrictFormulaEngine() {
        this(MathContext.DECIMAL128, 256);
    }

    /** 精度允许 1 至 1000 位，缓存容量允许 0 至 4096；零容量关闭缓存。 */
    public StrictFormulaEngine(MathContext mathContext, int cacheCapacity) {
        this.mathContext = Objects.requireNonNull(mathContext, "精度配置不能为空");
        if (mathContext.getPrecision() < 1 || mathContext.getPrecision() > 1000) {
            throw new IllegalArgumentException("精度必须在 1 至 1000 之间");
        }
        if (cacheCapacity < 0 || cacheCapacity > 4096) {
            throw new IllegalArgumentException("缓存容量必须在 0 至 4096 之间");
        }
        this.cacheCapacity = cacheCapacity;
    }

    /** 编译即检查全部分支的语法、函数名称及参数数量；不绑定数据和字段映射。 */
    public CompiledFormula compile(String formula) {
        Objects.requireNonNull(formula, "公式不能为空");
        synchronized (cache) {
            CompiledFormula existing = cache.get(formula);
            if (existing != null) return existing;
        }
        CompiledFormula compiled = new ExpressionParser(formula, mathContext).compile();
        if (cacheCapacity == 0) return compiled;
        synchronized (cache) {
            CompiledFormula existing = cache.get(formula);
            if (existing != null) return existing;
            cache.put(formula, compiled);
            if (cache.size() > cacheCapacity) cache.remove(cache.keySet().iterator().next());
            return compiled;
        }
    }

    /** 通过既有上下文进入严格语义；静态 FormulaCalculator 的兼容行为不受影响。 */
    @Override
    public Object execute(FormulaContext context) {
        Objects.requireNonNull(context, "执行上下文不能为空");
        return compile(context.getFormula()).evaluate(context.getData(), context.getFieldMapping());
    }

    /** 直接传入 Map，无需由调用方构造 JSONObject。 */
    public Object execute(String formula, Map<String, ?> data) {
        return compile(formula).evaluate(data);
    }

    /** 复用既有详细校验策略；结构化异常可由 compile/evaluate 入口直接获取。 */
    public VerificationResult verifyDetailed(String formula, Map<String, ?> data, Object frontend, double tolerance) {
        return ResultVerifier.verify(() -> execute(formula, data), frontend, tolerance);
    }

    /** 全部严格函数的只读元数据；不代表兼容引擎的完整函数列表。 */
    public List<FunctionDescriptor> getFunctions() { return StrictFunctions.descriptors(); }

    /** 清除已编译缓存，不影响调用方已持有的 CompiledFormula。 */
    public void clearCache() { synchronized (cache) { cache.clear(); } }

    public int getCachedFormulaCount() { synchronized (cache) { return cache.size(); } }
}
