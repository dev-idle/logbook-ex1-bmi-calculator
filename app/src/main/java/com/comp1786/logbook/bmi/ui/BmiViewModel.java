package com.comp1786.logbook.bmi.ui;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.comp1786.logbook.bmi.data.UnitPreferences;
import com.comp1786.logbook.bmi.domain.BmiCalculator;
import com.comp1786.logbook.bmi.domain.BmiResult;
import com.comp1786.logbook.bmi.domain.HeightUnit;
import com.comp1786.logbook.bmi.domain.MeasurementInput;
import com.comp1786.logbook.bmi.domain.MeasurementResult;
import com.comp1786.logbook.bmi.domain.MeasurementValidator;
import com.comp1786.logbook.bmi.domain.WeightUnit;

import java.util.Objects;
import java.util.function.UnaryOperator;

/**
 * Holds the calculator state, which survives rotation. The screen observes {@link #uiState()}
 * and sends events; it never changes the state itself.
 */
final class BmiViewModel extends ViewModel {

    private final UnitPreferences unitPreferences;
    private final MutableLiveData<BmiUiState> uiState;

    BmiViewModel(UnitPreferences unitPreferences) {
        this.unitPreferences = unitPreferences;
        uiState = new MutableLiveData<>(BmiUiState.initial(
                unitPreferences.weightUnit(), unitPreferences.heightUnit()));
    }

    /** Supplies the constructor argument, which the default factory cannot. */
    static ViewModelProvider.Factory factory(UnitPreferences unitPreferences) {
        return new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                return modelClass.cast(new BmiViewModel(unitPreferences));
            }
        };
    }

    LiveData<BmiUiState> uiState() {
        return uiState;
    }

    /** The current state; never {@code null}, as the LiveData starts with a value. */
    BmiUiState currentState() {
        return Objects.requireNonNull(uiState.getValue());
    }

    /** Switches the weight unit and remembers it for the next launch. */
    void selectWeightUnit(WeightUnit unit) {
        if (unit != currentState().weightUnit()) {
            unitPreferences.setWeightUnit(unit);
            update(state -> state.withWeightUnit(unit));
        }
    }

    /** Switches the height unit and remembers it for the next launch. */
    void selectHeightUnit(HeightUnit unit) {
        if (unit != currentState().heightUnit()) {
            unitPreferences.setHeightUnit(unit);
            update(state -> state.withHeightUnit(unit));
        }
    }

    /** The single weight field, or the whole field of a compound unit, was edited. */
    void onWeightEdited() {
        update(state -> state.withWeightErrors(state.weightErrors().withoutPrimary()));
    }

    /** The part field of a compound weight unit was edited. */
    void onWeightPartEdited() {
        update(state -> state.withWeightErrors(state.weightErrors().withoutPart()));
    }

    /** The single height field, or the whole field of a compound unit, was edited. */
    void onHeightEdited() {
        update(state -> state.withHeightErrors(state.heightErrors().withoutPrimary()));
    }

    /** The part field of a compound height unit was edited. */
    void onHeightPartEdited() {
        update(state -> state.withHeightErrors(state.heightErrors().withoutPart()));
    }

    /** Calculates the BMI, or reports every invalid field at once. */
    void calculate(MeasurementInput weightInput, MeasurementInput heightInput) {
        BmiUiState state = currentState();
        MeasurementResult weight = MeasurementValidator.validate(state.weightUnit(), weightInput);
        MeasurementResult height = MeasurementValidator.validate(state.heightUnit(), heightInput);

        if (!weight.isValid() || !height.isValid()) {
            uiState.setValue(state.withErrors(FieldErrors.of(weight), FieldErrors.of(height)));
            return;
        }

        double kilograms = state.weightUnit().toBaseUnit(weight.value());
        double meters = state.heightUnit().toBaseUnit(height.value());
        BmiResult result = BmiCalculator.calculate(kilograms, meters);
        uiState.setValue(state.withResult(result));
    }

    void clear() {
        update(BmiUiState::cleared);
    }

    private void update(UnaryOperator<BmiUiState> change) {
        BmiUiState current = currentState();
        BmiUiState next = change.apply(current);
        // Skipping identical states avoids a redraw on every keystroke.
        if (!next.equals(current)) {
            uiState.setValue(next);
        }
    }
}
