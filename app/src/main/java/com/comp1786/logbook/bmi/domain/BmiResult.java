package com.comp1786.logbook.bmi.domain;

import java.io.Serializable;

/**
 * The outcome of a BMI calculation. It is serializable so that the screen state can be saved.
 *
 * @param bmi                           the BMI, rounded to nine decimal places to remove
 *                                      floating-point error
 * @param category                      the category of {@code bmi}
 * @param healthyWeightMinimumKilograms the lowest healthy weight for the height (inclusive)
 * @param healthyWeightLimitKilograms   the weight at which overweight starts (exclusive)
 */
public record BmiResult(
        double bmi,
        BmiCategory category,
        double healthyWeightMinimumKilograms,
        double healthyWeightLimitKilograms) implements Serializable {

    /**
     * The BMI to two decimal places, so a value just below a bound, such as 24.96, is not shown
     * as the bound. It never rounds up into the next category: 24.996 shows as 24.99.
     */
    public double roundedBmi() {
        double rounded = Rounding.halfUp(bmi, 2);
        double nextCategory = category.upperBound();
        return rounded < nextCategory ? rounded : Rounding.below(nextCategory, 2);
    }

    /** The lowest healthy weight in {@code unit}, to one decimal place. */
    public double healthyWeightMinimum(WeightUnit unit) {
        return Rounding.up(inUnit(healthyWeightMinimumKilograms, unit), 1);
    }

    /** The highest healthy weight in {@code unit}, to one decimal place. */
    public double healthyWeightMaximum(WeightUnit unit) {
        return Rounding.below(inUnit(healthyWeightLimitKilograms, unit), 1);
    }

    // At 160 cm the overweight limit is exactly 64 kg but computes as 64.00000000000001.
    private static double inUnit(double kilograms, WeightUnit unit) {
        return Rounding.withoutFloatError(unit.fromBaseUnit(kilograms));
    }
}
