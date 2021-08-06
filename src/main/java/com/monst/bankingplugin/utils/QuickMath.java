package com.monst.bankingplugin.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class QuickMath {

    public static BigDecimal divide(BigDecimal dividend, double divisor) {
        return divide(dividend, divisor, 2);
    }

    public static BigDecimal divide(BigDecimal dividend, double divisor, int scale) {
        return dividend.divide(BigDecimal.valueOf(divisor), scale, RoundingMode.HALF_EVEN);
    }

    public static BigDecimal divide(BigDecimal dividend, long divisor) {
        return divide(dividend, divisor, 2);
    }

    public static BigDecimal divide(BigDecimal dividend, long divisor, int scale) {
        return dividend.divide(BigDecimal.valueOf(divisor), scale, RoundingMode.HALF_EVEN);
    }

    public static BigDecimal divide(BigDecimal dividend, BigDecimal divisor) {
        return divide(dividend, divisor, 2);
    }

    public static BigDecimal divide(BigDecimal dividend, BigDecimal divisor, int scale) {
        return dividend.divide(divisor, scale, RoundingMode.HALF_EVEN);
    }

    public static BigDecimal multiply(BigDecimal multiplicand, double factor) {
        return multiply(multiplicand, factor, 2);
    }

    public static BigDecimal multiply(BigDecimal multiplicand, double factor, int scale) {
        return multiplicand.multiply(BigDecimal.valueOf(factor)).setScale(scale, RoundingMode.HALF_EVEN);
    }

    public static BigDecimal multiply(BigDecimal multiplicand, long factor) {
        return multiply(multiplicand, factor, 2);
    }

    public static BigDecimal multiply(BigDecimal multiplicand, long factor, int scale) {
        return multiplicand.multiply(BigDecimal.valueOf(factor)).setScale(scale, RoundingMode.HALF_EVEN);
    }

    public static BigDecimal multiply(BigDecimal multiplicand, BigDecimal factor) {
        return multiply(multiplicand, factor, 2);
    }

    public static BigDecimal multiply(BigDecimal multiplicand, BigDecimal factor, int scale) {
        return multiplicand.multiply(factor).setScale(scale, RoundingMode.HALF_EVEN);
    }

    public static double scale(double d) {
        return scale(BigDecimal.valueOf(d)).doubleValue();
    }

    public static double scale(double d, int scale) {
        return scale(BigDecimal.valueOf(d), scale).doubleValue();
    }

    public static BigDecimal scale(BigDecimal bd) {
        return scale(bd, 2);
    }

    public static BigDecimal scale(BigDecimal bd, int scale) {
        return bd.setScale(scale, RoundingMode.HALF_EVEN);
    }

}
