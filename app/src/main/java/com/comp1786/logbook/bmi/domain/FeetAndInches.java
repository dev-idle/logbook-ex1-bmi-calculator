package com.comp1786.logbook.bmi.domain;

/**
 * A height expressed as whole feet plus inches, the way it is usually stated in the US and UK.
 *
 * @param feet   whole feet, zero or more
 * @param inches inches, from 0 (inclusive) to 12 (exclusive)
 */
public record FeetAndInches(int feet, double inches) {

    public FeetAndInches {
        if (feet < 0) {
            throw new IllegalArgumentException("feet must not be negative: " + feet);
        }
        if (inches < 0 || inches >= UnitConversions.INCHES_PER_FOOT) {
            throw new IllegalArgumentException("inches must be in [0, 12): " + inches);
        }
    }

    /**
     * Splits a total number of inches into feet and inches, with the inches rounded to one
     * decimal place.
     *
     * <p>Rounding happens before the split, so a remainder that rounds up to 12 is carried into
     * the feet: 71.96 in becomes 6 ft 0 in rather than 5 ft 12 in.
     *
     * @param totalInches a height in inches, zero or more
     */
    public static FeetAndInches fromTotalInches(double totalInches) {
        if (totalInches < 0 || !Double.isFinite(totalInches)) {
            throw new IllegalArgumentException("totalInches must be a finite, non-negative value");
        }
        // Whole tenths of an inch are exact integers, so the split has no rounding error.
        long tenths = Math.round(totalInches * 10);
        long tenthsPerFoot = UnitConversions.INCHES_PER_FOOT * 10L;
        return new FeetAndInches((int) (tenths / tenthsPerFoot), (tenths % tenthsPerFoot) / 10.0);
    }

    /** The height as a total number of inches. */
    public double totalInches() {
        return feet * UnitConversions.INCHES_PER_FOOT + inches;
    }
}
