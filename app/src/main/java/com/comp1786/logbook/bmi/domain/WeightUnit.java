package com.comp1786.logbook.bmi.domain;

/** Weight units. The ranges fit any adult but catch typing mistakes such as 700 kg. */
public enum WeightUnit implements MeasurementUnit {

    KILOGRAMS(UnitScale.simple(1.0, 10.0, 400.0)),

    /**
     * The limits are 10 kg and 400 kg converted to pounds and rounded, so a weight accepted in
     * one unit is accepted in every other.
     */
    POUNDS(UnitScale.simple(UnitConversions.KILOGRAMS_PER_POUND, 22.0, 881.8)),

    /** Common in the UK. Values are total pounds, with the same range as {@link #POUNDS}. */
    STONES_AND_POUNDS(UnitScale.compound(
            UnitConversions.KILOGRAMS_PER_POUND, 22.0, 881.8, UnitConversions.POUNDS_PER_STONE));

    private final UnitScale scale;

    WeightUnit(UnitScale scale) {
        this.scale = scale;
    }

    @Override
    public UnitScale scale() {
        return scale;
    }
}
