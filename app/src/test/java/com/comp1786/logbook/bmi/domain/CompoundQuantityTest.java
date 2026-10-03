package com.comp1786.logbook.bmi.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

public class CompoundQuantityTest {

    private static final double DELTA = 1e-9;
    private static final int INCHES_PER_FOOT = 12;

    @Test
    public void splitsATotalIntoWholeAndPart() {
        CompoundQuantity height = CompoundQuantity.fromTotal(68.9, INCHES_PER_FOOT);

        assertEquals(5, height.whole());
        assertEquals(8.9, height.part(), DELTA);
    }

    @Test
    public void roundsThePartToOneDecimalPlace() {
        assertEquals(8.9, CompoundQuantity.fromTotal(68.8976, INCHES_PER_FOOT).part(), DELTA);
    }

    @Test
    public void carriesAPartThatRoundsUpToAWholeUnit() {
        CompoundQuantity height = CompoundQuantity.fromTotal(71.96, INCHES_PER_FOOT);

        assertEquals(6, height.whole());
        assertEquals(0.0, height.part(), DELTA);
    }

    @Test
    public void rejectsAPartOfAWholeUnitOrMore() {
        assertThrows(IllegalArgumentException.class,
                () -> new CompoundQuantity(5, 12.0, INCHES_PER_FOOT));
        assertThrows(IllegalArgumentException.class,
                () -> new CompoundQuantity(5, -0.1, INCHES_PER_FOOT));
    }

    @Test
    public void rejectsNegativeTotals() {
        assertThrows(IllegalArgumentException.class,
                () -> CompoundQuantity.fromTotal(-1.0, INCHES_PER_FOOT));
    }
}
