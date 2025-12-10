package com.formula.calculator.utils;

import java.util.List;

import static com.formula.calculator.utils.FormulaMathUtils.*;

/**
 * 公式计算统计工具类
 * 提供统计相关的辅助函数
 * 
 * @author Formula Calculator
 */
public class FormulaStatisticsUtils {
    
    /**
     * 计算IRR（简化实现）
     */
    public static double calculateIRR(List<Double> values, double guess) {
        // 简化实现：使用二分法
        double low = -0.99;
        double high = 10.0;
        double tolerance = 0.0001;
        int maxIterations = 100;
        
        for (int i = 0; i < maxIterations; i++) {
            double mid = (low + high) / 2;
            double npv = calculateNPV(values, mid);
            if (Math.abs(npv) < tolerance) {
                return mid;
            }
            if (npv > 0) {
                low = mid;
            } else {
                high = mid;
            }
        }
        return (low + high) / 2;
    }
    
    /**
     * 计算NPV
     */
    public static double calculateNPV(List<Double> values, double rate) {
        double npv = 0;
        for (int i = 0; i < values.size(); i++) {
            npv += values.get(i) / Math.pow(1 + rate, i);
        }
        return npv;
    }
    
    /**
     * 计算利率（简化实现）
     */
    public static double calculateRate(int nper, double pmt, double pv, double fv, int type, double guess) {
        // 简化实现：使用迭代法
        double rate = guess;
        double tolerance = 0.0001;
        int maxIterations = 100;
        
        for (int i = 0; i < maxIterations; i++) {
            double f = pv * Math.pow(1 + rate, nper) + 
                      pmt * (1 + rate * type) * ((Math.pow(1 + rate, nper) - 1) / rate) + fv;
            double fPrime = nper * pv * Math.pow(1 + rate, nper - 1) + 
                           pmt * (1 + rate * type) * nper * Math.pow(1 + rate, nper - 1) / rate -
                           pmt * (1 + rate * type) * (Math.pow(1 + rate, nper) - 1) / (rate * rate);
            double newRate = rate - f / fPrime;
            if (Math.abs(newRate - rate) < tolerance) {
                return newRate;
            }
            rate = newRate;
        }
        return rate;
    }
    
    /**
     * 计算线性回归的斜率和截距
     */
    public static RegressionResult linearRegression(List<Double> xValues, List<Double> yValues) {
        if (xValues.size() != yValues.size() || xValues.isEmpty()) {
            return new RegressionResult(0, 0);
        }
        
        double avgX = xValues.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double avgY = yValues.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double sumXY = 0, sumX2 = 0;
        
        for (int i = 0; i < xValues.size(); i++) {
            double dx = xValues.get(i) - avgX;
            double dy = yValues.get(i) - avgY;
            sumXY += dx * dy;
            sumX2 += dx * dx;
        }
        
        double slope = sumX2 == 0 ? 0 : sumXY / sumX2;
        double intercept = avgY - slope * avgX;
        
        return new RegressionResult(slope, intercept);
    }
    
    /**
     * 计算相关系数
     */
    public static double correlation(List<Double> array1, List<Double> array2) {
        if (array1.size() != array2.size() || array1.isEmpty()) return 0;
        
        double avg1 = array1.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double avg2 = array2.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double sumXY = 0, sumX2 = 0, sumY2 = 0;
        
        for (int i = 0; i < array1.size(); i++) {
            double x = array1.get(i) - avg1;
            double y = array2.get(i) - avg2;
            sumXY += x * y;
            sumX2 += x * x;
            sumY2 += y * y;
        }
        
        double denominator = Math.sqrt(sumX2 * sumY2);
        return denominator == 0 ? 0 : sumXY / denominator;
    }
    
    /**
     * 计算协方差
     */
    public static double covariance(List<Double> array1, List<Double> array2, boolean isSample) {
        if (array1.size() != array2.size() || array1.isEmpty()) return 0;
        if (isSample && array1.size() < 2) return 0;
        
        double avg1 = array1.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double avg2 = array2.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double sum = 0;
        
        for (int i = 0; i < array1.size(); i++) {
            sum += (array1.get(i) - avg1) * (array2.get(i) - avg2);
        }
        
        return isSample ? sum / (array1.size() - 1) : sum / array1.size();
    }
    
    /**
     * 计算百分位数
     */
    public static double percentile(List<Double> values, double k) {
        if (values.isEmpty() || k < 0 || k > 1) return 0;
        values.sort(Double::compareTo);
        double index = k * (values.size() - 1);
        int lower = (int) Math.floor(index);
        int upper = (int) Math.ceil(index);
        if (lower == upper) {
            return values.get(lower);
        }
        double weight = index - lower;
        return values.get(lower) * (1 - weight) + values.get(upper) * weight;
    }
    
    /**
     * 线性回归结果
     */
    public static class RegressionResult {
        public final double slope;
        public final double intercept;
        
        public RegressionResult(double slope, double intercept) {
            this.slope = slope;
            this.intercept = intercept;
        }
        
        public double predict(double x) {
            return slope * x + intercept;
        }
    }
}

