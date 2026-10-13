# 使用指南

> 当前维护说明：计算结果类型、详细校验、实例隔离和脚本边界见[引擎行为契约](docs/engine-contract.md)，配套文档见 [Wiki](docs/wiki/README.md)。

本文补充 README 中没有展开的几种用法，内容以当前项目代码为准。

当前源码新增[严格模式](docs/strict-engine.md)：支持 Map、BigDecimal、编译复用、惰性 IF 和原公式错误位置。
这是未发布的新入口；旧静态 API 保持历史语义。跨语言行为见[Formula.js 4.6.1 实测矩阵](docs/formulajs-compatibility.md)。

## 1. 基本计算

```java
import com.alibaba.fastjson.JSONObject;
import com.formula.calculator.FormulaCalculator;

JSONObject data = new JSONObject();
data.put("price", 100);
data.put("quantity", 5);

Object result = FormulaCalculator.calculate("${price} * ${quantity}", data);
```

## 2. 使用 FormulaData

适合前端传入公式文本和字段映射的场景。

```java
import com.alibaba.fastjson.JSONObject;
import com.formula.calculator.FormulaCalculator;
import com.formula.calculator.model.FieldMark;
import com.formula.calculator.model.FormulaData;

FormulaData formulaData = new FormulaData();
formulaData.setTxt("SUM(单价,数量)");
formulaData.setMarks(java.util.Arrays.asList(
    new FieldMark("price", "单价"),
    new FieldMark("quantity", "数量")
));

JSONObject data = new JSONObject();
data.put("price", 100);
data.put("quantity", 20);

Object result = FormulaCalculator.calculate(formulaData, data);
```

## 3. 从 JSON 构造 FormulaData

```java
import com.alibaba.fastjson.JSONObject;
import com.formula.calculator.model.FormulaData;

String jsonStr = "{\n" +
    "  \"txt\": \"SUM(字段1,字段2)\",\n" +
    "  \"marks\": [\n" +
    "    {\"enCode\": \"field1\", \"menuId\": \"字段1\"},\n" +
    "    {\"enCode\": \"field2\", \"menuId\": \"字段2\"}\n" +
    "  ]\n" +
    "}";

FormulaData formulaData = FormulaData.fromJSON(JSONObject.parseObject(jsonStr));
```

## 4. 校验前端结果

```java
import com.alibaba.fastjson.JSONObject;
import com.formula.calculator.FormulaUtils;

JSONObject data = new JSONObject();
data.put("field1", 10);
data.put("field2", 20);

boolean verified = FormulaUtils.verify("${field1} + ${field2}", data, 30);
```

## 5. 自定义函数

```java
FormulaCalculator.registerCustomFunction("MYFUNC", (params, data, fieldMapping) -> {
    return params;
});

Object result = FormulaCalculator.calculate("MYFUNC(${field1}, ${field2})", data);

FormulaCalculator.unregisterCustomFunction("MYFUNC");
```

## 6. Formula.js 库加载

当前项目代码中保留了加载 Formula.js / JavaScript 自定义函数的能力，但这部分更依赖运行环境里的脚本引擎可用性。

如果你要使用这部分能力，建议先在自己的运行环境中做验证，再决定是否在线上启用。

## 7. 字段映射说明

- `menuId`：公式里展示给用户的字段名
- `enCode`：数据里真正取值的 key

例如：

- 公式：`SUM(单价,数量)`
- 映射：`单价 -> price`，`数量 -> quantity`
- 数据：`{"price":100,"quantity":20}`

注意：传入 `JSONObject` 时，key 应使用 `enCode`。
