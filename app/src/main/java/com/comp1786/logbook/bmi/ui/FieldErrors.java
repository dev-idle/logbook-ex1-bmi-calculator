package com.comp1786.logbook.bmi.ui;

import androidx.annotation.Nullable;

import com.comp1786.logbook.bmi.domain.InputError;
import com.comp1786.logbook.bmi.domain.MeasurementResult;

/**
 * The errors shown on one measurement's fields.
 *
 * @param primary the error for the single field, or the whole field of a compound unit
 * @param part    the error for the part field of a compound unit
 */
record FieldErrors(@Nullable InputError primary, @Nullable InputError part) {

    static final FieldErrors NONE = new FieldErrors(null, null);

    static FieldErrors of(MeasurementResult result) {
        return new FieldErrors(result.primaryError(), result.partError());
    }

    boolean any() {
        return primary != null || part != null;
    }

    FieldErrors withoutPrimary() {
        return new FieldErrors(null, part);
    }

    FieldErrors withoutPart() {
        return new FieldErrors(primary, null);
    }
}
