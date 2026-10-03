package com.comp1786.logbook.bmi.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.comp1786.logbook.bmi.domain.BmiCalculator;
import com.comp1786.logbook.bmi.domain.BmiResult;
import com.comp1786.logbook.bmi.domain.FieldResult;
import com.comp1786.logbook.bmi.domain.HeightResult;
import com.comp1786.logbook.bmi.domain.HeightUnit;
import com.comp1786.logbook.bmi.domain.MeasurementValidator;
import com.comp1786.logbook.bmi.domain.WeightUnit;

import java.util.Objects;
import java.util.function.UnaryOperator;

/**
 * Holds the calculator's state and turns user actions into new states.
 *
 * <p>The screen sends events (a unit was picked, a field was edited, Calculate was pressed) and
 * observes {@link #uiState()}; it never changes the state itself. Keeping the state here means
 * it survives configuration changes such as rotation.
 */
public final class BmiViewModel extends ViewModel {

    private final MutableLiveData<BmiUiState> uiState;

    public BmiViewModel() {
        uiState = new MutableLiveData<>(
                BmiUiState.initial(WeightUnit.KILOGRAMS, HeightUnit.CENTIMETERS));
    }

    public LiveData<BmiUiState> uiState() {
        return uiState;
    }

    /** The state currently shown; never {@code null} because the LiveData starts with a value. */
    public BmiUiState currentState() {
        return Objects.requireNonNull(uiState.getValue());
    }

    public void selectWeightUnit(WeightUnit unit) {
        if (unit != currentState().weightUnit()) {
            update(state -> state.withWeightUnit(unit));
        }
    }

    public void selectHeightUnit(HeightUnit unit) {
        if (unit != currentState().heightUnit()) {
            update(state -> state.withHeightUnit(unit));
        }
    }

    public void onWeightEdited() {
        update(BmiUiState::withWeightEdited);
    }

    public void onHeightEdited() {
        update(BmiUiState::withHeightEdited);
    }

    public void onInchesEdited() {
        update(BmiUiState::withInchesEdited);
    }

    /**
     * Validates the typed values and, when all are valid, calculates the BMI. Otherwise the
     * state reports an error for every invalid field at once.
     *
     * @param weightText  the weight field
     * @param heightText  the centimeters field, or the feet field for feet and inches
     * @param inchesText  the inches field; ignored for centimeters
     */
    public void calculate(String weightText, String heightText, String inchesText) {
        BmiUiState state = currentState();
        FieldResult weight =
                MeasurementValidator.validateWeight(weightText, state.weightUnit());
        HeightResult height =
                MeasurementValidator.validateHeight(state.heightUnit(), heightText, inchesText);

        if (!weight.isValid() || !height.isValid()) {
            uiState.setValue(
                    state.withErrors(weight.error(), height.primaryError(), height.inchesError()));
            return;
        }

        BmiResult result = BmiCalculator.calculate(
                state.weightUnit().toKilograms(weight.value()),
                state.heightUnit().toMeters(height.value()));
        uiState.setValue(state.withResult(result));
    }

    public void clear() {
        update(BmiUiState::cleared);
    }

    private void update(UnaryOperator<BmiUiState> change) {
        BmiUiState current = currentState();
        BmiUiState next = change.apply(current);
        // Skipping identical states avoids redrawing the screen on every keystroke.
        if (!next.equals(current)) {
            uiState.setValue(next);
        }
    }
}
