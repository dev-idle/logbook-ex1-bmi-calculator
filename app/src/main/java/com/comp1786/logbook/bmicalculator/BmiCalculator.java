package com.comp1786.logbook.bmicalculator;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Calculates BMI with the WHO formula: weight (kg) / height (m) squared.
 */
public final class BmiCalculator {

    /** Decimal places used in WHO Table 2.1. */
    public static final int BMI_SCALE = 2;

    // A BMI this far below a bound still rounds up to it, e.g. 24.995 to 25.00.
    private static final BigDecimal ROUNDING_MARGIN = new BigDecimal("0.005");

    private BmiCalculator() {
    }

    /**
     * Returns the BMI rounded half up to two decimal places.
     *
     * @throws IllegalArgumentException if {@code meters} is not positive
     */
    public static BigDecimal calculate(BigDecimal kilograms, BigDecimal meters) {
        if (meters.signum() <= 0) {
            throw new IllegalArgumentException("Height must be positive");
        }
        return kilograms.divide(meters.multiply(meters), BMI_SCALE, RoundingMode.HALF_UP);
    }

    /** Returns the lowest weight in kg whose BMI rounds into the normal range. */
    public static BigDecimal normalRangeMinimum(BigDecimal meters) {
        BigDecimal minimumBmi = BmiCategory.NORMAL_RANGE.getLowerBound().subtract(ROUNDING_MARGIN);
        return minimumBmi.multiply(meters.multiply(meters));
    }

    /** Returns the weight in kg at which BMI reaches 25.00; normal weights are below it. */
    public static BigDecimal normalRangeLimit(BigDecimal meters) {
        BigDecimal limitBmi = BmiCategory.PREOBESE.getLowerBound().subtract(ROUNDING_MARGIN);
        return limitBmi.multiply(meters.multiply(meters));
    }
}
