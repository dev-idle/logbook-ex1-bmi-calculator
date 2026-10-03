package com.comp1786.logbook.bmi.domain;

/**
 * Calculates body mass index: weight in kilograms divided by the square of height in meters.
 *
 * @see <a href="https://www.cdc.gov/bmi/adult-calculator/bmi-categories.html">
 *     CDC: Adult BMI Categories</a>
 */
public final class BmiCalculator {

    private BmiCalculator() {
    }

    /**
     * Calculates the BMI, its category and the healthy weight range for the height.
     *
     * <p>The category comes from the exact BMI, as the CDC defines it: healthy weight is "18.5
     * to less than 25", so a BMI of 24.96 is healthy.
     *
     * @throws IllegalArgumentException if either value is not positive and finite
     */
    public static BmiResult calculate(double weightKilograms, double heightMeters) {
        requirePositive(weightKilograms, "weightKilograms");
        requirePositive(heightMeters, "heightMeters");

        double heightSquared = heightMeters * heightMeters;
        double bmi = weightKilograms / heightSquared;
        BmiCategory healthy = BmiCategory.HEALTHY_WEIGHT;
        return new BmiResult(
                bmi,
                BmiCategory.of(bmi),
                healthy.lowerBound() * heightSquared,
                healthy.upperBound() * heightSquared);
    }

    private static void requirePositive(double value, String name) {
        if (!(value > 0) || !Double.isFinite(value)) {
            throw new IllegalArgumentException(name + " must be positive and finite: " + value);
        }
    }
}
