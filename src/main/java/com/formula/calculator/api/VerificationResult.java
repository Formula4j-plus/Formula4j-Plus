package com.formula.calculator.api;

/** 可诊断的结果校验报告；错误与不一致分别表达。 */
public final class VerificationResult {
    /** 校验状态。 */
    public enum Status { MATCH, MISMATCH, EVALUATION_ERROR, INVALID_COMPARISON }

    private final Status status;
    private final Object backendResult;
    private final Object frontendResult;
    private final String message;

    public VerificationResult(Status status, Object backendResult, Object frontendResult, String message) {
        this.status = status;
        this.backendResult = backendResult;
        this.frontendResult = frontendResult;
        this.message = message;
    }

    public Status getStatus() { return status; }
    public boolean isMatch() { return status == Status.MATCH; }
    public Object getBackendResult() { return backendResult; }
    public Object getFrontendResult() { return frontendResult; }
    public String getMessage() { return message; }
}
