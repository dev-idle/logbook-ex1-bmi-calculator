package com.comp1786.logbook.bmi.domain;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.OptionalDouble;

/**
 * Rewrites values the user has already typed when they switch units, so that 70 kg becomes
 * 154.3 lb instead of being read as 70 lb.
 *
 * <p>Converted values are rounded to the target unit's {@link MeasurementUnit#fractionDigits()}
 * and written with "." as the decimal separator, which {@link NumberInput} accepts in every
 * language. Text that is not a number is left for the user to correct, so no conversion is
 * returned for it.
 */
public final class MeasurementConverter {

    private MeasurementConverter() {
    }

    /**
     * Converts typed input from one unit to another of the same kind. The type parameter keeps
     * weight and height units from being mixed.
     *
     * @return the input to show in the new unit, or empty when the typed value cannot be read
     */
    public static <U extends Enum<U> & MeasurementUnit> Optional<MeasurementInput> convert(
            MeasurementInput input, U from, U to) {
        OptionalDouble value = read(input, from);
        if (!value.isPresent()) {
            return Optional.empty();
        }
        double converted = to.fromBaseUnit(from.toBaseUnit(value.getAsDouble()));
        if (!to.isCompound()) {
            return Optional.of(MeasurementInput.of(format(converted, to.fractionDigits())));
        }
        // CompoundQuantity rounds the part to tenths, matching the default precision.
        CompoundQuantity split = CompoundQuantity.fromTotal(converted, to.partsPerWhole());
        return Optional.of(new MeasurementInput(
                String.valueOf(split.whole()), format(split.part(), to.fractionDigits())));
    }

    /** Reads the typed value in {@code unit}: a plain number, or the total of a compound one. */
    private static OptionalDouble read(MeasurementInput input, MeasurementUnit unit) {
        OptionalDouble primary = NumberInput.parse(input.primary());
        if (!primary.isPresent() || !unit.isCompound()) {
            return primary;
        }
        OptionalDouble part = NumberInput.isBlank(input.part())
                ? OptionalDouble.of(0.0)
                : NumberInput.parse(input.part());
        if (!part.isPresent()) {
            return OptionalDouble.empty();
        }
        return OptionalDouble.of(
                primary.getAsDouble() * unit.partsPerWhole() + part.getAsDouble());
    }

    /**
     * Formats a value to at most {@code fractionDigits} decimal places without trailing zeros,
     * so 70.0 becomes "70".
     */
    private static String format(double value, int fractionDigits) {
        return BigDecimal.valueOf(Rounding.halfUp(value, fractionDigits))
                .stripTrailingZeros()
                .toPlainString();
    }
}
