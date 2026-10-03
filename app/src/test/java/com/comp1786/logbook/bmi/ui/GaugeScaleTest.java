package com.comp1786.logbook.bmi.ui;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class GaugeScaleTest {

    private static final float DELTA = 1e-6f;

    @Test
    public void categoryBoundsMapToSegmentEdges() {
        assertEquals(1.0f, GaugeScale.position(18.5), DELTA);
        assertEquals(2.0f, GaugeScale.position(25.0), DELTA);
        assertEquals(5.0f, GaugeScale.position(40.0), DELTA);
    }

    @Test
    public void positionIsLinearWithinACategory() {
        // Healthy weight spans 18.5 to 25.0, so 21.75 is its midpoint.
        assertEquals(1.5f, GaugeScale.position(21.75), DELTA);
    }

    @Test
    public void openEndedCategoriesAreDrawnFrom15To45() {
        assertEquals(0.5f, GaugeScale.position(16.75), DELTA);
        assertEquals(5.5f, GaugeScale.position(42.5), DELTA);
    }

    @Test
    public void valuesBeyondTheGaugeStayAtItsEnds() {
        assertEquals(0.0f, GaugeScale.position(10.0), DELTA);
        assertEquals(6.0f, GaugeScale.position(60.0), DELTA);
    }
}
