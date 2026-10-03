package com.comp1786.logbook.bmi.ui;

import androidx.annotation.Nullable;

import com.comp1786.logbook.bmi.domain.BmiResult;
import com.comp1786.logbook.bmi.domain.HeightUnit;
import com.comp1786.logbook.bmi.domain.WeightUnit;

import java.io.Serializable;

/**
 * An immutable snapshot of what the screen shows, apart from typed text, which the fields keep.
 * It is serializable so that the view model can save it.
 *
 * @param result the latest result, or {@code null} when none is shown
 */
record BmiUiState(
        WeightUnit weightUnit,
        HeightUnit heightUnit,
        FieldErrors weightErrors,
        FieldErrors heightErrors,
        @Nullable BmiResult result) implements Serializable {

    static BmiUiState initial(WeightUnit weightUnit, HeightUnit heightUnit) {
        return new BmiUiState(weightUnit, heightUnit, FieldErrors.NONE, FieldErrors.NONE, null);
    }

    /** Switches the weight unit, dropping the weight errors and the result. */
    BmiUiState withWeightUnit(WeightUnit unit) {
        return new BmiUiState(unit, heightUnit, FieldErrors.NONE, heightErrors, null);
    }

    /** Switches the height unit, dropping the height errors and the result. */
    BmiUiState withHeightUnit(HeightUnit unit) {
        return new BmiUiState(weightUnit, unit, weightErrors, FieldErrors.NONE, null);
    }

    /** Replaces the weight errors after an edit, which also drops the result. */
    BmiUiState withWeightErrors(FieldErrors errors) {
        return new BmiUiState(weightUnit, heightUnit, errors, heightErrors, null);
    }

    /** Replaces the height errors after an edit, which also drops the result. */
    BmiUiState withHeightErrors(FieldErrors errors) {
        return new BmiUiState(weightUnit, heightUnit, weightErrors, errors, null);
    }

    /** Shows validation errors; there is no result while any field is invalid. */
    BmiUiState withErrors(FieldErrors weight, FieldErrors height) {
        return new BmiUiState(weightUnit, heightUnit, weight, height, null);
    }

    BmiUiState withResult(BmiResult newResult) {
        return new BmiUiState(
                weightUnit, heightUnit, FieldErrors.NONE, FieldErrors.NONE, newResult);
    }

    /** Drops all errors and the result, keeping the selected units. */
    BmiUiState cleared() {
        return initial(weightUnit, heightUnit);
    }
}
