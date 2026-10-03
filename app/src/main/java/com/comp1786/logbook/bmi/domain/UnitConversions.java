package com.comp1786.logbook.bmi.domain;

/**
 * Conversion factors between metric and imperial units.
 *
 * <p>The pound and inch factors are exact: they were fixed by the 1959 international yard and
 * pound agreement, so no precision is lost by hard-coding them. A stone is defined as exactly
 * 14 pounds.
 */
final class UnitConversions {

    static final double KILOGRAMS_PER_POUND = 0.45359237;
    static final double CENTIMETERS_PER_INCH = 2.54;
    static final double METERS_PER_CENTIMETER = 0.01;
    static final int INCHES_PER_FOOT = 12;
    static final int POUNDS_PER_STONE = 14;

    private UnitConversions() {
    }
}
