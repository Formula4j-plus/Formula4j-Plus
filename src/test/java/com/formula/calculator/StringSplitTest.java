package com.formula.calculator;

import org.junit.Test;
import java.util.*;

/**
 * 测试字符串分割逻辑
 */
public class StringSplitTest {
    
    @Test
    public void testSplit() {
        String expression = "\"Hello\"+123+\"World\"";
        System.out.println("测试表达式: " + expression);
        
        List<String> parts = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inString = false;
        boolean escaped = false;
        
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            
            if (escaped) {
                current.append(c);
                escaped = false;
                continue;
            }
            
            if (c == '\\') {
                escaped = true;
                current.append(c);
                continue;
            }
            
            if (c == '"') {
                inString = !inString;
                current.append(c);
                continue;
            }
            
            if (c == '+' && !inString) {
                // 在字符串外遇到+号，分割
                String part = current.toString().trim();
                if (part.length() > 0) {
                    parts.add(part);
                    System.out.println("分割部分: [" + part + "]");
                }
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        
        // 添加最后一部分
        String part = current.toString().trim();
        if (part.length() > 0) {
            parts.add(part);
            System.out.println("最后部分: [" + part + "]");
        }
        
        System.out.println("总共分割成: " + parts.size() + " 部分");
        for (int i = 0; i < parts.size(); i++) {
            System.out.println("  部分 " + i + ": [" + parts.get(i) + "]");
        }
    }
}

