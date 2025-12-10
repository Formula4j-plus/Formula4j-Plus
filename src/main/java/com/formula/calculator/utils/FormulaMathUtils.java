package com.formula.calculator.utils;

/**
 * 公式计算数学工具类
 * 提供数学相关的辅助函数
 * 
 * @author Formula Calculator
 */
public class FormulaMathUtils {
    
    /**
     * 计算阶乘
     */
    public static double factorial(int n) {
        if (n < 0) return 0;
        if (n == 0 || n == 1) return 1;
        double result = 1;
        for (int i = 2; i <= n; i++) {
            result *= i;
        }
        return result;
    }
    
    /**
     * 计算伽马函数（简化实现）
     */
    public static double gamma(double z) {
        // 使用Stirling近似
        if (z < 0.5) {
            return Math.PI / (Math.sin(Math.PI * z) * gamma(1 - z));
        }
        z -= 1;
        double x = 0.99999999999980993;
        double[] coefficients = {
            676.5203681218851, -1259.1392167224028, 771.32342877765313,
            -176.61502916214059, 12.507343278686905, -0.13857109526572012,
            9.9843695780195716e-6, 1.5056327351493116e-7
        };
        for (int i = 0; i < coefficients.length; i++) {
            x += coefficients[i] / (z + i + 1);
        }
        double t = z + coefficients.length - 0.5;
        return Math.sqrt(2 * Math.PI) * Math.pow(t, z + 0.5) * Math.exp(-t) * x;
    }
    
    /**
     * 近似误差函数
     */
    public static double approximateErf(double x) {
        // 使用Abramowitz和Stegun的近似公式
        double a1 =  0.254829592;
        double a2 = -0.284496736;
        double a3 =  1.421413741;
        double a4 = -1.453152027;
        double a5 =  1.061405429;
        double p  =  0.3275911;
        
        int sign = x < 0 ? -1 : 1;
        x = Math.abs(x);
        
        double t = 1.0 / (1.0 + p * x);
        double y = 1.0 - (((((a5 * t + a4) * t) + a3) * t + a2) * t + a1) * t * Math.exp(-x * x);
        
        return sign * y;
    }
    
    /**
     * 计算Beta函数
     */
    public static double beta(double a, double b) {
        return gamma(a) * gamma(b) / gamma(a + b);
    }
    
    /**
     * 不完全Beta函数（简化实现）
     */
    public static double incompleteBeta(double x, double a, double b) {
        if (x <= 0) return 0;
        if (x >= 1) return 1;
        // 简化实现：使用数值积分
        int n = 100;
        double sum = 0;
        double dx = x / n;
        for (int i = 0; i < n; i++) {
            double t = (i + 0.5) * dx;
            sum += Math.pow(t, a - 1) * Math.pow(1 - t, b - 1) * dx;
        }
        return sum / beta(a, b);
    }
    
    /**
     * 近似正态分布反函数
     */
    public static double approximateNormInv(double p) {
        // Beasley-Springer-Moro算法（简化版）
        double[] a = {-3.969683028665376e+01, 2.209460984245205e+02,
                     -2.759285104469687e+02, 1.383577518672690e+02,
                     -3.066479806614716e+01, 2.506628277459239e+00};
        double[] b = {-5.447609879822406e+01, 1.615858368580409e+02,
                     -1.556989798598866e+02, 6.680131188771972e+01,
                     -1.328068155288572e+01};
        double[] c = {-7.784894002430293e-03, -3.223964580411365e-01,
                     -2.400758277161838e+00, -2.549732539343734e+00,
                     4.374664141464968e+00, 2.938163982698783e+00};
        double[] d = {7.784695709041462e-03, 3.224671290700398e-01,
                     2.445134137142996e+00, 3.754408661907416e+00};
        
        double q = p - 0.5;
        double r, x;
        
        if (Math.abs(q) <= 0.425) {
            r = 0.180625 - q * q;
            x = q * (((((a[0] * r + a[1]) * r + a[2]) * r + a[3]) * r + a[4]) * r + a[5]) /
                (((((b[0] * r + b[1]) * r + b[2]) * r + b[3]) * r + b[4]) * r + 1);
        } else {
            r = q < 0 ? p : 1 - p;
            r = Math.sqrt(-Math.log(r));
            if (r <= 5.0) {
                r = r - 1.6;
                x = (((((c[0] * r + c[1]) * r + c[2]) * r + c[3]) * r + c[4]) * r + c[5]) /
                    ((((d[0] * r + d[1]) * r + d[2]) * r + d[3]) * r + 1);
            } else {
                r = r - 5.0;
                x = (((((c[0] * r + c[1]) * r + c[2]) * r + c[3]) * r + c[4]) * r + c[5]) /
                    ((((d[0] * r + d[1]) * r + d[2]) * r + d[3]) * r + 1);
            }
            if (q < 0) x = -x;
        }
        return x;
    }
    
    /**
     * 计算最大公约数（GCD）
     */
    public static long gcd(long a, long b) {
        while (b != 0) {
            long temp = b;
            b = a % b;
            a = temp;
        }
        return Math.abs(a);
    }
    
    /**
     * 计算最小公倍数（LCM）
     */
    public static long lcm(long a, long b) {
        return Math.abs(a * b) / gcd(a, b);
    }
    
    /**
     * 计算二项分布概率
     */
    public static double binomialProbability(int n, int k, double p) {
        if (k < 0 || k > n) return 0;
        double comb = factorial(n) / (factorial(k) * factorial(n - k));
        return comb * Math.pow(p, k) * Math.pow(1 - p, n - k);
    }
    
    /**
     * 近似卡方分布累积分布函数
     */
    public static double approximateChiSquareCDF(double x, int df) {
        // 简化实现：使用正态近似
        if (df > 30) {
            double z = (Math.pow(x / df, 1.0/3) - (1 - 2.0/(9*df))) / Math.sqrt(2.0/(9*df));
            return 0.5 * (1 + approximateErf(z / Math.sqrt(2)));
        }
        // 小自由度：使用数值积分（简化）
        return 0.5; // 占位实现
    }
    
    /**
     * 近似卡方分布概率密度函数
     */
    public static double approximateChiSquarePDF(double x, int df) {
        if (x <= 0) return 0;
        double coefficient = Math.pow(2, -df/2.0) / gamma(df/2.0);
        return coefficient * Math.pow(x, df/2.0 - 1) * Math.exp(-x/2.0);
    }
    
    /**
     * 近似F分布累积分布函数
     */
    public static double approximateFCDF(double x, int df1, int df2) {
        // 简化实现
        return 0.5; // 占位实现
    }
    
    /**
     * 近似F分布概率密度函数
     */
    public static double approximateFPDF(double x, int df1, int df2) {
        if (x <= 0) return 0;
        double coefficient = Math.sqrt(Math.pow(df1 * x, df1) * Math.pow(df2, df2) / 
                                       Math.pow(df1 * x + df2, df1 + df2)) / 
                            (x * beta(df1/2.0, df2/2.0));
        return coefficient;
    }
}

