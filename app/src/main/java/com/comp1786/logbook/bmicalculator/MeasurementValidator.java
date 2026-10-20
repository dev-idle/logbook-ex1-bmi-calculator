package com.comp1786.logbook.bmicalculator;

import androidx.annotation.Nullable;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Checks the text typed into the weight or height fields.
 */
public final class MeasurementValidator {

    /** Reasons an input is rejected. */
    public enum Reason {
        EMPTY, NOT_A_NUMBER, NOT_A_WHOLE_NUMBER, PART_TOO_LARGE, OUT_OF_RANGE
    }

    // Accepts "70", "70.5", "70,5", "70." and ".5".
    private static final Pattern NUMBER = Pattern.compile("\\d+([.,]\\d*)?|[.,]\\d+");

    private MeasurementValidator() {
    }

    /** Validates a measurement, counting an empty part field as zero, so "5 ft" is accepted. */
    public static Result validate(MeasurementUnit unit, String wholeText, String partText) {
        String whole = wholeText.trim();
        if (whole.isEmpty()) {
            return Result.invalid(Reason.EMPTY, false);
        }
        BigDecimal wholeValue = parse(whole);
        if (wholeValue == null) {
            return Result.invalid(Reason.NOT_A_NUMBER, false);
        }

        BigDecimal amount = wholeValue;
        if (unit.isCompound()) {
            if (wholeValue.stripTrailingZeros().scale() > 0) {
                return Result.invalid(Reason.NOT_A_WHOLE_NUMBER, false);
            }
            BigDecimal partValue = BigDecimal.ZERO;
            String part = partText.trim();
            if (!part.isEmpty()) {
                partValue = parse(part);
                if (partValue == null) {
                    return Result.invalid(Reason.NOT_A_NUMBER, true);
                }
                if (partValue.compareTo(BigDecimal.valueOf(unit.getPartsPerWhole())) >= 0) {
                    return Result.invalid(Reason.PART_TOO_LARGE, true);
                }
            }
            amount = unit.combine(wholeValue, partValue);
        }

        if (amount.compareTo(unit.getMinimum()) < 0 || amount.compareTo(unit.getMaximum()) > 0) {
            return Result.invalid(Reason.OUT_OF_RANGE, false);
        }
        return Result.valid(amount);
    }

    @Nullable
    private static BigDecimal parse(String text) {
        if (!NUMBER.matcher(text).matches()) {
            return null;
        }
        return new BigDecimal(text.replace(',', '.'));
    }

    /** Either a valid amount or the reason it was rejected and the field it belongs to. */
    public static final class Result {

        private final BigDecimal amount;
        private final Reason reason;
        private final boolean errorInPart;

        private Result(BigDecimal amount, Reason reason, boolean errorInPart) {
            this.amount = amount;
            this.reason = reason;
            this.errorInPart = errorInPart;
        }

        static Result valid(BigDecimal amount) {
            return new Result(amount, null, false);
        }

        static Result invalid(Reason reason, boolean errorInPart) {
            return new Result(null, reason, errorInPart);
        }

        public boolean isValid() {
            return reason == null;
        }

        /** Returns the amount in the smallest unit, or null when invalid. */
        @Nullable
        public BigDecimal getAmount() {
            return amount;
        }

        @Nullable
        public Reason getReason() {
            return reason;
        }

        /** Returns true when the error belongs to the part field, such as inches. */
        public boolean isErrorInPart() {
            return errorInPart;
        }
    }
}
