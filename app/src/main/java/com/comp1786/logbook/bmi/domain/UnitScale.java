package com.comp1786.logbook.bmi.domain;

/**
 * The conversion factor and accepted range of a {@link MeasurementUnit}.
 *
 * @param baseUnitsPerUnit kilograms or meters in one of this unit, such as 0.01 m per cm
 * @param minimum          smallest accepted value, inclusive
 * @param maximum          largest accepted value, inclusive
 * @param partsPerWhole    small units per large unit, such as 12 in per ft; 0 if not compound
 * @param fractionDigits   decimal places kept when a value is converted into this unit
 */
public record UnitScale(
        double baseUnitsPerUnit,
        double minimum,
        double maximum,
        int partsPerWhole,
        int fractionDigits) {

    private static final int DEFAULT_FRACTION_DIGITS = 1;

    /** A unit entered as a single number, such as kilograms. */
    static UnitScale simple(double baseUnitsPerUnit, double minimum, double maximum) {
        return simple(baseUnitsPerUnit, minimum, maximum, DEFAULT_FRACTION_DIGITS);
    }

    /** A single-number unit that needs more decimal places, such as meters. */
    static UnitScale simple(
            double baseUnitsPerUnit, double minimum, double maximum, int fractionDigits) {
        return new UnitScale(baseUnitsPerUnit, minimum, maximum, 0, fractionDigits);
    }

    /** A whole-and-part unit such as feet and inches; all values are in the small unit. */
    static UnitScale compound(
            double baseUnitsPerPart, double minimum, double maximum, int partsPerWhole) {
        return new UnitScale(
                baseUnitsPerPart, minimum, maximum, partsPerWhole, DEFAULT_FRACTION_DIGITS);
    }
}
