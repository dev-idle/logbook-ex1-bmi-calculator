package com.comp1786.logbook.bmi.domain;

/**
 * Units a height can be entered in, each with the range of values the app accepts.
 *
 * <p>A {@link #FEET_AND_INCHES} value is a total number of inches. The screen collects it as
 * separate feet and inches fields; see {@link FeetAndInches}.
 */
public enum HeightUnit {

    /** Values are centimeters. */
    CENTIMETERS(UnitConversions.METERS_PER_CENTIMETER, 50.0, 250.0),

    /** Values are total inches: 20 in is 1 ft 8 in and 98 in is 8 ft 2 in. */
    FEET_AND_INCHES(
            UnitConversions.CENTIMETERS_PER_INCH * UnitConversions.METERS_PER_CENTIMETER,
            20.0, 98.0);

    private final double metersPerUnit;
    private final double minimum;
    private final double maximum;

    HeightUnit(double metersPerUnit, double minimum, double maximum) {
        this.metersPerUnit = metersPerUnit;
        this.minimum = minimum;
        this.maximum = maximum;
    }

    /** Converts a height in this unit to meters. */
    public double toMeters(double value) {
        return value * metersPerUnit;
    }

    /** Converts a height in meters to this unit. */
    public double fromMeters(double meters) {
        return meters / metersPerUnit;
    }

    /** The smallest accepted height in this unit (inclusive). */
    public double minimum() {
        return minimum;
    }

    /** The largest accepted height in this unit (inclusive). */
    public double maximum() {
        return maximum;
    }

    /** Returns whether {@code value}, in this unit, is within the accepted range. */
    public boolean accepts(double value) {
        return value >= minimum && value <= maximum;
    }
}
