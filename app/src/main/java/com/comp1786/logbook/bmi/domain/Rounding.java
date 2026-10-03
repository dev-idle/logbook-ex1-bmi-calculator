package com.comp1786.logbook.bmi.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Decimal rounding for values shown to the user.
 *
 * <p>{@link BigDecimal#valueOf(double)} rounds the shortest decimal representation of the
 * double, so a value such as 24.95 rounds as written instead of as its binary approximation.
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
