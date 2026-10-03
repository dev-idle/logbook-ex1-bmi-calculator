package com.comp1786.logbook.bmi.domain;

/**
 * Height units. The range runs from below the shortest adult on record (54.6 cm) to above the
 * tallest (272 cm), and still rejects typing mistakes such as 1750 cm.
 */
public enum HeightUnit implements MeasurementUnit {

    CENTIMETERS(UnitScale.simple(UnitConversions.METERS_PER_CENTIMETER, 50.0, 280.0)),

    /** Kept to the millimeter, so 175.5 cm converts to 1.755 m without loss. */
    METERS(UnitScale.simple(1.0, 0.5, 2.8, 3)),

    /**
     * Values are total inches. The limits are 50 cm and 280 cm converted to inches and rounded,
     * so a height accepted in one unit is accepted in every other: 1 ft 7.7 in to 9 ft 2.2 in.
     */
    FEET_AND_INCHES(UnitScale.compound(
            UnitConversions.CENTIMETERS_PER_INCH * UnitConversions.METERS_PER_CENTIMETER,
            19.7, 110.2, UnitConversions.INCHES_PER_FOOT));

    private final UnitScale scale;

    HeightUnit(UnitScale scale) {
        this.scale = scale;
    }

    @Override
    public UnitScale scale() {
        return scale;
    }
}
