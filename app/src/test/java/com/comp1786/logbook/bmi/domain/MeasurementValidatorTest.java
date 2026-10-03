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
        MeasurementResult result = validate(WeightUnit.KILOGRAMS, "70.5", "");

        assertTrue(result.isValid());
        assertEquals(70.5, result.value(), DELTA);
    }

    @Test
    public void reportsAMissingValue() {
        assertEquals(InputError.REQUIRED,
                validate(WeightUnit.KILOGRAMS, "  ", "").primaryError());
    }

    @Test
    public void reportsAValueThatIsNotANumber() {
        assertEquals(InputError.NOT_A_NUMBER,
                validate(WeightUnit.KILOGRAMS, "7O", "").primaryError());
        assertEquals(InputError.NOT_A_NUMBER,
                validate(HeightUnit.CENTIMETERS, "1.7.5", "").primaryError());
    }

    @Test
    public void checksTheRangeOfTheSelectedUnit() {
        // 500 is too heavy in kilograms but a normal weight in pounds.
        assertEquals(InputError.OUT_OF_RANGE,
                validate(WeightUnit.KILOGRAMS, "500", "").primaryError());
        assertTrue(validate(WeightUnit.POUNDS, "500", "").isValid());
        assertEquals(InputError.OUT_OF_RANGE,
                validate(HeightUnit.CENTIMETERS, "1.75", "").primaryError());
    }

    @Test
    public void catchesCentimetersTypedAsMeters() {
        assertTrue(validate(HeightUnit.METERS, "1.75", "").isValid());
        assertEquals(InputError.OUT_OF_RANGE,
                validate(HeightUnit.METERS, "175", "").primaryError());
    }

    @Test
    public void ignoresThePartFieldForSimpleUnits() {
        assertTrue(validate(HeightUnit.CENTIMETERS, "175", "abc").isValid());
    }

    // --- Compound units -------------------------------------------------------

    @Test
    public void combinesWholeAndPartIntoATotal() {
        MeasurementResult result = validate(HeightUnit.FEET_AND_INCHES, "5", "10.5");

        assertTrue(result.isValid());
        assertEquals(70.5, result.value(), DELTA);
    }

    @Test
    public void treatsAnEmptyPartAsZero() {
        MeasurementResult result = validate(HeightUnit.FEET_AND_INCHES, "6", "");

        assertTrue(result.isValid());
        assertEquals(72.0, result.value(), DELTA);
    }

    @Test
    public void requiresAWholeNumberInTheWholeField() {
        MeasurementResult result = validate(HeightUnit.FEET_AND_INCHES, "5.5", "");

        assertEquals(InputError.NOT_A_WHOLE_NUMBER, result.primaryError());
        assertNull(result.partError());
    }

    @Test
    public void rejectsAPartOfAWholeUnitOrMore() {
        MeasurementResult result = validate(HeightUnit.FEET_AND_INCHES, "5", "12");

        assertNull(result.primaryError());
        assertEquals(InputError.PART_OUT_OF_RANGE, result.partError());
    }

    @Test
    public void reportsErrorsInBothFieldsAtOnce() {
        MeasurementResult result = validate(HeightUnit.FEET_AND_INCHES, "", "abc");

        assertEquals(InputError.REQUIRED, result.primaryError());
        assertEquals(InputError.NOT_A_NUMBER, result.partError());
    }

    @Test
    public void acceptsStonesAndPounds() {
        MeasurementResult result = validate(WeightUnit.STONES_AND_POUNDS, "11", "3.5");

        assertTrue(result.isValid());
        // 11 st 3.5 lb = 11 x 14 + 3.5 = 157.5 lb.
        assertEquals(157.5, result.value(), DELTA);
    }

    @Test
    public void rejectsFourteenPoundsOrMore() {
        assertEquals(InputError.PART_OUT_OF_RANGE,
                validate(WeightUnit.STONES_AND_POUNDS, "11", "14").partError());
        assertTrue(validate(WeightUnit.STONES_AND_POUNDS, "11", "13.9").isValid());
    }

    @Test
    public void reportsACombinedValueOutsideTheRangeOnTheWholeField() {
        MeasurementResult result = validate(HeightUnit.FEET_AND_INCHES, "9", "3");

        assertEquals(InputError.OUT_OF_RANGE, result.primaryError());
        assertNull(result.partError());
    }

    private static MeasurementResult validate(MeasurementUnit unit, String primary, String part) {
        return MeasurementValidator.validate(unit, new MeasurementInput(primary, part));
    }
}
