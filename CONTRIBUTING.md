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
2. `FormulaCalculator` 或对应工具类中补实现
3. `src/test/java/com/formula/calculator/` 中补测试
4. 文档按需更新

## 提交建议

- 保持现有代码风格
- 优先做小步修改，避免大范围无关重构
- 新功能尽量附带测试
- 文档修改保持和当前项目能力一致

感谢你的贡献。