package com.formula.calculator.benchmark;

import com.alibaba.fastjson.JSONObject;
import com.formula.calculator.api.FormulaContext;
import com.formula.calculator.core.runtime.DefaultFormulaEngine;
import com.formula.calculator.core.strict.CompiledFormula;
import com.formula.calculator.core.strict.StrictFormulaEngine;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/** 相同业务公式的公共入口基准；所有路径均先检查结果，数值类型按各自语义保留。 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
public class FormulaBenchmark {
    @Param({"arithmetic", "functions", "conditional"})
    public String scenario;

    private final Map<String, Object> data = new HashMap<>();
    private final DefaultFormulaEngine legacy = new DefaultFormulaEngine();
    private final StrictFormulaEngine strict = new StrictFormulaEngine();
    private final StrictFormulaEngine uncached = new StrictFormulaEngine(MathContext.DECIMAL128, 0);
    private FormulaContext context;
    private CompiledFormula compiled;
    private String expression;

    /** 每个线程拥有引擎和输入；先确认所有路径结果一致，再计时。 */
    @Setup(Level.Trial)
    public void setup() {
        data.put("price", new BigDecimal("100"));
        data.put("quantity", 5);
        BigDecimal expected;
        switch (scenario) {
            case "arithmetic":
                expression = "${price}*${quantity}";
                expected = new BigDecimal("500");
                break;
            case "functions":
                expression = "SUM(1,2,3)+ABS(-5)+SQRT(16)";
                expected = new BigDecimal("15");
                break;
            case "conditional":
                expression = "IF(${quantity}>0,${price}*${quantity},0)";
                expected = new BigDecimal("500");
                break;
            default: throw new IllegalArgumentException("未知基准场景");
        }
        context = FormulaContext.of(expression, new JSONObject(data));
        compiled = strict.compile(expression);
        for (Object value : new Object[] {legacy(), strictCached(), strictCompiled(), strictUncached()}) {
            if (!(value instanceof Number) || new BigDecimal(value.toString()).compareTo(expected) != 0) {
                throw new IllegalStateException("基准公式结果不一致: " + scenario + " = " + value);
            }
        }
    }

    /** 兼容公共 API，包含当前字段替换、函数扫描和上下文读取。 */
    @Benchmark
    public Object legacy() { return legacy.execute(context); }

    /** 严格公共 API 的稳定缓存命中场景。 */
    @Benchmark
    public Object strictCached() { return strict.execute(expression, data); }

    /** 调用方直接复用编译对象，仍包含执行数据快照。 */
    @Benchmark
    public Object strictCompiled() { return compiled.evaluate(data); }

    /** 禁用编译缓存，每次调用包含解析与求值；引擎本身已初始化。 */
    @Benchmark
    public Object strictUncached() { return uncached.execute(expression, data); }
}
