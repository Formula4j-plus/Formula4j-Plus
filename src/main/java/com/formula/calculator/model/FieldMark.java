package com.formula.calculator.model;

import com.alibaba.fastjson.JSONObject;

/**
 * 字段标记信息
 */
public class FieldMark {
    
    /**
     * 字段编码（key），用于从数据中获取值
     */
    private String enCode;
    
    /**
     * 字段显示名称（menuId），在公式中使用
     */
    private String menuId;
    
    /**
     * 表单信息
     */
    private FormInfo form;
    
    public FieldMark() {
    }
    
    public FieldMark(String enCode, String menuId) {
        this.enCode = enCode;
        this.menuId = menuId;
    }
    
    /**
     * 从JSONObject创建FieldMark
     */
    public static FieldMark fromJSON(JSONObject json) {
        FieldMark mark = new FieldMark();
        mark.setEnCode(json.getString("enCode"));
        mark.setMenuId(json.getString("menuId"));
        
        JSONObject formJson = json.getJSONObject("form");
        if (formJson != null) {
            FormInfo formInfo = new FormInfo();
            formInfo.setCh(formJson.getInteger("ch"));
            formInfo.setLine(formJson.getInteger("line"));
            formInfo.setStick(formJson.get("stick"));
            mark.setForm(formInfo);
        }
        
        return mark;
    }
    
    // Getters and Setters
    public String getEnCode() {
        return enCode;
    }
    
    public void setEnCode(String enCode) {
        this.enCode = enCode;
    }
    
    public String getMenuId() {
        return menuId;
    }
    
    public void setMenuId(String menuId) {
        this.menuId = menuId;
    }
    
    public FormInfo getForm() {
        return form;
    }
    
    public void setForm(FormInfo form) {
        this.form = form;
    }
    
    /**
     * 表单信息内部类
     */
    public static class FormInfo {
        private Integer ch;
        private Integer line;
        private Object stick;
        
        public Integer getCh() {
            return ch;
        }
        
        public void setCh(Integer ch) {
            this.ch = ch;
        }
        
        public Integer getLine() {
            return line;
        }
        
        public void setLine(Integer line) {
            this.line = line;
        }
        
        public Object getStick() {
            return stick;
        }
        
        public void setStick(Object stick) {
            this.stick = stick;
        }
    }
}

