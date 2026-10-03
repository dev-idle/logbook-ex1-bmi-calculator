package com.comp1786.logbook.bmi.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Decimal rounding for displayed values. {@link BigDecimal#valueOf(double)} rounds 24.95 as
 * written, not as its binary approximation.
 */
final class Rounding {

    private Rounding() {
    }

    /** Rounds to {@code scale} decimal places, with halves rounded away from zero. */
    static double halfUp(double value, int scale) {
        return round(value, scale, RoundingMode.HALF_UP);
    }

    /** Rounds up to {@code scale} decimal places. */
    static double up(double value, int scale) {
        return round(value, scale, RoundingMode.CEILING);
    }

    /** Rounds down to {@code scale} decimal places. */
    static double down(double value, int scale) {
        return round(value, scale, RoundingMode.FLOOR);
    }

    private static double round(double value, int scale, RoundingMode mode) {
        return BigDecimal.valueOf(value).setScale(scale, mode).doubleValue();
    }
}
