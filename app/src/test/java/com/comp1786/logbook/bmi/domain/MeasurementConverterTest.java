package com.comp1786.logbook.bmi.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import com.comp1786.logbook.bmi.domain.MeasurementConverter.FieldText;

import org.junit.Test;

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
        assertFalse(MeasurementConverter
                .convert("abc", "", WeightUnit.KILOGRAMS, WeightUnit.POUNDS).isPresent());
        assertFalse(MeasurementConverter
                .convert("", "", WeightUnit.KILOGRAMS, WeightUnit.POUNDS).isPresent());
    }

    @Test
    public void convertsASimpleUnitToACompoundOne() {
        // 175 cm = 68.898 in = 5 ft 8.9 in.
        FieldText text = convert("175", "", HeightUnit.CENTIMETERS, HeightUnit.FEET_AND_INCHES);

        assertEquals("5", text.primary());
        assertEquals("8.9", text.part());
    }

    @Test
    public void convertsACompoundUnitToASimpleOne() {
        FieldText text = convert("5", "8.9", HeightUnit.FEET_AND_INCHES, HeightUnit.CENTIMETERS);

        assertEquals("175", text.primary());
        assertEquals("", text.part());
    }

    @Test
    public void treatsAnEmptyPartAsZero() {
        assertEquals("182.9",
                convert("6", "", HeightUnit.FEET_AND_INCHES, HeightUnit.CENTIMETERS).primary());
    }

    @Test
    public void leavesACompoundValueThatCannotBeRead() {
        assertFalse(MeasurementConverter.convert(
                "5", "x", HeightUnit.FEET_AND_INCHES, HeightUnit.CENTIMETERS).isPresent());
    }

    @Test
    public void repeatedSwitchingDoesNotDrift() {
        String pounds = convert("70", "", WeightUnit.KILOGRAMS, WeightUnit.POUNDS).primary();

        assertEquals("70", convert(pounds, "", WeightUnit.POUNDS, WeightUnit.KILOGRAMS).primary());
    }

    private static <U extends Enum<U> & MeasurementUnit> FieldText convert(
            String primary, String part, U from, U to) {
        return MeasurementConverter.convert(primary, part, from, to).orElseThrow();
    }
}
