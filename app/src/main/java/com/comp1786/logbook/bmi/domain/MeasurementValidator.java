package com.comp1786.logbook.bmi.domain;

import androidx.annotation.Nullable;

import java.util.OptionalDouble;

/** Validates typed weights and heights. Each field reports only its first problem. */
public final class MeasurementValidator {

    private MeasurementValidator() {
    }

    /** Validates {@code input} in {@code unit}. An empty part field counts as 0. */
    public static MeasurementResult validate(MeasurementUnit unit, MeasurementInput input) {
        return unit.isCompound()
                ? validateCompound(unit, input.primary(), input.part())
                : validateSimple(unit, input.primary());
    }

    private static MeasurementResult validateSimple(MeasurementUnit unit, String text) {
        FieldResult result = parseRequired(text);
        if (!result.isValid()) {
            return MeasurementResult.invalid(result.error(), null);
        }
        if (!unit.accepts(result.value())) {
            return MeasurementResult.invalid(InputError.OUT_OF_RANGE, null);
        }
        return MeasurementResult.valid(result.value());
    }

    private static MeasurementResult validateCompound(
            MeasurementUnit unit, String wholeText, String partText) {
        FieldResult whole = parseRequired(wholeText);
        if (whole.isValid() && whole.value() != Math.rint(whole.value())) {
            whole = FieldResult.invalid(InputError.NOT_A_WHOLE_NUMBER);
        }

        FieldResult part = NumberInput.isBlank(partText) ? FieldResult.valid(0.0) : parse(partText);
        if (part.isValid() && part.value() >= unit.partsPerWhole()) {
            part = FieldResult.invalid(InputError.PART_OUT_OF_RANGE);
        }

        if (!whole.isValid() || !part.isValid()) {
            return MeasurementResult.invalid(whole.error(), part.error());
        }

        double total = unit.combine(whole.value(), part.value());
        if (!unit.accepts(total)) {
            // The whole field carries the message for an out-of-range total.
            return MeasurementResult.invalid(InputError.OUT_OF_RANGE, null);
        }
        return MeasurementResult.valid(total);
    }

    private static FieldResult parseRequired(String text) {
        return NumberInput.isBlank(text) ? FieldResult.invalid(InputError.REQUIRED) : parse(text);
    }

    private static FieldResult parse(String text) {
        OptionalDouble value = NumberInput.parse(text);
        return value.isPresent()
                ? FieldResult.valid(value.getAsDouble())
                : FieldResult.invalid(InputError.NOT_A_NUMBER);
    }

    /** One parsed field: a value, or the error that rejected it. */
    private record FieldResult(double value, @Nullable InputError error) {

        static FieldResult valid(double value) {
            return new FieldResult(value, null);
        }

        static FieldResult invalid(InputError error) {
            return new FieldResult(Double.NaN, error);
        }

        boolean isValid() {
            return error == null;
        }
    }
}
