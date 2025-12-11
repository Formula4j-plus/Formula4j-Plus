# Formula Calculator

Java公式计算引擎，支持Formula.js语法，用于后端验证前端计算结果。

## 功能特性

- ✅ 支持Formula.js常用语法
- ✅ 支持字段引用：`${fieldName}` 或 `[fieldName]` 或直接字段名
- ✅ **支持249+个函数**，完全兼容Formula.js库
- ✅ 支持自定义函数扩展
- ✅ 支持前端数据结构（FormulaData）
- ✅ 支持字段映射（menuId -> enCode）
- ✅ 提供前端计算结果验证功能
- ✅ 支持JSONObject数据输入
- ✅ 支持函数嵌套和复杂表达式
- ✅ 支持字符串拼接和混合类型运算

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

## 支持的函数（249+个）

### 常用函数（15个）

- **SUM** - 求和
- **AVERAGE** - 平均值
- **MAX** - 最大值
- **MIN** - 最小值
- **COUNT** - 计数
- **COUNTIF** - 条件计数
- **SUMIF** - 条件求和
- **CONCATENATE** - 字符串连接
- **DATE** - 日期
- **IF** - 条件判断
- **IFS** - 多条件判断
- **ROUND** - 四舍五入
- **ROUNDUP** - 向上取整
- **ROUNDDOWN** - 向下取整
- **TESTABC** - 自定义函数示例

### 逻辑函数（9个）

- **AND** - 逻辑与
- **OR** - 逻辑或
- **NOT** - 逻辑非
- **XOR** - 异或
- **SWITCH** - 多值选择
- **IFERROR** - 错误处理
- **IFNA** - N/A值处理
- **IFS** - 多条件判断

### 统计函数（50+个）

#### 基础统计
- **STDEV** - 标准差（样本）
- **STDEVP** - 标准差（总体）
- **VAR** - 方差（样本）
- **VARP** - 方差（总体）
- **MEDIAN** - 中位数
- **MODE** - 众数
- **AVEDEV** - 平均绝对偏差
- **AVERAGEA** - 平均值（包括文本和逻辑值）
- **AVERAGEIF** - 条件平均值
- **AVERAGEIFS** - 多条件平均值
- **COUNTBLANK** - 统计空白单元格
- **COUNTA** - 统计非空单元格
- **COUNTIFS** - 多条件计数
- **SUMIFS** - 多条件求和
- **FREQUENCY** - 频率分布

#### 百分位数和排名
- **PERCENTILE** - 百分位数
- **PERCENTILE.INC** - 百分位数（包含）
- **PERCENTILE.EXC** - 百分位数（排除）
- **QUARTILE** - 四分位数
- **QUARTILE.INC** - 四分位数（包含）
- **QUARTILE.EXC** - 四分位数（排除）
- **RANK** - 排名
- **RANK.AVG** - 排名（平均值）
- **RANK.EQ** - 排名（相等）
- **PERCENTRANK** - 百分比排名
- **PERCENTRANK.INC** - 百分比排名（包含）
- **PERCENTRANK.EXC** - 百分比排名（排除）

#### 相关性分析
- **CORREL** - 相关系数
- **COVAR** - 协方差
- **COVARIANCE.S** - 样本协方差
- **COVARIANCE.P** - 总体协方差
- **PEARSON** - 皮尔逊相关系数

#### 回归分析
- **FORECAST** - 预测值
- **INTERCEPT** - 截距
- **SLOPE** - 斜率
- **RSQ** - 决定系数
- **STEYX** - 标准误差
- **GROWTH** - 指数增长
- **TREND** - 线性趋势
- **LINEST** - 线性回归
- **LOGEST** - 指数回归

#### 描述统计
- **TRIMMEAN** - 截尾平均值
- **DEVSQ** - 偏差平方和
- **SKEW** - 偏度
- **SKEW.P** - 总体偏度
- **KURT** - 峰度
- **MAXA** - 最大值（包括文本和逻辑值）
- **MINA** - 最小值（包括文本和逻辑值）

#### 统计检验
- **Z.TEST** - Z检验
- **T.TEST** - T检验
- **F.TEST** - F检验
- **CHISQ.TEST** - 卡方检验
- **CONFIDENCE** - 置信区间
- **CONFIDENCE.NORM** - 正态分布置信区间
- **CONFIDENCE.T** - T分布置信区间

