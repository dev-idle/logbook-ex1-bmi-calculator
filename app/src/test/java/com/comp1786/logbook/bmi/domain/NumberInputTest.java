package com.comp1786.logbook.bmi.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class NumberInputTest {

    private static final double DELTA = 1e-9;

    @Test
    public void parsesWholeAndDecimalNumbers() {
        assertEquals(70.0, NumberInput.parse("70").getAsDouble(), DELTA);
        assertEquals(70.5, NumberInput.parse("70.5").getAsDouble(), DELTA);
        assertEquals(0.5, NumberInput.parse(".5").getAsDouble(), DELTA);
        assertEquals(70.0, NumberInput.parse("70.").getAsDouble(), DELTA);
    }

    @Test
    public void acceptsCommaAsDecimalSeparator() {
        assertEquals(70.5, NumberInput.parse("70,5").getAsDouble(), DELTA);
    }

    @Test
    public void ignoresSurroundingWhitespace() {
        assertEquals(70.5, NumberInput.parse("  70.5 ").getAsDouble(), DELTA);
    }

    @Test
    public void rejectsTextThatIsNotAPlainNumber() {
        assertFalse(NumberInput.parse("abc").isPresent());
        assertFalse(NumberInput.parse("70.5.1").isPresent());
        assertFalse(NumberInput.parse("-70").isPresent());
        assertFalse(NumberInput.parse("+70").isPresent());
        assertFalse(NumberInput.parse("1e3").isPresent());
        assertFalse(NumberInput.parse("NaN").isPresent());
        assertFalse(NumberInput.parse("Infinity").isPresent());
        assertFalse(NumberInput.parse(".").isPresent());
        assertFalse(NumberInput.parse("").isPresent());
        assertFalse(NumberInput.parse(null).isPresent());
    }

    @Test
    public void detectsBlankText() {
        assertTrue(NumberInput.isBlank(null));
        assertTrue(NumberInput.isBlank(""));
        assertTrue(NumberInput.isBlank("   "));
        assertFalse(NumberInput.isBlank("0"));
    }
}
