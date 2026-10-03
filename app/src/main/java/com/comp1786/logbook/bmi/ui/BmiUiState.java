package com.comp1786.logbook.bmi.ui;

import androidx.annotation.Nullable;

import com.comp1786.logbook.bmi.domain.BmiResult;
import com.comp1786.logbook.bmi.domain.HeightUnit;
import com.comp1786.logbook.bmi.domain.WeightUnit;

/**
 * Everything the calculator screen displays apart from the text being typed, which the input
 * fields keep themselves.
 *
 * <p>The state is immutable: each change produces a new instance, so the screen is always
 * drawn from one consistent snapshot.
 *
 * @param weightUnit   the selected weight unit
 * @param heightUnit   the selected height unit
 * @param weightErrors the problems with the weight fields
 * @param heightErrors the problems with the height fields
 * @param result       the latest result, or {@code null} when none is shown
 */
public record BmiUiState(
        WeightUnit weightUnit,
        HeightUnit heightUnit,
        FieldErrors weightErrors,
        FieldErrors heightErrors,
        @Nullable BmiResult result) {

    static BmiUiState initial(WeightUnit weightUnit, HeightUnit heightUnit) {
        return new BmiUiState(weightUnit, heightUnit, FieldErrors.NONE, FieldErrors.NONE, null);
    }

    /** Switches the weight unit. The weight errors and the result no longer apply. */
    BmiUiState withWeightUnit(WeightUnit unit) {
        return new BmiUiState(unit, heightUnit, FieldErrors.NONE, heightErrors, null);
    }

    /** Switches the height unit. The height errors and the result no longer apply. */
    BmiUiState withHeightUnit(HeightUnit unit) {
        return new BmiUiState(weightUnit, unit, weightErrors, FieldErrors.NONE, null);
    }

    /** Replaces the weight errors after an edit, which also makes the result out of date. */
    BmiUiState withWeightErrors(FieldErrors errors) {
        return new BmiUiState(weightUnit, heightUnit, errors, heightErrors, null);
    }

    /** Replaces the height errors after an edit, which also makes the result out of date. */
    BmiUiState withHeightErrors(FieldErrors errors) {
        return new BmiUiState(weightUnit, heightUnit, weightErrors, errors, null);
    }

    /** Reports validation problems; no result is shown while any field is invalid. */
    BmiUiState withErrors(FieldErrors weight, FieldErrors height) {
        return new BmiUiState(weightUnit, heightUnit, weight, height, null);
    }

    BmiUiState withResult(BmiResult newResult) {
        return new BmiUiState(
                weightUnit, heightUnit, FieldErrors.NONE, FieldErrors.NONE, newResult);
    }

    /** Removes all errors and the result while keeping the selected units. */
    BmiUiState cleared() {
        return initial(weightUnit, heightUnit);
    }
}
