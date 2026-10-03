package com.comp1786.logbook.bmi.domain;

/**
 * Units a weight can be entered in, each with the range of values the app accepts.
 *
 * <p>The ranges are wide enough for any adult yet narrow enough to catch typing mistakes, such
 * as an extra digit turning 70 kg into 700 kg.
 */
public enum WeightUnit {

    KILOGRAMS(1.0, 10.0, 400.0),
    POUNDS(UnitConversions.KILOGRAMS_PER_POUND, 22.0, 880.0);

    private final double kilogramsPerUnit;
    private final double minimum;
    private final double maximum;

    WeightUnit(double kilogramsPerUnit, double minimum, double maximum) {
        this.kilogramsPerUnit = kilogramsPerUnit;
        this.minimum = minimum;
        this.maximum = maximum;
    }

    /** Converts a weight in this unit to kilograms. */
    public double toKilograms(double value) {
        return value * kilogramsPerUnit;
    }

    /** Converts a weight in kilograms to this unit. */
    public double fromKilograms(double kilograms) {
        return kilograms / kilogramsPerUnit;
    }

    /** The smallest accepted weight in this unit (inclusive). */
    public double minimum() {
        return minimum;
    }

    /** The largest accepted weight in this unit (inclusive). */
    public double maximum() {
        return maximum;
    }

    /** Returns whether {@code value}, in this unit, is within the accepted range. */
    public boolean accepts(double value) {
        return value >= minimum && value <= maximum;
    }
}
