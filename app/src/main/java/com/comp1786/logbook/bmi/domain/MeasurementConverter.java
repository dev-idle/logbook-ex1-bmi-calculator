package com.comp1786.logbook.bmi.domain;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.OptionalDouble;

/**
 * Rewrites values the user has already typed when they switch units, so that 70 kg becomes
 * 154.3 lb instead of being read as 70 lb.
 *
 * <p>Converted values are rounded to one decimal place and written with "." as the decimal
 * separator, which {@link NumberInput} accepts in every language. Text that is not a number is
 * left for the user to correct, so no conversion is returned for it.
 */
public final class MeasurementConverter {

    private MeasurementConverter() {
    }

    /**
     * The text of the height fields.
     *
     * @param primary the centimeters field, or the feet field for feet and inches
     * @param inches  the inches field; empty for centimeters
     */
    public record HeightText(String primary, String inches) {
    }

    /**
     * Converts a typed weight to another unit.
     *
     * @return the converted text, or empty when {@code text} is not a number
     */
    public static Optional<String> convertWeight(String text, WeightUnit from, WeightUnit to) {
        OptionalDouble value = NumberInput.parse(text);
        if (!value.isPresent()) {
            return Optional.empty();
        }
        return Optional.of(format(to.fromKilograms(from.toKilograms(value.getAsDouble()))));
    }

    /**
     * Converts typed height fields to another unit.
     *
     * @param primaryText the centimeters field, or the feet field for feet and inches
     * @param inchesText  the inches field; ignored for centimeters, and empty means 0 inches
     * @return the converted field texts, or empty when the height cannot be read
     */
    public static Optional<HeightText> convertHeight(
            String primaryText, String inchesText, HeightUnit from, HeightUnit to) {
        OptionalDouble value = readHeight(primaryText, inchesText, from);
        if (!value.isPresent()) {
            return Optional.empty();
        }
        double converted = to.fromMeters(from.toMeters(value.getAsDouble()));
        return Optional.of(switch (to) {
            case CENTIMETERS -> new HeightText(format(converted), "");
            case FEET_AND_INCHES -> {
                FeetAndInches split = FeetAndInches.fromTotalInches(converted);
                yield new HeightText(String.valueOf(split.feet()), format(split.inches()));
            }
        });
    }

    /** Reads the height in {@code unit}: centimeters, or total inches for feet and inches. */
    private static OptionalDouble readHeight(
            String primaryText, String inchesText, HeightUnit unit) {
        OptionalDouble primary = NumberInput.parse(primaryText);
        if (!primary.isPresent() || unit == HeightUnit.CENTIMETERS) {
            return primary;
        }
        OptionalDouble inches = NumberInput.isBlank(inchesText)
                ? OptionalDouble.of(0.0)
                : NumberInput.parse(inchesText);
        if (!inches.isPresent()) {
            return OptionalDouble.empty();
        }
        return OptionalDouble.of(
                primary.getAsDouble() * UnitConversions.INCHES_PER_FOOT + inches.getAsDouble());
    }

    /** Formats a value to at most one decimal place without trailing zeros: 70.0 becomes "70". */
    private static String format(double value) {
        return BigDecimal.valueOf(Rounding.halfUp(value, 1)).stripTrailingZeros().toPlainString();
    }
}
