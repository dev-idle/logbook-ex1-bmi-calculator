package com.comp1786.logbook.bmi.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.comp1786.logbook.bmi.domain.BmiCategory;
import com.comp1786.logbook.bmi.domain.HeightUnit;
import com.comp1786.logbook.bmi.domain.InputError;
import com.comp1786.logbook.bmi.domain.MeasurementInput;
import com.comp1786.logbook.bmi.domain.WeightUnit;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class BmiViewModelTest {

    private static final double DELTA = 1e-9;
    private static final MeasurementInput EMPTY = MeasurementInput.EMPTY;

    /** Runs LiveData updates synchronously so the state can be read straight after an action. */
    @Rule
    public final InstantTaskExecutorRule instantTaskExecutor = new InstantTaskExecutorRule();

    private FakeUnitPreferences preferences;
    private BmiViewModel viewModel;

    @Before
    public void setUp() {
        preferences = new FakeUnitPreferences(WeightUnit.KILOGRAMS, HeightUnit.CENTIMETERS);
        viewModel = new BmiViewModel(preferences);
    }

    @Test
    public void startsWithTheRememberedUnitsAndNoResult() {
        BmiUiState state = new BmiViewModel(
                new FakeUnitPreferences(WeightUnit.POUNDS, HeightUnit.FEET_AND_INCHES))
                .currentState();

        assertEquals(WeightUnit.POUNDS, state.weightUnit());
        assertEquals(HeightUnit.FEET_AND_INCHES, state.heightUnit());
        assertNull(state.result());
    }

    @Test
    public void remembersSelectedUnitsForTheNextLaunch() {
        viewModel.selectWeightUnit(WeightUnit.POUNDS);
        viewModel.selectHeightUnit(HeightUnit.FEET_AND_INCHES);

        BmiUiState nextLaunch = new BmiViewModel(preferences).currentState();
        assertEquals(WeightUnit.POUNDS, nextLaunch.weightUnit());
        assertEquals(HeightUnit.FEET_AND_INCHES, nextLaunch.heightUnit());
    }

    @Test
    public void calculatesFromValidMetricInput() {
        viewModel.calculate(MeasurementInput.of("70"), MeasurementInput.of("175"));

        BmiUiState state = viewModel.currentState();
        assertNotNull(state.result());
        assertEquals(22.9, state.result().bmi(), DELTA);
        assertEquals(BmiCategory.HEALTHY_WEIGHT, state.result().category());
    }

    @Test
    public void calculatesFromImperialInput() {
        viewModel.selectWeightUnit(WeightUnit.POUNDS);
        viewModel.selectHeightUnit(HeightUnit.FEET_AND_INCHES);

        viewModel.calculate(MeasurementInput.of("154.3"), new MeasurementInput("5", "8.9"));

        assertEquals(22.9, viewModel.currentState().result().bmi(), DELTA);
    }

    @Test
    public void calculatesFromStonesAndPounds() {
        viewModel.selectWeightUnit(WeightUnit.STONES_AND_POUNDS);

        // 11 st 0 lb = 154 lb = 69.85 kg; 69.85 / 1.75^2 = 22.8.
        viewModel.calculate(new MeasurementInput("11", "0"), MeasurementInput.of("175"));

        assertEquals(22.8, viewModel.currentState().result().bmi(), DELTA);
    }

    @Test
    public void reportsEveryInvalidFieldAtOnce() {
        viewModel.selectHeightUnit(HeightUnit.FEET_AND_INCHES);

        viewModel.calculate(EMPTY, new MeasurementInput("5", "13"));

        BmiUiState state = viewModel.currentState();
        assertEquals(InputError.REQUIRED, state.weightErrors().primary());
        assertNull(state.heightErrors().primary());
        assertEquals(InputError.PART_OUT_OF_RANGE, state.heightErrors().part());
        assertNull(state.result());
    }

    @Test
    public void editingAFieldClearsItsErrorAndKeepsTheOthers() {
        viewModel.calculate(EMPTY, EMPTY);

        viewModel.onWeightEdited();

        BmiUiState state = viewModel.currentState();
        assertNull(state.weightErrors().primary());
        assertEquals(InputError.REQUIRED, state.heightErrors().primary());
    }

    @Test
    public void editingThePartFieldClearsOnlyItsError() {
        viewModel.selectHeightUnit(HeightUnit.FEET_AND_INCHES);
        viewModel.calculate(MeasurementInput.of("70"), new MeasurementInput("", "13"));

        viewModel.onHeightPartEdited();

        BmiUiState state = viewModel.currentState();
        assertEquals(InputError.REQUIRED, state.heightErrors().primary());
        assertNull(state.heightErrors().part());
    }

    @Test
    public void editingAfterAResultHidesTheOutdatedResult() {
        viewModel.calculate(MeasurementInput.of("70"), MeasurementInput.of("175"));

        viewModel.onHeightEdited();

        assertNull(viewModel.currentState().result());
    }

    @Test
    public void changingAUnitHidesTheOutdatedResult() {
        viewModel.calculate(MeasurementInput.of("70"), MeasurementInput.of("175"));

        viewModel.selectWeightUnit(WeightUnit.POUNDS);

        assertEquals(WeightUnit.POUNDS, viewModel.currentState().weightUnit());
        assertNull(viewModel.currentState().result());
    }

    @Test
    public void clearRemovesErrorsAndResultButKeepsUnits() {
        viewModel.selectWeightUnit(WeightUnit.POUNDS);
        viewModel.calculate(EMPTY, EMPTY);

        viewModel.clear();

        assertEquals(
                BmiUiState.initial(WeightUnit.POUNDS, HeightUnit.CENTIMETERS),
                viewModel.currentState());
    }
}
