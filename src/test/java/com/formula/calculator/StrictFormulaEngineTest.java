package com.formula.calculator;

import com.alibaba.fastjson.JSONObject;
import com.formula.calculator.api.FormulaContext;
import com.formula.calculator.api.FunctionDescriptor;
import com.formula.calculator.api.StrictFormulaException;
import com.formula.calculator.api.VerificationResult;
import com.formula.calculator.core.strict.CompiledFormula;
import com.formula.calculator.core.strict.StrictFormulaEngine;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.formula.calculator.api.StrictFormulaException.Code.*;
import static org.junit.Assert.*;

/** 严格入口的业务语义、边界、缓存和并发复用验证。 */
public class StrictFormulaEngineTest {
    private final StrictFormulaEngine engine = new StrictFormulaEngine();

    @Test
    public void evaluatesDecimalsAndOperatorPrecedence() {
        number("0.3", "0.1+0.2");
        number("9007199254740994", "9007199254740993+1");
        number("512", "2^3^2");
        number("-4", "-2^2");
        number("0.25", "2^-2");
        number("14", "2+3*4");
        number("-1.01", "ROUND(-1.005,2)");
        number("2", "MOD(-3,5)");
        number("-2", "MOD(3,-5)");
        number("2", "SQRT(4)");
        number("1", "SQRT(1e-1000)*1e500");
        number("4", "1e1 * .4");
        BigDecimal root = (BigDecimal) engine.execute("SQRT(2)", null);
        assertTrue(root.subtract(new BigDecimal("1.414213562373095048801688724209698")).abs()
                .compareTo(new BigDecimal("1e-33")) < 0);
        StrictFormulaEngine rounded = new StrictFormulaEngine(new MathContext(6, RoundingMode.HALF_UP), 0);
        assertEquals(new BigDecimal("0.333333"), rounded.execute("1/3", null));
    }

    @Test
    public void keepsStringsLiteralThroughNestedFunctions() {
        assertEquals("a,(b)$\\tail", engine.execute("CONCATENATE(LEFT(\"a,(b)\",5),\"$\\\\tail\")", null));
        assertEquals("${missing},SUM(1,2)", engine.execute("\"${missing},SUM(1,2)\"", null));
        assertEquals("a\"b", engine.execute("\"a\"\"b\"", null));
        assertEquals("a\nb", engine.execute("'a\\nb'", null));
        assertEquals("中 文", engine.execute("TRIM(\"  中   文  \")", null));
        assertEquals("bc", engine.execute("MID(\"abcd\",2,2)", null));
        assertEquals("aTRUE3", engine.execute("\"a\"&TRUE&3", null));
        number("15", "SUM(1,POWER(2,3),MAX(2,6))");
    }

    @Test
    public void shortCircuitsUnselectedBranchesAndBooleanOperators() {
        number("7", "IF(TRUE,7,1/0)");
        number("9", "IF(FALSE,${missing},9)");
        number("2", "IFERROR(1/0,2)");
        number("4", "IFERROR(${missing},4)");
        assertEquals(Boolean.FALSE, engine.execute("FALSE && ${missing}", null));
        assertEquals(Boolean.TRUE, engine.execute("TRUE || (1/0)", null));
        assertEquals(Boolean.FALSE, engine.execute("AND(FALSE,${missing})", null));
        assertEquals(Boolean.TRUE, engine.execute("OR(TRUE,${missing})", null));
        assertEquals(Boolean.TRUE, engine.execute("NOT(0) && (2<>3) && (3>=2)", null));
        assertEquals(Boolean.TRUE, engine.execute("1=1.0", null));
        assertEquals(Boolean.FALSE, engine.execute("1=\"1\"", null));
        assertEquals(Boolean.TRUE, engine.execute("NULL=NULL", null));
        error("IF(TRUE,1,UNKNOWN(2))", UNKNOWN_FUNCTION);
    }

    @Test
    public void mapsFieldsWithoutReplacingTextAndPreservesNativeTypes() {
        Map<String, Object> data = new HashMap<>();
        data.put("price", new BigDecimal("12.5"));
        data.put("label", "报价");
        data.put("empty", null);
        CompiledFormula compiled = engine.compile("IF(ISBLANK([empty]),CONCATENATE(${label},单价*2),\"\")");
        assertEquals("报价25", compiled.evaluate(data, Collections.singletonMap("单价", "price")));
        assertEquals(new java.util.LinkedHashSet<>(java.util.Arrays.asList("empty", "label", "单价")),
                compiled.getDependencies());
        JSONObject json = new JSONObject(data);
        assertEquals(new BigDecimal("25.0"), engine.execute(new FormulaContext("${单价}*2", json,
                Collections.singletonMap("单价", "price"))));
        data.put("price", "12.5");
        try {
            engine.execute("${price}*2", data);
            fail("数字字符串应要求显式 VALUE");
        } catch (StrictFormulaException failure) {
            assertEquals(TYPE_ERROR, failure.getCode());
        }
        assertEquals(new BigDecimal("25.0"), engine.execute("VALUE(${price})*2", data));
        assertEquals(Boolean.TRUE, engine.execute("ISTEXT(${price})", data));
        assertEquals(Boolean.TRUE, engine.execute("ISBLANK(${empty})", data));
    }

