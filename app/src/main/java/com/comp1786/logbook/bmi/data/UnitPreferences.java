package com.comp1786.logbook.bmi.data;

import com.comp1786.logbook.bmi.domain.HeightUnit;
import com.comp1786.logbook.bmi.domain.WeightUnit;

/**
 * Remembers the units the user last selected, so the calculator opens in them next time.
 *
 * <p>An interface, so view model tests can use an in-memory fake instead of device storage.
 */
public interface UnitPreferences {

    WeightUnit weightUnit();

    HeightUnit heightUnit();

    void setWeightUnit(WeightUnit unit);

    void setHeightUnit(HeightUnit unit);
}
