package com.formula.calculator.api;

/** 严格模式函数的只读元数据，可用于文档、补全与参数数量校验。 */
public final class FunctionDescriptor {
    private final String name;
    private final int minArguments;
    private final int maxArguments;
    private final String parameterTypes;
    private final String resultType;
    private final boolean lazy;

    /** maxArguments 为 Integer.MAX_VALUE 表示可变参数。 */
    public FunctionDescriptor(String name, int minArguments, int maxArguments, String parameterTypes,
                              String resultType, boolean lazy) {
        this.name = name;
        this.minArguments = minArguments;
        this.maxArguments = maxArguments;
        this.parameterTypes = parameterTypes;
        this.resultType = resultType;
        this.lazy = lazy;
    }

    public String getName() { return name; }
    public int getMinArguments() { return minArguments; }
    public int getMaxArguments() { return maxArguments; }
    public String getParameterTypes() { return parameterTypes; }
    public String getResultType() { return resultType; }
    public boolean isLazy() { return lazy; }
    public boolean isPure() { return true; }
}
