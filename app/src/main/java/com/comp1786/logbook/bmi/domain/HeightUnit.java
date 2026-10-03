package com.comp1786.logbook.bmi.domain;

/**
 * Units a height can be entered in. Each converts to meters.
 *
 * <p>The accepted ranges are wide enough for any adult yet narrow enough to catch typing
 * mistakes.
 */
public enum HeightUnit implements MeasurementUnit {

    CENTIMETERS(UnitScale.simple(UnitConversions.METERS_PER_CENTIMETER, 50.0, 250.0)),

    /** Values are total inches: 20 in is 1 ft 8 in and 98 in is 8 ft 2 in. */
    FEET_AND_INCHES(UnitScale.compound(
            UnitConversions.CENTIMETERS_PER_INCH * UnitConversions.METERS_PER_CENTIMETER,
            20.0, 98.0, UnitConversions.INCHES_PER_FOOT));

    private final UnitScale scale;

    HeightUnit(UnitScale scale) {
        this.scale = scale;
    }

    @Override
    public UnitScale scale() {
        return scale;
    }
}
