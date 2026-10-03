package com.comp1786.logbook.bmi.domain;

import androidx.annotation.Nullable;

/**
 * The outcome of checking one input field: a valid value, or the reason it was rejected.
 *
 * @param value the parsed value, or {@link Double#NaN} when the field is invalid
 * @param error why the field was rejected, or {@code null} when it is valid
 */
public record FieldResult(double value, @Nullable InputError error) {

    public static FieldResult valid(double value) {
        return new FieldResult(value, null);
    }

    public static FieldResult invalid(InputError error) {
        return new FieldResult(Double.NaN, error);
    }

    public boolean isValid() {
        return error == null;
    }
}
