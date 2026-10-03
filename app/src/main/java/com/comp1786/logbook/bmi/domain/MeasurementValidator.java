package com.comp1786.logbook.bmi.domain;

import java.util.OptionalDouble;

/**
 * Checks the weight and height typed by the user before a BMI is calculated.
 *
 * <p>A field is checked in order: it must not be empty, it must be a number, and the number
 * must be within the range accepted for the selected unit. Only the first problem is reported,
 * so the user sees one clear message per field.
 */
public final class MeasurementValidator {

    private MeasurementValidator() {
    }

    /**
     * Validates a measurement entered in {@code unit}.
     *
     * <p>For a compound unit the part field is optional, so "6" feet alone means 6 ft 0 in. For
     * a simple unit the part field is ignored.
     */
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

        FieldResult part = NumberInput.isBlank(partText)
                ? FieldResult.valid(0.0)
                : parse(partText);
        if (part.isValid() && part.value() >= unit.partsPerWhole()) {
            part = FieldResult.invalid(InputError.PART_OUT_OF_RANGE);
        }

        if (!whole.isValid() || !part.isValid()) {
            return MeasurementResult.invalid(whole.error(), part.error());
        }

        double total = whole.value() * unit.partsPerWhole() + part.value();
        if (!unit.accepts(total)) {
            // The combined value is out of range; the whole field carries the message.
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
}
