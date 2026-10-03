package com.comp1786.logbook.bmi.ui;

import androidx.annotation.StringRes;

import com.comp1786.logbook.bmi.R;

/**
 * The two measurements the calculator collects, with the validation messages that name them.
 * Each measurement has one compound unit (stones and pounds, or feet and inches), so its
 * messages can name that unit's fields.
 */
enum MeasurementKind {

    WEIGHT(
            R.string.error_weight_required,
            R.string.error_stones_required,
            R.string.error_not_whole_stones,
            R.string.error_weight_out_of_range,
            R.string.error_pounds_out_of_range),

    HEIGHT(
            R.string.error_height_required,
            R.string.error_feet_required,
            R.string.error_not_whole_feet,
            R.string.error_height_out_of_range,
            R.string.error_inches_out_of_range);

    /** The single field is empty. */
    @StringRes
    final int required;

    /** The whole field of the compound unit is empty. */
    @StringRes
    final int wholeRequired;

    /** The whole field of the compound unit is not a whole number. */
    @StringRes
    final int notWhole;

    /** The value is out of range; the message takes the accepted range as its argument. */
    @StringRes
    final int outOfRange;

    /** The part field of the compound unit is a whole unit or more. */
    @StringRes
    final int partOutOfRange;

    MeasurementKind(@StringRes int required, @StringRes int wholeRequired,
                    @StringRes int notWhole, @StringRes int outOfRange,
                    @StringRes int partOutOfRange) {
        this.required = required;
        this.wholeRequired = wholeRequired;
        this.notWhole = notWhole;
        this.outOfRange = outOfRange;
        this.partOutOfRange = partOutOfRange;
    }
}
