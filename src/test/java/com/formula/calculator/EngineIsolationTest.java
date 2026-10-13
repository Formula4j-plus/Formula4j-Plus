package com.formula.calculator;

import com.alibaba.fastjson.JSONObject;
import com.formula.calculator.api.FormulaContext;
import com.formula.calculator.core.runtime.DefaultFormulaEngine;
import com.formula.calculator.functions.math.MathFunctions;
import com.formula.calculator.registry.FunctionRegistry;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/** 验证执行快照与实例隔离，不依赖入口之间自比较。 */
public class EngineIsolationTest {
    @Test
    public void isolatesRegistrationsAndCopiesContextInputs() {
        DefaultFormulaEngine first = new DefaultFormulaEngine();
        DefaultFormulaEngine second = new DefaultFormulaEngine();
        first.getCustomFunctions().register("LOCAL", (params, data, mapping) -> "9");
        assertEquals(9L, first.execute(FormulaContext.of("LOCAL()", new JSONObject())));
        assertFalse(second.getCustomFunctions().contains("LOCAL"));
        JSONObject data = new JSONObject();
        data.put("price", 5);
        FormulaContext context = new FormulaContext("单价 * 2", data, Collections.singletonMap("单价", "price"));
        data.put("price", 100);
        context.getData().put("price", 200);
        assertEquals(10L, first.execute(context));
    }

    @Test
    public void keepsRegistrySnapshotDuringConcurrentUpdate() throws Exception {
        DefaultFormulaEngine engine = new DefaultFormulaEngine();
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        engine.getCustomFunctions().register("WAITFORTEST", (params, data, mapping) -> {
            entered.countDown();
            try {
                if (!release.await(5, TimeUnit.SECONDS)) throw new IllegalStateException("测试等待超时");
            } catch (InterruptedException error) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(error);
            }
            return "1";
        });
        engine.getCustomFunctions().register("VALUEFORTEST", (params, data, mapping) -> "2");
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<Object> running = executor.submit(() -> engine.execute(
                    FormulaContext.of("WAITFORTEST()+VALUEFORTEST()", new JSONObject())));
            assertTrue(entered.await(5, TimeUnit.SECONDS));
            engine.getCustomFunctions().register("VALUEFORTEST", (params, data, mapping) -> "9");
            release.countDown();
            assertEquals(3L, running.get(5, TimeUnit.SECONDS));
            assertEquals(9L, engine.execute(FormulaContext.of("VALUEFORTEST()", new JSONObject())));
        } finally {
            release.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    public void dispatchesRealMathematicalImplementations() {
        FunctionRegistry registry = MathFunctions.createRegistry();
        assertTrue(registry.contains("sqrt"));
        assertEquals("4.0+8.0", registry.process("SQRT(16)+POWER(2,3)"));
        assertTrue(registry.names().size() > 50);
    }

    @Test
    public void preservesLiteralReplacementCharacters() {
        DefaultFormulaEngine engine = new DefaultFormulaEngine();
        engine.getCustomFunctions().register("MONEY", (params, data, mapping) -> "\"$5\\path\"");
        assertEquals("$5\\path", engine.execute(FormulaContext.of("MONEY()", new JSONObject())));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void registrySnapshotCannotBeChangedByCaller() {
        Map<String, FormulaCalculator.CustomFunction> snapshot = new DefaultFormulaEngine()
                .getCustomFunctions().snapshot();
        snapshot.clear();
    }
}
