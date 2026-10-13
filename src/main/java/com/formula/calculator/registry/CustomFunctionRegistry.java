package com.formula.calculator.registry;

import com.formula.calculator.FormulaCalculator.CustomFunction;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/** 写时复制的自定义函数表；一次执行持有稳定快照，注册影响下一次执行。 */
public final class CustomFunctionRegistry {
    private volatile Map<String, CustomFunction> functions = Collections.emptyMap();

    /** 注册或覆盖函数，保留确定的注册顺序。 */
    public synchronized void register(String name, CustomFunction function) {
        String key = normalize(name);
        Objects.requireNonNull(function, "自定义函数不能为空");
        Map<String, CustomFunction> updated = new LinkedHashMap<>(functions);
        updated.put(key, function);
        functions = Collections.unmodifiableMap(updated);
    }

    /** 注销函数，不改变已经开始的计算。 */
    public synchronized void unregister(String name) {
        Map<String, CustomFunction> updated = new LinkedHashMap<>(functions);
        updated.remove(normalize(name));
        functions = Collections.unmodifiableMap(updated);
    }

    public boolean contains(String name) { return name != null && functions.containsKey(normalize(name)); }

    /** 返回不可修改的稳定快照。 */
    public Map<String, CustomFunction> snapshot() { return functions; }

    private static String normalize(String name) {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("函数名称不能为空");
        return name.toUpperCase(Locale.ROOT);
    }
}
