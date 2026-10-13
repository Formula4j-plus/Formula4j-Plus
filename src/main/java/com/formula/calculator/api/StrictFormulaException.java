package com.formula.calculator.api;

import com.formula.calculator.FormulaException;

/** 严格模式错误；位置为原公式中从零开始、左闭右开的 UTF-16 字符区间。 */
public final class StrictFormulaException extends FormulaException {
    /** 可由接入方稳定判断的错误类别。 */
    public enum Code {
        SYNTAX_ERROR, UNKNOWN_FUNCTION, INVALID_ARGUMENT_COUNT, MISSING_FIELD, TYPE_ERROR,
        DIVISION_BY_ZERO, NUMERIC_ERROR, LIMIT_EXCEEDED
    }

    private final Code code;
    private final int start;
    private final int end;
    private final String symbol;

    /** 保存错误类别、原公式位置和相关字段、函数或运算符。 */
    public StrictFormulaException(Code code, String message, int start, int end, String symbol) {
        super(message + " [" + start + ", " + end + ")");
        this.code = code;
        this.start = start;
        this.end = end;
        this.symbol = symbol;
    }

    public Code getCode() { return code; }
    public int getStart() { return start; }
    public int getEnd() { return end; }
    public String getSymbol() { return symbol; }
}
