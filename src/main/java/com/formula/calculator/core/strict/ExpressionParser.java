package com.formula.calculator.core.strict;

import com.formula.calculator.api.FunctionDescriptor;
import com.formula.calculator.api.StrictFormulaException;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static com.formula.calculator.api.StrictFormulaException.Code.*;

/** 具有引号、转义、字段及括号意识的词法分析和优先级解析器。 */
final class ExpressionParser {
    private final String formula;
    private final MathContext mathContext;
    private final Set<String> dependencies = new LinkedHashSet<>();
    private int offset;
    private int tokens;
    private int depth;
    private Token token;

    ExpressionParser(String formula, MathContext mathContext) {
        this.formula = formula;
        this.mathContext = mathContext;
    }

    /** 编译全部表达式，拒绝尾部垃圾、过长输入和过深嵌套。 */
    CompiledFormula compile() {
        if (formula.length() > 16384) throw failure(LIMIT_EXCEEDED, "公式长度超过 16384", 0, formula.length(), "");
        next();
        Node root = expression(0);
        if (!token.is("EOF")) throw syntax("公式末尾存在多余内容");
        return new CompiledFormula(formula, root, dependencies, mathContext);
    }

    /** 按运算符优先级构建语法树；幂使用右结合，其余二元运算使用左结合。 */
    private Node expression(int minimum) {
        if (++depth > 128) throw failure(LIMIT_EXCEEDED, "解析深度超过 128", token.start, token.end, token.text);
        try {
            Node left = prefix();
            while (precedence(token.kind) >= minimum && precedence(token.kind) >= 0) {
                Token operator = token;
                int rank = precedence(operator.kind);
                next();
                Node right = expression(operator.is("^") ? rank : rank + 1);
                left = new Node("BINARY", operator.text, operator.start, operator.end, null,
                        java.util.Arrays.asList(left, right));
            }
            return left;
        } finally {
            depth--;
        }
    }

    private Node prefix() {
        Token current = token;
        if (current.is("+") || current.is("-") || current.is("!")) {
            next();
            return new Node("UNARY", current.text, current.start, current.end, null,
                    Collections.singletonList(expression(6)));
        }
        if (current.is("(")) {
            next();
            Node node = expression(0);
            expect(")");
            return node;
        }
        if (current.is("NUMBER") || current.is("STRING")) {
            next();
            Object literal = current.text;
            if (current.is("NUMBER")) {
                try {
                    literal = new BigDecimal(current.text);
                } catch (NumberFormatException failure) {
                    throw failure(NUMERIC_ERROR, "非法数字", current.start, current.end, current.text);
                }
            }
            Node node = new Node("LITERAL", current.text, current.start, current.end, literal, Collections.emptyList());
            new StrictValues(null, null, mathContext).checked(literal, node);
            return node;
        }
        if (current.is("FIELD") || current.is("IDENTIFIER")) {
            next();
            String upper = current.text.toUpperCase(Locale.ROOT);
            if (current.is("IDENTIFIER") && token.is("(")) return function(current, upper);
            if (current.is("IDENTIFIER") && (upper.equals("TRUE") || upper.equals("FALSE") || upper.equals("NULL"))) {
                Object literal = upper.equals("NULL") ? null : Boolean.valueOf(upper.equals("TRUE"));
                return new Node("LITERAL", current.text, current.start, current.end, literal, Collections.emptyList());
            }
            dependencies.add(current.text);
            return new Node("FIELD", current.text, current.start, current.end, null, Collections.emptyList());
        }
        throw syntax("需要数字、文本、字段、函数或括号表达式");
    }

    private Node function(Token name, String upper) {
        FunctionDescriptor descriptor = StrictFunctions.find(upper);
        if (descriptor == null) throw failure(UNKNOWN_FUNCTION, "严格模式不支持函数: " + upper,
                name.start, name.end, upper);
        next();
        List<Node> arguments = new ArrayList<>();
        if (!token.is(")")) {
            do {
                arguments.add(expression(0));
                if (!token.is(",")) break;
                next();
            } while (true);
        }
        int end = token.end;
        expect(")");
        if (arguments.size() < descriptor.getMinArguments() || arguments.size() > descriptor.getMaxArguments()) {
            throw failure(INVALID_ARGUMENT_COUNT, "函数参数数量不正确: " + upper, name.start, end, upper);
        }
        return new Node("FUNCTION", upper, name.start, end, null, arguments);
    }

    private void expect(String kind) {
        if (!token.is(kind)) throw syntax("需要 '" + kind + "'");
        next();
    }

