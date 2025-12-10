# Formula Calculator

Java公式计算引擎，支持Formula.js语法，用于后端验证前端计算结果。

## 功能特性

- ✅ 支持Formula.js常用语法
- ✅ 支持字段引用：`${fieldName}` 或 `[fieldName]` 或直接字段名
- ✅ **支持60+个函数**，包括：
  - 常用函数：SUM、AVERAGE、MAX、MIN、COUNT、CONCATNAME、DATE等
  - 逻辑函数：IF、AND、OR、NOT、XOR、SWITCH、IFERROR、IFNA、IFS等
  - 统计函数：STDEV、VAR、MEDIAN、MODE、AVEDEV等
  - 数学函数：PI、SQRT、SUMSQ、PRODUCT、GCD、LCM、FACT、三角函数等
- ✅ 支持自定义函数扩展（TESTABC已注册）
- ✅ 支持前端数据结构（FormulaData）
- ✅ 支持字段映射（menuId -> enCode）
- ✅ 提供前端计算结果验证功能
- ✅ 支持JSONObject数据输入

## 快速开始

### 1. 基本使用

```java
import com.alibaba.fastjson.JSONObject;
import com.formula.calculator.FormulaCalculator;

// 准备数据
JSONObject data = new JSONObject();
data.put("price", 100);
data.put("quantity", 5);

// 计算公式
String formula = "${price} * ${quantity}";
Object result = FormulaCalculator.calculate(formula, data);
System.out.println("结果: " + result); // 输出: 结果: 500.0
```

### 2. 使用FormulaData结构（推荐）

根据前端数据结构使用：

```java
import com.formula.calculator.model.FormulaData;
import com.formula.calculator.model.FieldMark;
import com.formula.calculator.FormulaCalculator;

// 构建FormulaData（模拟前端数据结构）
FormulaData formulaData = new FormulaData();
formulaData.setTxt("SUM(单价,数量) * (1 - 折扣率)");

// 添加字段标记
List<FieldMark> marks = new ArrayList<>();
marks.add(new FieldMark("price", "单价"));      // enCode: price, menuId: 单价
marks.add(new FieldMark("quantity", "数量"));   // enCode: quantity, menuId: 数量
marks.add(new FieldMark("discount", "折扣率")); // enCode: discount, menuId: 折扣率
formulaData.setMarks(marks);

// 准备数据（使用enCode作为key）
JSONObject data = new JSONObject();
data.put("price", 100);
data.put("quantity", 5);
data.put("discount", 0.1);

// 计算公式
Object result = FormulaCalculator.calculate(formulaData, data);
```

### 3. 从JSON创建FormulaData

```java
import com.alibaba.fastjson.JSONObject;
import com.formula.calculator.model.FormulaData;

// 前端传来的JSON数据
String jsonStr = "{\n" +
    "  \"txt\": \"SUM(字段1,字段2)\",\n" +
    "  \"marks\": [\n" +
    "    {\"enCode\": \"field1\", \"menuId\": \"字段1\", \"form\": {\"ch\": 0, \"line\": 0, \"stick\": null}},\n" +
    "    {\"enCode\": \"field2\", \"menuId\": \"字段2\", \"form\": {\"ch\": 0, \"line\": 0, \"stick\": null}}\n" +
    "  ]\n" +
    "}";

JSONObject json = JSONObject.parseObject(jsonStr);
FormulaData formulaData = FormulaData.fromJSON(json);

// 准备数据
JSONObject data = new JSONObject();
data.put("field1", 10);
data.put("field2", 20);

// 计算
Object result = FormulaCalculator.calculate(formulaData, data);
```

## 支持的函数

### 常用函数

#### SUM - 求和
```java
String formula = "SUM(${field1}, ${field2}, ${field3})";
// 或使用FormulaData: "SUM(字段1,字段2,字段3)"
```

#### AVERAGE - 平均值
```java
String formula = "AVERAGE(${field1}, ${field2}, ${field3})";
```

#### MAX - 最大值
```java
String formula = "MAX(${field1}, ${field2}, ${field3})";
```

#### MIN - 最小值
```java
String formula = "MIN(${field1}, ${field2}, ${field3})";
```

#### COUNT - 计数
```java
String formula = "COUNT(${field1}, ${field2}, ${field3})";
```

#### COUNTIF - 条件计数
```java
String formula = "COUNTIF(${field1}, ${field2}, ${field3}, \">10\")";
```

