package com.comp1786.logbook.bmi.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

import android.content.Context;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.comp1786.logbook.bmi.R;
import com.comp1786.logbook.bmi.data.SharedPreferencesUnitPreferences;
import com.comp1786.logbook.bmi.domain.HeightUnit;
import com.comp1786.logbook.bmi.domain.WeightUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.NumberFormat;
import java.util.Locale;

/** Drives the calculator screen on a device or emulator the way a user would. */
@RunWith(AndroidJUnit4.class)
public class CalculatorScreenTest {

    private Context context;
    private ActivityScenario<MainActivity> scenario;

    @Before
    public void launchInMetricUnits() {
        // The default units follow the device locale, so each test starts from known units.
        context = ApplicationProvider.getApplicationContext();
        SharedPreferencesUnitPreferences preferences =
                new SharedPreferencesUnitPreferences(context, Locale.getDefault());
        preferences.setWeightUnit(WeightUnit.KILOGRAMS);
        preferences.setHeightUnit(HeightUnit.CENTIMETERS);
        scenario = ActivityScenario.launch(MainActivity.class);
    }

    @After
    public void close() {
        scenario.close();
    }

    @Test
    public void calculatesTheBmiAndShowsItsCategory() {
        enterMeasurements("70", "175");

        onView(withId(R.id.calculate_button)).perform(click());

        onView(withId(R.id.bmi_value)).check(matches(withText(oneDecimal(22.9))));
        onView(withId(R.id.category_label))
                .check(matches(withText(R.string.category_healthy_weight)));
    }

    @Test
    public void reportsEveryEmptyRequiredField() {
        onView(withId(R.id.calculate_button)).perform(click());

        onView(withText(R.string.error_weight_required)).check(matches(isDisplayed()));
        onView(withText(R.string.error_height_required)).check(matches(isDisplayed()));
        onView(withId(R.id.result_card)).check(matches(not(isDisplayed())));
    }

    @Test
    public void rejectsAWeightOutsideTheAcceptedRange() {
        enterMeasurements("500", "175");

        onView(withId(R.id.calculate_button)).perform(click());

        String range = context.getString(R.string.range_with_unit, "10", "400",
                context.getString(R.string.unit_kilograms));
        onView(withText(context.getString(R.string.error_weight_out_of_range, range)))
                .check(matches(isDisplayed()));
    }

    @Test
    public void convertsTheTypedWeightWhenTheUnitChanges() {
        onView(withId(R.id.weight_input)).perform(replaceText("70"), closeSoftKeyboard());

        onView(withId(R.id.button_pounds)).perform(click());

        onView(withId(R.id.weight_input)).check(matches(withText("154.3")));
    }

    @Test
    public void calculatesFromStonesAndPounds() {
        onView(withId(R.id.button_stones_and_pounds)).perform(click());
        onView(withId(R.id.stones_input)).perform(replaceText("11"));
        onView(withId(R.id.weight_pounds_input)).perform(replaceText("0"));
        onView(withId(R.id.height_input)).perform(replaceText("175"), closeSoftKeyboard());

        onView(withId(R.id.calculate_button)).perform(click());

        // 11 st 0 lb = 154 lb = 69.85 kg; 69.85 / 1.75^2 = 22.8.
        onView(withId(R.id.bmi_value)).check(matches(withText(oneDecimal(22.8))));
    }

    @Test
    public void convertsKilogramsToStonesAndPounds() {
        onView(withId(R.id.weight_input)).perform(replaceText("70"), closeSoftKeyboard());

        onView(withId(R.id.button_stones_and_pounds)).perform(click());

        // 70 kg = 154.32 lb = 11 st 0.3 lb.
        onView(withId(R.id.stones_input)).check(matches(withText("11")));
        onView(withId(R.id.weight_pounds_input)).check(matches(withText("0.3")));
    }

    @Test
    public void convertsCentimetersToMetersWithoutLosingPrecision() {
        onView(withId(R.id.height_input)).perform(replaceText("175"), closeSoftKeyboard());

        onView(withId(R.id.button_meters)).perform(click());

        onView(withId(R.id.height_input)).check(matches(withText("1.75")));
    }

    private static void enterMeasurements(String weight, String centimeters) {
        onView(withId(R.id.weight_input)).perform(replaceText(weight));
        onView(withId(R.id.height_input))
                .perform(replaceText(centimeters), closeSoftKeyboard());
    }

    /** Formats a value the way the screen does, so the test passes in any locale. */
    private static String oneDecimal(double value) {
        NumberFormat format = NumberFormat.getNumberInstance(Locale.getDefault());
        format.setMinimumFractionDigits(1);
        format.setMaximumFractionDigits(1);
        return format.format(value);
    }
}
