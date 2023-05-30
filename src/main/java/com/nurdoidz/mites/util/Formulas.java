package com.nurdoidz.mites.util;

public interface Formulas {
    static int getFinalDigestTime(int baseDigestTime, int appetite) {
        return (int) (baseDigestTime * Math.pow(2, (0 - Math.pow((2 * appetite - 31), 3)) / 29791));
    }

    static double getRollPercentage(int greed) {
        return 1.0/20 * Math.pow(Math.pow(4, 1.0/31), greed);
    }
}
