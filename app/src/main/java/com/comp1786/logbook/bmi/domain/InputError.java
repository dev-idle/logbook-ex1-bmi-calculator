package com.comp1786.logbook.bmi.domain;

/** Reasons an input field can be rejected. The screen maps each one to a message. */
public enum InputError {

    /** A required field is empty. */
    REQUIRED,

    /** The text is not a number. */
    NOT_A_NUMBER,

    /** A whole number was expected, as for feet. */
    NOT_A_WHOLE_NUMBER,

    /** The value is outside the range accepted for its unit. */
    OUT_OF_RANGE,

    /** Inches must be less than 12; anything larger belongs in the feet field. */
    INCHES_OUT_OF_RANGE
}
