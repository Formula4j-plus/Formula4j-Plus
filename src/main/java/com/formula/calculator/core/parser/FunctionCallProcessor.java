package com.formula.calculator.core.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 兼容型函数调用处理器；替换值按字面量处理，参数扫描升级另行进行。 */
public final class FunctionCallProcessor {
    private FunctionCallProcessor() { }
    /** 处理一种函数，保留旧解析与异常回退规则。 */
    public static String process(String formula, String funcName, java.util.function.Function<String, String> processor) {
        // 使用单词边界确保函数名完整匹配，避免部分匹配（如ISEVEN匹配到IS）
        Pattern pattern = Pattern.compile("(?<![a-zA-Z0-9_])" + Pattern.quote(funcName) + "\\s*\\(([^)]*)\\)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(formula);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            try {
                String params = matcher.group(1);
                String result = processor.apply(params);
                matcher.appendReplacement(sb, Matcher.quoteReplacement(result));
            } catch (Exception e) {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group(0)));
            }
        }
        matcher.appendTail(sb);

        return sb.toString();
    }
}
