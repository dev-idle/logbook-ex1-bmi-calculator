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

    /** The part of a compound unit is a whole unit or more, such as 12 inches. */
    PART_OUT_OF_RANGE
}
