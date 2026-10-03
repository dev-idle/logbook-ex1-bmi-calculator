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
    public void matchesTheCdcWorkedExample() {
        // CDC: 150 lb at 5 ft 5 in gives [150 / 65^2] x 703 = 24.96, a healthy weight.
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