#### SUMIF - 条件求和
```java
String formula = "SUMIF(${range}, ${criteria})";
```

#### CONCATNAME - 连接名称（字符串连接）
```java
String formula = "CONCATNAME(\"Hello\", \"World\")";
```

#### DATE - 返回特定日期
```java
String formula = "DATE(2024, 1, 1)"; // 返回从1900-01-01开始的天数
```

#### IF - 条件判断
```java
String formula = "IF(${score} >= 60, 1, 0)";
```

#### IFS - 多条件判断
```java
String formula = "IFS(${score} >= 90, \"优秀\", ${score} >= 60, \"及格\", \"不及格\")";
```

#### ROUND - 四舍五入
```java
String formula = "ROUND(${value}, 2)";
```

#### ROUNDUP - 向上取整
```java
String formula = "ROUNDUP(${value}, 2)";
```

#### ROUNDDOWN - 向下取整
```java
String formula = "ROUNDDOWN(${value}, 2)";
```

#### TESTABC - 自定义函数（已注册，可通过自定义函数机制扩展）
```java
String formula = "TESTABC(${value})";
```

### 逻辑函数

#### AND - 逻辑与
```java
String formula = "AND(${a} > 5, ${b} > 10)";
```

#### OR - 逻辑或
```java
String formula = "OR(${a} < 5, ${b} > 10)";
```

#### NOT - 逻辑非
```java
String formula = "NOT(${a} < 5)";
```

#### XOR - 异或
```java
String formula = "XOR(${a} > 5, ${b} > 10)";
```

#### SWITCH - 多值选择
```java
String formula = "SWITCH(${value}, 1, \"One\", 2, \"Two\", \"Other\")";
```

#### IFERROR - 如果表达式错误，返回指定值
```java
String formula = "IFERROR(${expression}, ${errorValue})";
```

#### IFNA - 如果值为#N/A，返回指定值
```java
String formula = "IFNA(${value}, ${defaultValue})";
```

### 统计函数

#### STDEV - 标准差（样本）
```java
String formula = "STDEV(${field1}, ${field2}, ${field3})";
```

#### STDEVP - 标准差（总体）
```java
String formula = "STDEVP(${field1}, ${field2}, ${field3})";
```

#### VAR - 方差（样本）
```java
String formula = "VAR(${field1}, ${field2}, ${field3})";
```

#### VARP - 方差（总体）
```java
String formula = "VARP(${field1}, ${field2}, ${field3})";
```

#### MEDIAN - 中位数
```java
String formula = "MEDIAN(${field1}, ${field2}, ${field3})";
```

#### MODE - 众数
```java
String formula = "MODE(${field1}, ${field2}, ${field3})";
```

#### AVEDEV - 平均绝对偏差
```java
String formula = "AVEDEV(${field1}, ${field2}, ${field3})";
```

### 数学函数

#### PI - 圆周率
```java
String formula = "PI()"; // 返回3.141592653589793
```

#### ABS - 绝对值
```java
String formula = "ABS(${value})";
```

#### SQRT - 平方根
```java
String formula = "SQRT(${value})";
```

#### SQRTPI - 返回(数字*PI)的平方根
```java
String formula = "SQRTPI(${value})";
```

#### SUMSQ - 平方和
```java
String formula = "SUMSQ(${value1}, ${value2}, ${value3})";
```

#### PRODUCT - 乘积
```java
String formula = "PRODUCT(${value1}, ${value2}, ${value3})";
```

#### QUOTIENT - 商（整数除法）
```java
String formula = "QUOTIENT(${numerator}, ${denominator})";
```

#### POWER - 幂运算
```java
String formula = "POWER(${base}, ${exponent})";
```

#### LOG - 常用对数（以10为底）
```java
String formula = "LOG(${value})";
```

#### LN - 自然对数
```java
String formula = "LN(${value})";
```

#### EXP - 自然指数
```java
String formula = "EXP(${value})";
```

#### CEILING - 向上取整
```java
String formula = "CEILING(${value})";
```

#### CEILING.MATH - 向上取整到指定倍数
```java
String formula = "CEILING.MATH(${number}, ${significance})";
```

#### FLOOR - 向下取整
```java
String formula = "FLOOR(${value})";
```

#### FLOOR.MATH - 向下取整到指定倍数
```java
String formula = "FLOOR.MATH(${number}, ${significance})";
```

