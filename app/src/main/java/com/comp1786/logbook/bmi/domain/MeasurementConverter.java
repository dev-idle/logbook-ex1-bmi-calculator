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
     * The text of a measurement's input fields.
     *
     * @param primary the single field, or the whole field of a compound unit
     * @param part    the part field of a compound unit; empty for a simple unit
     */
    public record FieldText(String primary, String part) {
    }

    /**
     * Converts typed fields from one unit to another of the same kind. The type parameter keeps
     * weight and height units from being mixed.
     *
     * @param primaryText the single field, or the whole field of a compound unit
     * @param partText    the part field; ignored for simple units, and empty means 0
     * @return the converted field texts, or empty when the typed value cannot be read
     */
    public static <U extends Enum<U> & MeasurementUnit> Optional<FieldText> convert(
            String primaryText, String partText, U from, U to) {
        OptionalDouble value = read(primaryText, partText, from);
        if (!value.isPresent()) {
            return Optional.empty();
        }
        double converted = to.fromBaseUnit(from.toBaseUnit(value.getAsDouble()));
        if (!to.isCompound()) {
            return Optional.of(new FieldText(format(converted), ""));
        }
        CompoundQuantity split = CompoundQuantity.fromTotal(converted, to.partsPerWhole());
        return Optional.of(new FieldText(String.valueOf(split.whole()), format(split.part())));
    }

    /** Reads the typed value in {@code unit}: a plain number, or the total of a compound one. */
    private static OptionalDouble read(String primaryText, String partText, MeasurementUnit unit) {
        OptionalDouble primary = NumberInput.parse(primaryText);
        if (!primary.isPresent() || !unit.isCompound()) {
            return primary;
        }
        OptionalDouble part = NumberInput.isBlank(partText)
                ? OptionalDouble.of(0.0)
                : NumberInput.parse(partText);
        if (!part.isPresent()) {
            return OptionalDouble.empty();
        }
        return OptionalDouble.of(
                primary.getAsDouble() * unit.partsPerWhole() + part.getAsDouble());
    }

    /** Formats a value to at most one decimal place without trailing zeros: 70.0 becomes "70". */
    private static String format(double value) {
        return BigDecimal.valueOf(Rounding.halfUp(value, 1)).stripTrailingZeros().toPlainString();
    }
}
