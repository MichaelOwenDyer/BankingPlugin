package com.monst.bankingplugin.utils;

import com.monst.bankingplugin.geo.BlockVector2D;
import com.monst.bankingplugin.geo.BlockVector3D;

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

    public static double vectorMagnitude(int x1, int x2, int... xs) {
        double squaredSum = Math.pow(x1, 2) + Math.pow(x2, 2);
        for (int x : xs)
            squaredSum += Math.pow(x, 2);
        return Math.sqrt(squaredSum);
    }

    public static float[] unitVector(BlockVector2D from, BlockVector2D to) {
        BlockVector2D diff = from.vectorTo(to);
        double magnitude = vectorMagnitude(diff.getX(), diff.getZ());
        return new float[] {
                (float) (diff.getX() / magnitude),
                (float) (diff.getZ() / magnitude)
        };
    }

    public static float[] unitVector(BlockVector3D from, BlockVector3D to) {
        BlockVector3D diff = from.vectorTo(to);
        double magnitude = vectorMagnitude(diff.getX(), diff.getY(), diff.getZ());
        return new float[] {
                (float) (diff.getX() / magnitude),
                (float) (diff.getY() / magnitude),
                (float) (diff.getZ() / magnitude)
        };
    }

}