#### MROUND - 四舍五入到指定倍数
```java
String formula = "MROUND(${number}, ${multiple})";
```

#### MOD - 取余
```java
String formula = "MOD(${dividend}, ${divisor})";
```

#### INT - 向下取整到整数
```java
String formula = "INT(${value})";
```

#### TRUNC - 截断小数部分
```java
String formula = "TRUNC(${value}, ${decimals})";
```

#### SIGN - 返回数字的符号
```java
String formula = "SIGN(${value})"; // 正数返回1，负数返回-1，0返回0
```

#### RAND - 返回0到1之间的随机数
```java
String formula = "RAND()";
```

#### RANDBETWEEN - 返回指定范围内的随机整数
```java
String formula = "RANDBETWEEN(${bottom}, ${top})";
```

#### GCD - 最大公约数
```java
String formula = "GCD(${value1}, ${value2}, ${value3})";
```

#### LCM - 最小公倍数
```java
String formula = "LCM(${value1}, ${value2}, ${value3})";
```

#### FACT - 阶乘
```java
String formula = "FACT(${n})"; // FACT(5) = 120
```

#### FACTDOUBLE - 双阶乘
```java
String formula = "FACTDOUBLE(${n})";
```

#### COMBIN - 组合数 C(n,k)
```java
String formula = "COMBIN(${n}, ${k})"; // COMBIN(5, 2) = 10
```

#### PERMUT - 排列数 P(n,k)
```java
String formula = "PERMUT(${n}, ${k})"; // PERMUT(5, 2) = 20
```

#### DEGREES - 弧度转角度
```java
String formula = "DEGREES(${radians})"; // DEGREES(PI()) = 180
```

#### RADIANS - 角度转弧度
```java
String formula = "RADIANS(${degrees})"; // RADIANS(180) = PI
```

#### SIN - 正弦
```java
String formula = "SIN(${value})";
```

#### COS - 余弦
```java
String formula = "COS(${value})";
```

#### TAN - 正切
```java
String formula = "TAN(${value})";
```

#### ASIN - 反正弦
```java
String formula = "ASIN(${value})";
```

#### ACOS - 反余弦
```java
String formula = "ACOS(${value})";
```

#### ATAN - 反正切
```java
String formula = "ATAN(${value})";
```

#### ATAN2 - 返回两个参数的反正切值
```java
String formula = "ATAN2(${y}, ${x})";
```

#### SINH - 双曲正弦
```java
String formula = "SINH(${value})";
```

#### COSH - 双曲余弦
```java
String formula = "COSH(${value})";
```

#### TANH - 双曲正切
```java
String formula = "TANH(${value})";
```

#### ASINH - 反双曲正弦
```java
String formula = "ASINH(${value})";
```

#### ACOSH - 反双曲余弦
```java
String formula = "ACOSH(${value})";
```

#### ATANH - 反双曲正切
```java
String formula = "ATANH(${value})";
```

## 自定义函数

支持注册自定义函数：

```java
// 注册自定义函数
FormulaCalculator.registerCustomFunction("MYFUNC", (params, data, fieldMapping) -> {
    // 处理参数
    String[] parts = params.split(",");
    // 执行自定义逻辑
    return "计算结果";
});

// 使用自定义函数
String formula = "MYFUNC(${field1}, ${field2})";
Object result = FormulaCalculator.calculate(formula, data);

// 移除自定义函数
FormulaCalculator.unregisterCustomFunction("MYFUNC");
```

## 字段映射

FormulaData支持字段映射功能，将公式中的显示名称（menuId）映射到数据中的实际字段编码（enCode）：

**映射流程**：
1. 公式中使用 `menuId`（显示名称，如"单价"、"数量"）
2. 通过 `marks` 数组找到对应的 `enCode`（字段编码，如"price"、"quantity"）
3. 使用 `enCode` 去 `JSONObject` 中查找值（`data.get("price")`）

