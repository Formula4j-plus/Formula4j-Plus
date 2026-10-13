package com.formula.calculator.core.strict;

import java.math.MathContext;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/** 不可变已编译公式；可跨线程复用，各次执行使用自己的顶层数据快照。 */
public final class CompiledFormula {
    private final String formula;
    private final ExpressionParser.Node root;
    private final Set<String> dependencies;
    private final MathContext mathContext;

    CompiledFormula(String formula, ExpressionParser.Node root, Set<String> dependencies, MathContext mathContext) {
        this.formula = formula;
        this.root = root;
        this.dependencies = Collections.unmodifiableSet(new LinkedHashSet<>(dependencies));
        this.mathContext = mathContext;
    }

    /** 使用 Map 数据执行；返回 BigDecimal、String、Boolean 或 null。 */
    public Object evaluate(Map<String, ?> data) {
        return evaluate(data, Collections.<String, String>emptyMap());
    }

    /** 展示名称映射到数据键；映射和数据均只复制顶层。 */
    public Object evaluate(Map<String, ?> data, Map<String, String> fieldMapping) {
        return root.evaluate(new StrictValues(data, fieldMapping, mathContext));
    }

    public String getFormula() { return formula; }

    /** 所有分支中出现的字段名称；这是静态依赖，不等同于本次实际读取的字段。 */
    public Set<String> getDependencies() { return dependencies; }
}
