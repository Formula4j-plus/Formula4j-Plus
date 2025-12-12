@echo off
chcp 65001 >nul
echo ========================================
echo 代码混淆脚本
echo ========================================
echo.

echo [1/4] 构建项目...
call mvn clean package -DskipTests
if %errorlevel% neq 0 (
    echo 构建失败！
    pause
    exit /b 1
)

echo.
echo [2/4] 检查ProGuard工具...
if not exist "tools\proguard-base-7.2.2.jar" (
    echo 下载ProGuard工具...
    call mvn dependency:copy -Dartifact=com.guardsquare:proguard-base:7.2.2 -DoutputDirectory=./tools -q
    if not exist "tools\proguard-base-7.2.2.jar" (
        echo ProGuard工具下载失败！请手动下载。
        pause
        exit /b 1
    )
)

echo.
echo [3/4] 执行代码混淆...
if not exist "target\formula-calculator-1.0.0.jar" (
    echo 错误：未找到target\formula-calculator-1.0.0.jar文件！
    pause
    exit /b 1
)

java -jar tools\proguard-base-7.2.2.jar @proguard.conf
if %errorlevel% neq 0 (
    echo.
    echo 混淆失败！可能的原因：
    echo 1. ProGuard配置有误
    echo 2. Java版本不兼容
    echo 3. jar包依赖问题
    echo.
    echo 请检查错误信息并修复。
    pause
    exit /b 1
)

echo.
echo [4/4] 替换jar包...
if exist "target\formula-calculator-1.0.0-obfuscated.jar" (
    del /f /q "target\formula-calculator-1.0.0.jar" 2>nul
    ren "target\formula-calculator-1.0.0-obfuscated.jar" "formula-calculator-1.0.0.jar"
    echo 混淆完成！jar包已替换。
) else (
    echo 警告：未找到混淆后的jar包！
)

echo.
echo ========================================
echo 完成！
echo ========================================
pause

