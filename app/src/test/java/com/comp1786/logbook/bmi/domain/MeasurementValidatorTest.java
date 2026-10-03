package com.comp1786.logbook.bmi.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MeasurementValidatorTest {

    private static final double DELTA = 1e-9;

    // --- Weight ---------------------------------------------------------------

    @Test
    public void acceptsAWeightInsideTheRange() {
        FieldResult result = MeasurementValidator.validateWeight("70.5", WeightUnit.KILOGRAMS);

        assertTrue(result.isValid());
        assertEquals(70.5, result.value(), DELTA);
    }

    @Test
    public void reportsAMissingWeight() {
        assertEquals(InputError.REQUIRED,
                MeasurementValidator.validateWeight("  ", WeightUnit.KILOGRAMS).error());
    }

    @Test
    public void reportsAWeightThatIsNotANumber() {
        assertEquals(InputError.NOT_A_NUMBER,
                MeasurementValidator.validateWeight("7O", WeightUnit.KILOGRAMS).error());
    }

    @Test
    public void checksTheRangeOfTheSelectedUnit() {
        // 500 is too heavy in kilograms but a normal weight in pounds.
        assertEquals(InputError.OUT_OF_RANGE,
                MeasurementValidator.validateWeight("500", WeightUnit.KILOGRAMS).error());
        assertTrue(MeasurementValidator.validateWeight("500", WeightUnit.POUNDS).isValid());
        assertEquals(InputError.OUT_OF_RANGE,
                MeasurementValidator.validateWeight("0", WeightUnit.POUNDS).error());
    }

    // --- Height in centimeters ------------------------------------------------

    @Test
    public void acceptsAHeightInCentimeters() {
        HeightResult result =
                MeasurementValidator.validateHeight(HeightUnit.CENTIMETERS, "175", "");

        assertTrue(result.isValid());
        assertEquals(175.0, result.value(), DELTA);
    }

    @Test
    public void reportsInvalidCentimeters() {
        assertEquals(InputError.REQUIRED, MeasurementValidator
                .validateHeight(HeightUnit.CENTIMETERS, "", "").primaryError());
        assertEquals(InputError.NOT_A_NUMBER, MeasurementValidator
                .validateHeight(HeightUnit.CENTIMETERS, "1.7.5", "").primaryError());
        assertEquals(InputError.OUT_OF_RANGE, MeasurementValidator
                .validateHeight(HeightUnit.CENTIMETERS, "1.75", "").primaryError());
    }

    // --- Height in feet and inches --------------------------------------------

    @Test
    public void combinesFeetAndInchesIntoTotalInches() {
        HeightResult result =
                MeasurementValidator.validateHeight(HeightUnit.FEET_AND_INCHES, "5", "10.5");

        assertTrue(result.isValid());
        assertEquals(70.5, result.value(), DELTA);
    }

    @Test
    public void treatsEmptyInchesAsZero() {
        HeightResult result =
                MeasurementValidator.validateHeight(HeightUnit.FEET_AND_INCHES, "6", "");

        assertTrue(result.isValid());
        assertEquals(72.0, result.value(), DELTA);
    }

    @Test
    public void requiresWholeFeet() {
        HeightResult result =
                MeasurementValidator.validateHeight(HeightUnit.FEET_AND_INCHES, "5.5", "");

        assertEquals(InputError.NOT_A_WHOLE_NUMBER, result.primaryError());
        assertNull(result.inchesError());
    }

    @Test
    public void rejectsTwelveInchesOrMore() {
        HeightResult result =
                MeasurementValidator.validateHeight(HeightUnit.FEET_AND_INCHES, "5", "12");

        assertNull(result.primaryError());
        assertEquals(InputError.INCHES_OUT_OF_RANGE, result.inchesError());
    }

    @Test
    public void reportsErrorsInBothFieldsAtOnce() {
        HeightResult result =
                MeasurementValidator.validateHeight(HeightUnit.FEET_AND_INCHES, "", "abc");

        assertEquals(InputError.REQUIRED, result.primaryError());
        assertEquals(InputError.NOT_A_NUMBER, result.inchesError());
    }

    @Test
    public void reportsACombinedHeightOutsideTheRangeOnTheFeetField() {
        HeightResult result =
                MeasurementValidator.validateHeight(HeightUnit.FEET_AND_INCHES, "9", "0");

        assertEquals(InputError.OUT_OF_RANGE, result.primaryError());
        assertNull(result.inchesError());
    }
}
