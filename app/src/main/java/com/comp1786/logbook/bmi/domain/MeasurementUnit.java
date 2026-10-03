package com.comp1786.logbook.bmi.domain;

/**
 * A unit a weight or height can be entered in. Weight units convert to kilograms and height
 * units to meters, the units the BMI formula uses.
 *
 * <p>A compound unit, such as feet and inches, is entered as a whole number of its large unit
 * plus a part in its small unit. Its values are totals in the small unit, so 5 ft 10 in is 70.
 *
 * <p>Implementations only supply their {@link UnitScale}; conversion and range checks are
 * shared here so weight and height units behave identically.
 */
public interface MeasurementUnit {

    /** The conversion factor, accepted range and structure of this unit. */
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
}
