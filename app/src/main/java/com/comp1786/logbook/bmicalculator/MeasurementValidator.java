package com.comp1786.logbook.bmicalculator;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Checks the text typed into the weight or height fields.
 */
public final class MeasurementValidator {

    /** Reasons an input is rejected. */
    public enum Error {
        EMPTY, NOT_A_NUMBER, NOT_A_WHOLE_NUMBER, PART_TOO_LARGE, OUT_OF_RANGE
    }

    // Accepts "70", "70.5", "70,5", "70." and ".5".
    private static final Pattern NUMBER = Pattern.compile("\\d+([.,]\\d*)?|[.,]\\d+");

    private MeasurementValidator() {
    }

    /**
     * Validates a measurement. An empty part field counts as zero, so "5 ft" is accepted.
     */
    public static Result validate(UnitSystem.Scale scale, String wholeText, String partText) {
        String whole = wholeText.trim();
        if (whole.isEmpty()) {
            return Result.invalid(Error.EMPTY, false);
        }
        BigDecimal wholeValue = parse(whole);
        if (wholeValue == null) {
            return Result.invalid(Error.NOT_A_NUMBER, false);
        }

        BigDecimal amount = wholeValue;
        if (scale.isCompound()) {
            if (wholeValue.stripTrailingZeros().scale() > 0) {
                return Result.invalid(Error.NOT_A_WHOLE_NUMBER, false);
            }
            BigDecimal partValue = BigDecimal.ZERO;
            String part = partText.trim();
            if (!part.isEmpty()) {
                partValue = parse(part);
                if (partValue == null) {
                    return Result.invalid(Error.NOT_A_NUMBER, true);
                }
                if (partValue.compareTo(BigDecimal.valueOf(scale.getPartsPerWhole())) >= 0) {
                    return Result.invalid(Error.PART_TOO_LARGE, true);
                }
            }
            amount = scale.combine(wholeValue, partValue);
        }

        if (amount.compareTo(scale.getMinimum()) < 0 || amount.compareTo(scale.getMaximum()) > 0) {
            return Result.invalid(Error.OUT_OF_RANGE, false);
        }
        return Result.valid(amount);
    }

    private static BigDecimal parse(String text) {
        if (!NUMBER.matcher(text).matches()) {
            return null;
        }
        return new BigDecimal(text.replace(',', '.'));
    }

    /** Either a valid amount or the error and the field it belongs to. */
    public static final class Result {

        private final BigDecimal amount;
        private final Error error;
        private final boolean errorInPart;

        private Result(BigDecimal amount, Error error, boolean errorInPart) {
            this.amount = amount;
            this.error = error;
            this.errorInPart = errorInPart;
        }

        static Result valid(BigDecimal amount) {
            return new Result(amount, null, false);
        }

        static Result invalid(Error error, boolean errorInPart) {
            return new Result(null, error, errorInPart);
        }

        public boolean isValid() {
            return error == null;
        }

        /** Returns the amount in the smallest unit of the scale, or null when invalid. */
        public BigDecimal getAmount() {
            return amount;
        }

        public Error getError() {
            return error;
        }

        /** Returns true when the error belongs to the part field, such as inches. */
        public boolean isErrorInPart() {
            return errorInPart;
        }
    }
}
