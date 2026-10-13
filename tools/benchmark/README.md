# JMH 开发基准

独立开发模块，固定 JMH 1.37，不增加主计算库的运行时依赖。
包含纯字段算术、常用函数组合、条件公式三种场景，每种比较四个公共调用路径：
兼容入口、严格缓存命中、直接复用 CompiledFormula、禁用编译缓存。
每个线程先校验四种路径结果，JMH 消费返回值；输入、引擎和缓存按线程隔离。

## 构建

先构建包含严格入口的当前主项目源码。基准依赖使用仅限本地的 `3.0.0-benchmark` 别名，
避免覆盖已发布版本的本地缓存；不要将该别名发布到远程仓库。
以下命令中的 `/tmp/core-build` 是主项目构建副本，请换成实际路径：

```sh
mvn -B -f /tmp/core-build/pom.xml verify
python scripts/prepare_benchmark_pom.py /tmp/core-build/pom.xml /tmp/core-build/benchmark-core.pom
mvn org.apache.maven.plugins:maven-install-plugin:2.4:install-file \
  -Dfile=/tmp/core-build/target/formula4j-plus-3.0.0.jar \
  -DpomFile=/tmp/core-build/benchmark-core.pom
mvn -B -f tools/benchmark/pom.xml package
```

辅助脚本保留原 POM 的依赖声明，只改临时 POM 的项目版本。
也可自行安装源码构建产物并传入 `-Dformula.version=你的本地版本`。

## 执行和解释

```sh
# 默认两次独立 fork、3 轮预热、5 轮测量；输出 JSON 便于留存与比较。
java -jar tools/benchmark/target/benchmarks.jar -rf json -rff benchmark-results.json -prof gc

# 快速验证全部基准能启动；短测量不能作为正式性能结论。
java -jar tools/benchmark/target/benchmarks.jar -f 1 -wi 1 -i 2 -w 300ms -r 300ms

# 并发场景由 JMH 线程数控制。
java -jar tools/benchmark/target/benchmarks.jar -t 4 -rf json -rff benchmark-concurrent.json
```

平均耗时为 µs/op；保留误差、JDK、CPU、JVM 参数、线程数、fork 和预热配置。
严格无缓存是“每次解析”的路径，不是首次 JVM 启动；需要启动成本时单独测量 SingleShotTime。
兼容与严格入口返回不同数值类型，这里选用两者结果一致的简单公式，不外推全部公式的性能或兼容性。
不设置固定耗时 CI 门槛，不据短运行宣称微秒级保证或固定倍数提升。

[严格模式](../../docs/strict-engine.md) · [差分矩阵](../../docs/formulajs-compatibility.md)

本轮已完成默认配置的 12 组本地测量，环境、误差与分配量见[Java 17 基线](../../docs/benchmark-baseline.md)，
完整统计保存在 [baseline-java17.json](baseline-java17.json)。CI 只运行短检查，不比较固定耗时门槛。
