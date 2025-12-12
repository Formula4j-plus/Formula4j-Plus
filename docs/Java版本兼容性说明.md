# Java版本兼容性说明

## 支持的Java版本

Formula4j-Plus **支持Java 8到Java 25**的运行时环境。

### 编译目标
- **编译版本**: Java 8
- **运行时支持**: Java 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25

## JavaScript引擎兼容性

### Java 8-14
- **内置引擎**: Nashorn
- **状态**: 开箱即用，无需额外配置
- **说明**: Nashorn是Java 8-14内置的JavaScript引擎

### Java 15-25
- **内置引擎**: 无（Nashorn已移除）
- **推荐方案**: GraalVM JavaScript引擎
- **状态**: 需要额外添加依赖

#### 使用GraalVM JavaScript引擎（可选）

如果需要在Java 15+环境中使用JavaScript功能，需要添加以下依赖：

```xml
<dependency>
    <groupId>org.graalvm.js</groupId>
    <artifactId>js</artifactId>
    <version>23.0.0</version>
</dependency>
<dependency>
    <groupId>org.graalvm.js</groupId>
    <artifactId>js-scriptengine</artifactId>
    <version>23.0.0</version>
</dependency>
```

**注意**: 
- 如果不使用JavaScript自定义函数功能，可以不添加这些依赖
- 核心Java函数功能在所有Java版本中都可以正常使用

## 版本检测

项目会自动检测Java版本并选择合适的JavaScript引擎：

```java
// 自动检测Java版本
String javaVersion = System.getProperty("java.version");
int majorVersion = getJavaMajorVersion(javaVersion);

// Java 8-14: 使用Nashorn
// Java 15+: 尝试使用GraalVM（如果可用）
```

## 功能兼容性

### 完全兼容的功能（所有Java版本）
- ✅ 所有249+个内置函数
- ✅ 字段映射和引用
- ✅ FormulaData结构
- ✅ Java Lambda自定义函数
- ✅ 表达式计算
- ✅ 字符串处理
- ✅ 日期时间函数
- ✅ 统计和数学函数

### 需要JavaScript引擎的功能（Java 15+需要额外依赖）
- ⚠️ JavaScript字符串自定义函数
- ⚠️ JavaScript文件加载
- ⚠️ Formula.js库一次性加载

## 测试建议

### 测试不同Java版本

```bash
# Java 8
java -version  # 应该显示 1.8.x

# Java 11
java -version  # 应该显示 11.x.x

# Java 17
java -version  # 应该显示 17.x.x

# Java 21
java -version  # 应该显示 21.x.x

# Java 25
java -version  # 应该显示 25.x.x
```

### 验证JavaScript引擎

```java
import com.formula.calculator.FormulaCalculator;

// 测试JavaScript引擎是否可用
try {
    String jsScript = "function(x) { return x * 2; }";
    FormulaCalculator.registerCustomFunction("DOUBLE", "js", jsScript);
    
    JSONObject data = new JSONObject();
    String formula = "DOUBLE(10)";
    Object result = FormulaCalculator.calculate(formula, data);
    System.out.println("JavaScript引擎可用: " + result);
} catch (Exception e) {
    System.out.println("JavaScript引擎不可用（Java 15+需要GraalVM依赖）");
}
```

## 常见问题

### Q: Java 15+中JavaScript功能不可用？

A: Java 15+移除了Nashorn引擎，需要添加GraalVM JavaScript依赖。如果不使用JavaScript功能，可以忽略此问题。

### Q: 如何检查当前Java版本？

A: 运行 `java -version` 查看版本信息。

### Q: 编译和运行可以使用不同Java版本吗？

A: 可以。编译使用Java 8，运行时可以使用Java 8-25的任意版本。

### Q: 性能在不同Java版本下有差异吗？

A: 核心计算功能性能基本一致。Java 11+在GC和性能优化方面有改进，可能会有轻微的性能提升。

## 推荐配置

### 生产环境
- **推荐**: Java 11 LTS 或 Java 17 LTS
- **原因**: 长期支持版本，稳定可靠

### 开发环境
- **推荐**: Java 8 或 Java 17
- **原因**: 兼容性好，工具链成熟

### 最新特性
- **推荐**: Java 21 LTS 或 Java 25
- **原因**: 最新特性和性能优化

---

**总结**: Formula4j-Plus在Java 8-25的所有版本中都可以正常运行，核心功能完全兼容。JavaScript功能在Java 15+需要额外依赖，但不影响主要功能的使用。

