package com.comp1786.logbook.bmi.domain;

/**
 * The outcome of a BMI calculation.
 *
 * @param bmi                           the BMI rounded to one decimal place
 * @param category                      the category of {@code bmi}
 * @param healthyWeightMinimumKilograms the lowest healthy weight for the height, unrounded
 * @param healthyWeightMaximumKilograms the highest healthy weight for the height, unrounded
 */
public record BmiResult(
        double bmi,
        BmiCategory category,
        double healthyWeightMinimumKilograms,
        double healthyWeightMaximumKilograms) {

    /**
     * The lowest healthy weight for the height in {@code unit}, rounded up to one decimal
     * place so that it is still within the healthy category.
     */
    public double healthyWeightMinimum(WeightUnit unit) {
        return Rounding.up(unit.fromBaseUnit(healthyWeightMinimumKilograms), 1);
    }

    /**
     * The highest healthy weight for the height in {@code unit}, rounded down to one decimal
     * place so that it is still within the healthy category.
     */
    public double healthyWeightMaximum(WeightUnit unit) {
        return Rounding.down(unit.fromBaseUnit(healthyWeightMaximumKilograms), 1);
    }
}
