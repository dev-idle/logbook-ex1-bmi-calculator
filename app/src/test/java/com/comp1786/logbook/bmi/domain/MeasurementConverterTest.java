package com.comp1786.logbook.bmi.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import java.util.Optional;

public class MeasurementConverterTest {

    @Test
    public void convertsKilogramsToPounds() {
        assertEquals("154.3", convert("70", "", WeightUnit.KILOGRAMS, WeightUnit.POUNDS).primary());
    }

    @Test
    public void dropsTrailingZerosAfterConversion() {
        assertEquals("70", convert("154.3", "", WeightUnit.POUNDS, WeightUnit.KILOGRAMS).primary());
    }

    @Test
    public void readsACommaDecimalSeparator() {
        assertEquals("155.4",
                convert("70,5", "", WeightUnit.KILOGRAMS, WeightUnit.POUNDS).primary());
    }

    @Test
    public void leavesTextThatIsNotANumber() {
        assertFalse(tryConvert("abc", "", WeightUnit.KILOGRAMS, WeightUnit.POUNDS).isPresent());
        assertFalse(tryConvert("", "", WeightUnit.KILOGRAMS, WeightUnit.POUNDS).isPresent());
    }

    @Test
    public void convertsASimpleUnitToACompoundOne() {
        // 175 cm = 68.898 in = 5 ft 8.9 in.
        MeasurementInput input =
                convert("175", "", HeightUnit.CENTIMETERS, HeightUnit.FEET_AND_INCHES);

        assertEquals("5", input.primary());
        assertEquals("8.9", input.part());
    }

    @Test
    public void convertsACompoundUnitToASimpleOne() {
        MeasurementInput input =
                convert("5", "8.9", HeightUnit.FEET_AND_INCHES, HeightUnit.CENTIMETERS);

        assertEquals("175", input.primary());
        assertEquals("", input.part());
    }

    @Test
    public void convertsKilogramsToStonesAndPounds() {
        // 70 kg = 154.32 lb = 11 st 0.3 lb.
        MeasurementInput input =
                convert("70", "", WeightUnit.KILOGRAMS, WeightUnit.STONES_AND_POUNDS);

        assertEquals("11", input.primary());
        assertEquals("0.3", input.part());
    }

    @Test
    public void convertsStonesAndPoundsToPoundsWithoutLoss() {
        assertEquals("154",
                convert("11", "0", WeightUnit.STONES_AND_POUNDS, WeightUnit.POUNDS).primary());
    }

    @Test
    public void keepsTwoDecimalPlacesForMeters() {
        // One decimal place would turn 175 cm into 1.8 m, losing 5 cm.
        assertEquals("1.75",
                convert("175", "", HeightUnit.CENTIMETERS, HeightUnit.METERS).primary());
        assertEquals("175",
                convert("1.75", "", HeightUnit.METERS, HeightUnit.CENTIMETERS).primary());
    }

    @Test
    public void convertsFeetAndInchesToMeters() {
        // 5 ft 9 in = 69 in = 1.7526 m.
        assertEquals("1.75",
                convert("5", "9", HeightUnit.FEET_AND_INCHES, HeightUnit.METERS).primary());
    }

    @Test
    public void treatsAnEmptyPartAsZero() {
        assertEquals("182.9",
                convert("6", "", HeightUnit.FEET_AND_INCHES, HeightUnit.CENTIMETERS).primary());
    }

    @Test
    public void leavesACompoundValueThatCannotBeRead() {
        assertFalse(tryConvert("5", "x", HeightUnit.FEET_AND_INCHES, HeightUnit.CENTIMETERS)
                .isPresent());
    }

    @Test
    public void repeatedSwitchingDoesNotDrift() {
        String pounds = convert("70", "", WeightUnit.KILOGRAMS, WeightUnit.POUNDS).primary();

        assertEquals("70", convert(pounds, "", WeightUnit.POUNDS, WeightUnit.KILOGRAMS).primary());
    }

    private static <U extends Enum<U> & MeasurementUnit> MeasurementInput convert(
            String primary, String part, U from, U to) {
        return tryConvert(primary, part, from, to).orElseThrow();
    }

    private static <U extends Enum<U> & MeasurementUnit> Optional<MeasurementInput> tryConvert(
            String primary, String part, U from, U to) {
        return MeasurementConverter.convert(new MeasurementInput(primary, part), from, to);
    }
}
