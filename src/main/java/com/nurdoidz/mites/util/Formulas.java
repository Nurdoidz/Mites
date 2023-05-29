package com.nurdoidz.mites.util;

public interface Formulas {
    static int getFinalDigestTime(int baseDigestTime, int appetite) {
        return (int) (baseDigestTime * Math.pow(2, (0 - Math.pow((2 * appetite - 31), 3)) / 29791));
    }
}
