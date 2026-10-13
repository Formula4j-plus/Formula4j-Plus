package com.formula.calculator;

import com.alibaba.fastjson.JSONObject;
import com.formula.calculator.api.FormulaContext;
import com.formula.calculator.core.runtime.DefaultFormulaEngine;
import com.formula.calculator.model.FormulaData;
import java.util.Map;

/** 公式计算门面；保留旧静态 API，内核使用单向委托。 */
public class FormulaCalculator {
    private static final DefaultFormulaEngine DEFAULT_ENGINE = new DefaultFormulaEngine();

    /** 计算公式，返回 Long、Double、String 或 null；错误以 FormulaException 抛出。 */
    public static Object calculate(FormulaData formulaData, JSONObject data) throws FormulaException {
        if (formulaData == null || formulaData.getTxt() == null || formulaData.getTxt().trim().isEmpty()) {
            return null;
        }
        return calculate(formulaData.getTxt(), data, formulaData.buildFieldMapping());
    }

    /** 计算公式，返回 Long、Double、String 或 null；错误以 FormulaException 抛出。 */
    public static Object calculate(String formula, JSONObject data) throws FormulaException {
        return calculate(formula, data, null);
    }

    /** 计算公式，返回 Long、Double、String 或 null；错误以 FormulaException 抛出。 */
    public static Object calculate(String formula, JSONObject data, Map<String, String> fieldMapping) throws FormulaException {
        return DEFAULT_ENGINE.execute(new FormulaContext(formula, data, fieldMapping));
    }

    /** 注册默认引擎的自定义函数；更新对下一次计算生效。 */
    public static void registerCustomFunction(String name, CustomFunction function) {
        DEFAULT_ENGINE.getCustomFunctions().register(name, function);
    }

    /** 注册默认引擎的自定义函数；更新对下一次计算生效。 */
    public static void registerCustomFunction(String name, String type, String script) {
        DEFAULT_ENGINE.getScripts().registerCustomFunction(name, type, script);
    }

    /** 注册可信脚本文件中的函数，缓存由默认引擎管理。 */
    public static void registerCustomFunctionFromFile(String name, String formulaJsPath, String functionName) {
        DEFAULT_ENGINE.getScripts().registerCustomFunctionFromFile(name, formulaJsPath, functionName);
    }

    /** 加载可信 Formula.js 库；需要运行时提供 JavaScript 引擎。 */
    public static void loadFormulaJsLibrary(String formulaJsPath) {
        DEFAULT_ENGINE.getScripts().loadFormulaJsLibrary(formulaJsPath);
    }

    /** 清除默认引擎的全局脚本库，不中断已开始的调用。 */
    public static void clearFormulaJsLibrary() {
        DEFAULT_ENGINE.getScripts().clearFormulaJsLibrary();
    }

    /** 返回默认引擎当前加载的库路径，无库时返回 null。 */
    public static String getLoadedFormulaJsPath() {
        return DEFAULT_ENGINE.getScripts().getLoadedFormulaJsPath();
    }

    /** 判断默认引擎是否已加载全局脚本库。 */
    public static boolean isFormulaJsLibraryLoaded() {
        return DEFAULT_ENGINE.getScripts().isFormulaJsLibraryLoaded();
    }

    /** 清理默认引擎缓存，后续文件调用将重新加载。 */
    public static void clearFormulaJsCache() {
        DEFAULT_ENGINE.getScripts().clearFormulaJsCache();
    }

    /** 清理默认引擎缓存，后续文件调用将重新加载。 */
    public static void clearFormulaJsCache(String formulaJsPath) {
        DEFAULT_ENGINE.getScripts().clearFormulaJsCache(formulaJsPath);
    }

    /** 注销自定义函数，不影响已经开始的计算快照。 */
    public static void unregisterCustomFunction(String name) {
        DEFAULT_ENGINE.getCustomFunctions().unregister(name);
    }

    /** 自定义函数兼容接口，返回可供公式使用的表达式字符串。 */
    @FunctionalInterface
    public interface CustomFunction {
        /** 计算函数参数，data 和 fieldMapping 为本次输入。 */
        String execute(String params, JSONObject data, Map<String, String> fieldMapping);
    }
}