### 数学函数（80+个）

#### 基础数学
- **PI** - 圆周率
- **ABS** - 绝对值
- **SQRT** - 平方根
- **SQRTPI** - (数字*PI)的平方根
- **SUMSQ** - 平方和
- **PRODUCT** - 乘积
- **QUOTIENT** - 商（整数除法）
- **POWER** - 幂运算
- **LOG** - 常用对数（以10为底）
- **LOG10** - 以10为底的对数
- **LOG2** - 以2为底的对数
- **LN** - 自然对数
- **EXP** - 自然指数

#### 取整函数
- **CEILING** - 向上取整
- **CEILING.MATH** - 向上取整到指定倍数
- **CEILING.PRECISE** - 精确向上取整
- **FLOOR** - 向下取整
- **FLOOR.MATH** - 向下取整到指定倍数
- **FLOOR.PRECISE** - 精确向下取整
- **ISO.CEILING** - ISO向上取整
- **MROUND** - 四舍五入到指定倍数
- **INT** - 向下取整到整数
- **TRUNC** - 截断小数部分
- **SIGN** - 返回数字的符号
- **EVEN** - 向上取整到最接近的偶数
- **ODD** - 向上取整到最接近的奇数

#### 数论函数
- **GCD** - 最大公约数
- **LCM** - 最小公倍数
- **FACT** - 阶乘
- **FACTDOUBLE** - 双阶乘
- **COMBIN** - 组合数 C(n,k)
- **PERMUT** - 排列数 P(n,k)
- **MOD** - 取余
- **ISEVEN** - 判断是否为偶数
- **ISODD** - 判断是否为奇数
- **MULTINOMIAL** - 多项系数

#### 随机函数
- **RAND** - 返回0到1之间的随机数
- **RANDBETWEEN** - 返回指定范围内的随机整数

#### 排序函数
- **LARGE** - 返回第k个最大值
- **SMALL** - 返回第k个最小值

#### 平均数函数
- **GEOMEAN** - 几何平均数
- **HARMEAN** - 调和平均数

#### 角度转换
- **DEGREES** - 弧度转角度
- **RADIANS** - 角度转弧度

#### 三角函数
- **SIN** - 正弦
- **COS** - 余弦
- **TAN** - 正切
- **ASIN** - 反正弦
- **ACOS** - 反余弦
- **ATAN** - 反正切
- **ATAN2** - 返回两个参数的反正切值
- **SINH** - 双曲正弦
- **COSH** - 双曲余弦
- **TANH** - 双曲正切
- **ASINH** - 反双曲正弦
- **ACOSH** - 反双曲余弦
- **ATANH** - 反双曲正切
- **SEC** - 正割
- **SECH** - 双曲正割
- **CSC** - 余割
- **CSCH** - 双曲余割
- **COT** - 余切
- **COTH** - 双曲余切
- **ACOT** - 反余切
- **ACOTH** - 反双曲余切

#### 其他数学函数
- **BASE** - 将数字转换为指定进制的文本
- **ROMAN** - 将数字转换为罗马数字
- **ARABIC** - 将罗马数字转换为阿拉伯数字
- **MUNIT** - 返回单位矩阵
- **AGGREGATE** - 聚合函数

### 文本函数（25+个）

- **TEXT** - 格式化文本
- **UPPER** - 转换为大写
- **LOWER** - 转换为小写
- **PROPER** - 首字母大写
- **LEN** - 字符串长度
- **LEFT** - 从左侧提取字符
- **RIGHT** - 从右侧提取字符
- **MID** - 从中间提取字符
- **FIND** - 查找字符串（区分大小写）
- **SEARCH** - 查找字符串（不区分大小写）
- **REPLACE** - 替换字符串
- **SUBSTITUTE** - 替换指定字符串
- **TRIM** - 去除首尾空格
- **CONCAT** - 连接字符串
- **TEXTJOIN** - 文本连接（带分隔符）
- **VALUE** - 将文本转换为数字
- **CHAR** - 返回字符代码对应的字符
- **CODE** - 返回字符的字符代码
- **REPT** - 重复文本
- **EXACT** - 比较两个字符串是否完全相同
- **CLEAN** - 清除不可打印字符
- **T** - 返回文本值
- **FIXED** - 将数字格式化为固定小数位数
- **DOLLAR** - 将数字格式化为货币格式
- **NUMBERVALUE** - 将文本转换为数字

