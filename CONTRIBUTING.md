# 贡献指南

感谢你关注 `Formula4j-Plus`。

这个项目目前以维护核心公式计算能力为主，欢迎围绕以下方向提交 issue 或 PR：

- 公式兼容性问题
- 函数实现错误
- 字段映射相关问题
- 文档和示例改进
- 测试补充与回归修复

## 提交问题

如果你发现 bug 或有功能建议，建议在仓库 Issues 中提供：

- 问题描述
- 复现步骤
- 预期结果
- 实际结果
- Java 版本、操作系统
- 相关公式和输入数据

## 提交代码

1. Fork 仓库
2. 从当前主分支创建新分支
3. 完成修改并补充测试
4. 本地执行：

```bash
mvn test
```

5. 发起 Pull Request

## 开发说明

当前项目核心入口主要在以下位置：

- `src/main/java/com/formula/calculator/FormulaCalculator.java`
- `src/main/java/com/formula/calculator/FormulaUtils.java`
- `src/main/java/com/formula/calculator/model/`
- `src/main/java/com/formula/calculator/utils/`

如果你要新增函数，通常需要同步处理：

1. `FunctionName` 中增加函数名
2. 数学函数在 `functions/math/MathFunctions` 注册；其他类别在兼容求值流程或对应工具类补实现
3. `src/test/java/com/formula/calculator/` 中补测试
4. 文档按需更新

## 提交建议

- 保持现有代码风格
- 优先做小步修改，避免大范围无关重构
- 新功能尽量附带测试
- 文档修改保持和当前项目能力一致

感谢你的贡献。

网站视觉维护 `assets/site.css` 与 `assets/site.js`；Wiki 章节维护 `docs/wiki/`。提交前运行 `python scripts/check_docs.py`，关联本地 Wiki 使用 `python scripts/sync_wiki.py` 同步。

## 严格模式和差分契约

严格函数在 `core/strict/StrictFunctions` 中实现并描述元数据，类型化解析位于 `ExpressionParser`。
增加函数或调整语义时同步更新严格测试、接入文档及 `src/test/resources/compatibility/formulajs-4.6.1.json`。
语料同时保存 Formula.js 和 Java 两端的已审核结果与一致/差异/报错/未支持分类。
不要仅为了通过测试而改写期望值；先确认业务语义，说明变化理由，再更新契约。

```sh
mvn -B -Dtest=FormulaJsDifferentialTest -Duser.timezone=UTC -Dformula.differential.output=java-report.json test
npm --prefix tools/compatibility ci --ignore-scripts
node tools/compatibility/compare.cjs java-report.json --report docs/formulajs-compatibility.md --catalog docs/strict-functions.json
```

生成的矩阵与函数目录纳入 Git；CI 检查它们是否与实际执行一致。
Node/Formula.js 仅用于开发验证，不是 Java 计算库的新运行时依赖。
[严格模式说明](docs/strict-engine.md)与[差分矩阵](docs/formulajs-compatibility.md)记录适用范围。

性能修改使用独立[JMH 开发基准](tools/benchmark/README.md)，先检查同一输入的结果，再比较时间、误差与分配量。
