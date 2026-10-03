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
        assertEquals(22.9, result.bmi(), DELTA);
        assertEquals(BmiCategory.HEALTHY_WEIGHT, result.category());
    }

    @Test
    public void roundsHalvesUp() {
        assertEquals(18.5, BmiCalculator.calculate(18.45, 1.0).bmi(), DELTA);
        assertEquals(18.4, BmiCalculator.calculate(18.44, 1.0).bmi(), DELTA);
    }

    @Test
    public void classifiesTheRoundedValueSoTheCategoryMatchesTheDisplay() {
        assertEquals(BmiCategory.OVERWEIGHT, BmiCalculator.calculate(24.96, 1.0).category());
        assertEquals(BmiCategory.HEALTHY_WEIGHT, BmiCalculator.calculate(24.94, 1.0).category());
        assertEquals(BmiCategory.HEALTHY_WEIGHT, BmiCalculator.calculate(18.45, 1.0).category());
    }

    @Test
    public void healthyWeightRangeFollowsTheRoundedClassification() {
        BmiResult result = BmiCalculator.calculate(70.0, 1.75);

        // 18.45 x 1.75^2 = 56.503 rounds up; overweight starts at 24.95 x 1.75^2 = 76.409.
        assertEquals(56.6, result.healthyWeightMinimum(WeightUnit.KILOGRAMS), DELTA);
        assertEquals(76.4, result.healthyWeightMaximum(WeightUnit.KILOGRAMS), DELTA);
    }

    @Test
    public void healthyWeightRangeMatchesTheCategoryAtBothEnds() {
        assertEquals(BmiCategory.UNDERWEIGHT, BmiCalculator.calculate(56.5, 1.75).category());
        assertEquals(BmiCategory.HEALTHY_WEIGHT, BmiCalculator.calculate(56.6, 1.75).category());
        assertEquals(BmiCategory.HEALTHY_WEIGHT, BmiCalculator.calculate(76.4, 1.75).category());
        assertEquals(BmiCategory.OVERWEIGHT, BmiCalculator.calculate(76.5, 1.75).category());
    }

    @Test
    public void highestHealthyWeightStaysBelowAnExactLimit() {
        // At 1 m overweight starts at exactly 24.95 kg, which itself shows as BMI 25.0.
        assertEquals(24.9, BmiCalculator.calculate(70.0, 1.0)
                .healthyWeightMaximum(WeightUnit.KILOGRAMS), DELTA);
    }

    @Test
    public void healthyWeightRangeIsConvertedBeforeRounding() {
        BmiResult result = BmiCalculator.calculate(70.0, 1.75);

        // 56.503 kg = 124.568 lb rounds up; 76.409 kg = 168.454 lb rounds down.
        assertEquals(124.6, result.healthyWeightMinimum(WeightUnit.POUNDS), DELTA);
        assertEquals(168.4, result.healthyWeightMaximum(WeightUnit.POUNDS), DELTA);
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
