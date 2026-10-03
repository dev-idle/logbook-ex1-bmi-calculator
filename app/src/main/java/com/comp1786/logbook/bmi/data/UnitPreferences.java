package com.comp1786.logbook.bmi.data;

import com.comp1786.logbook.bmi.domain.HeightUnit;
import com.comp1786.logbook.bmi.domain.WeightUnit;

/** The units the user last selected. An interface, so tests can use an in-memory fake. */
public interface UnitPreferences {

    WeightUnit weightUnit();

    HeightUnit heightUnit();

    void setWeightUnit(WeightUnit unit);

    void setHeightUnit(HeightUnit unit);
}
