package com.comp1786.logbook.bmi.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MeasurementValidatorTest {

    private static final double DELTA = 1e-9;

    // --- Simple units ---------------------------------------------------------

    @Test
    public void acceptsAValueInsideTheRange() {
        MeasurementResult result =
                MeasurementValidator.validate(WeightUnit.KILOGRAMS, "70.5", "");

        assertTrue(result.isValid());
        assertEquals(70.5, result.value(), DELTA);
    }

    @Test
    public void reportsAMissingValue() {
        assertEquals(InputError.REQUIRED, MeasurementValidator
                .validate(WeightUnit.KILOGRAMS, "  ", "").primaryError());
    }

    @Test
    public void reportsAValueThatIsNotANumber() {
        assertEquals(InputError.NOT_A_NUMBER, MeasurementValidator
                .validate(WeightUnit.KILOGRAMS, "7O", "").primaryError());
        assertEquals(InputError.NOT_A_NUMBER, MeasurementValidator
                .validate(HeightUnit.CENTIMETERS, "1.7.5", "").primaryError());
    }

    @Test
    public void checksTheRangeOfTheSelectedUnit() {
        // 500 is too heavy in kilograms but a normal weight in pounds.
        assertEquals(InputError.OUT_OF_RANGE, MeasurementValidator
                .validate(WeightUnit.KILOGRAMS, "500", "").primaryError());
        assertTrue(MeasurementValidator.validate(WeightUnit.POUNDS, "500", "").isValid());
        assertEquals(InputError.OUT_OF_RANGE, MeasurementValidator
                .validate(HeightUnit.CENTIMETERS, "1.75", "").primaryError());
    }

    @Test
    public void ignoresThePartFieldForSimpleUnits() {
        assertTrue(MeasurementValidator.validate(HeightUnit.CENTIMETERS, "175", "abc").isValid());
    }

    // --- Compound units -------------------------------------------------------

    @Test
    public void combinesWholeAndPartIntoATotal() {
        MeasurementResult result =
                MeasurementValidator.validate(HeightUnit.FEET_AND_INCHES, "5", "10.5");

        assertTrue(result.isValid());
        assertEquals(70.5, result.value(), DELTA);
    }

    @Test
    public void treatsAnEmptyPartAsZero() {
        MeasurementResult result =
                MeasurementValidator.validate(HeightUnit.FEET_AND_INCHES, "6", "");

        assertTrue(result.isValid());
        assertEquals(72.0, result.value(), DELTA);
    }

    @Test
    public void requiresAWholeNumberInTheWholeField() {
        MeasurementResult result =
                MeasurementValidator.validate(HeightUnit.FEET_AND_INCHES, "5.5", "");

        assertEquals(InputError.NOT_A_WHOLE_NUMBER, result.primaryError());
        assertNull(result.partError());
    }

    @Test
    public void rejectsAPartOfAWholeUnitOrMore() {
        MeasurementResult result =
                MeasurementValidator.validate(HeightUnit.FEET_AND_INCHES, "5", "12");

        assertNull(result.primaryError());
        assertEquals(InputError.PART_OUT_OF_RANGE, result.partError());
    }

    @Test
    public void reportsErrorsInBothFieldsAtOnce() {
        MeasurementResult result =
                MeasurementValidator.validate(HeightUnit.FEET_AND_INCHES, "", "abc");

        assertEquals(InputError.REQUIRED, result.primaryError());
        assertEquals(InputError.NOT_A_NUMBER, result.partError());
    }

    @Test
    public void reportsACombinedValueOutsideTheRangeOnTheWholeField() {
        MeasurementResult result =
                MeasurementValidator.validate(HeightUnit.FEET_AND_INCHES, "9", "0");

        assertEquals(InputError.OUT_OF_RANGE, result.primaryError());
        assertNull(result.partError());
    }
}
