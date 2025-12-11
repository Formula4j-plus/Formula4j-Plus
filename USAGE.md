# 使用指南

## 快速开始：一次性加载Formula.js库

如果你需要一次性加载整个Formula.js库，然后直接使用所有函数，可以使用以下方式：

```java
import com.alibaba.fastjson.JSONObject;
import com.formula.calculator.FormulaCalculator;

// 1. 一次性加载Formula.js库（支持本地文件、URL或classpath）
FormulaCalculator.loadFormulaJsLibrary("file:///D:/formula.js");
// 或从URL加载
// FormulaCalculator.loadFormulaJsLibrary("https://cdn.jsdelivr.net/npm/@formulajs/formulajs/lib/browser/formula.min.js");
// 或从类路径加载
// FormulaCalculator.loadFormulaJsLibrary("classpath:formula.js");

// 2. 加载后，所有Formula.js中的函数都可以直接使用
JSONObject data = new JSONObject();
String formula = "SUM(1, 2, 3) + ABS(-5) + SQRT(16)";
Object result = FormulaCalculator.calculate(formula, data);
System.out.println("结果: " + result); // 输出: 15.0

// 3. 支持多个函数一起使用并相加
String complexFormula = "SUM(10, 20) + POWER(2, 3) + ROUND(3.14159, 2)";
Object complexResult = FormulaCalculator.calculate(complexFormula, data);
System.out.println("结果: " + complexResult); // 输出: 41.14
```

**说明**：
- ✅ 一次加载后，所有Formula.js中的函数都可以直接使用，无需单独注册
- ✅ 支持多个函数一起使用并相加
- ✅ 自动识别未在Java中实现的函数，从JS引擎调用
- ✅ 与Java实现的函数无缝混合使用

---

## 前端数据结构

前端传来的数据结构格式：

```json
{
  "txt": "SUM(字段1,字段2)",
  "marks": [
    {
      "enCode": "field1",
      "menuId": "字段1",
      "form": {
        "ch": 0,
        "line": 0,
        "stick": null
      }
    },
    {
      "enCode": "field2",
      "menuId": "字段2",
      "form": {
        "ch": 0,
        "line": 0,
        "stick": null
      }
    }
  ]
}
```

## 后端使用示例

### 1. 接收前端数据并计算

```java
import com.alibaba.fastjson.JSONObject;
import com.formula.calculator.FormulaCalculator;
import com.formula.calculator.model.FormulaData;

// 接收前端传来的JSON字符串
String frontendJson = "{\n" +
    "  \"txt\": \"SUM(字段1,字段2)\",\n" +
    "  \"marks\": [\n" +
    "    {\"enCode\": \"field1\", \"menuId\": \"字段1\", \"form\": {\"ch\": 0, \"line\": 0, \"stick\": null}},\n" +
    "    {\"enCode\": \"field2\", \"menuId\": \"字段2\", \"form\": {\"ch\": 0, \"line\": 0, \"stick\": null}}\n" +
    "  ]\n" +
    "}";

// 解析为FormulaData
JSONObject json = JSONObject.parseObject(frontendJson);
FormulaData formulaData = FormulaData.fromJSON(json);

// 准备数据（注意：key使用enCode，不是menuId）
JSONObject data = new JSONObject();
data.put("field1", 10);  // enCode
data.put("field2", 20);  // enCode

// 计算公式
Object result = FormulaCalculator.calculate(formulaData, data);
System.out.println("计算结果: " + result); // 输出: 30.0
```

### 2. 复杂公式示例

```java
// 前端传来的复杂公式
String complexFormula = "{\n" +
    "  \"txt\": \"IF(AND(销售额 >= 目标, 销售额 <= 100000), 1000, IF(销售额 > 100000, 2000, 500))\",\n" +
    "  \"marks\": [\n" +
    "    {\"enCode\": \"sales\", \"menuId\": \"销售额\", \"form\": {\"ch\": 0, \"line\": 0, \"stick\": null}},\n" +
    "    {\"enCode\": \"target\", \"menuId\": \"目标\", \"form\": {\"ch\": 0, \"line\": 0, \"stick\": null}}\n" +
    "  ]\n" +
    "}";

JSONObject json = JSONObject.parseObject(complexFormula);
FormulaData formulaData = FormulaData.fromJSON(json);

JSONObject data = new JSONObject();
data.put("sales", 50000);
data.put("target", 40000);

Object result = FormulaCalculator.calculate(formulaData, data);
```

### 3. 验证前端计算结果

```java
import com.formula.calculator.FormulaUtils;

// 前端使用Formula.js计算的结果
double frontendResult = 30.8;

// 后端验证
boolean verified = FormulaUtils.verify(formulaData, data, frontendResult);
if (verified) {
    System.out.println("计算结果一致，验证通过");
} else {
    System.out.println("计算结果不一致，需要重新计算");
    // 重新计算
    Object backendResult = FormulaCalculator.calculate(formulaData, data);
    System.out.println("后端计算结果: " + backendResult);
}
```

### 4. 使用统计函数

