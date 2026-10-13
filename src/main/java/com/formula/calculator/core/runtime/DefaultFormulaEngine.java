package com.formula.calculator.core.runtime;

import com.formula.calculator.api.FormulaContext;
import com.formula.calculator.api.FormulaEngine;
import com.formula.calculator.core.script.ScriptFunctionSupport;
import com.formula.calculator.registry.CustomFunctionRegistry;

import java.util.Objects;

/** 默认引擎；每个实例拥有独立函数注册表与脚本状态。 */
public final class DefaultFormulaEngine implements FormulaEngine {
    private final CustomFunctionRegistry customFunctions = new CustomFunctionRegistry();
    private final ScriptFunctionSupport scripts = new ScriptFunctionSupport(customFunctions);

    public DefaultFormulaEngine() {
        customFunctions.register("TESTABC", (params, data, mapping) -> params);
    }

    /** 每次调用创建求值器并固定注册快照，内核不反向调用 facade。 */
    @Override
    public Object execute(FormulaContext context) {
        Objects.requireNonNull(context, "执行上下文不能为空");
        return new CompatibilityEvaluator(customFunctions.snapshot(), scripts)
                .calculate(context.getFormula(), context.getData(), context.getFieldMapping());
    }

    public CustomFunctionRegistry getCustomFunctions() { return customFunctions; }
    public ScriptFunctionSupport getScripts() { return scripts; }
}
