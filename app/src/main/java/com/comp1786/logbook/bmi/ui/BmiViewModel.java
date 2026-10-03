package com.comp1786.logbook.bmi.ui;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.comp1786.logbook.bmi.data.UnitPreferences;
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

    private final UnitPreferences unitPreferences;
    private final MutableLiveData<BmiUiState> uiState;

    public BmiViewModel(UnitPreferences unitPreferences) {
        this.unitPreferences = unitPreferences;
        uiState = new MutableLiveData<>(BmiUiState.initial(
                unitPreferences.weightUnit(), unitPreferences.heightUnit()));
    }

    /** Creates the view model with its dependencies, since it has no no-argument constructor. */
    public static ViewModelProvider.Factory factory(UnitPreferences unitPreferences) {
        return new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                return modelClass.cast(new BmiViewModel(unitPreferences));
            }
        };
    }

    public LiveData<BmiUiState> uiState() {
        return uiState;
    }

    /** The state currently shown; never {@code null} because the LiveData starts with a value. */
    public BmiUiState currentState() {
        return Objects.requireNonNull(uiState.getValue());
    }

    /** Switches the weight unit and remembers it for the next launch. */
    public void selectWeightUnit(WeightUnit unit) {
        if (unit != currentState().weightUnit()) {
            unitPreferences.setWeightUnit(unit);
            update(state -> state.withWeightUnit(unit));
        }
    }

    /** Switches the height unit and remembers it for the next launch. */
    public void selectHeightUnit(HeightUnit unit) {
        if (unit != currentState().heightUnit()) {
            unitPreferences.setHeightUnit(unit);
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
