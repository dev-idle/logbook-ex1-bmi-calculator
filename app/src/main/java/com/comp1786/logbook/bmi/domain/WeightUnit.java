package com.comp1786.logbook.bmi.domain;

/**
 * Units a weight can be entered in. Each converts to kilograms.
 *
 * <p>The accepted ranges are wide enough for any adult yet narrow enough to catch typing
 * mistakes, such as an extra digit turning 70 kg into 700 kg.
 */
public enum WeightUnit implements MeasurementUnit {

    KILOGRAMS(UnitScale.simple(1.0, 10.0, 400.0)),
    POUNDS(UnitScale.simple(UnitConversions.KILOGRAMS_PER_POUND, 22.0, 880.0));

    private final UnitScale scale;

    WeightUnit(UnitScale scale) {
        this.scale = scale;
    }

    @Override
    public UnitScale scale() {
        return scale;
    }
}
