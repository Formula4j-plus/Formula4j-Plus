package com.formula.calculator.api;

/** 单次公式计算入口。 */
public interface FormulaEngine {
    /** 按上下文执行公式，保持兼容入口的结果类型和异常语义。 */
    Object execute(FormulaContext context);
}
