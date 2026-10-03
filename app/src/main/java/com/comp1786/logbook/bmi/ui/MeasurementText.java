package com.comp1786.logbook.bmi.ui;

import android.content.res.Resources;

import androidx.annotation.Nullable;

import com.comp1786.logbook.bmi.R;
import com.comp1786.logbook.bmi.domain.BmiCategory;
import com.comp1786.logbook.bmi.domain.CompoundQuantity;
import com.comp1786.logbook.bmi.domain.HeightUnit;
import com.comp1786.logbook.bmi.domain.InputError;
import com.comp1786.logbook.bmi.domain.WeightUnit;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Builds the text the calculator screen shows: unit labels, accepted ranges, error messages and
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

    /** Formats a calculated value, such as a BMI or a healthy weight, to one decimal place. */
    String oneDecimal(double value) {
        return oneDecimalFormat.format(value);
    }

    String unitLabel(WeightUnit unit) {
        return resources.getString(switch (unit) {
            case KILOGRAMS -> R.string.unit_kilograms;
            case POUNDS -> R.string.unit_pounds;
        });
    }

    /** The accepted weight range, e.g. "10 to 400 kg". */
    String weightRange(WeightUnit unit) {
        return resources.getString(R.string.range_with_unit,
                number(unit.minimum()), number(unit.maximum()), unitLabel(unit));
    }

    /** The accepted height range, e.g. "50 to 250 cm" or "1 ft 8 in to 8 ft 2 in". */
    String heightRange(HeightUnit unit) {
        return switch (unit) {
            case CENTIMETERS -> resources.getString(R.string.range_with_unit,
                    number(unit.minimum()), number(unit.maximum()),
                    resources.getString(R.string.unit_centimeters));
            case FEET_AND_INCHES -> resources.getString(R.string.range,
                    feetAndInches(unit.minimum()), feetAndInches(unit.maximum()));
        };
    }

    @Nullable
    String weightError(@Nullable InputError error, WeightUnit unit) {
        if (error == null) {
            return null;
        }
        return switch (error) {
            case REQUIRED -> resources.getString(R.string.error_weight_required);
            case OUT_OF_RANGE ->
                    resources.getString(R.string.error_weight_out_of_range, weightRange(unit));
            default -> resources.getString(R.string.error_not_a_number);
        };
    }

    /** The message for the centimeters field, or for the feet field in feet and inches. */
    @Nullable
    String heightError(@Nullable InputError error, HeightUnit unit) {
        if (error == null) {
            return null;
        }
        return switch (error) {
            case REQUIRED -> resources.getString(unit == HeightUnit.CENTIMETERS
                    ? R.string.error_height_required
                    : R.string.error_feet_required);
            case NOT_A_WHOLE_NUMBER -> resources.getString(R.string.error_not_whole_feet);
            case OUT_OF_RANGE ->
                    resources.getString(R.string.error_height_out_of_range, heightRange(unit));
            default -> resources.getString(R.string.error_not_a_number);
        };
    }

    @Nullable
    String inchesError(@Nullable InputError error) {
        if (error == null) {
            return null;
        }
        return resources.getString(error == InputError.PART_OUT_OF_RANGE
                ? R.string.error_inches_out_of_range
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

    private String feetAndInches(double totalInches) {
        CompoundQuantity height = CompoundQuantity.fromTotal(
                totalInches, HeightUnit.FEET_AND_INCHES.partsPerWhole());
        return resources.getString(
                R.string.feet_and_inches_value, height.whole(), number(height.part()));
    }
}
