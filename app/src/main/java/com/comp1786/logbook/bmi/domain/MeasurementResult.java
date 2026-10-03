package com.comp1786.logbook.bmi.domain;

import androidx.annotation.Nullable;

/**
 * The outcome of validating a measurement. A compound unit has two fields, each with its own
 * error.
 *
 * @param value        the measurement in its unit, or {@link Double#NaN} when invalid
 * @param primaryError the error for the single field or the whole field, or {@code null}
 * @param partError    the error for the part field of a compound unit, or {@code null}
 */
public record MeasurementResult(
        double value,
        @Nullable InputError primaryError,
        @Nullable InputError partError) {

    static MeasurementResult valid(double value) {
        return new MeasurementResult(value, null, null);
    }

    static MeasurementResult invalid(
            @Nullable InputError primaryError, @Nullable InputError partError) {
        return new MeasurementResult(Double.NaN, primaryError, partError);
    }

    public boolean isValid() {
        return primaryError == null && partError == null;
    }
}