```java
String statsFormula = "{\n" +
    "  \"txt\": \"STDEV(数据1,数据2,数据3,数据4,数据5)\",\n" +
    "  \"marks\": [\n" +
    "    {\"enCode\": \"data1\", \"menuId\": \"数据1\", \"form\": {\"ch\": 0, \"line\": 0, \"stick\": null}},\n" +
    "    {\"enCode\": \"data2\", \"menuId\": \"数据2\", \"form\": {\"ch\": 0, \"line\": 0, \"stick\": null}},\n" +
    "    {\"enCode\": \"data3\", \"menuId\": \"数据3\", \"form\": {\"ch\": 0, \"line\": 0, \"stick\": null}},\n" +
    "    {\"enCode\": \"data4\", \"menuId\": \"数据4\", \"form\": {\"ch\": 0, \"line\": 0, \"stick\": null}},\n" +
    "    {\"enCode\": \"data5\", \"menuId\": \"数据5\", \"form\": {\"ch\": 0, \"line\": 0, \"stick\": null}}\n" +
    "  ]\n" +
    "}";

JSONObject json = JSONObject.parseObject(statsFormula);
FormulaData formulaData = FormulaData.fromJSON(json);

JSONObject data = new JSONObject();
data.put("data1", 10);
data.put("data2", 20);
data.put("data3", 30);
data.put("data4", 40);
data.put("data5", 50);

Object result = FormulaCalculator.calculate(formulaData, data);
System.out.println("标准差: " + result);
```

### 5. 使用数学函数

```java
String mathFormula = "{\n" +
    "  \"txt\": \"SQRT(POWER(底数,指数))\",\n" +
    "  \"marks\": [\n" +
    "    {\"enCode\": \"base\", \"menuId\": \"底数\", \"form\": {\"ch\": 0, \"line\": 0, \"stick\": null}},\n" +
    "    {\"enCode\": \"exp\", \"menuId\": \"指数\", \"form\": {\"ch\": 0, \"line\": 0, \"stick\": null}}\n" +
    "  ]\n" +
    "}";

JSONObject json = JSONObject.parseObject(mathFormula);
FormulaData formulaData = FormulaData.fromJSON(json);

JSONObject data = new JSONObject();
data.put("base", 4);
data.put("exp", 2);

Object result = FormulaCalculator.calculate(formulaData, data);
System.out.println("结果: " + result); // SQRT(4^2) = 4.0
```

## 字段映射说明

### 字段映射关系

- **menuId**: 公式中使用的字段显示名称（如"单价"、"数量"）
- **enCode**: 数据JSONObject中使用的字段编码（如"price"、"quantity"）

### 映射流程

1. 前端公式中使用menuId（显示名称）：`SUM(单价,数量)`
2. 后端通过marks数组建立映射：`单价 -> price`, `数量 -> quantity`
3. 后端从数据中根据enCode获取值：`data.get("price")`, `data.get("quantity")`
4. 计算结果

### 注意事项

1. **数据key必须使用enCode**：`data.put("price", 100)` ✅，不要使用`data.put("单价", 100)` ❌
2. **公式中使用menuId**：`SUM(单价,数量)` ✅
3. **marks数组必须包含所有公式中使用的字段**

## 常见问题

### Q: 如果字段值为null怎么办？

A: 字段值为null时，会自动使用0替代，并记录警告日志。

### Q: 如何支持自定义函数？

A: 使用`FormulaCalculator.registerCustomFunction()`注册自定义函数：

```java
FormulaCalculator.registerCustomFunction("MYFUNC", (params, data, fieldMapping) -> {
    // 处理参数
    String[] parts = params.split(",");
    // 执行自定义逻辑
    return "计算结果";
});
```

### Q: 如何一次性加载Formula.js库，然后直接使用所有函数？

A: 使用`FormulaCalculator.loadFormulaJsLibrary()`一次性加载整个Formula.js库，后续所有函数都可以直接使用：

```java
// 1. 一次性加载Formula.js库（从本地文件、URL或classpath）
FormulaCalculator.loadFormulaJsLibrary("file:///D:/formula.js");
// 或从URL加载
FormulaCalculator.loadFormulaJsLibrary("https://cdn.jsdelivr.net/npm/@formulajs/formulajs/lib/browser/formula.min.js");
// 或从类路径加载
FormulaCalculator.loadFormulaJsLibrary("classpath:formula.js");

// 2. 加载后，所有Formula.js中的函数都可以直接使用，无需单独注册
String formula = "SUM(1, 2, 3) + ABS(-5) + SQRT(16)";
Object result = FormulaCalculator.calculate(formula, data);
System.out.println("结果: " + result); // 输出: 15.0

// 3. 支持多个函数一起使用并相加
String complexFormula = "SUM(10, 20) + POWER(2, 3) + ROUND(3.14159, 2)";
Object complexResult = FormulaCalculator.calculate(complexFormula, data);
System.out.println("结果: " + complexResult); // 输出: 41.14

// 4. 检查库是否已加载
boolean loaded = FormulaCalculator.isFormulaJsLibraryLoaded();
String path = FormulaCalculator.getLoadedFormulaJsPath();

// 5. 清除已加载的库（可选）
FormulaCalculator.clearFormulaJsLibrary();
```

**优势**：
- ✅ 一次加载，所有函数可用
- ✅ 无需为每个函数单独注册
- ✅ 支持多个函数一起使用并相加
- ✅ 自动识别未在Java中实现的函数，从JS引擎调用
- ✅ 与Java实现的函数无缝混合使用

### Q: 如何验证计算结果精度？

A: 使用`FormulaUtils.verify()`方法，可以指定误差范围：

```java
// 默认误差0.0001
boolean verified = FormulaUtils.verify(formulaData, data, frontendResult);

// 自定义误差范围
boolean verified = FormulaUtils.verify(formulaData, data, frontendResult, 0.01);
```

### Q: 支持哪些数据类型？

A: 支持Number类型（Integer、Long、Double、Float等）和String类型的数值。其他类型会尝试转换为double。

