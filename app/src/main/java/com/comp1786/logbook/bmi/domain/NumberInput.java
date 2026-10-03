package com.comp1786.logbook.bmi.domain;

import java.util.OptionalDouble;
import java.util.regex.Pattern;

/**
 * Parses numbers typed by the user. Both "." and "," are decimal separators, since the keyboard
 * offers the one of the device language; signs, exponents, NaN and Infinity are rejected.
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

    /** Returns the non-negative decimal in {@code text}, or empty if it is not one. */
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
