package com.comp1786.logbook.bmi.domain;

/**
 * The text typed into a measurement's fields.
 *
 * @param primary the single field, or the whole field of a compound unit (feet, stones)
 * @param part    the part field of a compound unit (inches, pounds); empty for a simple unit
 */
public record MeasurementInput(String primary, String part) {

    /** No text in any field. */
    public static final MeasurementInput EMPTY = new MeasurementInput("", "");

    /** Input for a simple unit, which has no part field. */
    public static MeasurementInput of(String primary) {
        return new MeasurementInput(primary, "");
    }
}
