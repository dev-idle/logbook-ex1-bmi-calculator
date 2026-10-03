package com.comp1786.logbook.bmi.domain;

/**
 * Calculates body mass index: weight in kilograms divided by the square of height in meters.
 *
 * @see <a href="https://www.cdc.gov/bmi/adult-calculator/bmi-categories.html">
 *     CDC: Adult BMI Categories</a>
 */
public final class BmiCalculator {

    /**
     * The highest BMI shown as healthy. BMI is displayed to one decimal place, and the healthy
     * category ends just below 25.0.
     */
    private static final double HIGHEST_HEALTHY_BMI = 24.9;

    private BmiCalculator() {
    }

    /**
     * Calculates the BMI, its category and the healthy weight range for the height.
     *
     * <p>The BMI is rounded to one decimal place before it is classified, so the category
     * always matches the value the user sees: 24.96 is shown as 25.0 and classified as
     * {@link BmiCategory#OVERWEIGHT}.
     *
     * @param weightKilograms weight in kilograms; must be positive and finite
     * @param heightMeters    height in meters; must be positive and finite
     * @throws IllegalArgumentException if either value is not positive and finite
     */
    public static BmiResult calculate(double weightKilograms, double heightMeters) {
        requirePositive(weightKilograms, "weightKilograms");
        requirePositive(heightMeters, "heightMeters");

        double heightSquared = heightMeters * heightMeters;
        double bmi = Rounding.halfUp(weightKilograms / heightSquared, 1);
        return new BmiResult(
                bmi,
                BmiCategory.of(bmi),
                BmiCategory.HEALTHY_WEIGHT.lowerBound() * heightSquared,
                HIGHEST_HEALTHY_BMI * heightSquared);
    }

    private static void requirePositive(double value, String name) {
        if (!(value > 0) || !Double.isFinite(value)) {
            throw new IllegalArgumentException(name + " must be positive and finite: " + value);
        }
    }
}
