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
    public void healthyWeightRangeStaysInsideTheHealthyCategory() {
        BmiResult result = BmiCalculator.calculate(70.0, 1.75);

        // 18.5 x 1.75^2 = 56.656 rounds up; 24.9 x 1.75^2 = 76.256 rounds down.
        assertEquals(56.7, result.healthyWeightMinimum(WeightUnit.KILOGRAMS), DELTA);
        assertEquals(76.2, result.healthyWeightMaximum(WeightUnit.KILOGRAMS), DELTA);
    }

    @Test
    public void healthyWeightRangeIsConvertedBeforeRounding() {
        BmiResult result = BmiCalculator.calculate(70.0, 1.75);

        // 56.656 kg = 124.906 lb rounds up; 76.256 kg = 168.116 lb rounds down.
        assertEquals(125.0, result.healthyWeightMinimum(WeightUnit.POUNDS), DELTA);
        assertEquals(168.1, result.healthyWeightMaximum(WeightUnit.POUNDS), DELTA);
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
