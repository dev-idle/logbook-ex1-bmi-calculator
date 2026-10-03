package com.comp1786.logbook.bmi.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class WeightUnitTest {

    private static final double DELTA = 1e-9;

    @Test
    public void kilogramsConvertToThemselves() {
        assertEquals(70.0, WeightUnit.KILOGRAMS.toKilograms(70.0), DELTA);
        assertEquals(70.0, WeightUnit.KILOGRAMS.fromKilograms(70.0), DELTA);
    }

    @Test
    public void poundsUseTheExactInternationalDefinition() {
        assertEquals(0.45359237, WeightUnit.POUNDS.toKilograms(1.0), DELTA);
        assertEquals(1.0, WeightUnit.POUNDS.fromKilograms(0.45359237), DELTA);
    }

    @Test
    public void conversionRoundTripPreservesTheValue() {
        double kilograms = WeightUnit.POUNDS.toKilograms(154.3);
        assertEquals(154.3, WeightUnit.POUNDS.fromKilograms(kilograms), DELTA);
    }

    @Test
    public void rangeIsInclusiveAtBothEnds() {
        assertTrue(WeightUnit.KILOGRAMS.accepts(10.0));
        assertTrue(WeightUnit.KILOGRAMS.accepts(400.0));
        assertFalse(WeightUnit.KILOGRAMS.accepts(9.9));
        assertFalse(WeightUnit.KILOGRAMS.accepts(400.1));
        assertTrue(WeightUnit.POUNDS.accepts(22.0));
        assertTrue(WeightUnit.POUNDS.accepts(880.0));
        assertFalse(WeightUnit.POUNDS.accepts(880.1));
    }
}