```java
// 前端传来的公式JSON
String jsonStr = "{\n" +
    "  \"txt\": \"SUM(单价,数量)\",\n" +
    "  \"marks\": [\n" +
    "    {\"enCode\": \"price\", \"menuId\": \"单价\", \"form\": {...}},\n" +
    "    {\"enCode\": \"quantity\", \"menuId\": \"数量\", \"form\": {...}}\n" +
    "  ]\n" +
    "}";

FormulaData formulaData = FormulaData.fromJSON(JSONObject.parseObject(jsonStr));

// JSONObject数据：key必须是enCode，value是字段值
JSONObject data = new JSONObject();
data.put("price", 100);      // ✅ 使用enCode作为key
data.put("quantity", 5);     // ✅ 使用enCode作为key

// 计算公式
// 公式中: "SUM(单价,数量)"
// 映射过程: 单价(menuId) -> price(enCode) -> data.get("price") -> 100
//          数量(menuId) -> quantity(enCode) -> data.get("quantity") -> 5
// 计算结果: SUM(100, 5) = 105
Object result = FormulaCalculator.calculate(formulaData, data);
```

**重要说明**：
- ✅ JSONObject的key必须使用 `enCode`（如"price"、"quantity"）
- ❌ JSONObject的key不能使用 `menuId`（如"单价"、"数量"）
- 如果JSONObject中没有对应的enCode，字段值会被视为0

## 验证前端计算结果

```java
// 前端使用Formula.js计算的结果
double frontendResult = 30.8;

// 后端验证
boolean verified = FormulaUtils.verify(formulaData, data, frontendResult);
if (verified) {
    System.out.println("计算结果一致");
} else {
    System.out.println("计算结果不一致，需要重新计算");
}
```

## 支持的运算符

- 加法：`+`
- 减法：`-`
- 乘法：`*`
- 除法：`/`
- 括号：`()` 用于改变运算优先级
- 比较运算符：`>`, `<`, `>=`, `<=`, `==`, `!=`

## 多个函数组合运算

**完全支持多个函数相加、相减、相乘、相除等运算！**

```java
// 示例：多个函数相加
String formula = "ABS(-10) + SUM(5, 10) + SQRT(16)";
// 结果: 10 + 15 + 4 = 29

// 示例：函数与字段混合运算
String formula = "POWER(${a}, ${b}) + PRODUCT(${a}, ${b}) + MOD(${a}, ${b})";
// 结果: 64 + 16 + 0 = 80

// 示例：嵌套函数参与运算
String formula = "SQRT(SUM(1,2,3)) + ABS(-5)";
// 结果: SQRT(6) + 5 ≈ 7.449

// 示例：复杂表达式
String formula = "IF(${score} >= 60, SUM(${a}, ${b}), PRODUCT(${a}, ${b}))";
```

所有函数计算结果都可以参与四则运算，支持任意组合！

## 字段引用方式

支持三种字段引用方式：

1. `${fieldName}` - 推荐使用
2. `[fieldName]` - 兼容Excel风格
3. 直接字段名（在FormulaData中使用menuId）

## 复杂示例

### 示例1：条件判断
```java
FormulaData formulaData = new FormulaData();
formulaData.setTxt("IF(AND(销售额 >= 目标, 销售额 <= 100000), 1000, IF(销售额 > 100000, 2000, 500))");

List<FieldMark> marks = new ArrayList<>();
marks.add(new FieldMark("sales", "销售额"));
marks.add(new FieldMark("target", "目标"));
formulaData.setMarks(marks);

JSONObject data = new JSONObject();
data.put("sales", 50000);
data.put("target", 40000);

Object result = FormulaCalculator.calculate(formulaData, data);
```

### 示例2：多个函数相加（支持）
```java
// 支持多个函数相加、相减、相乘、相除等运算
String formula = "ABS(-10) + SUM(5, 10) + SQRT(16)";
JSONObject data = new JSONObject();
Object result = FormulaCalculator.calculate(formula, data);
// 结果: ABS(-10) + SUM(5,10) + SQRT(16) = 10 + 15 + 4 = 29

// 带字段的多个函数运算
String formula2 = "POWER(${a}, ${b}) + PRODUCT(${a}, ${b}) + MOD(${a}, ${b})";
JSONObject data2 = new JSONObject();
data2.put("a", 8);
data2.put("b", 2);
Object result2 = FormulaCalculator.calculate(formula2, data2);
// 结果: POWER(8,2) + PRODUCT(8,2) + MOD(8,2) = 64 + 16 + 0 = 80

// 嵌套函数参与运算
String formula3 = "SQRT(SUM(1,2,3)) + ABS(-5)";
Object result3 = FormulaCalculator.calculate(formula3, data);
// 结果: SQRT(6) + 5 ≈ 7.449
```

