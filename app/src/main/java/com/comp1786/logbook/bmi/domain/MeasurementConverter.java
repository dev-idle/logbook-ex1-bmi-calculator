package com.comp1786.logbook.bmi.domain;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.OptionalDouble;

/**
 * Converts typed values when the user switches units, so 70 kg becomes 154.3 lb instead of
 * being read as 70 lb.
 */
public final class MeasurementConverter {

    private MeasurementConverter() {
    }

    /**
     * Converts {@code input} between two units of the same kind; the type bound stops weight
     * and height units being mixed.
     *
     * @return the converted input, or empty if the typed text is not a number
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
        return OptionalDouble.of(unit.combine(primary.getAsDouble(), part.getAsDouble()));
    }

    /**
     * Rounds to {@code fractionDigits} places without trailing zeros, so 70.0 becomes "70". The
     * "." separator is always used, as {@link NumberInput} reads it in every locale.
     */
    private static String format(double value, int fractionDigits) {
        return BigDecimal.valueOf(Rounding.halfUp(value, fractionDigits))
                .stripTrailingZeros()
                .toPlainString();
    }
}
