package com.formula.calculator.example;

import com.alibaba.fastjson.JSONObject;
import com.formula.calculator.FormulaCalculator;
import com.formula.calculator.FormulaUtils;
import com.formula.calculator.model.FieldMark;
import com.formula.calculator.model.FormulaData;

import java.util.ArrayList;
import java.util.List;

/**
 * 公式计算使用示例
 */
public class FormulaExample {
    
    public static void main(String[] args) {
        // 示例1: 基本计算
        example1();
        
        // 示例2: 使用FormulaData结构
        example2();
        
        // 示例3: 使用函数
        example3();
        
        // 示例4: 逻辑函数
        example4();
        
        // 示例5: 统计函数
        example5();
        
        // 示例6: 数学函数
        example6();
        
        // 示例7: 验证前端计算结果
        example7();
    }
    
    /**
     * 示例1: 基本四则运算
     */
    public static void example1() {
        System.out.println("=== 示例1: 基本四则运算 ===");
        
        JSONObject data = new JSONObject();
        data.put("price", 100);      // 单价
        data.put("quantity", 5);     // 数量
        data.put("discount", 0.1);   // 折扣率
        
        // 计算总价: 单价 * 数量 * (1 - 折扣率)
        String formula = "${price} * ${quantity} * (1 - ${discount})";
        Object result = FormulaCalculator.calculate(formula, data);
        
        System.out.println("公式: " + formula);
        System.out.println("数据: " + data.toJSONString());
        System.out.println("结果: " + result);
        System.out.println();
    }
    
    /**
     * 示例2: 使用FormulaData结构（根据前端数据结构）
     */
    public static void example2() {
        System.out.println("=== 示例2: 使用FormulaData结构 ===");
        
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
        
        System.out.println("公式文本: " + formulaData.getTxt());
        System.out.println("字段映射: 单价->price, 数量->quantity, 折扣率->discount");
        System.out.println("数据: " + data.toJSONString());
        System.out.println("结果: " + result);
        System.out.println();
    }
    
    /**
     * 示例3: 使用常用函数
     */
    public static void example3() {
        System.out.println("=== 示例3: 使用常用函数 ===");
        
        FormulaData formulaData = new FormulaData();
        formulaData.setTxt("AVERAGE(分数1,分数2,分数3,分数4)");
        
        List<FieldMark> marks = new ArrayList<>();
        marks.add(new FieldMark("score1", "分数1"));
        marks.add(new FieldMark("score2", "分数2"));
        marks.add(new FieldMark("score3", "分数3"));
        marks.add(new FieldMark("score4", "分数4"));
        formulaData.setMarks(marks);
        
        JSONObject data = new JSONObject();
        data.put("score1", 85);
        data.put("score2", 90);
        data.put("score3", 78);
        data.put("score4", 92);
        
        Object result = FormulaCalculator.calculate(formulaData, data);
        
        System.out.println("公式: " + formulaData.getTxt());
        System.out.println("结果: " + result);
        System.out.println();
    }
    
    /**
     * 示例4: 逻辑函数
     */
    public static void example4() {
        System.out.println("=== 示例4: 逻辑函数 ===");
        
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
        
        System.out.println("公式: " + formulaData.getTxt());
        System.out.println("数据: " + data.toJSONString());
        System.out.println("结果: " + result);
        System.out.println();
    }
    
    /**
     * 示例5: 统计函数
     */
    public static void example5() {
        System.out.println("=== 示例5: 统计函数 ===");
        
        FormulaData formulaData = new FormulaData();
        formulaData.setTxt("STDEV(数据1,数据2,数据3,数据4,数据5)");
        
        List<FieldMark> marks = new ArrayList<>();
        marks.add(new FieldMark("data1", "数据1"));
        marks.add(new FieldMark("data2", "数据2"));
        marks.add(new FieldMark("data3", "数据3"));
        marks.add(new FieldMark("data4", "数据4"));
        marks.add(new FieldMark("data5", "数据5"));
        formulaData.setMarks(marks);
        
        JSONObject data = new JSONObject();
        data.put("data1", 10);
        data.put("data2", 20);
        data.put("data3", 30);
        data.put("data4", 40);
        data.put("data5", 50);
        
        Object result = FormulaCalculator.calculate(formulaData, data);
        
        System.out.println("公式: " + formulaData.getTxt());
        System.out.println("结果（标准差）: " + result);
        System.out.println();
    }
    
    /**
     * 示例6: 数学函数
     */
    public static void example6() {
        System.out.println("=== 示例6: 数学函数 ===");
        
        FormulaData formulaData = new FormulaData();
        formulaData.setTxt("SQRT(POWER(底数,指数))");
        
        List<FieldMark> marks = new ArrayList<>();
        marks.add(new FieldMark("base", "底数"));
        marks.add(new FieldMark("exp", "指数"));
        formulaData.setMarks(marks);
        
        JSONObject data = new JSONObject();
        data.put("base", 4);
        data.put("exp", 2);
        
        Object result = FormulaCalculator.calculate(formulaData, data);
        
        System.out.println("公式: " + formulaData.getTxt());
        System.out.println("结果: " + result);
        System.out.println();
    }
    
    /**
     * 示例7: 验证前端计算结果
     */
    public static void example7() {
        System.out.println("=== 示例7: 验证前端计算结果 ===");
        
        FormulaData formulaData = new FormulaData();
        formulaData.setTxt("SUM(字段1,字段2)");
        
        List<FieldMark> marks = new ArrayList<>();
        marks.add(new FieldMark("field1", "字段1"));
        marks.add(new FieldMark("field2", "字段2"));
        formulaData.setMarks(marks);
        
        JSONObject data = new JSONObject();
        data.put("field1", 10.5);
        data.put("field2", 20.3);
        
        double frontendResult = 30.8; // 前端Formula.js计算的结果
        
        // 后端重新计算
        Object backendResult = FormulaCalculator.calculate(formulaData, data);
        
        // 验证是否一致
        boolean verified = FormulaUtils.verify(formulaData, data, frontendResult);
        
        System.out.println("公式: " + formulaData.getTxt());
        System.out.println("前端结果: " + frontendResult);
        System.out.println("后端结果: " + backendResult);
        System.out.println("验证结果: " + (verified ? "一致" : "不一致"));
        System.out.println();
    }
}
