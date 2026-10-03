package com.comp1786.logbook.bmi.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class HeightUnitTest {

    private static final double DELTA = 1e-9;

    @Test
    public void centimetersConvertToMeters() {
        assertEquals(1.75, HeightUnit.CENTIMETERS.toBaseUnit(175.0), DELTA);
        assertEquals(175.0, HeightUnit.CENTIMETERS.fromBaseUnit(1.75), DELTA);
    }

    @Test
    public void inchesUseTheExactInternationalDefinition() {
        assertEquals(0.0254, HeightUnit.FEET_AND_INCHES.toBaseUnit(1.0), DELTA);
        assertEquals(70.0, HeightUnit.FEET_AND_INCHES.fromBaseUnit(1.778), DELTA);
    }

    @Test
    public void rangeIsInclusiveAtBothEnds() {
        assertTrue(HeightUnit.CENTIMETERS.accepts(50.0));
        assertTrue(HeightUnit.CENTIMETERS.accepts(250.0));
        assertFalse(HeightUnit.CENTIMETERS.accepts(49.9));
        assertFalse(HeightUnit.CENTIMETERS.accepts(250.1));
        assertTrue(HeightUnit.FEET_AND_INCHES.accepts(20.0));
        assertTrue(HeightUnit.FEET_AND_INCHES.accepts(98.0));
        assertFalse(HeightUnit.FEET_AND_INCHES.accepts(19.9));
    }
}
