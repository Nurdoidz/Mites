package com.nurdoidz.mites.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class FormulasTest {

    @DisplayName("0 appetite returns double baseDigestTime")
    @Test
    void withBaseDigestTimeOf10_appetiteOf0_returns5() {
        int baseDigestTime = 10;
        int appetite = 0;
        int finalDigestTime = Formulas.getFinalDigestTime(baseDigestTime, appetite);
        assertEquals(20, finalDigestTime, "Given baseDigestTime of 10");
    }

    @DisplayName("31 appetite returns half baseDigestTime")
    @Test
    void withBaseDigestTimeOf10_appetiteOf31_returns20() {
        int baseDigestTime = 10;
        int appetite = 31;
        int finalDigestTime = Formulas.getFinalDigestTime(baseDigestTime, appetite);
        assertEquals(5, finalDigestTime, "Given baseDigestTime of 10");
    }
}
