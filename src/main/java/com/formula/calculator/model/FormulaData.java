package com.formula.calculator.model;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 前端公式数据结构
 * 
 * <p>默认结构：</p>
 * <pre>
 * {
 *   "txt": "公式(字段1,field2)",
 *   "marks": [
 *     {
 *       "enCode": "字段key",
 *       "menuId": "字段名称",
 *       "form": {"ch": 0, "line": 0, "stick": null}
 *     }
 *   ]
 * }
 * </pre>
 * 
 * <p>支持自定义字段名结构，例如：</p>
 * <pre>
 * {
 *   "formula": "SUM(字段1,字段2)",
 *   "fields": [
 *     {"enCode": "field1", "menuId": "字段1"}
 *   ]
 * }
 * </pre>
 * 
 * <p>使用方式：</p>
 * <ul>
 *   <li>默认：FormulaData.fromJSON(json) - 使用 txt/marks</li>
 *   <li>自定义：FormulaData.fromJSON(json, "formula", "fields") - 指定字段名</li>
 *   <li>映射配置：FormulaData.fromJSON(json, fieldMapping) - 使用字段名映射</li>
 * </ul>
 */
public class FormulaData {
    
    /**
     * 公式文本，如: "SUM(字段1,字段2)"
     */
    private String txt;
    
    /**
     * 字段标记数组
     */
    private List<FieldMark> marks;
    
    public FormulaData() {
        this.marks = new ArrayList<>();
    }
    
    public FormulaData(String txt, List<FieldMark> marks) {
        this.txt = txt;
        this.marks = marks != null ? marks : new ArrayList<>();
    }
    
    /**
     * 从JSONObject创建FormulaData（使用默认字段名：txt, marks）
     */
    public static FormulaData fromJSON(JSONObject json) {
        return fromJSON(json, "txt", "marks");
    }
    
    /**
     * 从JSONObject创建FormulaData（支持自定义字段名）
     * 
     * @param json JSON对象
     * @param formulaKey 公式文本的字段名（如 "txt", "formula", "expression" 等）
     * @param marksKey 字段标记数组的字段名（如 "marks", "fields", "fieldMarks" 等）
     * @return FormulaData对象
     */
    public static FormulaData fromJSON(JSONObject json, String formulaKey, String marksKey) {
        FormulaData data = new FormulaData();
        
        // 尝试从指定的字段名获取公式文本
        String formula = json.getString(formulaKey);
        if (formula == null) {
            // 如果指定字段不存在，尝试常见的字段名
            formula = json.getString("txt");
            if (formula == null) {
                formula = json.getString("formula");
                if (formula == null) {
                    formula = json.getString("expression");
                }
            }
        }
        data.setTxt(formula);
        
        // 尝试从指定的字段名获取字段标记数组
        JSONArray marksArray = json.getJSONArray(marksKey);
        if (marksArray == null) {
            // 如果指定字段不存在，尝试常见的字段名
            marksArray = json.getJSONArray("marks");
            if (marksArray == null) {
                marksArray = json.getJSONArray("fields");
                if (marksArray == null) {
                    marksArray = json.getJSONArray("fieldMarks");
                }
            }
        }
        
        if (marksArray != null) {
            for (int i = 0; i < marksArray.size(); i++) {
                JSONObject markJson = marksArray.getJSONObject(i);
                FieldMark mark = FieldMark.fromJSON(markJson);
                data.getMarks().add(mark);
            }
        }
        
        return data;
    }
    
    /**
     * 从JSONObject创建FormulaData（使用字段名映射配置）
     * 
     * @param json JSON对象
     * @param fieldMapping 字段名映射配置，key为逻辑字段名（"formula"或"marks"），value为JSON中的实际字段名
     * @return FormulaData对象
     */
    public static FormulaData fromJSON(JSONObject json, Map<String, String> fieldMapping) {
        String formulaKey = fieldMapping != null && fieldMapping.containsKey("formula") 
            ? fieldMapping.get("formula") : "txt";
        String marksKey = fieldMapping != null && fieldMapping.containsKey("marks") 
            ? fieldMapping.get("marks") : "marks";
        return fromJSON(json, formulaKey, marksKey);
    }
    
    /**
     * 构建字段映射表：字段显示名称 -> enCode
     */
    public Map<String, String> buildFieldMapping() {
        Map<String, String> mapping = new HashMap<>();
        for (FieldMark mark : marks) {
            if (mark.getMenuId() != null && mark.getEnCode() != null) {
                mapping.put(mark.getMenuId(), mark.getEnCode());
            }
        }
        return mapping;
    }
    
    /**
     * 根据enCode获取字段标记
     */
    public FieldMark getMarkByEnCode(String enCode) {
        for (FieldMark mark : marks) {
            if (enCode.equals(mark.getEnCode())) {
                return mark;
            }
        }
        return null;
    }
    
    /**
     * 根据menuId获取字段标记
     */
    public FieldMark getMarkByMenuId(String menuId) {
        for (FieldMark mark : marks) {
            if (menuId.equals(mark.getMenuId())) {
                return mark;
            }
        }
        return null;
    }
    
    // Getters and Setters
    public String getTxt() {
        return txt;
    }
    
    public void setTxt(String txt) {
        this.txt = txt;
    }
    
    public List<FieldMark> getMarks() {
        return marks;
    }
    
    public void setMarks(List<FieldMark> marks) {
        this.marks = marks;
    }
}

