package com.comp1786.logbook.bmi.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.comp1786.logbook.bmi.domain.BmiCategory;
import com.comp1786.logbook.bmi.domain.HeightUnit;
import com.comp1786.logbook.bmi.domain.InputError;
import com.comp1786.logbook.bmi.domain.WeightUnit;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class BmiViewModelTest {

    private static final double DELTA = 1e-9;

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
        viewModel.calculate("70", "175", "");

        BmiUiState state = viewModel.currentState();
        assertNotNull(state.result());
        assertEquals(22.9, state.result().bmi(), DELTA);
        assertEquals(BmiCategory.HEALTHY_WEIGHT, state.result().category());
    }

    @Test
    public void calculatesFromImperialInput() {
        viewModel.selectWeightUnit(WeightUnit.POUNDS);
        viewModel.selectHeightUnit(HeightUnit.FEET_AND_INCHES);

        viewModel.calculate("154.3", "5", "8.9");

        assertEquals(22.9, viewModel.currentState().result().bmi(), DELTA);
    }

    @Test
    public void reportsEveryInvalidFieldAtOnce() {
        viewModel.selectHeightUnit(HeightUnit.FEET_AND_INCHES);

        viewModel.calculate("", "5", "13");

        BmiUiState state = viewModel.currentState();
        assertEquals(InputError.REQUIRED, state.weightError());
        assertNull(state.heightError());
        assertEquals(InputError.PART_OUT_OF_RANGE, state.inchesError());
        assertNull(state.result());
    }

    @Test
    public void editingAFieldClearsItsErrorAndKeepsTheOthers() {
        viewModel.calculate("", "", "");

        viewModel.onWeightEdited();

        BmiUiState state = viewModel.currentState();
        assertNull(state.weightError());
        assertEquals(InputError.REQUIRED, state.heightError());
    }

    @Test
    public void editingAfterAResultHidesTheOutdatedResult() {
        viewModel.calculate("70", "175", "");

        viewModel.onHeightEdited();

        assertNull(viewModel.currentState().result());
    }

    @Test
    public void changingAUnitHidesTheOutdatedResult() {
        viewModel.calculate("70", "175", "");

        viewModel.selectWeightUnit(WeightUnit.POUNDS);

        assertEquals(WeightUnit.POUNDS, viewModel.currentState().weightUnit());
        assertNull(viewModel.currentState().result());
    }

    @Test
    public void clearRemovesErrorsAndResultButKeepsUnits() {
        viewModel.selectWeightUnit(WeightUnit.POUNDS);
        viewModel.calculate("", "", "");

        viewModel.clear();

        assertEquals(
                BmiUiState.initial(WeightUnit.POUNDS, HeightUnit.CENTIMETERS),
                viewModel.currentState());
    }
}
