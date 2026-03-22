#!/bin/bash
echo "========================================"
echo "代码混淆脚本"
echo "========================================"
echo

echo "[1/4] 构建项目..."
mvn clean package -DskipTests
if [ $? -ne 0 ]; then
    echo "构建失败！"
    exit 1
fi

echo
echo "[2/4] 检查ProGuard工具..."
if [ ! -f "tools/proguard-base-7.2.2.jar" ]; then
    echo "下载ProGuard工具..."
    mvn dependency:copy -Dartifact=com.guardsquare:proguard-base:7.2.2 -DoutputDirectory=./tools -q
    if [ ! -f "tools/proguard-base-7.2.2.jar" ]; then
        echo "ProGuard工具下载失败！请手动下载。"
        exit 1
    fi
fi

echo
echo "[3/4] 执行代码混淆..."
if [ ! -f "target/formula-calculator-1.0.0.jar" ]; then
    echo "错误：未找到target/formula-calculator-1.0.0.jar文件！"
    exit 1
fi

java -jar tools/proguard-base-7.2.2.jar @proguard.conf
if [ $? -ne 0 ]; then
    echo
    echo "混淆失败！可能的原因："
    echo "1. ProGuard配置有误"
    echo "2. Java版本不兼容"
    echo "3. jar包依赖问题"
    echo
    echo "请检查错误信息并修复。"
    exit 1
fi

echo
echo "[4/4] 替换jar包..."
if [ -f "target/formula-calculator-1.0.0-obfuscated.jar" ]; then
    rm -f target/formula-calculator-1.0.0.jar
    mv target/formula-calculator-1.0.0-obfuscated.jar target/formula-calculator-1.0.0.jar
    echo "混淆完成！jar包已替换。"
else
    echo "警告：未找到混淆后的jar包！"
    exit 1
fi

echo
echo "========================================"
echo "完成！"
echo "========================================"

