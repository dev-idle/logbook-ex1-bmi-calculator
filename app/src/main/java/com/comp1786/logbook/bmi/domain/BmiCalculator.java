package com.comp1786.logbook.bmi.domain;

/**
 * Calculates body mass index: weight in kilograms divided by the square of height in meters.
 *
 * @see <a href="https://www.cdc.gov/bmi/adult-calculator/bmi-categories.html">
 *     CDC: Adult BMI Categories</a>
 */
public final class BmiCalculator {

    /**
     * BMI is rounded half up before it is classified, so each category starts half a step below
     * its published bound: a BMI of 18.45 already shows as 18.5 and counts as healthy.
     */
    private static final double HALF_DISPLAY_STEP = 0.05;

    private BmiCalculator() {
    }

    /**
     * Calculates the BMI, its category and the healthy weight range for the height.
     *
     * <p>The BMI is rounded before it is classified, so the category matches the value shown:
     * 24.96 is shown as 25.0 and classified as {@link BmiCategory#OVERWEIGHT}.
     *
     * @throws IllegalArgumentException if either value is not positive and finite
     */
    public static BmiResult calculate(double weightKilograms, double heightMeters) {
        requirePositive(weightKilograms, "weightKilograms");
        requirePositive(heightMeters, "heightMeters");

        double heightSquared = heightMeters * heightMeters;
        double bmi = Rounding.halfUp(weightKilograms / heightSquared, 1);
        BmiCategory healthy = BmiCategory.HEALTHY_WEIGHT;
        return new BmiResult(
                bmi,
                BmiCategory.of(bmi),
                (healthy.lowerBound() - HALF_DISPLAY_STEP) * heightSquared,
                (healthy.upperBound() - HALF_DISPLAY_STEP) * heightSquared);
    }

    private static void requirePositive(double value, String name) {
        if (!(value > 0) || !Double.isFinite(value)) {
            throw new IllegalArgumentException(name + " must be positive and finite: " + value);
        }
    }
}
