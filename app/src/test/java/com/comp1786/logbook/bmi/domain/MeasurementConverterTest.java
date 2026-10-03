package com.comp1786.logbook.bmi.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import com.comp1786.logbook.bmi.domain.MeasurementConverter.HeightText;

import org.junit.Test;

public class MeasurementConverterTest {

    @Test
    public void convertsKilogramsToPounds() {
        assertEquals("154.3", MeasurementConverter
                .convertWeight("70", WeightUnit.KILOGRAMS, WeightUnit.POUNDS).orElseThrow());
    }

    @Test
    public void dropsTrailingZerosAfterConversion() {
        assertEquals("70", MeasurementConverter
                .convertWeight("154.3", WeightUnit.POUNDS, WeightUnit.KILOGRAMS).orElseThrow());
    }

    @Test
    public void readsACommaDecimalSeparator() {
        assertEquals("155.4", MeasurementConverter
                .convertWeight("70,5", WeightUnit.KILOGRAMS, WeightUnit.POUNDS).orElseThrow());
    }

    @Test
    public void leavesTextThatIsNotANumber() {
        assertFalse(MeasurementConverter
                .convertWeight("abc", WeightUnit.KILOGRAMS, WeightUnit.POUNDS).isPresent());
        assertFalse(MeasurementConverter
                .convertWeight("", WeightUnit.KILOGRAMS, WeightUnit.POUNDS).isPresent());
    }

    @Test
    public void convertsCentimetersToFeetAndInches() {
        HeightText text = MeasurementConverter.convertHeight(
                "175", "", HeightUnit.CENTIMETERS, HeightUnit.FEET_AND_INCHES).orElseThrow();

        // 175 cm = 68.898 in = 5 ft 8.9 in.
        assertEquals("5", text.primary());
        assertEquals("8.9", text.inches());
    }

    @Test
    public void convertsFeetAndInchesToCentimeters() {
        HeightText text = MeasurementConverter.convertHeight(
                "5", "8.9", HeightUnit.FEET_AND_INCHES, HeightUnit.CENTIMETERS).orElseThrow();

        assertEquals("175", text.primary());
        assertEquals("", text.inches());
    }

    @Test
    public void treatsEmptyInchesAsZero() {
        HeightText text = MeasurementConverter.convertHeight(
                "6", "", HeightUnit.FEET_AND_INCHES, HeightUnit.CENTIMETERS).orElseThrow();

        assertEquals("182.9", text.primary());
    }

    @Test
    public void leavesAHeightThatCannotBeRead() {
        assertFalse(MeasurementConverter.convertHeight(
                "", "", HeightUnit.CENTIMETERS, HeightUnit.FEET_AND_INCHES).isPresent());
        assertFalse(MeasurementConverter.convertHeight(
                "5", "x", HeightUnit.FEET_AND_INCHES, HeightUnit.CENTIMETERS).isPresent());
    }

    @Test
    public void repeatedSwitchingDoesNotDrift() {
        String pounds = MeasurementConverter
                .convertWeight("70", WeightUnit.KILOGRAMS, WeightUnit.POUNDS).orElseThrow();
        String kilograms = MeasurementConverter
                .convertWeight(pounds, WeightUnit.POUNDS, WeightUnit.KILOGRAMS).orElseThrow();

        assertEquals("70", kilograms);
    }
}