    private static int precedence(String kind) {
        switch (kind) {
            case "||": return 0;
            case "&&": return 1;
            case "=": case "==": case "!=": case "<>": return 2;
            case "<": case "<=": case ">": case ">=": return 3;
            case "+": case "-": case "&": return 4;
            case "*": case "/": case "%": return 5;
            case "^": return 7;
            default: return -1;
        }
    }

    /** 字符串内容保持字面量，不将其中的字段和函数名称重新解释。 */
    private void next() {
        while (offset < formula.length() && Character.isWhitespace(formula.charAt(offset))) offset++;
        int start = offset;
        if (++tokens > 4096) throw failure(LIMIT_EXCEEDED, "公式 token 数量超过 4096", start, start, "");
        if (offset == formula.length()) {
            token = new Token("EOF", "", offset, offset);
            return;
        }
        char ch = formula.charAt(offset++);
        if (ch == '\'' || ch == '"') {
            StringBuilder text = new StringBuilder();
            boolean closed = false;
            while (offset < formula.length()) {
                char current = formula.charAt(offset++);
                if (current == ch) {
                    if (offset < formula.length() && formula.charAt(offset) == ch) {
                        offset++;
                        text.append(ch);
                    } else {
                        closed = true;
                        break;
                    }
                } else if (current == '\\') {
                    if (offset == formula.length()) break;
                    char escaped = formula.charAt(offset++);
                    switch (escaped) {
                        case 'n': text.append('\n'); break;
                        case 'r': text.append('\r'); break;
                        case 't': text.append('\t'); break;
                        case '\\': case '\'': case '"': text.append(escaped); break;
                        default:
                            throw failure(SYNTAX_ERROR, "未知转义字符", offset - 2, offset, "\\" + escaped);
                    }
                } else {
                    text.append(current);
                }
            }
            if (!closed) throw failure(SYNTAX_ERROR, "字符串缺少闭合引号", start, offset, "");
            token = new Token("STRING", text.toString(), start, offset);
            return;
        }
        if (ch == '[' || (ch == '$' && offset < formula.length() && formula.charAt(offset) == '{')) {
            char closing = ch == '[' ? ']' : '}';
            if (ch == '$') offset++;
            int fieldStart = offset;
            while (offset < formula.length() && formula.charAt(offset) != closing) offset++;
            if (offset == formula.length()) throw failure(SYNTAX_ERROR, "字段缺少闭合符号", start, offset, "");
            String name = formula.substring(fieldStart, offset++).trim();
            if (name.isEmpty()) throw failure(SYNTAX_ERROR, "字段名称不能为空", start, offset, "");
            token = new Token("FIELD", name, start, offset);
            return;
        }
        if (isDigit(ch) || (ch == '.' && offset < formula.length() && isDigit(formula.charAt(offset)))) {
            while (offset < formula.length() && isDigit(formula.charAt(offset))) offset++;
            if (ch != '.' && offset < formula.length() && formula.charAt(offset) == '.') {
                offset++;
                while (offset < formula.length() && isDigit(formula.charAt(offset))) offset++;
            }
            if (offset < formula.length() && (formula.charAt(offset) == 'e' || formula.charAt(offset) == 'E')) {
                offset++;
                if (offset < formula.length() && (formula.charAt(offset) == '+' || formula.charAt(offset) == '-')) offset++;
                while (offset < formula.length() && isDigit(formula.charAt(offset))) offset++;
            }
            if (offset - start > 1100) throw failure(LIMIT_EXCEEDED, "数字字面量过长", start, offset, "");
            token = new Token("NUMBER", formula.substring(start, offset), start, offset);
            return;
        }
        if (Character.isLetter(ch) || ch == '_') {
            while (offset < formula.length()) {
                char current = formula.charAt(offset);
                if (!Character.isLetterOrDigit(current) && current != '_') break;
                offset++;
            }
            token = new Token("IDENTIFIER", formula.substring(start, offset), start, offset);
            return;
        }
        if (offset < formula.length()) {
            String pair = formula.substring(start, offset + 1);
            if (pair.equals("&&") || pair.equals("||") || pair.equals("<=") || pair.equals(">=")
                    || pair.equals("==") || pair.equals("!=") || pair.equals("<>")) {
                offset++;
                token = new Token(pair, pair, start, offset);
                return;
            }
        }
        if ("()+-*/%^<>=!&,".indexOf(ch) >= 0) {
            token = new Token(String.valueOf(ch), String.valueOf(ch), start, offset);
            return;
        }
        throw failure(SYNTAX_ERROR, "非法字符", start, offset, String.valueOf(ch));
    }

