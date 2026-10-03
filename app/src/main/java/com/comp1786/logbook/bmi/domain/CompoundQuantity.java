package com.comp1786.logbook.bmi.domain;

/**
 * A whole number of a large unit plus a part in a small unit, such as 5 ft 8.9 in.
 *
 * @param part          the remainder, from 0 inclusive to {@code partsPerWhole} exclusive
 * @param partsPerWhole small units per large unit, such as 12 inches per foot
 */
public record CompoundQuantity(int whole, double part, int partsPerWhole) {

    public CompoundQuantity {
        if (partsPerWhole <= 0) {
            throw new IllegalArgumentException("partsPerWhole must be positive: " + partsPerWhole);
        }
        if (whole < 0) {
            throw new IllegalArgumentException("whole must not be negative: " + whole);
        }
        if (part < 0 || part >= partsPerWhole) {
            throw new IllegalArgumentException(
                    "part must be in [0, " + partsPerWhole + "): " + part);
        }
    }

    /**
     * Splits a total in small units, with the part rounded to one decimal place. Rounding comes
     * first, so 71.96 in becomes 6 ft 0 in rather than 5 ft 12 in.
     */
    public static CompoundQuantity fromTotal(double total, int partsPerWhole) {
        if (total < 0 || !Double.isFinite(total)) {
            throw new IllegalArgumentException("total must be a finite, non-negative value");
        }
        // Splitting whole tenths as integers avoids floating-point error.
        long tenths = Math.round(total * 10);
        long tenthsPerWhole = partsPerWhole * 10L;
        return new CompoundQuantity(
                (int) (tenths / tenthsPerWhole),
                (tenths % tenthsPerWhole) / 10.0,
                partsPerWhole);
    }
}
