package com.comp1786.logbook.bmicalculator;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Guideline;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MainActivity extends AppCompatActivity {

    private static final String PREFERENCES_NAME = "settings";
    private static final String KEY_UNIT_SYSTEM = "unit_system";
    private static final String STATE_RESULT_SHOWN = "result_shown";

    private static final String DECIMAL_DIGITS = "0123456789.,";
    private static final String WHOLE_DIGITS = "0123456789";

    // BMI range of the scale bar in content_result.xml.
    private static final BigDecimal SCALE_MIN = new BigDecimal("15");
    private static final BigDecimal SCALE_SPAN = new BigDecimal("30");

    private NestedScrollView scrollView;
    private MaterialAutoCompleteTextView unitSystemInput;
    private TextInputLayout weightLayout;
    private TextInputLayout weightPartLayout;
    private TextInputLayout heightLayout;
    private TextInputLayout heightPartLayout;
    private EditText weightInput;
    private EditText weightPartInput;
    private EditText heightInput;
    private EditText heightPartInput;
    private MaterialCardView resultCard;
    private TextView bmiValueText;
    private TextView categoryLabel;
    private Guideline scaleMarkerGuide;
    private TextView normalRangeText;

    private SharedPreferences preferences;
    private UnitSystem unitSystem;
    private AlertDialog aboutDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        bindViews();
        setSupportActionBar(findViewById(R.id.toolbar));

        preferences = getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE);
        unitSystem = UnitSystem.valueOf(
                preferences.getString(KEY_UNIT_SYSTEM, UnitSystem.METRIC.name()));
        setUpUnitSystemMenu();
        applyUnitSystem();

        findViewById(R.id.calculate_button).setOnClickListener(v -> calculate());
        TextView.OnEditorActionListener calculateOnDone = (v, actionId, event) -> {
            boolean enterPressed = event != null
                    && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_DOWN;
            if (actionId == EditorInfo.IME_ACTION_DONE || enterPressed) {
                calculate();
                return true;
            }
            return false;
        };
        heightInput.setOnEditorActionListener(calculateOnDone);
        heightPartInput.setOnEditorActionListener(calculateOnDone);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Added after the views restore their text, so restoring is not treated as an edit.
        weightInput.addTextChangedListener(new InputWatcher(weightLayout));
        weightPartInput.addTextChangedListener(new InputWatcher(weightPartLayout));
        heightInput.addTextChangedListener(new InputWatcher(heightLayout));
        heightPartInput.addTextChangedListener(new InputWatcher(heightPartLayout));
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_RESULT_SHOWN, resultCard.getVisibility() == View.VISIBLE);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.getBoolean(STATE_RESULT_SHOWN)) {
            calculate();
        }
    }

    @Override
    protected void onDestroy() {
        if (aboutDialog != null) {
            aboutDialog.dismiss();
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_reset) {
            reset();
            return true;
        } else if (id == R.id.action_about) {
            showAboutDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void bindViews() {
        scrollView = findViewById(R.id.scroll_view);
        unitSystemInput = findViewById(R.id.unit_system_input);
        weightLayout = findViewById(R.id.weight_layout);
        weightPartLayout = findViewById(R.id.weight_part_layout);
        heightLayout = findViewById(R.id.height_layout);
        heightPartLayout = findViewById(R.id.height_part_layout);
        weightInput = findViewById(R.id.weight_input);
        weightPartInput = findViewById(R.id.weight_part_input);
        heightInput = findViewById(R.id.height_input);
        heightPartInput = findViewById(R.id.height_part_input);
        resultCard = findViewById(R.id.result_card);
        bmiValueText = findViewById(R.id.bmi_value);
        categoryLabel = findViewById(R.id.category_label);
        scaleMarkerGuide = findViewById(R.id.scale_marker_guide);
        normalRangeText = findViewById(R.id.normal_range_text);
    }

    private void setUpUnitSystemMenu() {
        UnitSystem[] systems = UnitSystem.values();
        String[] labels = new String[systems.length];
        for (int i = 0; i < systems.length; i++) {
            labels[i] = getString(systems[i].getLabelRes());
        }
        unitSystemInput.setSimpleItems(labels);
        unitSystemInput.setText(getString(unitSystem.getLabelRes()), false);
        unitSystemInput.setOnItemClickListener(
                (parent, view, position, id) -> changeUnitSystem(systems[position]));
    }

    private void changeUnitSystem(UnitSystem newSystem) {
        if (newSystem == unitSystem) {
            return;
        }
        BigDecimal kilograms = readBaseValue(unitSystem.getWeight(), weightInput, weightPartInput);
        BigDecimal meters = readBaseValue(unitSystem.getHeight(), heightInput, heightPartInput);

        unitSystem = newSystem;
        preferences.edit().putString(KEY_UNIT_SYSTEM, newSystem.name()).apply();
        applyUnitSystem();
        // Converted values are rounded, so the result is recalculated on the next Calculate.
        writeBaseValue(unitSystem.getWeight(), weightInput, weightPartInput, kilograms);
        writeBaseValue(unitSystem.getHeight(), heightInput, heightPartInput, meters);
        resultCard.setVisibility(View.GONE);
    }

    private void applyUnitSystem() {
        applyScale(unitSystem.getWeight(), weightLayout, weightInput, weightPartLayout);
        applyScale(unitSystem.getHeight(), heightLayout, heightInput, heightPartLayout);
        heightInput.setImeOptions(unitSystem.getHeight().isCompound()
                ? EditorInfo.IME_ACTION_NEXT : EditorInfo.IME_ACTION_DONE);
    }

    private void applyScale(UnitSystem.Scale scale, TextInputLayout wholeLayout,
                            EditText wholeInput, TextInputLayout partLayout) {
        wholeLayout.setSuffixText(getString(scale.getWholeUnitRes()));
        // Stones and feet are whole numbers.
        boolean decimal = !scale.isCompound();
        wholeInput.setKeyListener(DigitsKeyListener.getInstance(decimal ? DECIMAL_DIGITS : WHOLE_DIGITS));
        wholeInput.setRawInputType(InputType.TYPE_CLASS_NUMBER
                | (decimal ? InputType.TYPE_NUMBER_FLAG_DECIMAL : 0));

        partLayout.setVisibility(scale.isCompound() ? View.VISIBLE : View.GONE);
        if (scale.isCompound()) {
            partLayout.setSuffixText(getString(scale.getPartUnitRes()));
        }
    }

    private void calculate() {
        MeasurementValidator.Result weight = MeasurementValidator.validate(
                unitSystem.getWeight(), textOf(weightInput), textOf(weightPartInput));
        MeasurementValidator.Result height = MeasurementValidator.validate(
                unitSystem.getHeight(), textOf(heightInput), textOf(heightPartInput));
        showError(weight, unitSystem.getWeight(), weightLayout, weightPartLayout,
                R.string.error_weight_required);
        showError(height, unitSystem.getHeight(), heightLayout, heightPartLayout,
                R.string.error_height_required);

        if (!weight.isValid()) {
            resultCard.setVisibility(View.GONE);
            (weight.isErrorInPart() ? weightPartInput : weightInput).requestFocus();
        } else if (!height.isValid()) {
            resultCard.setVisibility(View.GONE);
            (height.isErrorInPart() ? heightPartInput : heightInput).requestFocus();
        } else {
            showResult(unitSystem.getWeight().toBase(weight.getAmount()),
                    unitSystem.getHeight().toBase(height.getAmount()));
        }
    }

    private void showError(MeasurementValidator.Result result, UnitSystem.Scale scale,
                           TextInputLayout wholeLayout, TextInputLayout partLayout,
                           @StringRes int requiredMessage) {
        clearError(wholeLayout);
        clearError(partLayout);
        if (!result.isValid()) {
            TextInputLayout layout = result.isErrorInPart() ? partLayout : wholeLayout;
            layout.setError(errorMessage(result.getError(), scale, requiredMessage));
        }
    }

    private String errorMessage(MeasurementValidator.Error error, UnitSystem.Scale scale,
                                @StringRes int requiredMessage) {
        switch (error) {
            case EMPTY:
                return getString(requiredMessage);
            case NOT_A_NUMBER:
                return getString(R.string.error_not_a_number);
            case NOT_A_WHOLE_NUMBER:
                return getString(R.string.error_not_a_whole_number);
            case PART_TOO_LARGE:
                return getString(R.string.error_part_too_large, getString(R.string.measurement_single,
                        String.valueOf(scale.getPartsPerWhole()), getString(scale.getPartUnitRes())));
            case OUT_OF_RANGE:
            default:
                return getString(R.string.error_out_of_range,
                        format(scale, scale.getMinimum()), format(scale, scale.getMaximum()));
        }
    }

    private void showResult(BigDecimal kilograms, BigDecimal meters) {
        BigDecimal bmi = BmiCalculator.calculate(kilograms, meters);
        BmiCategory category = BmiCategory.of(bmi);

        bmiValueText.setText(bmi.toPlainString());
        categoryLabel.setText(category.getLabelRes());
        categoryLabel.setBackgroundTintList(
                ColorStateList.valueOf(getColor(category.getContainerColorRes())));
        scaleMarkerGuide.setGuidelinePercent(scalePosition(bmi));

        UnitSystem.Scale weightScale = unitSystem.getWeight();
        BigDecimal minimum = weightScale.fromBase(
                BmiCalculator.normalRangeMinimum(meters), RoundingMode.CEILING);
        BigDecimal maximum = weightScale.fromBaseBelow(BmiCalculator.normalRangeLimit(meters));
        normalRangeText.setText(getString(R.string.result_normal_range,
                format(weightScale, minimum), format(weightScale, maximum)));

        resultCard.setVisibility(View.VISIBLE);
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                .hide(WindowInsetsCompat.Type.ime());
        scrollView.post(() -> scrollView.smoothScrollTo(0, resultCard.getBottom()));
    }

    private void reset() {
        weightInput.setText(null);
        weightPartInput.setText(null);
        heightInput.setText(null);
        heightPartInput.setText(null);
        clearError(weightLayout);
        clearError(weightPartLayout);
        clearError(heightLayout);
        clearError(heightPartLayout);
        resultCard.setVisibility(View.GONE);
        scrollView.smoothScrollTo(0, 0);
    }

    private void showAboutDialog() {
        aboutDialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.about_title)
                .setMessage(R.string.about_message)
                .setNeutralButton(R.string.about_open_source, (dialog, which) -> openSource())
                .setPositiveButton(R.string.about_close, null)
                .show();
    }

    private void openSource() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.about_source_url)));
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.error_no_browser, Toast.LENGTH_SHORT).show();
        }
    }

    private float scalePosition(BigDecimal bmi) {
        float position = bmi.subtract(SCALE_MIN)
                .divide(SCALE_SPAN, 4, RoundingMode.HALF_UP).floatValue();
        return Math.max(0f, Math.min(1f, position));
    }

    /** Returns the value in kilograms or meters, or null when the fields are not valid. */
    @Nullable
    private BigDecimal readBaseValue(UnitSystem.Scale scale, EditText wholeInput, EditText partInput) {
        MeasurementValidator.Result result =
                MeasurementValidator.validate(scale, textOf(wholeInput), textOf(partInput));
        return result.isValid() ? scale.toBase(result.getAmount()) : null;
    }

    private void writeBaseValue(UnitSystem.Scale scale, EditText wholeInput, EditText partInput,
                                @Nullable BigDecimal base) {
        if (base == null) {
            wholeInput.setText(null);
            partInput.setText(null);
            return;
        }
        BigDecimal amount = scale.fromBase(base, RoundingMode.HALF_UP);
        wholeInput.setText(plain(scale.wholeOf(amount)));
        partInput.setText(scale.isCompound() ? plain(scale.partOf(amount)) : null);
    }

    private String format(UnitSystem.Scale scale, BigDecimal amount) {
        String whole = plain(scale.wholeOf(amount));
        String wholeUnit = getString(scale.getWholeUnitRes());
        if (!scale.isCompound()) {
            return getString(R.string.measurement_single, whole, wholeUnit);
        }
        return getString(R.string.measurement_compound, whole, wholeUnit,
                plain(scale.partOf(amount)), getString(scale.getPartUnitRes()));
    }

    private static String plain(BigDecimal value) {
        return value.stripTrailingZeros().toPlainString();
    }

    private static String textOf(EditText input) {
        return input.getText() == null ? "" : input.getText().toString();
    }

    private static void clearError(TextInputLayout layout) {
        layout.setError(null);
        layout.setErrorEnabled(false);
    }

    /** Clears a field's error and hides the result once the user edits the field. */
    private class InputWatcher implements TextWatcher {

        private final TextInputLayout layout;
        private String textBefore = "";

        InputWatcher(TextInputLayout layout) {
            this.layout = layout;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            textBefore = s.toString();
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            // Some keyboards report changes that leave the text as it was.
            if (s.toString().equals(textBefore)) {
                return;
            }
            clearError(layout);
            resultCard.setVisibility(View.GONE);
        }
    }
}
