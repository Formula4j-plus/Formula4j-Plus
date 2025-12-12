# 贡献指南

感谢您对 Formula4j-Plus 项目的关注！我们欢迎所有形式的贡献。

## 如何贡献

### 报告问题

如果您发现了bug或有功能建议，请通过以下方式提交：

1. **GitHub Issues**: 在 [Issues](https://github.com/Formula4j-plus/formula4j-plus/issues) 页面创建新issue
2. **Bug报告**: 请包含以下信息：
   - 问题描述
   - 复现步骤
   - 预期行为
   - 实际行为
   - 环境信息（Java版本、操作系统等）
   - 错误日志（如果有）

### 提交代码

1. **Fork 项目**: 点击 GitHub 上的 Fork 按钮
2. **创建分支**: 从 `main` 分支创建新分支
   ```bash
   git checkout -b feature/your-feature-name
   ```
3. **编写代码**: 
   - 遵循现有代码风格
   - 添加必要的注释和文档
   - 编写单元测试
4. **提交更改**: 
   ```bash
   git commit -m "feat: 添加新功能描述"
   ```
5. **推送分支**: 
   ```bash
   git push origin feature/your-feature-name
   ```
6. **创建 Pull Request**: 在 GitHub 上创建 PR，描述您的更改

### 代码规范

- **Java代码风格**: 遵循 Google Java Style Guide
- **命名规范**: 
  - 类名：大驼峰（PascalCase）
  - 方法名：小驼峰（camelCase）
  - 常量：全大写下划线分隔（UPPER_SNAKE_CASE）
- **注释**: 公共API必须添加JavaDoc注释
- **测试**: 新功能必须包含单元测试

### Commit 信息规范

我们使用 [Conventional Commits](https://www.conventionalcommits.org/) 规范：

- `feat`: 新功能
- `fix`: 修复bug
- `docs`: 文档更新
- `style`: 代码格式调整（不影响功能）
- `refactor`: 代码重构
- `test`: 测试相关
- `chore`: 构建过程或辅助工具的变动

示例：
```
feat: 添加ABS函数支持
fix: 修复SUM函数空值处理问题
docs: 更新README中的使用示例
```

### 开发环境设置

1. **克隆项目**:
   ```bash
   git clone https://github.com/Formula4j-plus/formula4j-plus.git
   cd formula4j-plus
   ```

2. **构建项目**:
   ```bash
   mvn clean compile
   ```

3. **运行测试**:
   ```bash
   mvn test
   ```

4. **打包项目**:
   ```bash
   mvn clean package
   ```

### 添加新函数

如果您想添加新的公式函数：

1. 在 `FunctionName` 枚举中添加函数名
2. 在 `FormulaCalculator` 中实现函数逻辑
3. 添加单元测试
4. 更新 `README.md` 中的函数列表

### 文档贡献

- 更新 `README.md`
- 更新 `USAGE.md`
- 添加代码示例
- 改进文档的可读性

### 问题讨论

如果您有任何问题或建议，欢迎：
- 在 GitHub Issues 中讨论
- 参与代码审查
- 帮助回答其他用户的问题

## 行为准则

- 尊重所有贡献者
- 接受建设性批评
- 专注于对项目最有利的事情
- 对其他社区成员表示同理心

感谢您的贡献！🎉

