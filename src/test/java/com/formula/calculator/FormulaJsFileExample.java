package com.formula.calculator;

import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 从Formula.js文件加载自定义函数示例
 * 
 * 展示如何从指定路径加载Formula.js文件并注册自定义函数
 */
public class FormulaJsFileExample {
    
    public static void main(String[] args) {
        // ============================================
        // 示例1: 从本地文件加载Formula.js
        // ============================================
//        example1_LoadFromLocalFile();
        
        // ============================================
        // 示例2: 从类路径加载Formula.js
        // ============================================
//        example2_LoadFromClasspath();
        
        // ============================================
        // 示例3: 从URL加载Formula.js
        // ============================================
         example3_LoadFromURL(); // 需要网络连接，注释掉
    }
    
    /**
     * 示例1: 从本地文件加载Formula.js
     * 
     * 创建一个Formula.js文件，然后从文件路径加载
     */
    private static void example1_LoadFromLocalFile() {
        System.out.println("\n========== 示例1: 从本地文件加载Formula.js ==========");
        
        try {
            // 1. 创建一个临时的Formula.js文件
            Path tempFile = Files.createTempFile("formula", ".js");
            String formulaJsContent = 
                "// Formula.js自定义函数\n" +
                "var MYJSSUM = function(a, b) {\n" +
                "    return a + b;\n" +
                "};\n" +
                "\n" +
                "var MYJSPRODUCT = function(a, b) {\n" +
                "    return a * b;\n" +
                "};\n" +
                "\n" +
                "var DISCOUNT_PRICE = function(price, discount) {\n" +
                "    return price * (1 - discount);\n" +
                "};";
            
            try (java.io.OutputStreamWriter writer = new java.io.OutputStreamWriter(
                    new java.io.FileOutputStream(tempFile.toFile()), 
                    java.nio.charset.StandardCharsets.UTF_8)) {
                writer.write(formulaJsContent);
            }
            
            System.out.println("创建Formula.js文件: " + tempFile.toAbsolutePath());
            
            // 2. 从文件路径注册函数
            // 方式1: 使用file://前缀
            String filePath = "file://" + tempFile.toAbsolutePath().toString().replace("\\", "/");
            FormulaCalculator.registerCustomFunctionFromFile("MYJSSUM", filePath, "MYJSSUM");
            
            // 方式2: 直接使用文件路径（Windows需要处理）
            String directPath = tempFile.toAbsolutePath().toString();
            FormulaCalculator.registerCustomFunctionFromFile("MYJSPRODUCT", directPath, "MYJSPRODUCT");
            
            // 方式3: 注册另一个函数
            FormulaCalculator.registerCustomFunctionFromFile("DISCOUNT_PRICE", filePath, "DISCOUNT_PRICE");
            
            // 3. 使用自定义函数
            String formula1 = "MYJSSUM(10, 20)";
            JSONObject data = new JSONObject();
            Object result1 = FormulaCalculator.calculate(formula1, data);
            System.out.println("公式: " + formula1);
            System.out.println("结果: " + result1); // 应该输出: 30.0
            
            String formula2 = "MYJSPRODUCT(5, 6)";
            Object result2 = FormulaCalculator.calculate(formula2, data);
            System.out.println("公式: " + formula2);
            System.out.println("结果: " + result2); // 应该输出: 30.0
            
            String formula3 = "DISCOUNT_PRICE(100, 0.2)";
            Object result3 = FormulaCalculator.calculate(formula3, data);
            System.out.println("公式: " + formula3);
            System.out.println("结果: " + result3); // 应该输出: 80.0
            
            // 4. 清理临时文件
            Files.deleteIfExists(tempFile);
            System.out.println("已删除临时文件");
            
        } catch (IOException e) {
            System.err.println("创建临时文件失败: " + e.getMessage());
        }
    }
    
    /**
     * 示例2: 从类路径加载Formula.js
     * 
     * 将Formula.js文件放在resources目录下，然后从类路径加载
     */
    private static void example2_LoadFromClasspath() {
        System.out.println("\n========== 示例2: 从类路径加载Formula.js ==========");
        
        try {
            // 1. 创建一个Formula.js文件在resources目录
            // 注意：实际使用时，这个文件应该已经存在于resources目录
            // 这里我们创建一个临时文件来演示
            
            // 假设resources目录下有formula-custom.js文件
            String classpathPath = "classpath:formula-custom.js";
            
            // 如果文件不存在，创建一个示例文件
            Path resourcesDir = Paths.get("src", "test", "resources");
            if (!Files.exists(resourcesDir)) {
                Files.createDirectories(resourcesDir);
            }
            
            Path formulaFile = resourcesDir.resolve("formula-custom.js");
            String formulaJsContent = 
                "// Formula.js自定义函数（从类路径加载）\n" +
                "var MYJSPOWER = function(base, exponent) {\n" +
                "    return Math.pow(base, exponent);\n" +
                "};\n" +
                "\n" +
                "var MYJSROOT = function(value, root) {\n" +
                "    return Math.pow(value, 1 / root);\n" +
                "};";
            
            try (java.io.OutputStreamWriter writer = new java.io.OutputStreamWriter(
                    new java.io.FileOutputStream(formulaFile.toFile()), 
                    java.nio.charset.StandardCharsets.UTF_8)) {
                writer.write(formulaJsContent);
            }
            
            System.out.println("创建Formula.js文件: " + formulaFile.toAbsolutePath());
            
            // 2. 从类路径注册函数
            FormulaCalculator.registerCustomFunctionFromFile("MYJSPOWER", classpathPath, "MYJSPOWER");
            FormulaCalculator.registerCustomFunctionFromFile("MYJSROOT", classpathPath, "MYJSROOT");
            
            // 3. 使用自定义函数
            String formula1 = "MYJSPOWER(2, 3)";
            JSONObject data = new JSONObject();
            Object result1 = FormulaCalculator.calculate(formula1, data);
            System.out.println("公式: " + formula1);
            System.out.println("结果: " + result1); // 应该输出: 8.0
            
            String formula2 = "MYJSROOT(8, 3)";
            Object result2 = FormulaCalculator.calculate(formula2, data);
            System.out.println("公式: " + formula2);
            System.out.println("结果: " + result2); // 应该输出: 2.0（8的3次方根）
            
        } catch (IOException e) {
            System.err.println("处理类路径文件失败: " + e.getMessage());
        }
    }
    
    /**
     * 示例3: 从URL加载Formula.js
     * 
     * 从HTTP/HTTPS URL加载Formula.js文件
     */
    private static void example3_LoadFromURL() {
        System.out.println("\n========== 示例3: 从URL加载Formula.js ==========");
        
        // 注意：这需要网络连接，实际URL需要替换为真实的Formula.js文件地址
        String url = "https://cdn.jsdelivr.net/npm/@formulajs/formulajs/lib/browser/formula.min.js";
        
        try {
            // 从URL注册函数
            FormulaCalculator.registerCustomFunctionFromFile("SUM", url, "SUM");
            
            // 使用自定义函数
            String formula = "SUM(10, 20)";
            JSONObject data = new JSONObject();
            Object result = FormulaCalculator.calculate(formula, data);
            System.out.println("公式: " + formula);
            System.out.println("结果: " + result);
            
        } catch (Exception e) {
            System.err.println("从URL加载失败: " + e.getMessage());
        }
    }
}

