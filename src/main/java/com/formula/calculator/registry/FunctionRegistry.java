package com.formula.calculator.registry;

import com.formula.calculator.core.parser.FunctionCallProcessor;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/** 内置函数的实际处理器注册表；按既有顺序分发，不仅记录函数名称。 */
public final class FunctionRegistry {
    private final Map<String, Function<String, String>> functions = new LinkedHashMap<>();
    private boolean frozen;

    /** 仅允许在构造阶段注册函数。 */
    public void register(String name, Function<String, String> function) {
        if (frozen) throw new IllegalStateException("内置函数表已冻结");
        if (name == null || function == null) throw new IllegalArgumentException("函数名称和实现不能为空");
        String key = name.toUpperCase(Locale.ROOT);
        if (functions.containsKey(key)) throw new IllegalArgumentException("重复函数: " + name);
        functions.put(key, function);
    }

    /** 冻结后可供并发计算共享。 */
    public FunctionRegistry freeze() { frozen = true; return this; }

    public boolean contains(String name) {
        return name != null && functions.containsKey(name.toUpperCase(Locale.ROOT));
    }

    public Set<String> names() { return Collections.unmodifiableSet(functions.keySet()); }

    /** 按注册顺序执行本组处理器，兼容旧引擎的类别处理时序。 */
    public String process(String formula) {
        if (!frozen) throw new IllegalStateException("先冻结函数表再执行");
        String result = formula;
        for (Map.Entry<String, Function<String, String>> entry : functions.entrySet()) {
            result = FunctionCallProcessor.process(result, entry.getKey(), entry.getValue());
        }
        return result;
    }
}