### 日期时间函数（20+个）

- **TODAY** - 返回当前日期
- **NOW** - 返回当前日期和时间
- **YEAR** - 返回年份
- **MONTH** - 返回月份
- **DAY** - 返回日期
- **HOUR** - 返回小时
- **MINUTE** - 返回分钟
- **SECOND** - 返回秒
- **WEEKDAY** - 返回星期几
- **WEEKNUM** - 返回周数
- **DAYS** - 返回两个日期之间的天数
- **DAYS360** - 返回两个日期之间的天数（360天制）
- **EDATE** - 返回指定月数后的日期
- **EOMONTH** - 返回指定月数后的月末日期
- **YEARFRAC** - 返回两个日期之间的年数
- **TIME** - 返回时间序列号
- **TIMEVALUE** - 将文本时间转换为时间序列号
- **DATEVALUE** - 将文本日期转换为日期序列号
- **WORKDAY** - 返回工作日
- **WORKDAY.INTL** - 返回工作日（自定义周末）
- **NETWORKDAYS** - 返回两个日期之间的工作日数
- **NETWORKDAYS.INTL** - 返回工作日数（自定义周末）
- **DATEDIF** - 返回两个日期之间的差值

### 查找和引用函数（20+个）

- **VLOOKUP** - 垂直查找
- **HLOOKUP** - 水平查找
- **INDEX** - 返回指定位置的值
- **MATCH** - 查找值的位置
- **CHOOSE** - 从值列表中选择值
- **ROW** - 返回行号
- **COLUMN** - 返回列号
- **ROWS** - 返回行数
- **COLUMNS** - 返回列数
- **INDIRECT** - 返回引用指定的值
- **OFFSET** - 返回偏移引用
- **ADDRESS** - 返回单元格地址
- **AREAS** - 返回引用中的区域数
- **TRANSPOSE** - 转置数组
- **UNIQUE** - 返回唯一值
- **SORT** - 排序数组
- **FILTER** - 筛选数组
- **XLOOKUP** - 扩展查找
- **XMATCH** - 扩展匹配

### 信息函数（15+个）

- **ISBLANK** - 判断是否为空
- **ISNUMBER** - 判断是否为数字
- **ISTEXT** - 判断是否为文本
- **ISLOGICAL** - 判断是否为逻辑值
- **ISERROR** - 判断是否为错误值
- **ISNA** - 判断是否为#N/A
- **ISERR** - 判断是否为错误值（不包括#N/A）
- **ISFORMULA** - 判断是否为公式
- **ISREF** - 判断是否为引用
- **TYPE** - 返回值的类型
- **CELL** - 返回单元格信息
- **INFO** - 返回系统信息
- **ERROR.TYPE** - 返回错误类型
- **NA** - 返回#N/A错误
- **N** - 返回数值
- **TRUE** - 返回TRUE
- **FALSE** - 返回FALSE

### 财务函数（40+个）

#### 基础财务
- **PMT** - 每期付款额
- **IPMT** - 利息付款
- **PPMT** - 本金付款
- **FV** - 未来值
- **PV** - 现值
- **NPV** - 净现值
- **IRR** - 内部收益率
- **MIRR** - 修正内部收益率
- **RATE** - 利率
- **NPER** - 期数

#### 累计函数
- **CUMIPMT** - 累计利息
- **CUMPRINC** - 累计本金

#### 折旧函数
- **SLN** - 直线折旧
- **SYD** - 年数总和折旧
- **DB** - 余额递减折旧
- **DDB** - 双倍余额递减折旧
- **VDB** - 可变余额递减折旧

