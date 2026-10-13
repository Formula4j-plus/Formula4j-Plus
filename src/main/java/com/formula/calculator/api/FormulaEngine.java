package com.formula.calculator.api;

/** 单次公式计算入口。 */
public interface FormulaEngine {
    /** 按上下文执行公式；结果类型和错误策略由兼容或严格实现定义。 */
    Object execute(FormulaContext context);
}
