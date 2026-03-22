# Formula4j-Plus

`Formula4j-Plus` 是一个 Java 公式计算引擎，主要用于在后端执行公式、复算前端结果，以及处理带字段映射的动态表达式。

项目本身偏工程落地，不是脚本语言或规则平台，核心目标是把前端已有的公式表达式在 Java 侧稳定跑起来。

## 适用场景

- 前后端共用一套公式表达式
- 后端复算前端提交的计算结果
- 按字段映射执行动态公式
- 在 Java 8 项目中集成轻量公式能力

## 已支持的能力

- 常见公式、逻辑、统计、文本、日期、财务等函数
- 字段引用：`${fieldName}`、`[fieldName]`
- `FormulaData` 结构与字段映射（`menuId -> enCode`）
- 自定义函数扩展
- `JSONObject` 输入
- 嵌套表达式与结果校验
- 编译目标 Java 8

## 安装

### Maven

```xml
<dependency>
    <groupId>com.formula</groupId>
    <artifactId>formula4j-plus</artifactId>
    <version>3.0.0</version>
</dependency>
```

## 快速开始

### 1. 直接计算字符串公式

```java
import com.alibaba.fastjson.JSONObject;
import com.formula.calculator.FormulaCalculator;

JSONObject data = new JSONObject();
data.put("price", 100);
data.put("quantity", 5);

Object result = FormulaCalculator.calculate("${price} * ${quantity}", data);
System.out.println(result); // 500.0
```

### 2. 使用 FormulaData

```java
import com.alibaba.fastjson.JSONObject;
import com.formula.calculator.FormulaCalculator;
import com.formula.calculator.model.FieldMark;
import com.formula.calculator.model.FormulaData;

FormulaData formulaData = new FormulaData();
formulaData.setTxt("SUM(单价,数量) * (1 - 折扣率)");
formulaData.setMarks(java.util.Arrays.asList(
    new FieldMark("price", "单价"),
    new FieldMark("quantity", "数量"),
    new FieldMark("discount", "折扣率")
));

JSONObject data = new JSONObject();
data.put("price", 100);
data.put("quantity", 5);
data.put("discount", 0.1);

Object result = FormulaCalculator.calculate(formulaData, data);
```

### 3. 校验前端结果

```java
import com.alibaba.fastjson.JSONObject;
import com.formula.calculator.FormulaUtils;

JSONObject data = new JSONObject();
data.put("price", 100);
data.put("quantity", 5);

boolean ok = FormulaUtils.verify("${price} * ${quantity}", data, 500);
```

## 字段映射

`FormulaData` 里的公式通常使用展示字段名，运行时再映射到实际数据字段。

例如：

- 公式：`SUM(单价,数量)`
- 映射：`单价 -> price`，`数量 -> quantity`
- 数据：`{"price":100,"quantity":5}`

传入 `JSONObject` 时，key 应使用 `enCode`，不是 `menuId`。

## 自定义函数

```java
FormulaCalculator.registerCustomFunction("MYFUNC", (params, data, fieldMapping) -> {
    return params;
});

Object result = FormulaCalculator.calculate("MYFUNC(${field1}, ${field2})", data);

FormulaCalculator.unregisterCustomFunction("MYFUNC");
```

## 支持范围

当前版本已覆盖项目里常见的函数类型，包括：

- 数学与统计
- 逻辑
- 文本
- 日期时间
- 查找与引用
- 财务
- 工程与分布
- 部分旧版 Excel 兼容函数名

完整函数名可直接查看源码：

- 函数入口：`src/main/java/com/formula/calculator/FormulaCalculator.java`
- 函数枚举：`src/main/java/com/formula/calculator/FunctionName.java`
- 示例代码：`src/main/java/com/formula/calculator/example/FormulaExample.java`
- 测试用例：`src/test/java/com/formula/calculator/`

## 发布到 Maven 仓库

当前项目已补充 Maven Central 所需的基础元数据、源码包、Javadoc 包、GPG 签名和发布插件配置。

### 本地发布

```bash
mvn clean deploy -Pcentral-publish
```

发布前需要准备：

- Sonatype Central Portal 的 `MAVEN_CENTRAL_USERNAME` / `MAVEN_CENTRAL_TOKEN`
- GPG 私钥与口令
- Maven `settings.xml` 中 `serverId=central` 的认证配置

### GitHub Actions Secrets

工作流依赖以下 Secrets：

- `MAVEN_CENTRAL_USERNAME`
- `MAVEN_CENTRAL_TOKEN`
- `MAVEN_GPG_PRIVATE_KEY`
- `MAVEN_GPG_PASSPHRASE`

## 构建

```bash
mvn clean compile
mvn test
mvn clean package
mvn clean install
```

## 注意事项

- 字段值为 `null` 时按 `0` 处理
- 字符串数值会尝试转换为数字
- 默认校验误差为 `0.0001`
- 返回值可能是 `Long`、`Double` 或 `String`

## 版本

当前版本：`3.0.0`

## 许可证

This project is licensed under the Business Source License 1.1 (BUSL-1.1). See [LICENSE](LICENSE) for details.

- 非生产使用：允许用于开发、测试、评估和教育目的
- 生产使用：需要获得商业许可，请联系 1498610052@qq.com
- 变更日期：2029-01-01 后自动转换为 Apache License 2.0

Copyright 2025 WanShen
