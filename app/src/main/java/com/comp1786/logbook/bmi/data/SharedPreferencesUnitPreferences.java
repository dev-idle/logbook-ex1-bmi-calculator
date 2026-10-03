package com.comp1786.logbook.bmi.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.icu.util.LocaleData;
import android.icu.util.ULocale;

import com.comp1786.logbook.bmi.domain.HeightUnit;
import com.comp1786.logbook.bmi.domain.WeightUnit;

import java.util.Locale;

/**
 * Stores the selected units in {@link SharedPreferences}. Until the user chooses, units follow
 * the locale: stones and feet in the UK, pounds and feet in the US, metric elsewhere.
 *
 * <p>DataStore is recommended instead, but from Java it needs its RxJava artifacts, which two
 * values read at startup do not justify.
 *
 * @see <a href="https://developer.android.com/topic/libraries/architecture/datastore">
 *     DataStore</a>
 */
public final class SharedPreferencesUnitPreferences implements UnitPreferences {

    /** Also named in res/xml/data_extraction_rules.xml. */
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

    /** Reads a stored enum, or {@code defaultValue} if none is stored or the name is unknown. */
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
