# 更新日志

所有重要的项目变更都会记录在此文件中。

格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.0.0/)，
版本号遵循 [Semantic Versioning](https://semver.org/lang/zh-CN/)。

## [3.0.0] - 2025-01-XX

### 新增
- 扩展函数覆盖范围，补充文本、日期时间、查找引用、信息、财务、工程、分布等函数
- 增加部分旧版 Excel 兼容函数名支持
- 增加更多示例与测试用例

### 改进
- 调整 README 和开源文档结构，便于对外发布
- 优化字段映射、自定义函数和结果校验相关说明
- 补充源码打包、混淆和发布相关配置

## [2.2.0] - 2025-01-XX

### 新增
- 支持一次性加载 Formula.js 库，所有函数可直接使用
- 新增 `loadFormulaJsLibrary()`、`isFormulaJsLibraryLoaded()`、`getLoadedFormulaJsPath()`、`clearFormulaJsLibrary()`
- 自动识别未在 Java 中实现的函数，并尝试从 JS 引擎调用
- 支持多个函数一起使用并相加

### 改进
- 优化 JavaScript 函数调用性能
- 改进函数参数解析逻辑
- 增强错误处理机制

### 文档
- 添加 `FormulaJsLibraryExample.java` 示例
- 更新 `USAGE.md` 使用文档

## [2.1.0] - 2025-01-XX

### 新增
- 支持从文件加载 JavaScript 函数（本地、classpath、URL）
- 新增 `registerCustomFunctionFromFile()` 方法
- 新增 `clearFormulaJsCache()` 方法

### 改进
- 优化 JavaScript 引擎缓存机制
- 改进文件加载错误处理

## [2.0.0] - 2025-01-XX

### 新增
- 支持大量常用函数，兼容 Formula.js 常见用法
- 支持自定义函数扩展（Java lambda 和 JavaScript）
- 支持 JavaScript 字符串函数注册
- 新增工具类：`FormulaMathUtils`、`FormulaStatisticsUtils`、`FormulaDateUtils`、`FormulaStringUtils`、`FormulaParamUtils`
- 支持财务函数、工程函数、分布函数、兼容性函数

### 改进
- 重构代码结构，提高可维护性
- 优化函数处理性能
- 改进错误处理和日志记录

### 修复
- 修复 Java 8 兼容性问题
- 修复 VerifyError 问题（StackMapTable）
- 修复 NoClassDefFoundError 问题（Fat JAR）

## [1.0.0] - 2024-XX-XX

### 新增
- 初始版本发布
- 基础公式计算功能
- 支持常用函数：`SUM`、`AVERAGE`、`MAX`、`MIN`、`COUNT`、`IF`、`ROUND`
- 支持 `FormulaData` 数据结构
- 支持字段映射（`menuId -> enCode`）