#### 债券函数
- **ACCRINT** - 应计利息
- **ACCRINTM** - 到期应计利息
- **COUPDAYBS** - 付息期开始到结算日的天数
- **COUPDAYS** - 包含结算日的付息期天数
- **COUPDAYSNC** - 结算日到下一个付息日的天数
- **COUPNCD** - 下一个付息日
- **COUPNUM** - 付息期数
- **COUPPCD** - 上一个付息日
- **DISC** - 贴现率
- **DURATION** - 久期
- **EFFECT** - 有效年利率
- **INTRATE** - 利率
- **MDURATION** - 修正久期
- **NOMINAL** - 名义年利率
- **PRICE** - 债券价格
- **PRICEDISC** - 折价债券价格
- **PRICEMAT** - 到期付息债券价格
- **RECEIVED** - 到期收回金额
- **TBILLEQ** - 国库券等效收益率
- **TBILLPRICE** - 国库券价格
- **TBILLYIELD** - 国库券收益率
- **YIELD** - 债券收益率
- **YIELDDISC** - 折价债券收益率
- **YIELDMAT** - 到期付息债券收益率
- **XIRR** - 不定期现金流内部收益率
- **XNPV** - 不定期现金流净现值

### 工程函数（50+个）

#### 进制转换
- **BIN2DEC** - 二进制转十进制
- **DEC2BIN** - 十进制转二进制
- **HEX2DEC** - 十六进制转十进制
- **DEC2HEX** - 十进制转十六进制
- **OCT2DEC** - 八进制转十进制
- **DEC2OCT** - 十进制转八进制
- **BIN2HEX** - 二进制转十六进制
- **HEX2BIN** - 十六进制转二进制
- **BIN2OCT** - 二进制转八进制
- **OCT2BIN** - 八进制转二进制
- **HEX2OCT** - 十六进制转八进制
- **OCT2HEX** - 八进制转十六进制

#### 其他工程函数
- **DELTA** - 检验两个值是否相等
- **GESTEP** - 检验数字是否大于阈值
- **ERF** - 误差函数
- **ERF.PRECISE** - 精确误差函数
- **ERFC** - 互补误差函数
- **ERFC.PRECISE** - 精确互补误差函数
- **BESSELI** - 修正贝塞尔函数In(x)
- **BESSELJ** - 贝塞尔函数Jn(x)
- **BESSELK** - 修正贝塞尔函数Kn(x)
- **BESSELY** - 贝塞尔函数Yn(x)

#### 复数函数（简化实现）
- **COMPLEX** - 创建复数
- **IMABS** - 复数的绝对值
- **IMAGINARY** - 复数的虚部
- **IMARGUMENT** - 复数的幅角
- **IMCONJUGATE** - 复数的共轭
- **IMCOS** - 复数的余弦
- **IMCOSH** - 复数的双曲余弦
- **IMCOT** - 复数的余切
- **IMCSC** - 复数的余割
- **IMCSCH** - 复数的双曲余割
- **IMDIV** - 复数的除法
- **IMEXP** - 复数的指数
- **IMLN** - 复数的自然对数
- **IMLOG10** - 复数的常用对数
- **IMLOG2** - 复数的以2为底的对数
- **IMPOWER** - 复数的幂
- **IMPRODUCT** - 复数的乘积
- **IMREAL** - 复数的实部
- **IMSEC** - 复数的正割
- **IMSECH** - 复数的双曲正割
- **IMSIN** - 复数的正弦
- **IMSINH** - 复数的双曲正弦
- **IMSQRT** - 复数的平方根
- **IMSUB** - 复数的减法
- **IMSUM** - 复数的和
- **IMTAN** - 复数的正切

### 分布函数（30+个）

#### 正态分布
- **NORM.DIST** - 正态分布
- **NORM.INV** - 正态分布反函数
- **NORM.S.DIST** - 标准正态分布
- **NORM.S.INV** - 标准正态分布反函数

#### 其他分布
- **BINOM.DIST** - 二项分布
- **BINOM.INV** - 二项分布反函数
- **POISSON.DIST** - 泊松分布
- **EXPON.DIST** - 指数分布
- **GAMMA.DIST** - 伽马分布
- **GAMMA.INV** - 伽马分布反函数
- **BETA.DIST** - 贝塔分布
- **BETA.INV** - 贝塔分布反函数
- **WEIBULL.DIST** - 威布尔分布
- **LOGNORM.DIST** - 对数正态分布
- **LOGNORM.INV** - 对数正态分布反函数
- **HYPGEOM.DIST** - 超几何分布
- **NEGBINOM.DIST** - 负二项分布
- **T.DIST** - T分布
- **T.INV** - T分布反函数
- **F.DIST** - F分布
- **F.INV** - F分布反函数
- **CHISQ.DIST** - 卡方分布
- **CHISQ.INV** - 卡方分布反函数
- **CRITBINOM** - 二项分布临界值

