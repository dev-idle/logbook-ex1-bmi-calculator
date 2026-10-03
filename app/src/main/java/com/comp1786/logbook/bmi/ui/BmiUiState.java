package com.comp1786.logbook.bmi.ui;

import androidx.annotation.Nullable;

import com.comp1786.logbook.bmi.domain.BmiResult;
import com.comp1786.logbook.bmi.domain.HeightUnit;
import com.comp1786.logbook.bmi.domain.InputError;
import com.comp1786.logbook.bmi.domain.WeightUnit;

/**
 * Everything the calculator screen displays apart from the text being typed, which the input
 * fields keep themselves.
 *
 * <p>The state is immutable: each change produces a new instance, so the screen is always
 * drawn from one consistent snapshot.
 *
 * @param weightUnit  the selected weight unit
 * @param heightUnit  the selected height unit
 * @param weightError the problem with the weight field, or {@code null}
 * @param heightError the problem with the centimeters or feet field, or {@code null}
 * @param inchesError the problem with the inches field, or {@code null}
 * @param result      the latest result, or {@code null} when none is shown
 */
public record BmiUiState(
        WeightUnit weightUnit,
        HeightUnit heightUnit,
        @Nullable InputError weightError,
        @Nullable InputError heightError,
        @Nullable InputError inchesError,
        @Nullable BmiResult result) {

    static BmiUiState initial(WeightUnit weightUnit, HeightUnit heightUnit) {
        return new BmiUiState(weightUnit, heightUnit, null, null, null, null);
    }

    /** Switches the weight unit. The weight error and the result no longer apply. */
    BmiUiState withWeightUnit(WeightUnit unit) {
        return new BmiUiState(unit, heightUnit, null, heightError, inchesError, null);
    }

    /** Switches the height unit. The height errors and the result no longer apply. */
    BmiUiState withHeightUnit(HeightUnit unit) {
        return new BmiUiState(weightUnit, unit, weightError, null, null, null);
    }

    /** The weight was edited, so its error and the result are out of date. */
    BmiUiState withWeightEdited() {
        return new BmiUiState(weightUnit, heightUnit, null, heightError, inchesError, null);
    }

    /** The centimeters or feet field was edited, so its error and the result are out of date. */
    BmiUiState withHeightEdited() {
        return new BmiUiState(weightUnit, heightUnit, weightError, null, inchesError, null);
    }

    /** The inches field was edited, so its error and the result are out of date. */
    BmiUiState withInchesEdited() {
        return new BmiUiState(weightUnit, heightUnit, weightError, heightError, null, null);
    }

    BmiUiState withErrors(
            @Nullable InputError weight, @Nullable InputError height, @Nullable InputError inches) {
        return new BmiUiState(weightUnit, heightUnit, weight, height, inches, null);
    }

    BmiUiState withResult(BmiResult newResult) {
        return new BmiUiState(weightUnit, heightUnit, null, null, null, newResult);
    }

    /** Removes all errors and the result while keeping the selected units. */
    BmiUiState cleared() {
        return initial(weightUnit, heightUnit);
    }
}
