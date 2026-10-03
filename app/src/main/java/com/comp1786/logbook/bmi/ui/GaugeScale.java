package com.comp1786.logbook.bmi.ui;

import com.comp1786.logbook.bmi.domain.BmiCategory;

/**
 * Places a BMI on the category gauge, where every category has the same width. The open-ended
 * first and last categories are drawn from BMI 15 and up to BMI 45.
 */
final class GaugeScale {

    private static final double LOWEST_BMI = 15.0;
    private static final double HIGHEST_BMI = 45.0;

    private GaugeScale() {
    }

    /** The position of {@code bmi} in segments: 1.5 is the middle of the second category. */
    static float position(double bmi) {
        BmiCategory category = BmiCategory.of(bmi);
        double lower = Math.max(category.lowerBound(), LOWEST_BMI);
        double upper = Math.min(category.upperBound(), HIGHEST_BMI);
        double offset = Math.max(0.0, Math.min(1.0, (bmi - lower) / (upper - lower)));
        return (float) (category.ordinal() + offset);
    }
}
