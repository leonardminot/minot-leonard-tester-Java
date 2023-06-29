package com.parkit.parkingsystem.util;

public class NumberUtil {
    public static double roundDoubleToNDecimals(double numberToRound, int decimals) {
        return (double) Math.round(numberToRound * Math.pow(10, decimals)) / Math.pow(10, decimals);
    }
}
