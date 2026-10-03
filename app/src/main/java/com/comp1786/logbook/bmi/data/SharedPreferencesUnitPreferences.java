package com.comp1786.logbook.bmi.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.icu.util.LocaleData;
import android.icu.util.ULocale;

import com.comp1786.logbook.bmi.domain.HeightUnit;
import com.comp1786.logbook.bmi.domain.WeightUnit;

import java.util.Locale;

/**
 * Stores the selected units in {@link SharedPreferences}.
 *
 * <p>Before the user picks anything, the units follow the measurement system of the device's
 * locale: stones and feet in the UK, pounds and feet in the US, and kilograms and centimeters
 * elsewhere.
 */
public final class SharedPreferencesUnitPreferences implements UnitPreferences {

    /** Also named in res/xml/data_extraction_rules.xml, which backs the file up. */
    private static final String FILE_NAME = "unit_preferences";
    private static final String KEY_WEIGHT_UNIT = "weight_unit";
    private static final String KEY_HEIGHT_UNIT = "height_unit";

    private final SharedPreferences preferences;
    private final LocaleData.MeasurementSystem measurementSystem;

    public SharedPreferencesUnitPreferences(Context context, Locale locale) {
        preferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        measurementSystem = LocaleData.getMeasurementSystem(ULocale.forLocale(locale));
    }

    @Override
    public WeightUnit weightUnit() {
        WeightUnit defaultUnit;
        if (measurementSystem == LocaleData.MeasurementSystem.UK) {
            defaultUnit = WeightUnit.STONES_AND_POUNDS;
        } else if (measurementSystem == LocaleData.MeasurementSystem.US) {
            defaultUnit = WeightUnit.POUNDS;
        } else {
            defaultUnit = WeightUnit.KILOGRAMS;
        }
        return read(KEY_WEIGHT_UNIT, WeightUnit.class, defaultUnit);
    }

    @Override
    public HeightUnit heightUnit() {
        boolean imperial = measurementSystem == LocaleData.MeasurementSystem.UK
                || measurementSystem == LocaleData.MeasurementSystem.US;
        return read(KEY_HEIGHT_UNIT, HeightUnit.class,
                imperial ? HeightUnit.FEET_AND_INCHES : HeightUnit.CENTIMETERS);
    }

    @Override
    public void setWeightUnit(WeightUnit unit) {
        preferences.edit().putString(KEY_WEIGHT_UNIT, unit.name()).apply();
    }

    @Override
    public void setHeightUnit(HeightUnit unit) {
        preferences.edit().putString(KEY_HEIGHT_UNIT, unit.name()).apply();
    }

    /**
     * Reads a stored enum constant, falling back to {@code defaultValue} when nothing is
     * stored or the stored name no longer exists, for example after a constant is renamed.
     */
    private <E extends Enum<E>> E read(String key, Class<E> type, E defaultValue) {
        String name = preferences.getString(key, null);
        if (name == null) {
            return defaultValue;
        }
        try {
            return Enum.valueOf(type, name);
        } catch (IllegalArgumentException unknownName) {
            return defaultValue;
        }
    }
}
