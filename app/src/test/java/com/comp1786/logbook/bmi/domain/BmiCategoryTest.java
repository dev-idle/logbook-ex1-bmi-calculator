package com.comp1786.logbook.bmi.domain;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class BmiCategoryTest {

    private static final double DELTA = 1e-9;

    @Test
    public void classifiesValuesJustBelowAndAtEachBoundary() {
        assertEquals(BmiCategory.UNDERWEIGHT, BmiCategory.of(18.4));
        assertEquals(BmiCategory.HEALTHY_WEIGHT, BmiCategory.of(18.5));
        assertEquals(BmiCategory.HEALTHY_WEIGHT, BmiCategory.of(24.9));
        assertEquals(BmiCategory.OVERWEIGHT, BmiCategory.of(25.0));
        assertEquals(BmiCategory.OVERWEIGHT, BmiCategory.of(29.9));
        assertEquals(BmiCategory.OBESITY_CLASS_1, BmiCategory.of(30.0));
        assertEquals(BmiCategory.OBESITY_CLASS_1, BmiCategory.of(34.9));
        assertEquals(BmiCategory.OBESITY_CLASS_2, BmiCategory.of(35.0));
        assertEquals(BmiCategory.OBESITY_CLASS_2, BmiCategory.of(39.9));
        assertEquals(BmiCategory.OBESITY_CLASS_3, BmiCategory.of(40.0));
    }

    @Test
    public void classifiesExtremeValues() {
        assertEquals(BmiCategory.UNDERWEIGHT, BmiCategory.of(0.0));
        assertEquals(BmiCategory.OBESITY_CLASS_3, BmiCategory.of(120.0));
    }

    @Test
    public void upperBoundIsTheNextCategoryLowerBound() {
        assertEquals(18.5, BmiCategory.UNDERWEIGHT.upperBound(), DELTA);
        assertEquals(25.0, BmiCategory.HEALTHY_WEIGHT.upperBound(), DELTA);
        assertEquals(Double.POSITIVE_INFINITY, BmiCategory.OBESITY_CLASS_3.upperBound(), DELTA);
    }
}
