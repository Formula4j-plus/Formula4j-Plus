package com.formula.calculator.api;

import com.alibaba.fastjson.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** 单次执行的输入；复制顶层数据与映射，调用期间请勿修改嵌套对象。 */
public final class FormulaContext {
    private final String formula;
    private final JSONObject data;
    private final Map<String, String> fieldMapping;

    /** 创建上下文，保留空数据与空公式的历史处理规则。 */
    public FormulaContext(String formula, JSONObject data, Map<String, String> fieldMapping) {
        this.formula = formula;
        this.data = data == null ? null : new JSONObject(new HashMap<>(data));
        this.fieldMapping = fieldMapping == null ? null
                : Collections.unmodifiableMap(new HashMap<>(fieldMapping));
    }

    /** 创建无展示字段映射的上下文。 */
    public static FormulaContext of(String formula, JSONObject data) {
        return new FormulaContext(formula, data, null);
    }

    public String getFormula() { return formula; }
    public JSONObject getData() { return data == null ? null : new JSONObject(new HashMap<>(data)); }
    public Map<String, String> getFieldMapping() { return fieldMapping; }
}
