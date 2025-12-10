package com.formula.calculator;

/**
 * 公式计算异常
 * 
 * @author Formula Calculator
 * @version 1.0.0
 */
public class FormulaException extends RuntimeException {
    
    public FormulaException(String message) {
        super(message);
    }
    
    public FormulaException(String message, Throwable cause) {
        super(message, cause);
    }
}

