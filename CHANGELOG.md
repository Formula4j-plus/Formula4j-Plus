# 更新日志

所有重要的项目变更都会记录在此文件中。

格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.0.0/)，
版本号遵循 [Semantic Versioning](https://semver.org/lang/zh-CN/)。

## [2.2.0] - 2025-01-XX

### 新增
- 支持一次性加载Formula.js库，所有函数可直接使用
- 新增 `loadFormulaJsLibrary()` 方法，支持从本地文件、URL或classpath加载
- 新增 `isFormulaJsLibraryLoaded()` 和 `getLoadedFormulaJsPath()` 方法
- 新增 `clearFormulaJsLibrary()` 方法
- 自动识别未在Java中实现的函数，从JS引擎调用
- 支持多个函数一起使用并相加

### 改进
- 优化JavaScript函数调用性能
- 改进函数参数解析逻辑
- 增强错误处理机制

### 文档
- 添加 `FormulaJsLibraryExample.java` 示例
- 更新 `USAGE.md` 使用文档
- 添加开源评估报告

## [2.1.0] - 2025-01-XX

### 新增
- 支持从文件加载JavaScript函数（本地、classpath、URL）
- 新增 `registerCustomFunctionFromFile()` 方法
- 新增 `clearFormulaJsCache()` 方法

### 改进
- 优化JavaScript引擎缓存机制
- 改进文件加载错误处理

## [2.0.0] - 2025-01-XX

### 新增
- 支持249+个函数，完全兼容Formula.js
- 支持自定义函数扩展（Java lambda和JavaScript）
- 支持JavaScript字符串函数注册
- 新增工具类：FormulaMathUtils、FormulaStatisticsUtils、FormulaDateUtils、FormulaStringUtils、FormulaParamUtils
- 支持财务函数、工程函数、分布函数、兼容性函数

### 改进
- 重构代码结构，提高可维护性
- 优化函数处理性能
- 改进错误处理和日志记录

### 修复
- 修复Java 8兼容性问题
- 修复VerifyError问题（StackMapTable）
- 修复NoClassDefFoundError问题（Fat JAR）

## [1.0.0] - 2024-XX-XX

### 新增
- 初始版本发布
- 基础公式计算功能
- 支持常用函数：SUM、AVERAGE、MAX、MIN、COUNT、IF、ROUND
- 支持FormulaData数据结构
- 支持字段映射（menuId -> enCode）

---

[2.2.0]: https://github.com/your-username/formula4j-plus/compare/v2.1.0...v2.2.0
[2.1.0]: https://github.com/your-username/formula4j-plus/compare/v2.0.0...v2.1.0
[2.0.0]: https://github.com/your-username/formula4j-plus/compare/v1.0.0...v2.0.0
[1.0.0]: https://github.com/your-username/formula4j-plus/releases/tag/v1.0.0

