package com.comp1786.logbook.bmi.ui;

import android.content.res.Resources;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.comp1786.logbook.bmi.R;
import com.comp1786.logbook.bmi.domain.BmiCategory;
import com.comp1786.logbook.bmi.domain.CompoundQuantity;
import com.comp1786.logbook.bmi.domain.HeightUnit;
import com.comp1786.logbook.bmi.domain.InputError;
import com.comp1786.logbook.bmi.domain.MeasurementUnit;
import com.comp1786.logbook.bmi.domain.WeightUnit;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Builds the text the calculator screen shows: unit symbols, accepted ranges, error messages and
 * numbers formatted for the user's locale.
 */
final class MeasurementText {

    private final Resources resources;
    private final NumberFormat numberFormat;
    private final NumberFormat oneDecimalFormat;

    MeasurementText(Resources resources, Locale locale) {
        this.resources = resources;

        // Limits such as "10 to 400 kg": whole numbers stay whole, never "10.0".
        numberFormat = NumberFormat.getNumberInstance(locale);
        numberFormat.setMinimumFractionDigits(0);
        numberFormat.setMaximumFractionDigits(1);

        // Calculated values always show one decimal place, so "25.0" and "125.0 to 168.1"
        // read consistently.
        oneDecimalFormat = NumberFormat.getNumberInstance(locale);
        oneDecimalFormat.setMinimumFractionDigits(1);
        oneDecimalFormat.setMaximumFractionDigits(1);
    }

    String number(double value) {
        return numberFormat.format(value);
    }

    /** Formats a calculated value, such as a BMI, to one decimal place. */
    String oneDecimal(double value) {
        return oneDecimalFormat.format(value);
    }

    /** The symbol of a simple unit, or of the whole part of a compound unit: "kg" or "ft". */
    String symbol(MeasurementUnit unit) {
        return resources.getString(symbolRes(unit));
    }

    /** The accepted range, e.g. "10 to 400 kg" or "1 ft 8 in to 8 ft 2 in". */
    String range(MeasurementUnit unit) {
        if (unit.isCompound()) {
            return resources.getString(R.string.range,
                    compound(unit.minimum(), unit, numberFormat),
                    compound(unit.maximum(), unit, numberFormat));
        }
        return resources.getString(R.string.range_with_unit,
                number(unit.minimum()), number(unit.maximum()), symbol(unit));
    }

    /** A calculated value with its unit, to one decimal place: "56.7 kg" or "8 st 13.0 lb". */
    String quantity(double value, MeasurementUnit unit) {
        if (unit.isCompound()) {
            return compound(value, unit, oneDecimalFormat);
        }
        return resources.getString(R.string.value_with_unit, oneDecimal(value), symbol(unit));
    }

    /** The message for the single field, or the whole field of a compound unit. */
    @Nullable
    String primaryError(MeasurementKind kind, MeasurementUnit unit, @Nullable InputError error) {
        if (error == null) {
            return null;
        }
        return switch (error) {
            case REQUIRED ->
                    resources.getString(unit.isCompound() ? kind.wholeRequired : kind.required);
            case NOT_A_WHOLE_NUMBER -> resources.getString(kind.notWhole);
            case OUT_OF_RANGE -> resources.getString(kind.outOfRange, range(unit));
            default -> resources.getString(R.string.error_not_a_number);
        };
    }

    /** The message for the part field of a compound unit. */
    @Nullable
    String partError(MeasurementKind kind, @Nullable InputError error) {
        if (error == null) {
            return null;
        }
        return resources.getString(error == InputError.PART_OUT_OF_RANGE
                ? kind.partOutOfRange
                : R.string.error_not_a_number);
    }

    /**
     * The BMI values a category covers, e.g. "Below 18.5", "18.5 to 24.9" or "40.0 and above".
     *
     * <p>A category ends just below the next one, and BMI is shown to one decimal place, so
     * the highest value shown is 0.1 below the next category's lower bound.
     */
    String categoryRange(BmiCategory category) {
        if (category == BmiCategory.UNDERWEIGHT) {
            return resources.getString(
                    R.string.category_range_below, oneDecimal(category.upperBound()));
        }
        if (Double.isInfinite(category.upperBound())) {
            return resources.getString(
                    R.string.category_range_above, oneDecimal(category.lowerBound()));
        }
        return resources.getString(R.string.category_range_between,
                oneDecimal(category.lowerBound()), oneDecimal(category.upperBound() - 0.1));
    }

    /** A compound value such as "5 ft 8.9 in", with the part written by {@code partFormat}. */
    private String compound(double total, MeasurementUnit unit, NumberFormat partFormat) {
        CompoundQuantity quantity = CompoundQuantity.fromTotal(total, unit.partsPerWhole());
        return resources.getString(R.string.compound_value,
                quantity.whole(), symbol(unit),
                partFormat.format(quantity.part()), resources.getString(partSymbolRes(unit)));
    }

    @StringRes
    private static int symbolRes(MeasurementUnit unit) {
        if (unit instanceof WeightUnit weight) {
            return switch (weight) {
                case KILOGRAMS -> R.string.unit_kilograms;
                case POUNDS -> R.string.unit_pounds;
                case STONES_AND_POUNDS -> R.string.unit_stones;
            };
        }
        if (unit instanceof HeightUnit height) {
            return switch (height) {
                case CENTIMETERS -> R.string.unit_centimeters;
                case FEET_AND_INCHES -> R.string.unit_feet;
            };
        }
        throw new IllegalArgumentException("Unknown unit: " + unit);
    }

    /** The symbol of the part of a compound unit: "in" for feet and inches, "lb" for stones. */
    @StringRes
    private static int partSymbolRes(MeasurementUnit unit) {
        if (unit == HeightUnit.FEET_AND_INCHES) {
            return R.string.unit_inches;
        }
        if (unit == WeightUnit.STONES_AND_POUNDS) {
            return R.string.unit_pounds;
        }
        throw new IllegalArgumentException("Not a compound unit: " + unit);
    }
}
