package com.comp1786.logbook.bmi.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

public class BmiCalculatorTest {

    private static final double DELTA = 1e-9;

    @Test
    public void dividesWeightByHeightSquaredAndRoundsToOneDecimal() {
        BmiResult result = BmiCalculator.calculate(70.0, 1.75);

        // 70 / 1.75^2 = 22.857...
        assertEquals(22.857142857, result.bmi(), 1e-9);
        assertEquals(22.86, result.roundedBmi(), DELTA);
        assertEquals(BmiCategory.HEALTHY_WEIGHT, result.category());
    }

    @Test
    public void matchesTheCdcFormulaForPoundsAndInches() {
        // CDC formula: 150 lb at 5 ft 5 in gives 150 / 65^2 x 703 = 24.96, a healthy weight.
        BmiResult result = BmiCalculator.calculate(
                WeightUnit.POUNDS.toBaseUnit(150.0), HeightUnit.FEET_AND_INCHES.toBaseUnit(65.0));

        assertEquals(24.96, result.roundedBmi(), DELTA);
        assertEquals(BmiCategory.HEALTHY_WEIGHT, result.category());
    }

    @Test
    public void classifiesTheExactValueWithInclusiveLowerBounds() {
        assertEquals(BmiCategory.UNDERWEIGHT, BmiCalculator.calculate(18.49, 1.0).category());
        assertEquals(BmiCategory.HEALTHY_WEIGHT, BmiCalculator.calculate(18.5, 1.0).category());
        assertEquals(BmiCategory.HEALTHY_WEIGHT, BmiCalculator.calculate(24.99, 1.0).category());
        assertEquals(BmiCategory.OVERWEIGHT, BmiCalculator.calculate(25.0, 1.0).category());
    }

    @Test
    public void roundedBmiNeverRoundsIntoTheNextCategory() {
        BmiResult result = BmiCalculator.calculate(24.996, 1.0);

        assertEquals(BmiCategory.HEALTHY_WEIGHT, result.category());
        assertEquals(24.99, result.roundedBmi(), DELTA);
    }

    @Test
    public void classifiesABmiExactlyOnABoundDespiteFloatingPointError() {
        // 64 / 1.6^2 is exactly 25, but double arithmetic gives 24.999999999999996.
        BmiResult result = BmiCalculator.calculate(64.0, HeightUnit.CENTIMETERS.toBaseUnit(160.0));

        assertEquals(BmiCategory.OVERWEIGHT, result.category());
        assertEquals(25.0, result.roundedBmi(), DELTA);
    }

    @Test
    public void classifiesEveryMetricInputThatLandsExactlyOnABound() {
        // Searches every height and weight the metric fields accept, in 0.1 steps, with integer
        // arithmetic: weight (tenths of kg) x 1,000,000 = bound (tenths) x height (mm) squared.
        int checked = 0;
        for (BmiCategory category : BmiCategory.values()) {
            long bound = Math.round(category.lowerBound() * 10);
            for (long millimeters = 500; millimeters <= 2800 && bound > 0; millimeters++) {
                long product = bound * millimeters * millimeters;
                long tenthsOfKilograms = product / 1_000_000;
                if (product % 1_000_000 != 0 || tenthsOfKilograms < 100
                        || tenthsOfKilograms > 4000) {
                    continue;
                }
                BmiResult result = BmiCalculator.calculate(tenthsOfKilograms / 10.0,
                        HeightUnit.CENTIMETERS.toBaseUnit(millimeters / 10.0));
                assertEquals(tenthsOfKilograms + " at " + millimeters,
                        category, result.category());
                checked++;
            }
        }
        assertEquals(95, checked);
    }

    @Test
    public void healthyWeightRangeMatchesTheCategoryAtEveryMetricHeight() {
        for (long millimeters = 500; millimeters <= 2800; millimeters++) {
            double meters = HeightUnit.CENTIMETERS.toBaseUnit(millimeters / 10.0);
            BmiResult result = BmiCalculator.calculate(70.0, meters);
            double lowest = result.healthyWeightMinimum(WeightUnit.KILOGRAMS);
            double highest = result.healthyWeightMaximum(WeightUnit.KILOGRAMS);
            String height = millimeters + " mm";

            assertEquals(height, BmiCategory.HEALTHY_WEIGHT,
                    BmiCalculator.calculate(lowest, meters).category());
            assertEquals(height, BmiCategory.UNDERWEIGHT,
                    BmiCalculator.calculate(oneDecimal(lowest - 0.1), meters).category());
            assertEquals(height, BmiCategory.HEALTHY_WEIGHT,
                    BmiCalculator.calculate(highest, meters).category());
            assertEquals(height, BmiCategory.OVERWEIGHT,
                    BmiCalculator.calculate(oneDecimal(highest + 0.1), meters).category());
        }
    }

    @Test
    public void healthyWeightRangeFollowsTheCategoryBounds() {
        BmiResult result = BmiCalculator.calculate(70.0, 1.75);

        // 18.5 x 1.75^2 = 56.656 rounds up; overweight starts at 25 x 1.75^2 = 76.5625.
        assertEquals(56.7, result.healthyWeightMinimum(WeightUnit.KILOGRAMS), DELTA);
        assertEquals(76.5, result.healthyWeightMaximum(WeightUnit.KILOGRAMS), DELTA);
    }

    @Test
    public void healthyWeightRangeMatchesTheCategoryAtBothEnds() {
        assertEquals(BmiCategory.UNDERWEIGHT, BmiCalculator.calculate(56.6, 1.75).category());
        assertEquals(BmiCategory.HEALTHY_WEIGHT, BmiCalculator.calculate(56.7, 1.75).category());
        assertEquals(BmiCategory.HEALTHY_WEIGHT, BmiCalculator.calculate(76.5, 1.75).category());
        assertEquals(BmiCategory.OVERWEIGHT, BmiCalculator.calculate(76.6, 1.75).category());
    }

    @Test
    public void highestHealthyWeightStaysBelowAnExactLimit() {
        // At 1 m overweight starts at exactly 25 kg.
        assertEquals(24.9, BmiCalculator.calculate(70.0, 1.0)
                .healthyWeightMaximum(WeightUnit.KILOGRAMS), DELTA);
    }

    @Test
    public void healthyWeightRangeIsConvertedBeforeRounding() {
        BmiResult result = BmiCalculator.calculate(70.0, 1.75);

        // 56.656 kg = 124.906 lb rounds up; 76.5625 kg = 168.791 lb rounds down.
        assertEquals(125.0, result.healthyWeightMinimum(WeightUnit.POUNDS), DELTA);
        assertEquals(168.7, result.healthyWeightMaximum(WeightUnit.POUNDS), DELTA);
    }

    private static double oneDecimal(double value) {
        return Math.round(value * 10) / 10.0;
    }

    @Test
    public void rejectsValuesThatAreNotPositiveAndFinite() {
        assertThrows(IllegalArgumentException.class, () -> BmiCalculator.calculate(0.0, 1.75));
        assertThrows(IllegalArgumentException.class, () -> BmiCalculator.calculate(70.0, -1.0));
        assertThrows(IllegalArgumentException.class,
                () -> BmiCalculator.calculate(Double.NaN, 1.75));
        assertThrows(IllegalArgumentException.class,
                () -> BmiCalculator.calculate(70.0, Double.POSITIVE_INFINITY));
    }
}
