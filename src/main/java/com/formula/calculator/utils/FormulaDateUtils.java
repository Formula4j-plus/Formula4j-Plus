package com.formula.calculator.utils;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * 公式计算日期工具类
 * 提供日期相关的辅助函数
 * 
 * @author WanShen
 */
public class FormulaDateUtils {
    
    /**
     * Excel日期序列号起始日期（1900-01-01）
     */
    private static final Calendar EXCEL_EPOCH = new GregorianCalendar(1900, 0, 1);
    
    /**
     * 将Excel日期序列号转换为Calendar
     */
    public static Calendar dateFromSerial(double serial) {
        Calendar cal = Calendar.getInstance();
        long days = (long) serial;
        // Excel日期系统：1900-01-01是第1天
        cal.setTimeInMillis(EXCEL_EPOCH.getTimeInMillis());
        cal.add(Calendar.DAY_OF_MONTH, (int) days - 1);
        return cal;
    }
    
    /**
     * 将Calendar转换为Excel日期序列号
     */
    public static long dateToSerial(Calendar cal) {
        long days = (cal.getTimeInMillis() - EXCEL_EPOCH.getTimeInMillis()) / (1000L * 60 * 60 * 24);
        return days + 1; // Excel日期系统：1900-01-01是第1天
    }
    
    /**
     * 计算两个日期之间的工作日数（不考虑节假日）
     */
    public static int networkDays(Calendar startDate, Calendar endDate) {
        int workdays = 0;
        Calendar current = (Calendar) startDate.clone();
        while (!current.after(endDate)) {
            int dayOfWeek = current.get(Calendar.DAY_OF_WEEK);
            if (dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY) {
                workdays++;
            }
            current.add(Calendar.DAY_OF_MONTH, 1);
        }
        return workdays;
    }
    
    /**
     * 计算指定天数后的工作日（不考虑节假日）
     */
    public static Calendar workday(Calendar startDate, int days) {
        Calendar cal = (Calendar) startDate.clone();
        int addedDays = 0;
        while (addedDays < Math.abs(days)) {
            cal.add(Calendar.DAY_OF_MONTH, days > 0 ? 1 : -1);
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            if (dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY) {
                addedDays++;
            }
        }
        return cal;
    }
}

