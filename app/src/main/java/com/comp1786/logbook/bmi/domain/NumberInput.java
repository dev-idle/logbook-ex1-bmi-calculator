package com.comp1786.logbook.bmi.domain;

import java.util.OptionalDouble;
import java.util.regex.Pattern;

/**
 * Parses numbers typed by the user.
 *
 * <p>Either "." or "," is accepted as the decimal separator, because the soft keyboard offers
 * the separator used by the device's language. Signs, exponents, "NaN" and "Infinity", which
 * {@link Double#parseDouble(String)} would otherwise accept, are rejected.
 */
public final class NumberInput {

    /** Digits with an optional fractional part, such as "70", "70.5", "70," or ".5". */
    private static final Pattern DECIMAL = Pattern.compile("\\d+(?:[.,]\\d*)?|[.,]\\d+");

    private NumberInput() {
    }

    /** Returns whether {@code text} is null, empty or only whitespace. */
    public static boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }

    /**
     * Parses a non-negative decimal number.
     *
     * @return the value, or an empty result if {@code text} is not a plain decimal number
     */
    public static OptionalDouble parse(String text) {
        if (text == null) {
            return OptionalDouble.empty();
        }
        String trimmed = text.trim();
        if (!DECIMAL.matcher(trimmed).matches()) {
            return OptionalDouble.empty();
        }
        return OptionalDouble.of(Double.parseDouble(trimmed.replace(',', '.')));
    }
}
