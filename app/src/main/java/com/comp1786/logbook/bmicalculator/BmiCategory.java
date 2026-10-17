package com.comp1786.logbook.bmicalculator;

import androidx.annotation.ColorRes;
import androidx.annotation.StringRes;

import java.math.BigDecimal;

/**
 * Adult BMI categories from WHO Technical Report Series 894, Table 2.1.
 */
public enum BmiCategory {

    UNDERWEIGHT("0.00", R.string.category_underweight,
            R.color.category_underweight, R.color.category_underweight_container),
    NORMAL_RANGE("18.50", R.string.category_normal_range,
            R.color.category_normal_range, R.color.category_normal_range_container),
    PREOBESE("25.00", R.string.category_preobese,
            R.color.category_preobese, R.color.category_preobese_container),
    OBESE_CLASS_I("30.00", R.string.category_obese_class_1,
            R.color.category_obese_class_1, R.color.category_obese_class_1_container),
    OBESE_CLASS_II("35.00", R.string.category_obese_class_2,
            R.color.category_obese_class_2, R.color.category_obese_class_2_container),
    OBESE_CLASS_III("40.00", R.string.category_obese_class_3,
            R.color.category_obese_class_3, R.color.category_obese_class_3_container);

    private final BigDecimal lowerBound;
    @StringRes
    private final int labelRes;
    @ColorRes
    private final int colorRes;
    @ColorRes
    private final int containerColorRes;

    BmiCategory(String lowerBound, @StringRes int labelRes,
                @ColorRes int colorRes, @ColorRes int containerColorRes) {
        this.lowerBound = new BigDecimal(lowerBound);
        this.labelRes = labelRes;
        this.colorRes = colorRes;
        this.containerColorRes = containerColorRes;
    }

    /** Returns the category for a BMI rounded to two decimal places. */
    public static BmiCategory of(BigDecimal bmi) {
        BmiCategory[] categories = values();
        for (int i = categories.length - 1; i > 0; i--) {
            if (bmi.compareTo(categories[i].lowerBound) >= 0) {
                return categories[i];
            }
        }
        return UNDERWEIGHT;
    }

    public BigDecimal getLowerBound() {
        return lowerBound;
    }

    @StringRes
    public int getLabelRes() {
        return labelRes;
    }

    @ColorRes
    public int getColorRes() {
        return colorRes;
    }

    @ColorRes
    public int getContainerColorRes() {
        return containerColorRes;
    }
}
