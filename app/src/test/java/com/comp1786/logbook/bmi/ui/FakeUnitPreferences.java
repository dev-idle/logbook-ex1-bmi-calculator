package com.comp1786.logbook.bmi.ui;

import com.comp1786.logbook.bmi.data.UnitPreferences;
import com.comp1786.logbook.bmi.domain.HeightUnit;
import com.comp1786.logbook.bmi.domain.WeightUnit;

/** Keeps the selected units in memory, standing in for device storage in tests. */
final class FakeUnitPreferences implements UnitPreferences {

    private WeightUnit weightUnit;
    private HeightUnit heightUnit;

    FakeUnitPreferences(WeightUnit weightUnit, HeightUnit heightUnit) {
        this.weightUnit = weightUnit;
        this.heightUnit = heightUnit;
    }

    @Override
    public WeightUnit weightUnit() {
        return weightUnit;
    }

    @Override
    public HeightUnit heightUnit() {
        return heightUnit;
    }

    @Override
    public void setWeightUnit(WeightUnit unit) {
        weightUnit = unit;
    }

    @Override
    public void setHeightUnit(HeightUnit unit) {
        heightUnit = unit;
    }
}
