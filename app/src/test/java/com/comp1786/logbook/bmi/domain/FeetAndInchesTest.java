package com.comp1786.logbook.bmi.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

public class FeetAndInchesTest {

    private static final double DELTA = 1e-9;

    @Test
    public void splitsTotalInchesIntoFeetAndInches() {
        FeetAndInches height = FeetAndInches.fromTotalInches(68.9);

        assertEquals(5, height.feet());
        assertEquals(8.9, height.inches(), DELTA);
    }

    @Test
    public void roundsInchesToOneDecimalPlace() {
        assertEquals(8.9, FeetAndInches.fromTotalInches(68.8976).inches(), DELTA);
    }

    @Test
    public void carriesInchesThatRoundUpToTwelveIntoFeet() {
        FeetAndInches height = FeetAndInches.fromTotalInches(71.96);

        assertEquals(6, height.feet());
        assertEquals(0.0, height.inches(), DELTA);
    }

    @Test
    public void totalInchesCombinesBothParts() {
        assertEquals(70.5, new FeetAndInches(5, 10.5).totalInches(), DELTA);
    }

    @Test
    public void rejectsInchesOutsideOneFoot() {
        assertThrows(IllegalArgumentException.class, () -> new FeetAndInches(5, 12.0));
        assertThrows(IllegalArgumentException.class, () -> new FeetAndInches(5, -0.1));
    }

    @Test
    public void rejectsNegativeTotals() {
        assertThrows(IllegalArgumentException.class, () -> FeetAndInches.fromTotalInches(-1.0));
    }
}