### 兼容性函数（30+个）

为了兼容旧版本Excel，提供了以下兼容性函数：

- **NORMDIST** - 正态分布（兼容）
- **NORMINV** - 正态分布反函数（兼容）
- **NORMSDIST** - 标准正态分布（兼容）
- **NORMSINV** - 标准正态分布反函数（兼容）
- **BINOMDIST** - 二项分布（兼容）
- **POISSON** - 泊松分布（兼容）
- **EXPONDIST** - 指数分布（兼容）
- **TDIST** - T分布（兼容）
- **TINV** - T分布反函数（兼容）
- **TTEST** - T检验（兼容）
- **CHIDIST** - 卡方分布（兼容）
- **CHIINV** - 卡方分布反函数（兼容）
- **CHITEST** - 卡方检验（兼容）
- **FDIST** - F分布（兼容）
- **FINV** - F分布反函数（兼容）
- **FTEST** - F检验（兼容）
- **GAMMADIST** - 伽马分布（兼容）
- **GAMMAINV** - 伽马分布反函数（兼容）
- **LOGNORMDIST** - 对数正态分布（兼容）
- **LOGINV** - 对数正态分布反函数（兼容）
- **WEIBULL** - 威布尔分布（兼容）
- **BETADIST** - 贝塔分布（兼容）
- **BETAINV** - 贝塔分布反函数（兼容）
- **HYPGEOMDIST** - 超几何分布（兼容）
- **NEGBINOMDIST** - 负二项分布（兼容）
- **VAR.S** - 样本方差（新版本）
- **VAR.P** - 总体方差（新版本）
- **VARA** - 样本方差（包括文本和逻辑值）
- **VARPA** - 总体方差（包括文本和逻辑值）
- **ZTEST** - Z检验（兼容）

## 函数使用示例

### 常用函数示例

```java
// SUM - 求和
String formula = "SUM(${field1}, ${field2}, ${field3})";

// AVERAGE - 平均值
String formula = "AVERAGE(${field1}, ${field2}, ${field3})";

// MAX - 最大值
String formula = "MAX(${field1}, ${field2}, ${field3})";

// MIN - 最小值
String formula = "MIN(${field1}, ${field2}, ${field3})";

// COUNT - 计数
String formula = "COUNT(${field1}, ${field2}, ${field3})";

// COUNTIF - 条件计数
String formula = "COUNTIF(${field1}, ${field2}, ${field3}, \">10\")";

// SUMIF - 条件求和
String formula = "SUMIF(${range}, ${criteria})";

// CONCATENATE - 字符串连接
String formula = "CONCATENATE(\"Hello\", \"World\")";

// IF - 条件判断
String formula = "IF(${score} >= 60, 1, 0)";

// IFS - 多条件判断
String formula = "IFS(${score} >= 90, \"优秀\", ${score} >= 60, \"及格\", \"不及格\")";

// ROUND - 四舍五入
String formula = "ROUND(${value}, 2)";
```

### 逻辑函数示例

```java
// AND - 逻辑与
String formula = "AND(${a} > 5, ${b} > 10)";

// OR - 逻辑或
String formula = "OR(${a} < 5, ${b} > 10)";

// NOT - 逻辑非
String formula = "NOT(${a} < 5)";

// XOR - 异或
String formula = "XOR(${a} > 5, ${b} > 10)";

// SWITCH - 多值选择
String formula = "SWITCH(${value}, 1, \"One\", 2, \"Two\", \"Other\")";

// IFERROR - 如果表达式错误，返回指定值
String formula = "IFERROR(${expression}, ${errorValue})";

// IFNA - 如果值为#N/A，返回指定值
String formula = "IFNA(${value}, ${defaultValue})";
```

### 统计函数示例

```java
// STDEV - 标准差（样本）
String formula = "STDEV(${field1}, ${field2}, ${field3})";

// MEDIAN - 中位数
String formula = "MEDIAN(${field1}, ${field2}, ${field3})";

// PERCENTILE - 百分位数
String formula = "PERCENTILE(${array}, 0.5)";

// RANK - 排名
String formula = "RANK(${value}, ${array})";

// CORREL - 相关系数
String formula = "CORREL(${array1}, ${array2})";

// FORECAST - 预测值
String formula = "FORECAST(${x}, ${known_y}, ${known_x})";
```

