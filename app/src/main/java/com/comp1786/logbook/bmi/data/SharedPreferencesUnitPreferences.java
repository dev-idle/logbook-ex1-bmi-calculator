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
 * locale: pounds and feet in the US and UK, kilograms and centimeters elsewhere.
 */
public final class SharedPreferencesUnitPreferences implements UnitPreferences {

    /** Also named in res/xml/data_extraction_rules.xml, which backs the file up. */
    private static final String FILE_NAME = "unit_preferences";
    private static final String KEY_WEIGHT_UNIT = "weight_unit";
    private static final String KEY_HEIGHT_UNIT = "height_unit";

    private final SharedPreferences preferences;
    private final boolean imperialByDefault;

    public SharedPreferencesUnitPreferences(Context context, Locale locale) {
        preferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        LocaleData.MeasurementSystem system =
                LocaleData.getMeasurementSystem(ULocale.forLocale(locale));
        imperialByDefault = system == LocaleData.MeasurementSystem.US
                || system == LocaleData.MeasurementSystem.UK;
    }

    @Override
    public WeightUnit weightUnit() {
        return read(KEY_WEIGHT_UNIT, WeightUnit.class,
                imperialByDefault ? WeightUnit.POUNDS : WeightUnit.KILOGRAMS);
    }

    @Override
    public HeightUnit heightUnit() {
        return read(KEY_HEIGHT_UNIT, HeightUnit.class,
                imperialByDefault ? HeightUnit.FEET_AND_INCHES : HeightUnit.CENTIMETERS);
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
