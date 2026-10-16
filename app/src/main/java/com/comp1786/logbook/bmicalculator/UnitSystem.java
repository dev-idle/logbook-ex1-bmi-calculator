package com.comp1786.logbook.bmicalculator;

import androidx.annotation.StringRes;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Unit systems for entering weight and height.
 */
public enum UnitSystem {

    METRIC(R.string.unit_system_metric,
            new Scale("1", 0, R.string.unit_kg, 0, "10", "400"),
            new Scale("0.01", 0, R.string.unit_cm, 0, "50", "280")),
    US(R.string.unit_system_us,
            new Scale(Scale.KILOGRAMS_PER_POUND, 0, R.string.unit_lb, 0, "22", "881.8"),
            new Scale(Scale.METERS_PER_INCH, 12, R.string.unit_ft, R.string.unit_in, "19.7", "110.2")),
    UK(R.string.unit_system_uk,
            new Scale(Scale.KILOGRAMS_PER_POUND, 14, R.string.unit_st, R.string.unit_lb, "22", "881.8"),
            new Scale(Scale.METERS_PER_INCH, 12, R.string.unit_ft, R.string.unit_in, "19.7", "110.2"));

    @StringRes
    private final int labelRes;
    private final Scale weight;
    private final Scale height;

    UnitSystem(@StringRes int labelRes, Scale weight, Scale height) {
        this.labelRes = labelRes;
        this.weight = weight;
        this.height = height;
    }

    @StringRes
    public int getLabelRes() {
        return labelRes;
    }

    /** Returns the weight scale, whose base unit is the kilogram. */
    public Scale getWeight() {
        return weight;
    }

    /** Returns the height scale, whose base unit is the meter. */
    public Scale getHeight() {
        return height;
    }

    /**
     * One measurement entered either as a single value (70 kg) or as a whole unit plus
     * a part (11 st 0.3 lb). Amounts are counted in the smallest unit of the scale.
     */
    public static final class Scale {

        // Exact definitions (NIST SP 811; UK Weights and Measures Act 1985, Schedule 1).
        static final String KILOGRAMS_PER_POUND = "0.45359237";
        static final String METERS_PER_INCH = "0.0254";

        private static final int DECIMALS = 1;
        private static final BigDecimal STEP = new BigDecimal("0.1");

        private final BigDecimal baseUnitsPerAmount;
        private final int partsPerWhole;
        @StringRes
        private final int wholeUnitRes;
        @StringRes
        private final int partUnitRes;
        private final BigDecimal minimum;
        private final BigDecimal maximum;

        Scale(String baseUnitsPerAmount, int partsPerWhole, @StringRes int wholeUnitRes,
              @StringRes int partUnitRes, String minimum, String maximum) {
            this.baseUnitsPerAmount = new BigDecimal(baseUnitsPerAmount);
            this.partsPerWhole = partsPerWhole;
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

        /** Returns the part field value of an amount, or zero for a single-field scale. */
        public BigDecimal partOf(BigDecimal amount) {
            return isCompound() ? amount.remainder(BigDecimal.valueOf(partsPerWhole)) : BigDecimal.ZERO;
        }

        /** Converts an amount to kilograms or meters. */
        public BigDecimal toBase(BigDecimal amount) {
            return amount.multiply(baseUnitsPerAmount);
        }

        /** Converts kilograms or meters to an amount with one decimal place. */
        public BigDecimal fromBase(BigDecimal base, RoundingMode roundingMode) {
            return base.divide(baseUnitsPerAmount, DECIMALS, roundingMode);
        }

        /** Returns the largest amount with one decimal place that is strictly below {@code base}. */
        public BigDecimal fromBaseBelow(BigDecimal base) {
            BigDecimal amount = fromBase(base, RoundingMode.FLOOR);
            return toBase(amount).compareTo(base) < 0 ? amount : amount.subtract(STEP);
        }
    }
}
