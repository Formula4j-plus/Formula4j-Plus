# 严格模式接入与边界

本轮增加独立的 `StrictFormulaEngine`。它以类型化语法树执行公式，提供十进制运算、分支惰性求值、
原公式错误位置和已编译公式复用。既有 `FormulaCalculator` 与 `DefaultFormulaEngine` 继续使用兼容语义。
本轮源码能力位于“未发布”变更中，使用已发布 3.0.0 的项目需等包含这些类的新版本，或自行构建当前源码。

## 接入和重复执行

```java
import com.formula.calculator.core.strict.StrictFormulaEngine;
import com.formula.calculator.core.strict.CompiledFormula;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

StrictFormulaEngine engine = new StrictFormulaEngine();
CompiledFormula formula = engine.compile("IF(${quantity}>0,ROUND(${price}*${quantity},2),0)");
Map<String, Object> data = new HashMap<>();
data.put("price", new BigDecimal("12.35"));
data.put("quantity", 2);
Object result = formula.evaluate(data); // BigDecimal 24.70
data.put("quantity", 3);
Object next = formula.evaluate(data);   // BigDecimal 37.05
```

也可用 `engine.execute("0.1+0.2", data)`，结果为 BigDecimal 0.3。
`compile` 返回不可变对象，可跨线程共享；每次执行复制顶层输入及字段映射。
调用方在快照复制期间不得并发修改原 Map。字段值仅支持数字、文本、布尔及 null，暂不支持嵌套数组或对象。
严格公共入口支持 Map；项目仍保留 fastjson 依赖以兼容原 API，不宣称整个计算库已移除该依赖。

## 字段、类型和语法

- 支持 `${field}`、`[field]`、普通标识符及中文标识符。含空格、标点或保留词的字段用显式字段符号包围。
- 展示名称映射：`formula.evaluate(data, Collections.singletonMap("单价", "price"))`。
- 字符串内的字段、逗号、括号和函数名称均为字面量；支持单、双引号，重复引号转义，以及 `\\`、`\"`、`\'`、`\n`、`\r`、`\t`。
- 常量 `TRUE`、`FALSE`、`NULL` 不区分大小写。函数名称不区分大小写，字段名称区分大小写。
- 数值统一返回 BigDecimal；文本为 String，逻辑结果为 Boolean，空值为 null。
- 支持 `+ - * / % ^`、比较 `= == != <> < <= > >=`、逻辑 `! && ||`，以及文本拼接 `&`。
- `^` 从右结合且优先于单目负号：`2^3^2=512`、`-2^2=-4`；`%` 采用严格 MOD 规则。
- 数字字符串不参与隐式数值运算，需要 `VALUE(${price})`。布尔值不参与算术。
- 条件接受布尔值或数字（零为 false）；null、文本不能直接作为条件。
- 数值比较使用十进制值；文本排序使用 Java String 字典序。相等比较不将文本 `"1"` 与数字 1 等同。

## 精度与舍入

默认 `MathContext.DECIMAL128`：34 位有效数字、HALF_EVEN，应用于算术运算。
这是一种明确的精度策略，不代表无限精度。十进制字面量及传入数字在读取时保留自身精度，算术结果按策略舍入。
金额从字符串构造 BigDecimal；已有 Double 输入的历史浮点误差不能自动恢复。

```java
import java.math.MathContext;
import java.math.RoundingMode;
StrictFormulaEngine engine = new StrictFormulaEngine(new MathContext(20, RoundingMode.HALF_UP), 128);
```

`ROUND` 单独采用 HALF_UP，`ROUNDUP` 远离零，`ROUNDDOWN` 向零；小数位参数为 -1000 至 1000 的整数。
`POWER` 与 `^` 暂只支持 -1000 至 1000 的整数幂，非整数幂显式报错。
`SQRT` 使用十进制牛顿迭代，按 MathContext 舍入；它与 JS 的二进制浮点平方根可能出现尾数差异。
`MOD` 结果的符号与除数一致；`MOD(3,-5)=-2`，与 Formula.js 4.6.1 实测结果 -3 存在已记录差异。

## 条件和错误定位

`IF` 只执行选中的分支；`AND`、`OR`、`&&`、`||` 短路执行。
`IFERROR` 只在首个参数出现执行错误时计算备用值，不捕获资源上限错误。
编译阶段仍检查全部分支的语法、函数是否存在、参数数量，因此 `IF(TRUE,1,UNKNOWN(2))` 在编译时失败。
静态依赖列表包含全部分支的字段，不等于当前实际读取的字段。

