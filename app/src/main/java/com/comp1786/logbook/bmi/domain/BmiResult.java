package com.comp1786.logbook.bmi.domain;

/**
 * The outcome of a BMI calculation.
 *
 * @param bmi                           the BMI rounded to one decimal place
 * @param category                      the category of {@code bmi}
 * @param healthyWeightMinimumKilograms the lowest healthy weight for the height (inclusive)
 * @param healthyWeightLimitKilograms   the weight at which overweight starts (exclusive)
 */
public record BmiResult(
        double bmi,
        BmiCategory category,
        double healthyWeightMinimumKilograms,
        double healthyWeightLimitKilograms) {

    /** The lowest healthy weight in {@code unit}, to one decimal place. */
    public double healthyWeightMinimum(WeightUnit unit) {
        return Rounding.up(unit.fromBaseUnit(healthyWeightMinimumKilograms), 1);
    }

    /** The highest healthy weight in {@code unit}, to one decimal place. */
    public double healthyWeightMaximum(WeightUnit unit) {
        return Rounding.below(unit.fromBaseUnit(healthyWeightLimitKilograms), 1);
    }
}