    @Test
    public void providesStableCodesAndOriginalSourceLocations() {
        StrictFormulaException failure = error("SUM(1,${missing})", MISSING_FIELD);
        assertEquals(6, failure.getStart());
        assertEquals(16, failure.getEnd());
        assertEquals("missing", failure.getSymbol());
        failure = error("1 / 0", DIVISION_BY_ZERO);
        assertEquals(2, failure.getStart());
        assertEquals(3, failure.getEnd());
        error("SUM()", INVALID_ARGUMENT_COUNT);
        error("IF(TRUE,1)", INVALID_ARGUMENT_COUNT);
        error("POWER(2,0.5)", NUMERIC_ERROR);
        error("1+NULL", TYPE_ERROR);
        error("TRUE+1", TYPE_ERROR);
        error("SQRT(-1)", NUMERIC_ERROR);
        error("VALUE(\"abc\")", NUMERIC_ERROR);
        error("ROUND(1,1.5)", NUMERIC_ERROR);
        for (String formula : new String[] {"", "SUM(1,)", "(1+2", "1 2", "\"open", "${}", "${open", "1e", "@"}) {
            try {
                engine.compile(formula);
                fail("错误公式应拒绝: " + formula);
            } catch (StrictFormulaException expected) {
                assertTrue(expected.getStart() >= 0);
                assertTrue(expected.getEnd() <= formula.length());
            }
        }
        Map<String, Object> data = Collections.singletonMap("x", Double.NaN);
        try {
            engine.execute("x", data);
            fail("非有限数值应拒绝");
        } catch (StrictFormulaException expected) {
            assertEquals(NUMERIC_ERROR, expected.getCode());
        }
    }

    @Test
    public void limitsResourceConsumptionAndDoesNotHideLimitErrors() {
        error("1e1001", LIMIT_EXCEEDED);
        error("POWER(2,1001)", NUMERIC_ERROR);
        String nested = "1";
        for (int i = 0; i < 140; i++) nested = "(" + nested + ")";
        error(nested, LIMIT_EXCEEDED);
        StringBuilder chain = new StringBuilder("1");
        for (int i = 0; i < 150; i++) chain.append("+1");
        error("IFERROR(" + chain + ",0)", LIMIT_EXCEEDED);
        StringBuilder oversized = new StringBuilder();
        for (int i = 0; i < 16385; i++) oversized.append(' ');
        error(oversized.toString(), LIMIT_EXCEEDED);
    }

    @Test
    public void reusesImmutableCompilationAcrossDifferentInputsAndThreads() throws Exception {
        CompiledFormula compiled = engine.compile("price*quantity");
        assertSame(compiled, engine.compile("price*quantity"));
        try {
            compiled.getDependencies().clear();
            fail("依赖列表应只读");
        } catch (UnsupportedOperationException expected) {
            // 依赖列表可共享，不接受调用方写入。
        }
        ExecutorService pool = Executors.newFixedThreadPool(4);
        try {
            List<Callable<Object>> tasks = new ArrayList<>();
            for (int i = 1; i <= 40; i++) {
                final int price = i;
                tasks.add(() -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("price", price);
                    data.put("quantity", 2);
                    return compiled.evaluate(data);
                });
            }
            List<Future<Object>> results = pool.invokeAll(tasks);
            for (int i = 0; i < results.size(); i++) assertEquals(BigDecimal.valueOf((i + 1) * 2), results.get(i).get());
        } finally {
            pool.shutdownNow();
        }
        engine.clearCache();
        assertEquals(0, engine.getCachedFormulaCount());
        assertNotSame(compiled, engine.compile("price*quantity"));
        StrictFormulaEngine small = new StrictFormulaEngine(MathContext.DECIMAL128, 2);
        CompiledFormula first = small.compile("1");
        small.compile("2");
        assertSame(first, small.compile("1"));
        small.compile("3");
        assertEquals(2, small.getCachedFormulaCount());
        assertNotSame(first, new StrictFormulaEngine(MathContext.DECIMAL128, 0).compile("1"));
    }

    @Test
    public void exposesFunctionMetadataAndDetailedVerification() {
        assertEquals(28, engine.getFunctions().size());
        for (FunctionDescriptor function : engine.getFunctions()) {
            assertTrue(function.isPure());
            assertTrue(function.getMaxArguments() >= function.getMinArguments());
            assertNotNull(function.getParameterTypes());
            assertNotNull(function.getResultType());
        }
        assertEquals(VerificationResult.Status.MATCH, engine.verifyDetailed("0.1+0.2", null, "0.3", 0).getStatus());
        assertEquals(VerificationResult.Status.EVALUATION_ERROR,
                engine.verifyDetailed("${missing}", null, 0, 0).getStatus());
        assertEquals(VerificationResult.Status.MISMATCH, engine.verifyDetailed("1", null, 2, 0).getStatus());
        assertEquals(VerificationResult.Status.INVALID_COMPARISON,
                engine.verifyDetailed("1", null, 1, Double.NaN).getStatus());
        // 兼容入口依旧返回 Long，缺失字段仍按旧规则处理。
        assertEquals(Long.valueOf(1), FormulaCalculator.calculate("${missing}+1", new JSONObject()));
    }

    private void number(String expected, String formula) {
        Object actual = engine.execute(formula, Collections.emptyMap());
        assertTrue(actual instanceof BigDecimal);
        assertEquals(formula, 0, new BigDecimal(expected).compareTo((BigDecimal) actual));
    }

    private StrictFormulaException error(String formula, StrictFormulaException.Code code) {
        try {
            engine.execute(formula, Collections.emptyMap());
            fail("应失败: " + formula);
            return null;
        } catch (StrictFormulaException failure) {
            assertEquals(formula, code, failure.getCode());
            return failure;
        }
    }
}
