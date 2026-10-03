package com.comp1786.logbook.bmi.domain;

/**
 * Units a weight can be entered in. Each converts to kilograms.
 *
 * <p>The accepted ranges are wide enough for any adult yet narrow enough to catch typing
 * mistakes, such as an extra digit turning 70 kg into 700 kg.
 */
public enum WeightUnit implements MeasurementUnit {

    KILOGRAMS(UnitScale.simple(1.0, 10.0, 400.0)),
    POUNDS(UnitScale.simple(UnitConversions.KILOGRAMS_PER_POUND, 22.0, 880.0)),

    /**
     * The way weight is usually stated in the UK. Values are total pounds, with the same range
     * as {@link #POUNDS}: 22 lb is 1 st 8 lb and 880 lb is 62 st 12 lb.
     */
    STONES_AND_POUNDS(UnitScale.compound(
            UnitConversions.KILOGRAMS_PER_POUND, 22.0, 880.0, UnitConversions.POUNDS_PER_STONE));

    private final UnitScale scale;

    WeightUnit(UnitScale scale) {
        this.scale = scale;
    }

    @Override
    public UnitScale scale() {
        return scale;
    }
}
