package com.formula.calculator.utils;

/**
 * 公式计算字符串工具类
 * 提供字符串相关的辅助函数
 * 
 * @author Formula Calculator
 */
public class FormulaStringUtils {
    
    /**
     * 转换为罗马数字
     */
    public static String toRoman(int number) {
        if (number <= 0 || number > 3999) {
            return "";
        }
        
        String[] thousands = {"", "M", "MM", "MMM"};
        String[] hundreds = {"", "C", "CC", "CCC", "CD", "D", "DC", "DCC", "DCCC", "CM"};
        String[] tens = {"", "X", "XX", "XXX", "XL", "L", "LX", "LXX", "LXXX", "XC"};
        String[] ones = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX"};
        
        return thousands[number / 1000] +
               hundreds[(number % 1000) / 100] +
               tens[(number % 100) / 10] +
               ones[number % 10];
    }
    
    /**
     * 从罗马数字转换
     */
    public static int fromRoman(String roman) {
        if (roman == null || roman.isEmpty()) {
            return 0;
        }
        
        java.util.Map<Character, Integer> map = new java.util.HashMap<>();
        map.put('I', 1);
        map.put('V', 5);
        map.put('X', 10);
        map.put('L', 50);
        map.put('C', 100);
        map.put('D', 500);
        map.put('M', 1000);
        
        int result = 0;
        int prev = 0;
        for (int i = roman.length() - 1; i >= 0; i--) {
            int curr = map.getOrDefault(roman.charAt(i), 0);
            if (curr < prev) {
                result -= curr;
            } else {
                result += curr;
            }
            prev = curr;
        }
        return result;
    }
}

