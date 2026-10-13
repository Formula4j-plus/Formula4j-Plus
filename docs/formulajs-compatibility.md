# Formula.js 差分兼容矩阵

对照版本：`@formulajs/formulajs@4.6.1`，由 package-lock.json 固定依赖树。共 66 个已审核用例。

这是有限语料的实测结果，不代表全部函数或 Excel 完全兼容。默认要求数值精确一致；少量浮点用例在表中列出绝对误差。

Java 结果与 Formula.js 结果分别固定为契约；CI 重新执行两端，任何结果或分类变化都会失败。

严格入口返回 BigDecimal、String、Boolean 或 null；兼容入口保留历史返回类型。

| 用例 | Java 公式 | 兼容入口 | 严格入口 | 绝对误差 | 说明 |
| --- | --- | --- | --- | --- | --- |
| decimal-add | `0.1+0.2` | 一致 | 一致 | 0.000000000001 | 浮点参考与十进制计算；允许 1e-12 绝对误差 |
| large-integer | `9007199254740993+1` | 一致 | 差异 | 0 | JS Number 已将输入压缩；严格入口保留精确整数，因此明确标记差异 |
| precedence | `2+3*4` | 一致 | 一致 | 0 | 基础标量输入 |
| divide | `5/2` | 一致 | 一致 | 0 | 基础标量输入 |
| divide-zero | `1/0` | 报错 | 报错 | 0 | 严格入口显式除零错误 |
| nested-number | `SUM(1,POWER(2,3),MAX(2,6))` | 报错 | 一致 | 0 | 兼容正则无法正确解析该嵌套调用；严格语法树结果一致 |
| fn-sum | `SUM(1,2,3)` | 一致 | 一致 | 0 | 基础标量输入 |
| fn-average | `AVERAGE(2,4,6)` | 一致 | 一致 | 0 | 基础标量输入 |
| fn-min | `MIN(3,-1,2)` | 一致 | 一致 | 0 | 基础标量输入 |
| fn-max | `MAX(3,-1,2)` | 一致 | 一致 | 0 | 基础标量输入 |
| fn-abs | `ABS(-12)` | 一致 | 一致 | 0 | 基础标量输入 |
| fn-sqrt | `SQRT(16)` | 一致 | 一致 | 0 | 基础标量输入 |
| fn-power | `POWER(2,3)` | 一致 | 一致 | 0 | 基础标量输入 |
| fn-round | `ROUND(1.235,2)` | 一致 | 一致 | 0 | 基础标量输入 |
| fn-roundup | `ROUNDUP(1.231,2)` | 一致 | 一致 | 0 | 基础标量输入 |
| fn-rounddown | `ROUNDDOWN(1.239,2)` | 一致 | 一致 | 0 | 基础标量输入 |
| fn-mod | `MOD(-3,5)` | 差异 | 一致 | 0 | 兼容入口使用 Java 余数符号；严格入口与本例 Formula.js 一致 |
| fn-concatenate | `CONCATENATE("Hello",123,"World")` | 一致 | 一致 | 0 | 基础标量输入 |
| fn-len | `LEN("abcd")` | 一致 | 一致 | 0 | 基础标量输入 |
| fn-left | `LEFT("abcd",2)` | 一致 | 一致 | 0 | 基础标量输入 |
| fn-right | `RIGHT("abcd",2)` | 一致 | 一致 | 0 | 基础标量输入 |
| fn-mid | `MID("abcd",2,2)` | 一致 | 一致 | 0 | 基础标量输入 |
| fn-upper | `UPPER("aBc")` | 一致 | 一致 | 0 | 基础标量输入 |
| fn-lower | `LOWER("AbC")` | 一致 | 一致 | 0 | 基础标量输入 |
| fn-trim | `TRIM("  a   b  ")` | 一致 | 一致 | 0 | 基础标量输入 |
| fn-isnumber | `ISNUMBER(3)` | 差异 | 一致 | 0 | 兼容入口返回数字 1，严格入口与 Formula.js 返回布尔 true |
| fn-istext | `ISTEXT("x")` | 差异 | 一致 | 0 | 兼容入口返回数字 1，严格入口与 Formula.js 返回布尔 true |
| fn-isblank | `ISBLANK(NULL)` | 差异 | 一致 | 0 | 兼容入口未将 NULL 识别为空值；严格入口与 Formula.js 返回布尔 true |
| fn-value | `VALUE("12.5")` | 一致 | 一致 | 0 | 基础标量输入 |
| fn-not | `NOT(TRUE)` | 差异 | 一致 | 0 | 兼容入口未正确解释 TRUE；严格入口与 Formula.js 返回布尔 false |
| fn-and | `AND(TRUE,FALSE)` | 差异 | 一致 | 0 | 兼容入口返回数字 0；严格入口与 Formula.js 返回布尔 false |
| fn-or | `OR(FALSE,TRUE)` | 差异 | 一致 | 0 | 兼容入口未正确解释布尔常量；严格入口与 Formula.js 返回布尔 true |
| fn-if | `IF(TRUE,7,9)` | 一致 | 一致 | 0 | 基础标量输入 |
| fn-iferror | `IFERROR(3,9)` | 一致 | 一致 | 0 | 基础标量输入 |
| sum-negative | `SUM(-2,1.5,3)` | 一致 | 一致 | 0 | 基础标量输入 |
| sum-text | `SUM(1,"2")` | 差异 | 报错 | 0 | Formula.js 将文本 2 转数字；兼容入口忽略文本；严格入口要求显式 VALUE |
| sum-boolean | `SUM(1,TRUE)` | 一致 | 报错 | 0 | Formula.js 与兼容入口忽略布尔参数；严格入口拒绝非数字参数 |
| average-zero | `AVERAGE(0,0)` | 一致 | 一致 | 0 | 基础标量输入 |
| sqrt-fraction | `SQRT(2)` | 一致 | 一致 | 0.000000000001 | 十进制平方根与 JS 浮点参考，误差 1e-12 |
| sqrt-negative | `SQRT(-1)` | 报错 | 报错 | 0 | 负数平方根的错误表示 |
| power-fraction | `POWER(4,0.5)` | 一致 | 报错 | 0 | 严格模式暂只支持整数幂 |
| round-negative-tie | `ROUND(-1.005,2)` | 一致 | 一致 | 0 | 本例实测三者均为 -1.01；不外推所有舍入输入均一致 |
| round-positive-tie | `ROUND(1.005,2)` | 一致 | 一致 | 0 | 十进制半值舍入 |
| mod-negative-divisor | `MOD(3,-5)` | 差异 | 差异 | 0 | Formula.js 4.6.1 实测 -3，兼容入口 3；严格入口按除数符号规则返回 -2 |
| mod-zero | `MOD(3,0)` | 报错 | 报错 | 0 | 除数为零的错误表示 |
| concat-literal | `CONCATENATE("a,(b)","$tail")` | 差异 | 一致 | 0 | 兼容正则破坏字符串内括号、逗号；严格入口按字面量处理 |
| nested-text | `CONCATENATE(LEFT("abcd",2),UPPER("x"))` | 差异 | 一致 | 0 | 兼容正则破坏嵌套文本调用；严格入口结果一致 |
| left-default | `LEFT("abcd")` | 一致 | 一致 | 0 | 默认长度 |
| right-default | `RIGHT("abcd")` | 一致 | 一致 | 0 | 默认长度 |
| mid-outside | `MID("abcd",9,2)` | 一致 | 一致 | 0 | 起始位置超过文本长度 |
| left-negative | `LEFT("abcd",-1)` | 一致 | 报错 | 0 | Formula.js 和兼容入口返回空文本；严格入口明确报非法长度 |
| value-invalid | `VALUE("abc")` | 一致 | 报错 | 0 | Formula.js 和兼容入口返回 0；严格入口明确报非法数字文本 |
| isnumber-text | `ISNUMBER("3")` | 差异 | 一致 | 0 | 兼容入口返回数字 0；严格入口与 Formula.js 返回布尔 false |
| isblank-empty-text | `ISBLANK("")` | 差异 | 一致 | 0 | 兼容入口将空文本认作空值；严格入口与 Formula.js 保持 null 区别 |
| if-false | `IF(FALSE,7,9)` | 一致 | 一致 | 0 | 逻辑常量与分支 |
| if-comparison | `IF(2>1,1,0)` | 一致 | 一致 | 0 | 比较条件 |
| if-lazy-error | `IF(TRUE,7,1/0)` | 一致 | 一致 | 0 | 参考端直接取正确分支结果；Java 惰性行为由独立严格模式测试验证 |
| if-lazy-missing | `IF(FALSE,${missing},9)` | 一致 | 一致 | 0 | 参考端显式传入占位值；Java 未选分支不读取字段由独立测试验证 |
| field-number | `${price}*2` | 一致 | 一致 | 0 | 数字字段 |
| field-numeric-text | `${price}*2` | 一致 | 报错 | 0 | 严格入口要求 VALUE 显式转换数字文本 |
| field-explicit-value | `VALUE(${price})*2` | 一致 | 一致 | 0 | 显式数字转换 |
| field-missing | `${missing}+1` | 一致 | 报错 | 0 | 参考端显式使用旧缺失字段默认值；严格模式报错 |
| field-null | `${price}+1` | 一致 | 报错 | 0 | 参考端显式使用旧 null 默认值；严格模式保留 null |
| date-basic | `DATE(2024,1,2)` | 差异 | 未支持 | 0 | UTC；Date 与历史数值表示不同，严格日期暂未支持 |
| date-leap | `DATE(2024,2,29)` | 差异 | 未支持 | 0 | UTC；闰年日期 |
| date-overflow | `DATE(2024,13,1)` | 差异 | 未支持 | 0 | UTC；跨年月份 |

## 复现

```sh
mvn -B -Dtest=FormulaJsDifferentialTest -Duser.timezone=UTC -Dformula.differential.output=java-report.json test
npm --prefix tools/compatibility ci --ignore-scripts
node tools/compatibility/compare.cjs java-report.json --report docs/formulajs-compatibility.md --catalog docs/strict-functions.json
```

Formula.js 是函数库。参考侧直接执行结构化函数调用和明确的算术操作，不使用 eval，也不将其当成与 Java 同构的表达式解析器。

日期对照固定 UTC：Formula.js 返回 Date，兼容入口的历史日期表示存在差异；严格入口当前不支持日期函数。

报错和未支持不计入一致。预期差异被明确记录，不能通过扩大误差或忽略异常来标记为兼容。

[严格模式接入与边界](strict-engine.md) · [引擎行为契约](engine-contract.md)