**注意**：CONCATNAME函数返回字符串的hashCode（数值），可以参与数值运算，但主要用于字符串连接功能。

## 构建项目

```bash
# 编译项目
mvn clean compile

# 运行测试
mvn test

# 打包成jar（自动混淆）
mvn clean package

# 安装到本地Maven仓库
mvn clean install

# 打包Fat JAR（包含所有依赖）
mvn clean package -f pom-fatjar.xml
```

打包后会生成：
- `formula-calculator-1.0.0.jar` - **混淆后的jar包**（已自动混淆，源码不可见）
- `formula-calculator-1.0.0-sources.jar` - 源码jar包
- `target/mapping.txt` - 混淆映射文件（包含源码信息，请妥善保管）

**注意**：打包时会自动进行代码混淆，生成的jar包已加密保护，无法查看源码和进行二次开发。

## 注意事项

1. 字段值为null时，会自动使用0替代
2. 字符串类型的数值会自动转换为数字进行计算
3. 计算结果如果是整数会返回Long类型，否则返回Double类型
4. 验证函数默认允许0.0001的误差范围
5. 公式中的字段名（menuId）会通过marks数组映射到enCode
6. 数据JSONObject中的key应使用enCode，而不是menuId

## 函数列表总览

### 常用函数（15个）
SUM、AVERAGE、MAX、MIN、COUNT、COUNTIF、SUMIF、CONCATNAME、DATE、IF、IFS、ROUND、ROUNDUP、ROUNDDOWN、TESTABC

### 逻辑函数（8个）
IF、AND、OR、NOT、XOR、SWITCH、IFERROR、IFNA、IFS

### 统计函数（7个）
STDEV、STDEVP、VAR、VARP、MEDIAN、MODE、AVEDEV

### 数学函数（50+个）
**基础数学**：PI、ABS、SQRT、SQRTPI、SUMSQ、PRODUCT、QUOTIENT、POWER、LOG、LN、EXP、LOG10、LOG2

**取整函数**：CEILING、CEILING.MATH、FLOOR、FLOOR.MATH、MROUND、INT、TRUNC、ROUND、ROUNDUP、ROUNDDOWN、EVEN、ODD

**数论函数**：GCD、LCM、FACT、FACTDOUBLE、COMBIN、PERMUT、MOD、SIGN、ISEVEN、ISODD

**随机函数**：RAND、RANDBETWEEN

**排序函数**：LARGE、SMALL

**平均数函数**：GEOMEAN（几何平均数）、HARMEAN（调和平均数）

**角度转换**：DEGREES、RADIANS

**三角函数**：SIN、COS、TAN、ASIN、ACOS、ATAN、ATAN2

**双曲函数**：SINH、COSH、TANH、ASINH、ACOSH、ATANH

## 版本历史

### v2.2.0
- 新增奇偶函数：EVEN、ODD、ISEVEN、ISODD
- 新增排序函数：LARGE、SMALL
- 新增平均数函数：GEOMEAN、HARMEAN
- 新增对数函数：LOG10、LOG2

### v2.1.0
- 新增常用函数：CONCATNAME、DATE、TESTABC
- 新增逻辑函数：SWITCH、IFERROR、IFNA
- 新增统计函数：AVEDEV
- 新增数学函数：PI、SQRTPI、SUMSQ、PRODUCT、QUOTIENT、GCD、LCM、FACT、FACTDOUBLE、COMBIN、PERMUT、DEGREES、RADIANS、ATAN2、双曲函数等
- 新增取整函数：CEILING.MATH、FLOOR.MATH、MROUND、INT、TRUNC、SIGN
- 新增随机函数：RAND、RANDBETWEEN

### v2.0.0
- 新增FormulaData数据结构支持
- 新增字段映射功能（menuId -> enCode）
- 新增逻辑函数：AND、OR、NOT、XOR、IFS
- 新增统计函数：STDEV、STDEVP、VAR、VARP、MEDIAN、MODE
- 新增数学函数：ABS、SQRT、POWER、LOG、LN、EXP、CEILING、FLOOR、MOD、三角函数等
- 新增自定义函数扩展机制
- 优化函数处理顺序和嵌套支持

### v1.0.0
- 基础公式计算功能
- 支持常用函数：SUM、AVERAGE、MAX、MIN、COUNT、IF、ROUND

## 许可证

Apache License 2.0
