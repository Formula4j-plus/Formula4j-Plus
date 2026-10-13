package com.formula.calculator.core.script;

import com.alibaba.fastjson.JSONObject;
import com.formula.calculator.FormulaCalculator;
import com.formula.calculator.FormulaCalculator.CustomFunction;
import com.formula.calculator.FunctionName;
import com.formula.calculator.registry.CustomFunctionRegistry;
import com.formula.calculator.utils.FormulaParamUtils;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import static com.formula.calculator.core.resolver.FieldResolver.getEnCode;

/** 实例级脚本状态；脚本须为可信代码，不提供不可信脚本沙箱。 */
public final class ScriptFunctionSupport {
    private final CustomFunctionRegistry registry;
    private final Map<String, ScriptEngine> formulaJsEngines = new LinkedHashMap<>(16, 0.75f, true);
    private volatile ScriptEngine globalFormulaJsEngine;
    private String globalFormulaJsPath;

    /** 将脚本注册和缓存绑定到指定引擎实例。 */
    public ScriptFunctionSupport(CustomFunctionRegistry registry) { this.registry = registry; }
    /** 使用本次选中的脚本引擎处理未实现函数；共享引擎的 eval 串行执行。 */
    public String processJavaScriptFunctions(String formula, JSONObject data, Map<String, String> fieldMapping, Map<String, CustomFunction> customFunctions) {
        ScriptEngine selectedEngine = globalFormulaJsEngine;
        if (selectedEngine == null) return formula;
        // 匹配函数调用：函数名(参数)
        Pattern funcPattern = Pattern.compile("([A-Z_][A-Z0-9_.]*)\\s*\\(([^)]*)\\)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = funcPattern.matcher(formula);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String funcName = matcher.group(1).toUpperCase();
            String params = matcher.group(2);

            // 检查是否已经在Java中实现或者是已注册的自定义函数
            boolean isJavaFunction = isJavaImplementedFunction(funcName) || customFunctions.containsKey(funcName);

            if (!isJavaFunction) {
                // 尝试从JS引擎调用
                try {
                    String result = callJavaScriptFunction(funcName, params, data, fieldMapping, selectedEngine);
                    if (result != null) {
                        matcher.appendReplacement(sb, Matcher.quoteReplacement(result));
                        continue;
                    }
                } catch (Exception e) {
                    // JS调用失败，保持原样
                }
            }

            // 保持原样（Java已实现的函数或JS调用失败）
            matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group(0)));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    private boolean isJavaImplementedFunction(String funcName) {
        // 检查FunctionName枚举中是否包含该函数
        try {
            FunctionName.valueOf(funcName);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private String callJavaScriptFunction(String funcName, String params, JSONObject data, Map<String, String> fieldMapping, ScriptEngine selectedEngine) {
        try {
            // 解析参数
            String[] parts = FormulaParamUtils.splitFunctionParams(params, -1);

            // 构建JavaScript调用代码
            StringBuilder jsCode = new StringBuilder();
            jsCode.append("(function() {");
            jsCode.append("  try {");
            jsCode.append("    var params = [");

            // 将参数转换为JavaScript数组
            for (int i = 0; i < parts.length; i++) {
                if (i > 0) jsCode.append(", ");
                jsCode.append(convertRawParamToJavaScript(parts[i].trim(), data, fieldMapping));
            }

            jsCode.append("    ];");
            jsCode.append("    if (typeof ").append(funcName).append(" === 'function') {");
            jsCode.append("      return ").append(funcName).append(".apply(null, params);");
            jsCode.append("    } else {");
            jsCode.append("      return null;");
            jsCode.append("    }");
            jsCode.append("  } catch(e) { return null; }");
            jsCode.append("})();");

            // 执行JavaScript代码
            Object result;
            synchronized (selectedEngine) {
                result = selectedEngine.eval(jsCode.toString());
            }

            return normalizeFunctionResult(result, true);
        } catch (Exception e) {
            return null; // 调用失败
        }
    }

    /** 注册可信 JavaScript，保留历史脚本返回值转换与失败回退。 */
    public void registerCustomFunction(String name, String type, String script) {
        if (!"js".equalsIgnoreCase(type) && !"javascript".equalsIgnoreCase(type)) {
            throw new IllegalArgumentException("不支持的类型: " + type + "，仅支持 'js' 或 'javascript'");
        }
        final String normalizedScript = script == null ? "" : script.trim();

        // 将JavaScript脚本包装为CustomFunction
        registry.register(name, (params, data, fieldMapping) -> {
            try {
                // 使用Nashorn引擎执行JavaScript（Java 8-14）
                // 注意：Java 15+ 已移除Nashorn，需要使用GraalVM或其他方案
                ScriptEngine engine = getJavaScriptEngine();
                if (engine == null) {
                    return "0";
                }

                engine.put("params", params);
                engine.put("data", data);

                Object result;
                if (normalizedScript.startsWith("function") || normalizedScript.contains("=>")) {
                    String[] parts = FormulaParamUtils.splitFunctionParams(params, -1);
                    StringBuilder jsCode = new StringBuilder();
                    jsCode.append("(function() {");
                    jsCode.append("  var func = ").append(normalizedScript).append(";");
                    jsCode.append("  var args = [");

                    for (int i = 0; i < parts.length; i++) {
                        if (i > 0) jsCode.append(", ");
                        jsCode.append(convertParamToJavaScript(parts[i].trim(), data, fieldMapping));
                    }

                    jsCode.append("];\n");
                    jsCode.append("  return func.apply(null, args);");
                    jsCode.append("})();");
                    result = engine.eval(jsCode.toString());
                } else {
                    result = engine.eval(normalizedScript);
                }

                return normalizeFunctionResult(result, false);
            } catch (Exception e) {
                return "0";
            }
        });
    }

    /** 注册来自可信路径的函数；文件引擎按实例缓存。 */
    public void registerCustomFunctionFromFile(String name, String formulaJsPath, String functionName) {
        if (formulaJsPath == null || formulaJsPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Formula.js文件路径不能为空");
        }

        String actualFunctionName = (functionName == null || functionName.trim().isEmpty()) ? name : functionName;

        // 将JavaScript脚本包装为CustomFunction
        registry.register(name, (params, data, fieldMapping) -> {
            try {
                // 获取或加载Formula.js引擎
                ScriptEngine engine = getOrLoadFormulaJsEngine(formulaJsPath);
                if (engine == null) {
                    return "0";
                }

                // 解析参数
                String[] parts = FormulaParamUtils.splitFunctionParams(params, -1);

                // 构建JavaScript调用代码
                StringBuilder jsCode = new StringBuilder();
                jsCode.append("(function() {");
                jsCode.append("  var params = [");

                // 将参数转换为JavaScript数组
                for (int i = 0; i < parts.length; i++) {
                    if (i > 0) jsCode.append(", ");
                    jsCode.append(convertRawParamToJavaScript(parts[i].trim(), data, fieldMapping));
                }

                jsCode.append("  ];");
                jsCode.append("  return ").append(actualFunctionName).append(".apply(null, params);");
                jsCode.append("})();");

                // 执行JavaScript代码
                Object result;
                synchronized (engine) {
                    result = engine.eval(jsCode.toString());
                }

                return normalizeFunctionResult(result, false);
            } catch (Exception e) {
                return "0";
            }
        });
    }

    /** 加载可信库并在成功后替换全局库，失败时保留旧库。 */
    public synchronized void loadFormulaJsLibrary(String formulaJsPath) {
        if (formulaJsPath == null || formulaJsPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Formula.js文件路径不能为空");
        }

        try {
            // 获取JavaScript引擎
            ScriptEngine engine = getJavaScriptEngine();
            if (engine == null) {
                throw new RuntimeException("JavaScript引擎不可用");
            }

            // 读取Formula.js文件内容
            String formulaJsContent = loadFormulaJsFile(formulaJsPath);
            if (formulaJsContent == null || formulaJsContent.trim().isEmpty()) {
                throw new RuntimeException("Formula.js文件内容为空: " + formulaJsPath);
            }

            // 执行Formula.js文件（加载所有函数到引擎中）
            engine.eval(formulaJsContent);

            // 保存全局引擎
            globalFormulaJsEngine = engine;
            globalFormulaJsPath = formulaJsPath;
        } catch (Exception e) {
            throw new RuntimeException("加载Formula.js库失败: " + e.getMessage(), e);
        }
    }

    /** 清除后续计算使用的全局库，不中断已开始的执行。 */
    public synchronized void clearFormulaJsLibrary() {
        globalFormulaJsEngine = null;
        globalFormulaJsPath = null;
        // 已清除Formula.js库
    }

    /** 取得当前库路径。 */
    public synchronized String getLoadedFormulaJsPath() {
        return globalFormulaJsPath;
    }

    /** 检查当前是否存在已加载库。 */
    public synchronized boolean isFormulaJsLibraryLoaded() {
        return globalFormulaJsEngine != null;
    }

    /** 串行加载并缓存最多 32 个文件引擎，按最近使用顺序淘汰。 */
    private synchronized ScriptEngine getOrLoadFormulaJsEngine(String formulaJsPath) {
        // 检查缓存
        if (formulaJsEngines.containsKey(formulaJsPath)) {
            return formulaJsEngines.get(formulaJsPath);
        }

        try {
            // 获取JavaScript引擎
            ScriptEngine engine = getJavaScriptEngine();
            if (engine == null) {
                return null;
            }

            // 读取Formula.js文件内容
            String formulaJsContent = loadFormulaJsFile(formulaJsPath);
            if (formulaJsContent == null || formulaJsContent.trim().isEmpty()) {
                return null;
            }

            // 执行Formula.js文件（加载所有函数到引擎中）
            engine.eval(formulaJsContent);

            // 缓存引擎
            if (formulaJsEngines.size() >= 32) {
                formulaJsEngines.remove(formulaJsEngines.keySet().iterator().next());
            }
            formulaJsEngines.put(formulaJsPath, engine);

            return engine;
        } catch (Exception e) {
            return null;
        }
    }

    /** 读取最多 4 MiB 字符，远程加载设定连接和读取超时。 */
    private String loadFormulaJsFile(String path) {
        try {
            java.io.InputStream inputStream = null;

            // 处理不同的路径格式
            if (path.startsWith("classpath:")) {
                // 从类路径加载
                String resourcePath = path.substring("classpath:".length());
                inputStream = FormulaCalculator.class.getClassLoader().getResourceAsStream(resourcePath);
                if (inputStream == null) {
                    inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
                }
            } else if (path.startsWith("http://") || path.startsWith("https://")) {
                // 从URL加载
                java.net.URL url = new java.net.URL(path);
                java.net.URLConnection connection = url.openConnection();
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(10000);
                inputStream = connection.getInputStream();
            } else if (path.startsWith("file://")) {
                // 从文件路径加载（file://格式）
                String filePath = path.substring("file://".length());
                // 处理Windows路径
                if (filePath.startsWith("/") && System.getProperty("os.name").toLowerCase().contains("win")) {
                    filePath = filePath.substring(1);
                }
                inputStream = new java.io.FileInputStream(filePath);
            } else {
                // 从文件路径加载（普通路径）
                inputStream = new java.io.FileInputStream(path);
            }

            if (inputStream == null) {
                return null;
            }

            // 读取文件内容
            try (java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(inputStream, "UTF-8"))) {
                StringBuilder content = new StringBuilder();
                char[] buffer = new char[4096];
                int count;
                while ((count = reader.read(buffer)) != -1) {
                    if (content.length() + count > 4 * 1024 * 1024) {
                        throw new IllegalArgumentException("脚本内容超过 4 MiB 字符限制");
                    }
                    content.append(buffer, 0, count);
                }
                return content.toString();
            }
        } catch (Exception e) {
            return null;
        }
    }

    private String escapeJavaScriptString(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\")
                 .replace("\"", "\\\"")
                 .replace("\n", "\\n")
                 .replace("\r", "\\r")
                 .replace("\t", "\\t");
    }

    /** 清理文件引擎缓存，不中断正在使用已取得引擎的计算。 */
    public synchronized void clearFormulaJsCache() {
        formulaJsEngines.clear();
        // 已清除Formula.js引擎缓存
    }

    /** 清理文件引擎缓存，不中断正在使用已取得引擎的计算。 */
    public synchronized void clearFormulaJsCache(String formulaJsPath) {
        formulaJsEngines.remove(formulaJsPath);
        // 已清除Formula.js引擎缓存: formulaJsPath
    }

    /** 查找运行时已安装的脚本引擎，不自动引入依赖。 */
    private ScriptEngine getJavaScriptEngine() {
        try {
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = null;

            // 检测Java版本
            String javaVersion = System.getProperty("java.version");
            int majorVersion = getJavaMajorVersion(javaVersion);

            // Java 8-14: 优先使用Nashorn
            if (majorVersion >= 8 && majorVersion <= 14) {
                try {
                    engine = manager.getEngineByName("nashorn");
                    if (engine != null) {
                        return engine;
                    }
                } catch (Exception e) {
                    // Nashorn不可用，继续尝试其他引擎
                }
            }

            // Java 15+: 尝试使用GraalVM JavaScript引擎
            // 注意：需要额外添加GraalVM JavaScript依赖
            try {
                engine = manager.getEngineByName("graal.js");
                if (engine != null) {
                    return engine;
                }
            } catch (Exception e) {
                // GraalVM不可用，继续尝试
            }

            // 尝试通用的JavaScript引擎名称
            try {
                engine = manager.getEngineByName("javascript");
                if (engine != null) {
                    return engine;
                }
            } catch (Exception e) {
                // 继续尝试
            }

            // 尝试js引擎名称
            try {
                engine = manager.getEngineByName("js");
                if (engine != null) {
                    return engine;
                }
            } catch (Exception e) {
                // 继续尝试
            }

            // 尝试ECMAScript引擎名称
            try {
                engine = manager.getEngineByName("ECMAScript");
                if (engine != null) {
                    return engine;
                }
            } catch (Exception e) {
                // 所有尝试都失败
            }

            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private int getJavaMajorVersion(String javaVersion) {
        if (javaVersion == null || javaVersion.isEmpty()) {
            return 8; // 默认假设Java 8
        }

        // Java 9+ 格式: "9", "10", "11", "17", "21", "25" 等
        // Java 8 格式: "1.8.0_xxx"
        if (javaVersion.startsWith("1.")) {
            // Java 8及之前版本
            return 8;
        }

        // 提取主版本号
        try {
            // 格式可能是 "9", "11.0.1", "17.0.1", "21", "25" 等
            int dotIndex = javaVersion.indexOf('.');
            if (dotIndex > 0) {
                return Integer.parseInt(javaVersion.substring(0, dotIndex));
            } else {
                return Integer.parseInt(javaVersion);
            }
        } catch (NumberFormatException e) {
            // 解析失败，默认返回8
            return 8;
        }
    }

    private String convertParamToJavaScript(String param, JSONObject data, Map<String, String> fieldMapping) {
        return convertRawParamToJavaScript(param, data, fieldMapping);
    }

    private String convertRawParamToJavaScript(String param, JSONObject data, Map<String, String> fieldMapping) {
        try {
            double numValue = Double.parseDouble(param);
            return String.valueOf(numValue);
        } catch (NumberFormatException e) {
            if (isBooleanLiteral(param)) {
                return param.toLowerCase();
            }

            if (isQuotedLiteral(param)) {
                return param;
            }

            String enCode = getEnCode(param, fieldMapping);
            if (enCode != null && data.containsKey(enCode)) {
                Object value = data.get(enCode);
                if (value instanceof Number) {
                    return String.valueOf(((Number) value).doubleValue());
                }
                if (value instanceof Boolean) {
                    return value.toString().toLowerCase();
                }
                if (value instanceof String) {
                    return "\"" + escapeJavaScriptString(value.toString()) + "\"";
                }
            }

            return "0";
        }
    }

    private boolean isQuotedLiteral(String param) {
        return param != null && param.length() >= 2
                && ((param.startsWith("\"") && param.endsWith("\""))
                || (param.startsWith("'") && param.endsWith("'")));
    }

    private boolean isBooleanLiteral(String param) {
        return "true".equalsIgnoreCase(param) || "false".equalsIgnoreCase(param);
    }

    private String normalizeFunctionResult(Object result, boolean allowNull) {
        if (result == null) {
            return allowNull ? null : "0";
        }

        if (result instanceof Number) {
            return String.valueOf(result);
        }

        if (result instanceof Boolean) {
            return ((Boolean) result) ? "1" : "0";
        }

        String text = result.toString();
        if (text == null) {
            return allowNull ? null : "0";
        }

        String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            return "\"\"";
        }

        if (isQuotedLiteral(trimmed)) {
            if (trimmed.startsWith("'")) {
                return "\"" + escapeJavaScriptString(trimmed.substring(1, trimmed.length() - 1)) + "\"";
            }
            return trimmed;
        }

        if (isBooleanLiteral(trimmed)) {
            return "true".equalsIgnoreCase(trimmed) ? "1" : "0";
        }

        try {
            Double.parseDouble(trimmed);
            return trimmed;
        } catch (NumberFormatException e) {
            return "\"" + escapeJavaScriptString(text) + "\"";
        }
    }
}
