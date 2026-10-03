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

    /**
     * Removes floating-point error, which is around 1e-14 here, by rounding to nine decimal
     * places. From input at the precision the converter writes (0.1 kg or lb, 1 mm, 0.1 in),
     * every BMI and healthy weight limit lies exactly on a bound or at least 2e-7 from it, so
     * this cannot move such a value across a bound.
     */
    static double withoutFloatError(double value) {
        return halfUp(value, 9);
    }

    /** Rounds to {@code scale} decimal places, with halves rounded away from zero. */
    static double halfUp(double value, int scale) {
        return round(value, scale, RoundingMode.HALF_UP);
    }

    /** Rounds up to {@code scale} decimal places. */
    static double up(double value, int scale) {
        return round(value, scale, RoundingMode.CEILING);
    }

    /** The largest value with {@code scale} decimal places that is strictly below {@code value}. */
    static double below(double value, int scale) {
        BigDecimal exact = BigDecimal.valueOf(value);
        BigDecimal floor = exact.setScale(scale, RoundingMode.FLOOR);
        if (floor.compareTo(exact) == 0) {
            floor = floor.subtract(BigDecimal.ONE.movePointLeft(scale));
        }
        return floor.doubleValue();
    }

    private static double round(double value, int scale, RoundingMode mode) {
        return BigDecimal.valueOf(value).setScale(scale, mode).doubleValue();
    }
}
