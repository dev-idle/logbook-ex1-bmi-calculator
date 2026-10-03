package com.comp1786.logbook.bmi.domain;

/**
 * How a {@link MeasurementUnit} relates to its base unit (kilograms or meters), the range of
 * values the app accepts in it, and whether it is compound.
 *
 * @param baseUnitsPerUnit how many base units one of this unit is, e.g. 0.01 meters per cm
 * @param minimum          the smallest accepted value in this unit (inclusive)
 * @param maximum          the largest accepted value in this unit (inclusive)
 * @param partsPerWhole    small units per large unit for a compound unit, e.g. 12 inches per
 *                         foot, or 0 for a simple unit
 * @param fractionDigits   decimal places kept when a value is converted into this unit, enough
 *                         to keep about a millimeter or a tenth of a pound of precision
 */
public record UnitScale(
        double baseUnitsPerUnit,
        double minimum,
        double maximum,
        int partsPerWhole,
        int fractionDigits) {

    /** Most units are precise enough with one decimal place: 70.5 kg, 175.3 cm, 8.9 in. */
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

    /**
     * A unit entered as a whole large unit plus a part in a small unit, such as feet and
     * inches. Its values, range and factor are all in the small unit.
     */
    static UnitScale compound(
            double baseUnitsPerPart, double minimum, double maximum, int partsPerWhole) {
        return new UnitScale(
                baseUnitsPerPart, minimum, maximum, partsPerWhole, DEFAULT_FRACTION_DIGITS);
    }
}