### 数学函数示例

```java
// PI - 圆周率
String formula = "PI()"; // 返回3.141592653589793

// ABS - 绝对值
String formula = "ABS(${value})";

// SQRT - 平方根
String formula = "SQRT(${value})";

// POWER - 幂运算
String formula = "POWER(${base}, ${exponent})";

// LOG - 常用对数
String formula = "LOG(${value})";

// SIN - 正弦
String formula = "SIN(${value})";

// GCD - 最大公约数
String formula = "GCD(${value1}, ${value2}, ${value3})";

// FACT - 阶乘
String formula = "FACT(${n})"; // FACT(5) = 120
```

### 文本函数示例

```java
// UPPER - 转换为大写
String formula = "UPPER(\"hello\")"; // 返回 "HELLO"

// LOWER - 转换为小写
String formula = "LOWER(\"HELLO\")"; // 返回 "hello"

// LEN - 字符串长度
String formula = "LEN(\"Hello\")"; // 返回 5

// LEFT - 从左侧提取字符
String formula = "LEFT(\"Hello\", 3)"; // 返回 "Hel"

// MID - 从中间提取字符
String formula = "MID(\"Hello\", 2, 3)"; // 返回 "ell"

// FIND - 查找字符串
String formula = "FIND(\"l\", \"Hello\")"; // 返回 3

// REPLACE - 替换字符串
String formula = "REPLACE(\"Hello\", 1, 3, \"Hi\")"; // 返回 "Hilo"
```

### 日期时间函数示例

```java
// TODAY - 返回当前日期
String formula = "TODAY()";

// NOW - 返回当前日期和时间
String formula = "NOW()";

// YEAR - 返回年份
String formula = "YEAR(${date})";

// MONTH - 返回月份
String formula = "MONTH(${date})";

// DAY - 返回日期
String formula = "DAY(${date})";

// DATEDIF - 返回两个日期之间的差值
String formula = "DATEDIF(${start_date}, ${end_date}, \"d\")";
```

### 财务函数示例

```java
// PMT - 每期付款额
String formula = "PMT(${rate}, ${nper}, ${pv})";

// FV - 未来值
String formula = "FV(${rate}, ${nper}, ${pmt}, ${pv})";

// PV - 现值
String formula = "PV(${rate}, ${nper}, ${pmt}, ${fv})";

// NPV - 净现值
String formula = "NPV(${rate}, ${value1}, ${value2}, ${value3})";

// IRR - 内部收益率
String formula = "IRR(${values})";
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
- 字符串连接：`+`（当包含字符串字面量时）

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

// 示例：字符串拼接
String formula = "CONCATENATE(\"Hello\", \"World\") + SUM(1, 2)";
// 结果: "HelloWorld" + 3 = "HelloWorld3"
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

### 示例2：多个函数相加

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

### 示例3：字符串拼接

```java
String formula = "CONCATENATE(\"Hello\", \"World\")";
Object result = FormulaCalculator.calculate(formula, new JSONObject());
// 结果: "HelloWorld"

// 字符串与数字混合拼接
String formula2 = "CONCATENATE(\"价格: \", ${price}, \" 元\")";
JSONObject data = new JSONObject();
data.put("price", 100);
Object result2 = FormulaCalculator.calculate(formula2, data);
// 结果: "价格: 100 元"
```

## 构建项目

```bash
# 编译项目
mvn clean compile

# 运行测试
mvn test

# 打包成jar
mvn clean package

