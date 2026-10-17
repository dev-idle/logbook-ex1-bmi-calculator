package com.comp1786.logbook.bmicalculator;

import androidx.annotation.StringRes;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Units for entering weight (base unit kg) or height (base unit m).
 * A unit is entered either as one value (70 kg) or as a whole unit plus a part
 * (11 st 0.3 lb); amounts are counted in the smallest unit.
 */
public enum MeasurementUnit {

    // Exact factors: 1 lb = 0.45359237 kg, 1 in = 0.0254 m (NIST SP 811;
    // UK Weights and Measures Act 1985, Schedule 1).
    // Limits cover every adult recorded by Guinness World Records.
    KILOGRAMS("1", 0, 1, R.string.unit_kg, 0,
            "5", "650"),
    POUNDS("0.45359237", 0, 1, R.string.unit_lb, 0,
            "11", "1433"),
    STONES_AND_POUNDS("0.45359237", 14, 1, R.string.unit_st, R.string.unit_lb,
            "11", "1433"),
    CENTIMETERS("0.01", 0, 1, R.string.unit_cm, 0,
            "50", "280"),
    METERS("1", 0, 3, R.string.unit_m, 0,
            "0.5", "2.8"),
    FEET_AND_INCHES("0.0254", 12, 1, R.string.unit_ft, R.string.unit_in,
            "19.7", "110.2");

    private final BigDecimal baseUnitsPerAmount;
    private final int partsPerWhole;
    private final int decimals;
    @StringRes
    private final int wholeUnitRes;
    @StringRes
    private final int partUnitRes;
    private final BigDecimal minimum;
    private final BigDecimal maximum;

    MeasurementUnit(String baseUnitsPerAmount, int partsPerWhole, int decimals,
                    @StringRes int wholeUnitRes, @StringRes int partUnitRes,
                    String minimum, String maximum) {
        this.baseUnitsPerAmount = new BigDecimal(baseUnitsPerAmount);
        this.partsPerWhole = partsPerWhole;
        this.decimals = decimals;
        this.wholeUnitRes = wholeUnitRes;
        this.partUnitRes = partUnitRes;
        this.minimum = new BigDecimal(minimum);
        this.maximum = new BigDecimal(maximum);
    }

    /** Returns true when the value is entered in two fields, such as feet and inches. */
    public boolean isCompound() {
        return partsPerWhole > 0;
    }

    public int getPartsPerWhole() {
        return partsPerWhole;
    }

    @StringRes
    public int getWholeUnitRes() {
        return wholeUnitRes;
    }

    @StringRes
    public int getPartUnitRes() {
        return partUnitRes;
    }

    public BigDecimal getMinimum() {
        return minimum;
    }

    public BigDecimal getMaximum() {
        return maximum;
    }

    /** Returns the amount for a whole value and its part, e.g. 11 st 0.3 lb as 154.3 lb. */
    public BigDecimal combine(BigDecimal whole, BigDecimal part) {
        return isCompound() ? whole.multiply(BigDecimal.valueOf(partsPerWhole)).add(part) : whole;
    }

    /** Returns the whole-unit field value of an amount. */
    public BigDecimal wholeOf(BigDecimal amount) {
        return isCompound() ? amount.divideToIntegralValue(BigDecimal.valueOf(partsPerWhole)) : amount;
    }

    /** Returns the part field value of an amount, or zero for a single-field unit. */
    public BigDecimal partOf(BigDecimal amount) {
        return isCompound() ? amount.remainder(BigDecimal.valueOf(partsPerWhole)) : BigDecimal.ZERO;
    }

    /** Converts an amount to kilograms or meters. */
    public BigDecimal toBase(BigDecimal amount) {
        return amount.multiply(baseUnitsPerAmount);
    }

    /** Converts kilograms or meters to an amount rounded to this unit's decimal places. */
    public BigDecimal fromBase(BigDecimal base, RoundingMode roundingMode) {
        return base.divide(baseUnitsPerAmount, decimals, roundingMode);
    }

    /** Returns the largest amount at this unit's precision that is strictly below {@code base}. */
    public BigDecimal fromBaseBelow(BigDecimal base) {
        BigDecimal amount = fromBase(base, RoundingMode.FLOOR);
        return toBase(amount).compareTo(base) < 0
                ? amount : amount.subtract(BigDecimal.ONE.movePointLeft(decimals));
    }
}
