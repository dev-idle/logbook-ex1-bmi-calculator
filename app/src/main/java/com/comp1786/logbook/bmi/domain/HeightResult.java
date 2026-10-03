package com.comp1786.logbook.bmi.domain;

import androidx.annotation.Nullable;

/**
 * The outcome of checking the height fields. In feet and inches the height spans two fields,
 * so each can carry its own error.
 *
 * @param value        the height in the selected {@link HeightUnit}, or {@link Double#NaN}
 *                     when invalid
 * @param primaryError the error for the centimeters or feet field, or {@code null}
 * @param inchesError  the error for the inches field, or {@code null}
 */
public record HeightResult(
        double value,
        @Nullable InputError primaryError,
        @Nullable InputError inchesError) {

    public static HeightResult valid(double value) {
        return new HeightResult(value, null, null);
    }

    public static HeightResult invalid(
            @Nullable InputError primaryError, @Nullable InputError inchesError) {
        return new HeightResult(Double.NaN, primaryError, inchesError);
    }

    public boolean isValid() {
        return primaryError == null && inchesError == null;
    }
}
