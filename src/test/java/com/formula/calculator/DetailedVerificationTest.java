package com.formula.calculator;

import com.alibaba.fastjson.JSONObject;
import com.formula.calculator.support.ResultVerifier;
import org.junit.Test;

import static com.formula.calculator.api.VerificationResult.Status.*;
import static org.junit.Assert.*;

/** 校验失败类型与结果类型的独立验收。 */
public class DetailedVerificationTest {
    @Test
    public void distinguishesMismatchAndEvaluationError() {
        JSONObject data = new JSONObject();
        assertEquals(MATCH, FormulaUtils.verifyDetailed("1+2", data, "3").getStatus());
        assertEquals(MISMATCH, FormulaUtils.verifyDetailed("1+2", data, 4).getStatus());
        assertEquals(EVALUATION_ERROR, FormulaUtils.verifyDetailed("UNKNOWN(1)", data, 0).getStatus());
        assertEquals(INVALID_COMPARISON, FormulaUtils.verifyDetailed("1", data, "invalid").getStatus());
    }

    @Test
    public void comparesStringsAndBooleansWithoutNumericCoercion() {
        assertTrue(FormulaUtils.verifyDetailed("\"hello\"", new JSONObject(), "hello").isMatch());
        assertFalse(FormulaUtils.verifyDetailed("\"001\"", new JSONObject(), 1).isMatch());
        assertTrue(ResultVerifier.verify(() -> true, true, 0).isMatch());
        assertFalse(ResultVerifier.verify(() -> true, 1, 0).isMatch());
    }

    @Test
    public void rejectsInvalidToleranceAndNonFiniteNumbers() {
        JSONObject data = new JSONObject();
        assertEquals(INVALID_COMPARISON, FormulaUtils.verifyDetailed("1", data, 1, -1).getStatus());
        assertEquals(INVALID_COMPARISON, FormulaUtils.verifyDetailed("1", data, 1, Double.NaN).getStatus());
        assertEquals(INVALID_COMPARISON, FormulaUtils.verifyDetailed("1", data, 1,
                Double.POSITIVE_INFINITY).getStatus());
        assertEquals(INVALID_COMPARISON, ResultVerifier.verify(() -> Double.NaN, 0, 0).getStatus());
        assertEquals(MISMATCH, ResultVerifier.verify(() -> 9007199254740992L, 9007199254740993L, 0).getStatus());
    }
}
