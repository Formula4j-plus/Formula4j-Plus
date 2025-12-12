package com.formula.calculator;

import com.alibaba.fastjson.JSONObject;
import com.formula.calculator.model.FormulaData;
import com.formula.calculator.model.FieldMark;
import java.util.ArrayList;
import java.util.List;

/**
 * Formula.js库一次性加载示例
 * 
 * 功能说明：
 * 1. 一次性加载整个Formula.js库
 * 2. 后续所有函数都可以直接使用，无需单独注册
 * 3. 支持多个函数一起使用并相加
 */
public class FormulaJsLibraryExample {
    
    public static void main(String[] args) {
        // 示例1: 从本地文件加载Formula.js库
        example1_LoadFromLocalFile();
        
        // 示例2: 从URL加载Formula.js库
        example2_LoadFromURL();
        
        // 示例3: 从类路径加载Formula.js库
        example3_LoadFromClasspath();
        
        // 示例4: 多个函数一起使用并相加
        example4_MultipleFunctions();
        
        // 示例5: 混合使用Java函数和JS函数
        example5_MixedFunctions();
    }
    
    /**
     * 示例1: 从本地文件加载Formula.js库
     */
    public static void example1_LoadFromLocalFile() {
        System.out.println("=== 示例1: 从本地文件加载Formula.js库 ===");
        
        try {
            // 一次性加载Formula.js库
            // 注意：请将路径替换为实际的Formula.js文件路径
            String localPath = "file:///D:/formula.js"; // 或者 "D:/formula.js"
            FormulaCalculator.loadFormulaJsLibrary(localPath);
            
            System.out.println("✅ Formula.js库加载成功");
            System.out.println("已加载路径: " + FormulaCalculator.getLoadedFormulaJsPath());
            
            // 准备数据
            JSONObject data = new JSONObject();
            
            // 现在可以直接使用Formula.js中的所有函数，无需单独注册
            String formula1 = "SUM(1, 2, 3)";
            Object result1 = FormulaCalculator.calculate(formula1, data);
            System.out.println("SUM(1, 2, 3) = " + result1);
            
            String formula2 = "ABS(-10)";
            Object result2 = FormulaCalculator.calculate(formula2, data);
            System.out.println("ABS(-10) = " + result2);
            
        } catch (Exception e) {
            System.err.println("❌ 加载失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 示例2: 从URL加载Formula.js库
     */
    public static void example2_LoadFromURL() {
        System.out.println("\n=== 示例2: 从URL加载Formula.js库 ===");
        
        try {
            // 从CDN加载Formula.js库
            String url = "https://cdn.jsdelivr.net/npm/@formulajs/formulajs/lib/browser/formula.min.js";
            FormulaCalculator.loadFormulaJsLibrary(url);
            
            System.out.println("✅ Formula.js库加载成功");
            
            // 准备数据
            JSONObject data = new JSONObject();
            
            // 使用Formula.js中的函数
            String formula = "SQRT(16)";
            Object result = FormulaCalculator.calculate(formula, data);
            System.out.println("SQRT(16) = " + result);
            
        } catch (Exception e) {
            System.err.println("❌ 加载失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 示例3: 从类路径加载Formula.js库
     */
    public static void example3_LoadFromClasspath() {
        System.out.println("\n=== 示例3: 从类路径加载Formula.js库 ===");
        
        try {
            // 从类路径加载（需要将formula.js放在src/main/resources目录下）
            String classpath = "classpath:formula.js";
            FormulaCalculator.loadFormulaJsLibrary(classpath);
            
            System.out.println("✅ Formula.js库加载成功");
            
            // 准备数据
            JSONObject data = new JSONObject();
            
            // 使用Formula.js中的函数
            String formula = "POWER(2, 3)";
            Object result = FormulaCalculator.calculate(formula, data);
            System.out.println("POWER(2, 3) = " + result);
            
        } catch (Exception e) {
            System.err.println("❌ 加载失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 示例4: 多个函数一起使用并相加
     */
    public static void example4_MultipleFunctions() {
        System.out.println("\n=== 示例4: 多个函数一起使用并相加 ===");
        
        try {
            // 确保已加载Formula.js库
            if (!FormulaCalculator.isFormulaJsLibraryLoaded()) {
                // 如果未加载，尝试从URL加载
                FormulaCalculator.loadFormulaJsLibrary(
                    "https://cdn.jsdelivr.net/npm/@formulajs/formulajs/lib/browser/formula.min.js"
                );
            }
            
            // 准备数据
            JSONObject data = new JSONObject();
            
            // 多个函数相加
            String formula1 = "SUM(1, 2, 3) + ABS(-5) + SQRT(16)";
            Object result1 = FormulaCalculator.calculate(formula1, data);
            System.out.println("SUM(1, 2, 3) + ABS(-5) + SQRT(16) = " + result1);
            // 预期结果: 6 + 5 + 4 = 15
            
            // 更复杂的组合
            String formula2 = "SUM(10, 20) + POWER(2, 3) + ROUND(3.14159, 2)";
            Object result2 = FormulaCalculator.calculate(formula2, data);
            System.out.println("SUM(10, 20) + POWER(2, 3) + ROUND(3.14159, 2) = " + result2);
            // 预期结果: 30 + 8 + 3.14 = 41.14
            
            // 嵌套函数相加
            String formula3 = "SUM(1, 2) + SUM(3, 4) + ABS(-10)";
            Object result3 = FormulaCalculator.calculate(formula3, data);
            System.out.println("SUM(1, 2) + SUM(3, 4) + ABS(-10) = " + result3);
            // 预期结果: 3 + 7 + 10 = 20
            
        } catch (Exception e) {
            System.err.println("❌ 计算失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 示例5: 混合使用Java函数和JS函数
     */
    public static void example5_MixedFunctions() {
        System.out.println("\n=== 示例5: 混合使用Java函数和JS函数 ===");
        
        try {
            // 确保已加载Formula.js库
            if (!FormulaCalculator.isFormulaJsLibraryLoaded()) {
                FormulaCalculator.loadFormulaJsLibrary(
                    "https://cdn.jsdelivr.net/npm/@formulajs/formulajs/lib/browser/formula.min.js"
                );
            }
            
            // 准备数据
            JSONObject data = new JSONObject();
            data.put("price", 100);
            data.put("quantity", 5);
            
            // Java实现的函数（如SUM）和JS函数混合使用
            String formula1 = "SUM(${price}, ${quantity}) + ABS(-50)";
            Object result1 = FormulaCalculator.calculate(formula1, data);
            System.out.println("SUM(${price}, ${quantity}) + ABS(-50) = " + result1);
            // 预期结果: 105 + 50 = 155
            
            // 使用FormulaData结构
            FormulaData formulaData = new FormulaData();
            formulaData.setTxt("SUM(单价,数量) + ABS(-10)");
            
            List<FieldMark> marks = new ArrayList<>();
            marks.add(new FieldMark("price", "单价"));
            marks.add(new FieldMark("quantity", "数量"));
            formulaData.setMarks(marks);
            
            Object result2 = FormulaCalculator.calculate(formulaData, data);
            System.out.println("SUM(单价,数量) + ABS(-10) = " + result2);
            // 预期结果: 105 + 10 = 115
            
        } catch (Exception e) {
            System.err.println("❌ 计算失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 示例6: 检查是否已加载库
     */
    public static void example6_CheckLibraryStatus() {
        System.out.println("\n=== 示例6: 检查库状态 ===");
        
        boolean loaded = FormulaCalculator.isFormulaJsLibraryLoaded();
        System.out.println("Formula.js库是否已加载: " + loaded);
        
        if (loaded) {
            String path = FormulaCalculator.getLoadedFormulaJsPath();
            System.out.println("已加载路径: " + path);
        }
        
        // 清除库
        FormulaCalculator.clearFormulaJsLibrary();
        System.out.println("已清除Formula.js库");
        
        loaded = FormulaCalculator.isFormulaJsLibraryLoaded();
        System.out.println("清除后是否已加载: " + loaded);
    }
}