```java
import com.formula.calculator.api.StrictFormulaException;
try {
    engine.execute("SUM(1,${missing})", data);
} catch (StrictFormulaException error) {
    // MISSING_FIELD；symbol="missing"；start=6；end=16
    System.out.println(error.getCode());
    System.out.println(error.getSymbol());
    System.out.println(error.getStart());
    System.out.println(error.getEnd());
}
```

区间是原公式中从零开始、左闭右开的 UTF-16 字符索引，可供前端标注。
字段错误定位字段引用，函数参数类型错误定位函数调用，运算错误定位运算符。
空公式属于语法错误；null 公式或 null 上下文属于调用参数错误（NullPointerException）。

| 错误码 | 含义 |
| --- | --- |
| SYNTAX_ERROR | 引号、括号、转义或表达式不合法 |
| UNKNOWN_FUNCTION | 严格模式未支持的函数 |
| INVALID_ARGUMENT_COUNT | 参数数量不满足函数元数据 |
| MISSING_FIELD | 数据中不存在字段键；不默认替换成零 |
| TYPE_ERROR | 字段或参数类型不满足当前运算 |
| DIVISION_BY_ZERO | 除数为零或零的负数幂 |
| NUMERIC_ERROR | 非法数字、负数平方根或越界整数参数等 |
| LIMIT_EXCEEDED | 触发长度、深度或数字规模限制 |

详细结果比较可用 `engine.verifyDetailed(formula, data, frontendResult, tolerance)`。
该方法复用 VerificationResult 的四种状态，错误说明含位置；需要结构化错误码时直接调用 compile/evaluate。

## 当前严格函数

共 28 个函数，范围与兼容入口的函数集合不同：

| 类别 | 函数 |
| --- | --- |
| 条件 | IF、IFERROR、AND、OR、NOT |
| 聚合 | SUM、AVERAGE、MIN、MAX |
| 数学 | ABS、SQRT、ROUND、ROUNDUP、ROUNDDOWN、POWER、MOD |
| 文本 | CONCATENATE、LEN、LEFT、RIGHT、MID、UPPER、LOWER、TRIM |
| 类型 | ISNUMBER、ISTEXT、ISBLANK、VALUE |

元数据通过 `engine.getFunctions()` 获取，包含参数数量、参数描述、结果类型、惰性及纯函数标记。
[网站函数目录数据](strict-functions.json)由真实 Java 元数据生成，CI 检查与源码一致。
聚合函数至少接收一个数字参数；暂不支持数组范围。`LEFT/RIGHT` 默认长度为 1；`MID` 从 1 开始。
长度和位置参数要求整数，不悄悄截断小数。文本长度与截取按 UTF-16 单元，TRIM 仅合并 ASCII 空格。
`CONCATENATE` 可拼接标量（null 为空文本，布尔为 TRUE/FALSE）。`ISBLANK` 仅判断 null，空字符串为 false。

## 缓存与执行上限

- 每个引擎默认最多缓存 256 个公式，按最近访问淘汰；配置容量为 0 至 4096，0 禁用缓存。
- 缓存键是原公式文本。精度、函数实现固定在引擎中；数据和字段映射在执行时绑定，不能缓存结果值。
- `clearCache()` 只清除缓存条目，调用方已持有的编译对象仍可执行。
- 原公式最多 16384 个 UTF-16 单元、4096 个 token；解析和求值深度最多 128。
- 数字有效位数最多 1100，scale 绝对值最多 1000；输入数字文本最多 1100 个字符，结果文本最多 16384 个单元。
- MathContext 精度允许 1 至 1000；不允许无限精度配置导致无限小数除法策略不明确。

当前严格入口不执行脚本、自定义 Lambda、日期、财务等剩余类别。这些能力仍可通过兼容入口使用，
应逐类审核后接入严格语义。已提供独立[JMH 开发基准](../tools/benchmark/README.md)，
编译复用不等于已证明特定业务吞吐倍数；短运行只验证基准可执行，正式测量需充分预热和独立 fork。

## 兼容迁移

先查看[差分矩阵](formulajs-compatibility.md)，对业务公式执行严格模式影子计算，再显式切换入口。
不要用“严格失败后自动调用兼容入口”的方式掩盖字段缺失、类型错误和精度差异。
旧的数字字符串、缺失字段默认值、逻辑返回 0/1 和日期表示需要逐项确认。

[引擎行为契约](engine-contract.md) · [使用指南](../USAGE.md) · [Wiki](wiki/README.md)
