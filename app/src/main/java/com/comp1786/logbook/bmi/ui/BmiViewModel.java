package com.comp1786.logbook.bmi.ui;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.SavedStateHandleSupport;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.viewmodel.ViewModelInitializer;

import com.comp1786.logbook.bmi.data.SharedPreferencesUnitPreferences;
import com.comp1786.logbook.bmi.data.UnitPreferences;
import com.comp1786.logbook.bmi.domain.BmiCalculator;
import com.comp1786.logbook.bmi.domain.BmiResult;
import com.comp1786.logbook.bmi.domain.HeightUnit;
import com.comp1786.logbook.bmi.domain.MeasurementInput;
import com.comp1786.logbook.bmi.domain.MeasurementResult;
import com.comp1786.logbook.bmi.domain.MeasurementValidator;
import com.comp1786.logbook.bmi.domain.WeightUnit;

import java.util.Locale;
import java.util.Objects;
import java.util.function.UnaryOperator;

/**
 * Holds the calculator state. The screen observes {@link #uiState()} and sends events; it never
 * changes the state itself. The state is kept in a {@link SavedStateHandle}, so it survives the
 * system ending the app in the background as well as rotation.
 */
final class BmiViewModel extends ViewModel {

    /**
     * Creates the view model with its dependencies, following the Android guide "Create
     * ViewModels with dependencies".
     */
    static final ViewModelInitializer<BmiViewModel> INITIALIZER = new ViewModelInitializer<>(
            BmiViewModel.class,
            extras -> {
                Application application = Objects.requireNonNull(
                        extras.get(ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY));
                Locale locale = application.getResources().getConfiguration().getLocales().get(0);
                return new BmiViewModel(
                        new SharedPreferencesUnitPreferences(application, locale),
                        SavedStateHandleSupport.createSavedStateHandle(extras));
            });

    private static final String UI_STATE_KEY = "ui_state";

    private final UnitPreferences unitPreferences;
    private final MutableLiveData<BmiUiState> uiState;

    BmiViewModel(UnitPreferences unitPreferences, SavedStateHandle savedState) {
        this.unitPreferences = unitPreferences;
        uiState = savedState.getLiveData(UI_STATE_KEY, BmiUiState.initial(
                unitPreferences.weightUnit(), unitPreferences.heightUnit()));
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
