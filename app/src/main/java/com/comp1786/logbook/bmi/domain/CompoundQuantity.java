package com.comp1786.logbook.bmi.domain;

/**
 * A quantity written as a whole number of a large unit plus a part in a smaller unit, such as
 * 5 ft 8.9 in.
 *
 * @param whole         whole large units, zero or more
 * @param part          the remainder in small units, from 0 (inclusive) to
 *                      {@code partsPerWhole} (exclusive)
 * @param partsPerWhole small units per large unit, e.g. 12 inches per foot
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
     * Splits a total in small units into whole and part, with the part rounded to one decimal
     * place.
     *
     * <p>Rounding happens before the split, so a part that rounds up to a whole unit is carried:
     * 71.96 in becomes 6 ft 0 in rather than 5 ft 12 in.
     *
     * @param total         a total in small units, zero or more
     * @param partsPerWhole small units per large unit
     */
    public static CompoundQuantity fromTotal(double total, int partsPerWhole) {
        if (total < 0 || !Double.isFinite(total)) {
            throw new IllegalArgumentException("total must be a finite, non-negative value");
        }
        // Whole tenths of a small unit are exact integers, so the split has no rounding error.
        long tenths = Math.round(total * 10);
        long tenthsPerWhole = partsPerWhole * 10L;
        return new CompoundQuantity(
                (int) (tenths / tenthsPerWhole),
                (tenths % tenthsPerWhole) / 10.0,
                partsPerWhole);
    }
}
