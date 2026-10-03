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

    /** Validates a weight entered in {@code unit}. */
    public static FieldResult validateWeight(String text, WeightUnit unit) {
        FieldResult result = parseRequired(text);
        if (result.isValid() && !unit.accepts(result.value())) {
            return FieldResult.invalid(InputError.OUT_OF_RANGE);
        }
        return result;
    }

    /**
     * Validates a height entered in {@code unit}.
     *
     * @param primaryText the centimeters field, or the feet field for feet and inches
     * @param inchesText  the inches field; ignored for centimeters and optional otherwise,
     *                    so "6" feet with no inches means 6 ft 0 in
     */
    public static HeightResult validateHeight(
            HeightUnit unit, String primaryText, String inchesText) {
        return switch (unit) {
            case CENTIMETERS -> validateCentimeters(primaryText);
            case FEET_AND_INCHES -> validateFeetAndInches(primaryText, inchesText);
        };
    }

    private static HeightResult validateCentimeters(String text) {
        FieldResult result = parseRequired(text);
        if (!result.isValid()) {
            return HeightResult.invalid(result.error(), null);
        }
        if (!HeightUnit.CENTIMETERS.accepts(result.value())) {
            return HeightResult.invalid(InputError.OUT_OF_RANGE, null);
        }
        return HeightResult.valid(result.value());
    }

    private static HeightResult validateFeetAndInches(String feetText, String inchesText) {
        FieldResult feet = parseRequired(feetText);
        if (feet.isValid() && feet.value() != Math.rint(feet.value())) {
            feet = FieldResult.invalid(InputError.NOT_A_WHOLE_NUMBER);
        }

        FieldResult inches = NumberInput.isBlank(inchesText)
                ? FieldResult.valid(0.0)
                : parse(inchesText);
        if (inches.isValid() && inches.value() >= UnitConversions.INCHES_PER_FOOT) {
            inches = FieldResult.invalid(InputError.INCHES_OUT_OF_RANGE);
        }

        if (!feet.isValid() || !inches.isValid()) {
            return HeightResult.invalid(feet.error(), inches.error());
        }

        double totalInches = feet.value() * UnitConversions.INCHES_PER_FOOT + inches.value();
        if (!HeightUnit.FEET_AND_INCHES.accepts(totalInches)) {
            // The combined height is too small or too large; the feet field carries the message.
            return HeightResult.invalid(InputError.OUT_OF_RANGE, null);
        }
        return HeightResult.valid(totalInches);
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