    private static boolean isDigit(char ch) { return ch >= '0' && ch <= '9'; }

    private StrictFormulaException syntax(String message) {
        return failure(SYNTAX_ERROR, message, token.start, token.end, token.text);
    }

    private static StrictFormulaException failure(StrictFormulaException.Code code, String message,
                                                   int start, int end, String symbol) {
        return new StrictFormulaException(code, message, start, end, symbol);
    }

    private static final class Token {
        final String kind;
        final String text;
        final int start;
        final int end;

        Token(String kind, String text, int start, int end) {
            this.kind = kind;
            this.text = text;
            this.start = start;
            this.end = end;
        }

        boolean is(String expected) { return kind.equals(expected); }
    }

    /** 仅在编译时构建，参数列表冻结；执行状态保存在 StrictValues 中。 */
    static final class Node {
        final String kind;
        final String symbol;
        final int start;
        final int end;
        final Object literal;
        final List<Node> arguments;

        Node(String kind, String symbol, int start, int end, Object literal, List<Node> arguments) {
            this.kind = kind;
            this.symbol = symbol;
            this.start = start;
            this.end = end;
            this.literal = literal;
            this.arguments = Collections.unmodifiableList(new ArrayList<>(arguments));
        }

        /** 执行不可变节点，将数字及资源错误转换为原公式中的结构化诊断。 */
        Object evaluate(StrictValues values) {
            values.enter(this);
            try {
                Object result;
                switch (kind) {
                    case "LITERAL": result = literal; break;
                    case "FIELD": result = values.field(this); break;
                    case "UNARY":
                        Object operand = arguments.get(0).evaluate(values);
                        result = symbol.equals("!") ? !values.bool(operand, this)
                                : symbol.equals("-") ? values.number(operand, this).negate(values.mathContext)
                                : values.number(operand, this).plus(values.mathContext);
                        break;
                    case "BINARY": result = binary(values); break;
                    case "FUNCTION": result = StrictFunctions.evaluate(this, values); break;
                    default: throw new IllegalStateException("未知节点类型");
                }
                return values.checked(result, this);
            } catch (ArithmeticException failure) {
                throw StrictValues.error(NUMERIC_ERROR, "数值计算失败: " + failure.getMessage(), this);
            } finally {
                values.leave();
            }
        }

        private Object binary(StrictValues values) {
            Object left = arguments.get(0).evaluate(values);
            // 在执行右侧之前判断，避免未选分支读取缺失字段或执行错误函数。
            if (symbol.equals("&&")) {
                return values.bool(left, this) && values.bool(arguments.get(1).evaluate(values), this);
            }
            if (symbol.equals("||")) {
                return values.bool(left, this) || values.bool(arguments.get(1).evaluate(values), this);
            }
            Object right = arguments.get(1).evaluate(values);
            if (symbol.equals("&")) return values.display(left, this) + values.display(right, this);
            if (precedence(symbol) == 2) {
                boolean equal = left instanceof BigDecimal && right instanceof BigDecimal
                        ? ((BigDecimal) left).compareTo((BigDecimal) right) == 0 : java.util.Objects.equals(left, right);
                return symbol.equals("!=") || symbol.equals("<>") ? !equal : equal;
            }
            if (precedence(symbol) == 3) {
                int compared;
                if (left instanceof String && right instanceof String) {
                    compared = ((String) left).compareTo((String) right);
                } else {
                    compared = values.number(left, this).compareTo(values.number(right, this));
                }
                switch (symbol) {
                    case "<": return compared < 0;
                    case "<=": return compared <= 0;
                    case ">": return compared > 0;
                    default: return compared >= 0;
                }
            }
            BigDecimal a = values.number(left, this);
            BigDecimal b = values.number(right, this);
            switch (symbol) {
                case "+": return a.add(b, values.mathContext);
                case "-": return a.subtract(b, values.mathContext);
                case "*": return a.multiply(b, values.mathContext);
                case "/":
                    if (b.signum() == 0) throw StrictValues.error(DIVISION_BY_ZERO, "除数不能为零", this);
                    return a.divide(b, values.mathContext);
                case "%": return StrictFunctions.mod(a, b, this, values);
                case "^": return StrictFunctions.power(a, b, this, values);
                default: throw new IllegalStateException("未知运算符");
            }
        }
    }
}
