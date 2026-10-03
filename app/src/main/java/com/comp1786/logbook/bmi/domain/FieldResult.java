package com.comp1786.logbook.bmi.domain;

import androidx.annotation.Nullable;

/**
 * The outcome of parsing one input field: a value, or the reason it was rejected. Used by
 * {@link MeasurementValidator} while it builds a {@link MeasurementResult}.
 *
 * @param value the parsed value, or {@link Double#NaN} when the field is invalid
 * @param error why the field was rejected, or {@code null} when it is valid
 */
record FieldResult(double value, @Nullable InputError error) {

    static FieldResult valid(double value) {
        return new FieldResult(value, null);
    }

    static FieldResult invalid(InputError error) {
        return new FieldResult(Double.NaN, error);
    }

    boolean isValid() {
        return error == null;
    }
}
