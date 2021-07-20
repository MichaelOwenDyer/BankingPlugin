package com.monst.bankingplugin.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class QuickMath {

    public static BigDecimal divide(BigDecimal dividend, double divisor) {
        return divide(dividend, BigDecimal.valueOf(divisor));
    }

    public static BigDecimal divide(BigDecimal dividend, long divisor) {
        return divide(dividend, BigDecimal.valueOf(divisor));
    }

    public static BigDecimal divide(BigDecimal dividend, BigDecimal divisor) {
        return dividend.divide(divisor, RoundingMode.HALF_EVEN);
    }

    public static BigDecimal multiply(BigDecimal multiplicand, double factor) {
        return multiplicand.multiply(BigDecimal.valueOf(factor));
    }

    public static BigDecimal multiply(BigDecimal multiplicand, long factor) {
        return multiplicand.multiply(BigDecimal.valueOf(factor));
    }

    public static double scale(double d) {
        return scale(d, 2);
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
