package com.formula.calculator;

import com.alibaba.fastjson.JSONObject;
import com.formula.calculator.api.FormulaContext;
import com.formula.calculator.core.runtime.DefaultFormulaEngine;
import org.junit.Assume;
import org.junit.Test;

import javax.script.ScriptEngineManager;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/** 验证脚本库隔离、清理和共享引擎串行执行。 */
public class ScriptLifecycleTest {
    @Test
    public void isolatesLibrariesAndSerializesSharedScriptState() throws Exception {
        Assume.assumeTrue(new ScriptEngineManager().getEngineByName("nashorn") != null);
        Path script = Files.createTempFile("formula-library-", ".js");
        Files.write(script, "var counter = 0; function NEXTVALUE() { return ++counter; }"
                .getBytes(StandardCharsets.UTF_8));
        DefaultFormulaEngine first = new DefaultFormulaEngine();
        DefaultFormulaEngine second = new DefaultFormulaEngine();
        ExecutorService executor = Executors.newFixedThreadPool(4);
        try {
            first.getScripts().loadFormulaJsLibrary(script.toString());
            assertFalse(second.getScripts().isFormulaJsLibraryLoaded());
            assertEquals(script.toString(), first.getScripts().getLoadedFormulaJsPath());
            List<Future<Object>> results = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                results.add(executor.submit(() -> first.execute(
                        FormulaContext.of("NEXTVALUE()", new JSONObject()))));
            }
            long total = 0;
            for (Future<Object> result : results) total += ((Number) result.get(10, TimeUnit.SECONDS)).longValue();
            assertEquals(210, total);
            first.getScripts().clearFormulaJsLibrary();
            assertFalse(first.getScripts().isFormulaJsLibraryLoaded());
            assertNull(first.getScripts().getLoadedFormulaJsPath());
        } finally {
            executor.shutdownNow();
            first.getScripts().clearFormulaJsLibrary();
            Files.deleteIfExists(script);
        }
    }
}
