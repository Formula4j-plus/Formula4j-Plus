package com.formula.calculator.model;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 前端公式数据结构
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
     * 从JSONObject创建FormulaData
     */
    public static FormulaData fromJSON(JSONObject json) {
        FormulaData data = new FormulaData();
        data.setTxt(json.getString("txt"));
        
        JSONArray marksArray = json.getJSONArray("marks");
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