# 安装到本地Maven仓库
mvn clean install
```

## 注意事项

1. 字段值为null时，会自动使用0替代
2. 字符串类型的数值会自动转换为数字进行计算
3. 计算结果如果是整数会返回Long类型，否则返回Double类型
4. 验证函数默认允许0.0001的误差范围
5. 公式中的字段名（menuId）会通过marks数组映射到enCode
6. 数据JSONObject中的key应使用enCode，而不是menuId
7. 字符串字面量需要用引号包裹，如 `"Hello"`
8. CONCATENATE函数返回字符串，可以与其他字符串或数字进行拼接

## 版本历史

### v3.0.0（当前版本）
- ✅ 实现249+个函数，完全兼容Formula.js库
- ✅ 新增文本函数：TEXT, UPPER, LOWER, PROPER, LEN, LEFT, RIGHT, MID, FIND, SEARCH, REPLACE, SUBSTITUTE, TRIM, CONCAT, TEXTJOIN, VALUE, CHAR, CODE, REPT, EXACT, CLEAN, T, FIXED, DOLLAR, NUMBERVALUE
- ✅ 新增日期时间函数：TODAY, NOW, YEAR, MONTH, DAY, HOUR, MINUTE, SECOND, WEEKDAY, WEEKNUM, DAYS, DAYS360, EDATE, EOMONTH, YEARFRAC, TIME, TIMEVALUE, DATEVALUE, WORKDAY, WORKDAY.INTL, NETWORKDAYS, NETWORKDAYS.INTL, DATEDIF
- ✅ 新增查找函数：VLOOKUP, HLOOKUP, INDEX, MATCH, CHOOSE, ROW, COLUMN, ROWS, COLUMNS, INDIRECT, OFFSET, ADDRESS, AREAS, TRANSPOSE, UNIQUE, SORT, FILTER, XLOOKUP, XMATCH
- ✅ 新增信息函数：ISBLANK, ISNUMBER, ISTEXT, ISLOGICAL, ISERROR, ISNA, ISERR, ISFORMULA, ISREF, TYPE, CELL, INFO, ERROR.TYPE, NA, N, TRUE, FALSE
- ✅ 新增统计扩展函数：AVERAGEA, AVERAGEIF, AVERAGEIFS, COUNTBLANK, COUNTA, COUNTIFS, SUMIFS, FREQUENCY, PERCENTILE系列, QUARTILE系列, RANK系列, PERCENTRANK系列, CORREL, COVAR, COVARIANCE.S, COVARIANCE.P, PEARSON, FORECAST, INTERCEPT, SLOPE, RSQ, STEYX, GROWTH, TREND, LINEST, LOGEST, TRIMMEAN, DEVSQ, SKEW, SKEW.P, KURT, MAXA, MINA, Z.TEST, T.TEST, F.TEST, CHISQ.TEST, CONFIDENCE系列
- ✅ 新增数学扩展函数：LOG10, LOG2, CEILING.PRECISE, FLOOR.PRECISE, ISO.CEILING, MUNIT, MULTINOMIAL, AGGREGATE, SEC, SECH, CSC, CSCH, COT, COTH, ACOT, ACOTH, BASE, ROMAN, ARABIC
- ✅ 新增财务函数：PMT, IPMT, PPMT, FV, PV, NPV, IRR, MIRR, RATE, NPER, CUMIPMT, CUMPRINC, SLN, SYD, DB, DDB, VDB及债券相关函数
- ✅ 新增工程函数：进制转换函数（BIN2DEC, DEC2BIN, HEX2DEC等），DELTA, GESTEP, ERF系列, BESSEL系列, 复数函数系列
- ✅ 新增分布函数：NORM.DIST, NORM.INV, NORM.S.DIST, NORM.S.INV, BINOM.DIST, BINOM.INV, POISSON.DIST, EXPON.DIST, GAMMA.DIST, GAMMA.INV, BETA.DIST, BETA.INV, WEIBULL.DIST, LOGNORM.DIST, LOGNORM.INV, HYPGEOM.DIST, NEGBINOM.DIST, T.DIST, T.INV, F.DIST, F.INV, CHISQ.DIST, CHISQ.INV, CRITBINOM
- ✅ 新增兼容性函数：所有旧版本函数名的兼容实现
- ✅ 优化字符串处理：支持CONCATENATE函数和字符串拼接
- ✅ 代码重构：提取工具类（FormulaMathUtils, FormulaStatisticsUtils, FormulaDateUtils, FormulaStringUtils, FormulaParamUtils）

### v2.2.0
- 新增奇偶函数：EVEN、ODD、ISEVEN、ISODD
- 新增排序函数：LARGE、SMALL
- 新增平均数函数：GEOMEAN、HARMEAN
- 新增对数函数：LOG10、LOG2

### v2.1.0
- 新增常用函数：CONCATENATE、DATE、TESTABC
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
