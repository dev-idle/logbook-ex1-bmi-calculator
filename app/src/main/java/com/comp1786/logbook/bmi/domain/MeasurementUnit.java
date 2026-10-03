package com.comp1786.logbook.bmi.domain;

/**
 * A unit a weight or height can be entered in. Weight converts to kilograms and height to
 * meters, the units of the BMI formula.
 *
 * <p>A compound unit such as feet and inches holds totals in its small unit: 5 ft 10 in is 70.
 */
public interface MeasurementUnit {

    UnitScale scale();

    /** Converts a value in this unit to the base unit: kilograms or meters. */
    default double toBaseUnit(double value) {
        return value * scale().baseUnitsPerUnit();
    }

    /** Converts a value in the base unit to this unit. */
    default double fromBaseUnit(double baseValue) {
        return baseValue / scale().baseUnitsPerUnit();
    }

    /** The smallest accepted value in this unit (inclusive). */
    default double minimum() {
        return scale().minimum();
    }

    /** The largest accepted value in this unit (inclusive). */
    default double maximum() {
        return scale().maximum();
    }

    /** Returns whether {@code value}, in this unit, is within the accepted range. */
    default boolean accepts(double value) {
        return value >= minimum() && value <= maximum();
    }

    /** Small units per large unit, such as 12 inches per foot, or 0 for a simple unit. */
    default int partsPerWhole() {
        return scale().partsPerWhole();
    }

    /** Returns whether the unit is entered as a whole and a part, like feet and inches. */
    default boolean isCompound() {
        return partsPerWhole() > 0;
    }

    /** Combines a compound value into a total in the small unit: 5 ft 8.9 in is 68.9. */
    default double combine(double whole, double part) {
        return whole * partsPerWhole() + part;
    }

    /** Decimal places kept when a value is converted into this unit. */
    default int fractionDigits() {
        return scale().fractionDigits();
    }
}
