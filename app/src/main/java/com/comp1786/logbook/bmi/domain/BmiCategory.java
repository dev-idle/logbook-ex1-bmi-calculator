package com.comp1786.logbook.bmi.domain;

/**
 * Adult BMI categories, with the boundaries published by the U.S. Centers for Disease Control
 * and Prevention (CDC), which match the World Health Organization classification.
 *
 * <p>Each category runs from its lower bound (inclusive) up to the next category's lower bound
 * (exclusive): "18.5 to less than 25" is {@link #HEALTHY_WEIGHT}. The categories apply to
 * adults aged 20 and over.
 *
 * @see <a href="https://www.cdc.gov/bmi/adult-calculator/bmi-categories.html">
 *     CDC: Adult BMI Categories</a>
 */
public enum BmiCategory {

    UNDERWEIGHT(0.0),
    HEALTHY_WEIGHT(18.5),
    OVERWEIGHT(25.0),
    OBESITY_CLASS_1(30.0),
    OBESITY_CLASS_2(35.0),
    OBESITY_CLASS_3(40.0);

    // values() returns a new array on every call, so a single copy is kept for lookups.
    private static final BmiCategory[] ALL = values();

    private final double lowerBound;

    BmiCategory(double lowerBound) {
        this.lowerBound = lowerBound;
    }

    /** The lowest BMI in this category (inclusive). */
    public double lowerBound() {
        return lowerBound;
    }

    /**
     * The BMI at which the next category starts (exclusive), or
     * {@link Double#POSITIVE_INFINITY} for the highest category.
     */
    public double upperBound() {
        int next = ordinal() + 1;
        return next < ALL.length ? ALL[next].lowerBound : Double.POSITIVE_INFINITY;
    }

    /** Returns the category that {@code bmi} falls into. */
    public static BmiCategory of(double bmi) {
        for (int i = ALL.length - 1; i > 0; i--) {
            if (bmi >= ALL[i].lowerBound) {
                return ALL[i];
            }
        }
        return UNDERWEIGHT;
    }
}
