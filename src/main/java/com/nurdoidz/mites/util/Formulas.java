package com.nurdoidz.mites.util;

import com.nurdoidz.mites.entity.Mite;
import java.util.Random;

public interface Formulas {

    static int getFinalDigestTime(int baseDigestTime, int appetite) {
        return (int) (baseDigestTime * Math.pow(2, (0 - Math.pow((2 * appetite - 31), 3)) / 29791));
    }

    static double getRollPercentage(int greed) {
        return 1.0 / 20 * Math.pow(Math.pow(4, 1.0 / 31), greed);
    }

    static byte getNewIV() {
        return (byte) new Random().nextInt(32);
    }

    static void applyIvInheritance(Mite pFather, Mite pMother, Mite pChild) {
        byte maxAppetite = (byte) (Math.max(pFather.getAppetite(), pMother.getAppetite()));
        byte maxGreed = (byte) (Math.max(pFather.getGreed(), pMother.getGreed()));
        if (maxAppetite == maxGreed) {
            if (maxAppetite == 31) {
                pChild.setAppetite((byte) 31);
                pChild.setGreed((byte) 31);
                return;
            }
            pChild.setAppetite((byte) (new Random().nextInt(31 - maxAppetite) + maxAppetite));
            pChild.setGreed((byte) (new Random().nextInt(31 - maxGreed) + maxGreed));
            return;
        } else if (maxAppetite > maxGreed) {
            pChild.setAppetite(maxAppetite);
            pChild.setGreed(getNewIV());
            return;
        }
        pChild.setAppetite(getNewIV());
        pChild.setGreed(maxGreed);
    }
}
