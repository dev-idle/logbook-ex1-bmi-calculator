package com.comp1786.logbook.bmi.ui;

import androidx.annotation.ColorRes;
import androidx.annotation.StringRes;

import com.comp1786.logbook.bmi.R;
import com.comp1786.logbook.bmi.domain.BmiCategory;

/** The label and color of each category, kept out of the domain so it has no Android resources. */
final class CategoryAppearance {

    private CategoryAppearance() {
    }

    @StringRes
    static int label(BmiCategory category) {
        return switch (category) {
            case UNDERWEIGHT -> R.string.category_underweight;
            case HEALTHY_WEIGHT -> R.string.category_healthy_weight;
            case OVERWEIGHT -> R.string.category_overweight;
            case OBESITY_CLASS_1 -> R.string.category_obesity_class_1;
            case OBESITY_CLASS_2 -> R.string.category_obesity_class_2;
            case OBESITY_CLASS_3 -> R.string.category_obesity_class_3;
        };
    }

    @ColorRes
    static int color(BmiCategory category) {
        return switch (category) {
            case UNDERWEIGHT -> R.color.bmi_underweight;
            case HEALTHY_WEIGHT -> R.color.bmi_healthy_weight;
            case OVERWEIGHT -> R.color.bmi_overweight;
            case OBESITY_CLASS_1 -> R.color.bmi_obesity_class_1;
            case OBESITY_CLASS_2 -> R.color.bmi_obesity_class_2;
            case OBESITY_CLASS_3 -> R.color.bmi_obesity_class_3;
        };
    }
}
