# 公式引擎行为与维护契约

本轮采用保守迁移：外部静态 API 保留，字段转换、函数类别执行顺序和大部分容错行为保持不变。
这不是完整 Formula.js 或 Excel 语义的承诺。页面只是文档展示，未提供在线 Java 计算服务。

## 当前行为基线

| 情况 | 当前约定 |
| --- | --- |
| 空公式、空 FormulaData | 返回 null |
| 整数算术结果 | 返回 Long，例如 `1+2` 返回 `3L` |
| 非整数算术结果 | 返回 Double，例如 `5/2` 返回 `2.5` |
| 双引号包围的最终文本 | 返回 String |
| 字段缺失、null、无法转成数字 | 算术字段替换仍按 0 处理；不新增警告日志承诺 |
| 展示名称 | 根据 menuId → enCode 映射获取 JSONObject 数据 |
| 数值精度 | 计算内核仍是 double；不承诺十进制金额精确计算 |
| 旧 verify | 数字转换后按绝对误差比较，默认 0.0001；异常返回 false |
| 新 verifyDetailed | 区分 MATCH、MISMATCH、EVALUATION_ERROR、INVALID_COMPARISON |
| 函数中的容错 | 仍保留旧的局部返回 0/原参数行为；详细校验只能报告实际向外抛出的错误 |
| 条件执行 | 保留按函数类别处理的顺序，不承诺 IF 未选分支完全惰性求值 |
| 自定义函数输出 | 修复替换过程对 `$` 和反斜杠的正则分组误解释，按字面量替换 |

`CompatibilityContractTest` 的预期值在旧实现上确认过，迁移验证不通过“两个入口调用同一实现”的比较代替。

## 执行结构

```text
FormulaCalculator（原静态 API）
  → DefaultFormulaEngine（实例级状态）
    → FormulaContext（顶层输入副本）
    → CompatibilityEvaluator（本次自定义函数快照）
      → FieldResolver
      → FunctionRegistry → MathFunctions
      → 其余兼容函数类别
      → ScriptFunctionSupport（可选）
      → exp4j
```

数学类别已注册真实执行函数。其他类别暂留在兼容求值器中，后续可按类别迁移；不应在这次结构迁移中同时重写全部解析语义。

## 实例与并发

```java
DefaultFormulaEngine engine = new DefaultFormulaEngine();
engine.getCustomFunctions().register("LOCAL", (params, data, mapping) -> "7");
Object value = engine.execute(FormulaContext.of("LOCAL()+1", new JSONObject())); // 8L
```

- 每个实例独立拥有注册表、脚本库和文件脚本缓存。静态 API 使用自己的默认实例。
- 自定义函数表写时复制。计算开始后持有稳定快照，注册/注销影响下一次计算。
- 自定义函数按注册顺序处理，内置函数处理仍在其之前；不保证覆盖同名内置函数。
- 函数名采用 Locale.ROOT 大写归一化；空名称和 null 实现立即报错。
- 上下文复制顶层 JSONObject 与映射；嵌套对象和自定义函数内部状态仍由接入方管理并发。

## 详细校验

```java
VerificationResult report = FormulaUtils.verifyDetailed("${price}*2", data, frontendResult);
if (report.isMatch()) {
    // 数值一致
}
// report.getStatus()、getBackendResult()、getMessage() 可用于诊断。
```

数值后端结果允许比较数字字符串；文本精确比较，不将 `"001"` 与数字 1 等同。
布尔值仅与布尔值相等，当前兼容公式中的逻辑结果通常仍为 0/1。
误差必须有限且非负；NaN、Infinity 和非法数字返回 INVALID_COMPARISON。
比较环节采用 BigDecimal 保留大整数差异，不意味着公式计算本身切换了精度模型。

## 脚本能力边界

- 仅执行开发者配置的可信脚本。此功能没有不可信脚本沙箱，也没有 CPU 执行时间限制。
- 纯 Java 计算无需脚本引擎。脚本能力取决于运行时是否安装可用引擎。
- 文件引擎缓存最多 32 项，按最近使用淘汰；共享引擎的 eval 串行执行。
- 远程读取连接超时 5 秒、读取超时 10 秒，内容最多 4 MiB 字符。超时不等于脚本执行超时。
- 清除缓存/全局库不强行终止已经取得引擎的调用；下一次调用按新状态获取。
- 保留旧脚本失败回退值，严格脚本错误策略应在独立兼容版本中引入。

## 验证和后续阶段

本地验证 Java 8 与 Java 17；CI 配置 Java 8、17、21，CI 配置不代表已在本机运行 Java 21。
Java 8 验证现有脚本测试；无脚本引擎时明确跳过脚本测试，纯 Java 测试继续执行。
普通 `mvn verify` 无需签名，发布签名限定在 `central-publish` profile。

本轮增加严格入口后，本地 Java 8 的 103 个测试全部通过，Java 17 的 103 个测试中 5 个脚本引擎用例跳过，
两种 JDK 均通过 verify。独立 JMH 模块已完成 12 组默认预热/fork 测量，数据与限制见[基准记录](benchmark-baseline.md)。

后续分阶段实施：统一引号/转义/括号扫描、类型化参数、分支惰性语义、其他函数类别注册化。
兼容入口继续遵循上述历史规则。当前新增独立[严格入口](strict-engine.md)，以类型化语法树实现首批 28 个函数，
支持 Map、BigDecimal、条件惰性求值、结构化错误、编译复用和有界缓存。
Formula.js 对照已固定为 4.6.1，[66 个已审核差分用例](formulajs-compatibility.md)纳入 CI；有限语料不代表跨语言全量兼容。

严格入口为本轮未发布源码能力，既有发布版本不自动具有这些类。错误、缺失字段、数字文本、逻辑类型和日期表示
都需要按严格模式迁移说明评审，不自动切换旧公式入口。

## 页面与 Wiki

当前 `index.html` 是唯一首页，四个网页共用 `assets/site.css` 与 `assets/site.js`。
默认浅色蓝白，用户可选择深色或跟随系统；支持简体、繁体、英文和 375px 移动端。
页面界面支持三语，Markdown 正文保留源文件语言。

可发布的 Wiki 位于 `docs/wiki/`，并纳入主仓库提交。相邻 `../formula-wiki` 是本地配套副本，
用 `python scripts/sync_wiki.py` 同步章节；不复制历史工作流、许可证和模板。
