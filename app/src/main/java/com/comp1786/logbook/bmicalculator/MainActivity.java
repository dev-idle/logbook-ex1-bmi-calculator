package com.comp1786.logbook.bmicalculator;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MainActivity extends AppCompatActivity {

    private static final String PREFERENCES_NAME = "settings";
    private static final String KEY_WEIGHT_UNIT = "weight_unit";
    private static final String KEY_HEIGHT_UNIT = "height_unit";
    private static final String STATE_RESULT_SHOWN = "result_shown";

    private static final String DECIMAL_DIGITS = "0123456789.,";
    private static final String WHOLE_DIGITS = "0123456789";

    // BMI range of the scale bar in content_result.xml.
    private static final BigDecimal SCALE_MIN = new BigDecimal("15");
    private static final BigDecimal SCALE_SPAN = new BigDecimal("30");
    // Smallest BMI step at two decimal places.
    private static final BigDecimal BMI_STEP = new BigDecimal("0.01");

    private NestedScrollView scrollView;
    private MaterialCardView resultCard;
    private TextView bmiValueText;
    private TextView categoryLabel;
    private TextView summaryText;
    private Guideline scaleMarkerGuide;
    private LinearLayout categoryTable;
    private TextView normalRangeText;
    private View cautionBox;
    private View[] categoryRows;

    private SharedPreferences preferences;
    private MeasurementField weight;
    private MeasurementField height;
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
        buildCategoryTable();

        preferences = getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE);
        weight = new MeasurementField(findViewById(R.id.weight_unit_group),
                findViewById(R.id.weight_layout), findViewById(R.id.weight_part_layout),
                KEY_WEIGHT_UNIT, MeasurementUnit.KILOGRAMS, R.string.error_weight_required);
        height = new MeasurementField(findViewById(R.id.height_unit_group),
                findViewById(R.id.height_layout), findViewById(R.id.height_part_layout),
                KEY_HEIGHT_UNIT, MeasurementUnit.CENTIMETERS, R.string.error_height_required);

        findViewById(R.id.calculate_button).setOnClickListener(v -> calculate());
        TextView.OnEditorActionListener editorAction = (v, actionId, event) -> {
            boolean enterKey = event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER;
            if (actionId != EditorInfo.IME_ACTION_DONE && !enterKey) {
                return false;
            }
            // A hardware Enter key acts once, like the field's keyboard action.
            if (event == null || event.getAction() == KeyEvent.ACTION_DOWN) {
                if ((v.getImeOptions() & EditorInfo.IME_MASK_ACTION) == EditorInfo.IME_ACTION_DONE) {
                    calculate();
                } else {
                    focusNextInput(v);
                }
            }
            return true;
        };
        weight.setOnEditorActionListener(editorAction);
        height.setOnEditorActionListener(editorAction);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Added after the views restore their text, so restoring is not treated as an edit.
        weight.watchEdits();
        height.watchEdits();
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
        resultCard = findViewById(R.id.result_card);
        bmiValueText = findViewById(R.id.bmi_value);
        categoryLabel = findViewById(R.id.category_label);
        summaryText = findViewById(R.id.summary_text);
        scaleMarkerGuide = findViewById(R.id.scale_marker_guide);
        categoryTable = findViewById(R.id.category_table);
        normalRangeText = findViewById(R.id.normal_range_text);
        cautionBox = findViewById(R.id.caution_box);
    }

    private void buildCategoryTable() {
        LayoutInflater inflater = getLayoutInflater();
        BmiCategory[] categories = BmiCategory.values();
        categoryRows = new View[categories.length];
        for (int i = 0; i < categories.length; i++) {
            View row = inflater.inflate(R.layout.item_bmi_category, categoryTable, false);
            row.findViewById(R.id.category_dot).setBackgroundTintList(
                    ColorStateList.valueOf(getColor(categories[i].getColorRes())));
            ((TextView) row.findViewById(R.id.category_name)).setText(categories[i].getLabelRes());
            ((TextView) row.findViewById(R.id.category_range)).setText(rangeText(categories, i));
            categoryTable.addView(row);
            categoryRows[i] = row;
        }
    }

    private String rangeText(BmiCategory[] categories, int index) {
        if (index == 0) {
            return getString(R.string.category_range_below,
                    categories[1].getLowerBound().toPlainString());
        }
        String lower = categories[index].getLowerBound().toPlainString();
        if (index == categories.length - 1) {
            return getString(R.string.category_range_or_more, lower);
        }
        String upper = categories[index + 1].getLowerBound().subtract(BMI_STEP).toPlainString();
        return getString(R.string.category_range_between, lower, upper);
    }

    private void calculate() {
        MeasurementValidator.Result weightResult = weight.validate();
        MeasurementValidator.Result heightResult = height.validate();
        weight.showError(weightResult);
        height.showError(heightResult);

        if (!weightResult.isValid()) {
            resultCard.setVisibility(View.GONE);
            weight.focusError(weightResult);
        } else if (!heightResult.isValid()) {
            resultCard.setVisibility(View.GONE);
            height.focusError(heightResult);
        } else {
            showResult(weightResult.getAmount(), heightResult.getAmount());
        }
    }

    private void showResult(BigDecimal weightAmount, BigDecimal heightAmount) {
        MeasurementUnit weightUnit = weight.unit;
        BigDecimal meters = height.unit.toBase(heightAmount);
        BigDecimal bmi = BmiCalculator.calculate(weightUnit.toBase(weightAmount), meters);
        BmiCategory category = BmiCategory.of(bmi);
        BigDecimal minimum = weightUnit.fromBase(
                BmiCalculator.normalRangeMinimum(meters), RoundingMode.CEILING);
        BigDecimal maximum = weightUnit.fromBaseBelow(BmiCalculator.normalRangeLimit(meters));

        bmiValueText.setText(bmi.toPlainString());
        categoryLabel.setText(category.getLabelRes());
        categoryLabel.setBackgroundTintList(
                ColorStateList.valueOf(getColor(category.getContainerColorRes())));
        summaryText.setText(summary(category, weightAmount, minimum, maximum));
        boolean withinNhsRange = weightUnit.isWithinNhsRange(weightAmount)
                && height.unit.isWithinNhsRange(heightAmount);
        cautionBox.setVisibility(withinNhsRange ? View.GONE : View.VISIBLE);
        moveScaleMarker(bmi);
        highlightCategory(category);
        normalRangeText.setText(getString(R.string.result_normal_range,
                formatValue(weightUnit, minimum), formatValue(weightUnit, maximum)));

        resultCard.setVisibility(View.VISIBLE);
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                .hide(WindowInsetsCompat.Type.ime());
        // Scrolls only as far as needed to show the card, if at all.
        resultCard.post(() -> resultCard.requestRectangleOnScreen(
                new Rect(0, 0, resultCard.getWidth(), resultCard.getHeight()), false));
    }

    private String summary(BmiCategory category, BigDecimal weightAmount,
                           BigDecimal minimum, BigDecimal maximum) {
        if (category == BmiCategory.NORMAL_RANGE) {
            return getString(R.string.result_summary_normal);
        } else if (category == BmiCategory.UNDERWEIGHT) {
            return getString(R.string.result_summary_below,
                    formatValue(weight.unit, roundUp(minimum.subtract(weightAmount))));
        }
        return getString(R.string.result_summary_above,
                formatValue(weight.unit, roundUp(weightAmount.subtract(maximum))));
    }

    /** Rounds a gap up to the weight unit's precision so it never shows as zero. */
    private BigDecimal roundUp(BigDecimal difference) {
        return difference.setScale(weight.unit.getDecimals(), RoundingMode.UP);
    }

    private void moveScaleMarker(BigDecimal bmi) {
        float position = bmi.subtract(SCALE_MIN)
                .divide(SCALE_SPAN, 4, RoundingMode.HALF_UP).floatValue();
        position = Math.max(0f, Math.min(1f, position));
        scaleMarkerGuide.setGuidelinePercent(position);
    }

    private void highlightCategory(BmiCategory current) {
        for (BmiCategory category : BmiCategory.values()) {
            boolean selected = category == current;
            View row = categoryRows[category.ordinal()];
            row.setBackgroundTintList(ColorStateList.valueOf(
                    selected ? getColor(category.getContainerColorRes()) : Color.TRANSPARENT));
            row.findViewById(R.id.category_you).setVisibility(selected ? View.VISIBLE : View.GONE);
        }
    }

    private void reset() {
        weight.clear();
        height.clear();
        resultCard.setVisibility(View.GONE);
        scrollView.smoothScrollTo(0, 0);
    }

    /** Moves to the next visible field, like the keyboard's Next, without stopping at the chips. */
    private void focusNextInput(View current) {
        EditText[] inputs = {weight.wholeInput, weight.partInput, height.wholeInput, height.partInput};
        boolean passedCurrent = false;
        for (EditText input : inputs) {
            if (passedCurrent && input.isShown()) {
                input.requestFocus();
                return;
            }
            if (input == current) {
                passedCurrent = true;
            }
        }
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

    /** Formats a limit without trailing zeros, such as "5 kg" or "4 ft 7 in". */
    private String formatLimit(MeasurementUnit unit, BigDecimal amount) {
        BigDecimal whole = unit.wholeOf(amount);
        BigDecimal part = unit.partOf(amount);
        String wholeUnit = getString(unit.getWholeUnitRes());
        if (!unit.isCompound() || part.signum() == 0) {
            return getString(R.string.measurement_single, plain(whole), wholeUnit);
        }
        String partUnit = getString(unit.getPartUnitRes());
        if (whole.signum() == 0) {
            return getString(R.string.measurement_single, plain(part), partUnit);
        }
        return getString(R.string.measurement_compound, plain(whole), wholeUnit, plain(part), partUnit);
    }

    /** Formats a weight at the unit's precision so values line up, such as "73.0 kg". */
    private String formatValue(MeasurementUnit unit, BigDecimal amount) {
        BigDecimal value = amount.setScale(unit.getDecimals(), RoundingMode.HALF_UP);
        String wholeUnit = getString(unit.getWholeUnitRes());
        if (!unit.isCompound()) {
            return getString(R.string.measurement_single, value.toPlainString(), wholeUnit);
        }
        BigDecimal whole = unit.wholeOf(value);
        String part = unit.partOf(value).setScale(unit.getDecimals(), RoundingMode.HALF_UP).toPlainString();
        String partUnit = getString(unit.getPartUnitRes());
        if (whole.signum() == 0) {
            return getString(R.string.measurement_single, part, partUnit);
        }
        return getString(R.string.measurement_compound, plain(whole), wholeUnit, part, partUnit);
    }

    private static String plain(BigDecimal value) {
        return value.stripTrailingZeros().toPlainString();
    }

    private static String textOf(EditText input) {
        return input.getText() == null ? "" : input.getText().toString();
    }

    private static void setTextAtEnd(EditText input, CharSequence text) {
        input.setText(text);
        input.setSelection(input.length());
    }

    private static void clearError(TextInputLayout layout) {
        layout.setError(null);
        layout.setErrorEnabled(false);
    }

    /** The unit chips and input fields of one measurement, weight or height. */
    private final class MeasurementField {

        private final ChipGroup unitGroup;
        private final TextInputLayout wholeLayout;
        private final TextInputLayout partLayout;
        private final EditText wholeInput;
        private final EditText partInput;
        private final String preferenceKey;
        @StringRes
        private final int requiredMessage;
        private final int lastFieldAction;
        private MeasurementUnit unit;

        MeasurementField(ChipGroup unitGroup, TextInputLayout wholeLayout,
                         TextInputLayout partLayout, String preferenceKey,
                         MeasurementUnit defaultUnit, @StringRes int requiredMessage) {
            this.unitGroup = unitGroup;
            this.wholeLayout = wholeLayout;
            this.partLayout = partLayout;
            this.wholeInput = wholeLayout.getEditText();
            this.partInput = partLayout.getEditText();
            this.preferenceKey = preferenceKey;
            this.requiredMessage = requiredMessage;
            // Weight moves on to height; height is the last field and calculates.
            this.lastFieldAction = wholeInput.getImeOptions() & EditorInfo.IME_MASK_ACTION;

            unit = MeasurementUnit.valueOf(preferences.getString(preferenceKey, defaultUnit.name()));
            unitGroup.check(unitGroup.findViewWithTag(unit.name()).getId());
            applyUnit();
            unitGroup.setOnCheckedStateChangeListener((group, checkedIds) -> changeUnit(
                    MeasurementUnit.valueOf((String) group.findViewById(group.getCheckedChipId()).getTag())));
        }

        MeasurementValidator.Result validate() {
            return MeasurementValidator.validate(unit, textOf(wholeInput), textOf(partInput));
        }

        void showError(MeasurementValidator.Result result) {
            clearError(wholeLayout);
            clearError(partLayout);
            if (!result.isValid()) {
                TextInputLayout layout = result.isErrorInPart() ? partLayout : wholeLayout;
                layout.setError(errorMessage(result.getError()));
            }
        }

        void focusError(MeasurementValidator.Result result) {
            (result.isErrorInPart() ? partInput : wholeInput).requestFocus();
        }

        void clear() {
            wholeInput.setText(null);
            partInput.setText(null);
            clearError(wholeLayout);
            clearError(partLayout);
        }

        void watchEdits() {
            wholeInput.addTextChangedListener(new InputWatcher(wholeLayout));
            partInput.addTextChangedListener(new InputWatcher(partLayout));
        }

        void setOnEditorActionListener(TextView.OnEditorActionListener listener) {
            wholeInput.setOnEditorActionListener(listener);
            partInput.setOnEditorActionListener(listener);
        }

        private void changeUnit(MeasurementUnit newUnit) {
            if (newUnit == unit) {
                return;
            }
            MeasurementValidator.Result current = validate();
            BigDecimal base = current.isValid() ? unit.toBase(current.getAmount()) : null;
            boolean sameFields = unit.isCompound() == newUnit.isCompound();

            unit = newUnit;
            preferences.edit().putString(preferenceKey, newUnit.name()).apply();
            applyUnit();
            // Converted values are rounded, so the result is recalculated on the next Calculate.
            if (base != null) {
                BigDecimal amount = unit.fromBase(base, RoundingMode.HALF_UP);
                setTextAtEnd(wholeInput, plain(unit.wholeOf(amount)));
                setTextAtEnd(partInput, unit.isCompound() ? plain(unit.partOf(amount)) : "");
            } else if (!sameFields) {
                clear();
            }
            // Otherwise the text stays as typed, in case it was entered in the wrong unit.
            clearError(wholeLayout);
            clearError(partLayout);
            resultCard.setVisibility(View.GONE);
        }

        private void applyUnit() {
            wholeLayout.setSuffixText(getString(unit.getWholeUnitRes()));
            // Stones and feet are whole numbers.
            boolean decimal = !unit.isCompound();
            wholeInput.setKeyListener(DigitsKeyListener.getInstance(decimal ? DECIMAL_DIGITS : WHOLE_DIGITS));
            wholeInput.setRawInputType(InputType.TYPE_CLASS_NUMBER
                    | (decimal ? InputType.TYPE_NUMBER_FLAG_DECIMAL : 0));
            int action = unit.isCompound() ? EditorInfo.IME_ACTION_NEXT : lastFieldAction;
            wholeInput.setImeOptions(action | EditorInfo.IME_FLAG_NO_EXTRACT_UI);

            partLayout.setVisibility(unit.isCompound() ? View.VISIBLE : View.GONE);
            if (unit.isCompound()) {
                partLayout.setSuffixText(getString(unit.getPartUnitRes()));
            }
        }

        private String errorMessage(MeasurementValidator.Error error) {
            switch (error) {
                case EMPTY:
                    return getString(requiredMessage);
                case NOT_A_NUMBER:
                    return getString(R.string.error_not_a_number);
                case NOT_A_WHOLE_NUMBER:
                    return getString(R.string.error_not_a_whole_number);
                case PART_TOO_LARGE:
                    return getString(R.string.error_part_too_large, getString(R.string.measurement_single,
                            String.valueOf(unit.getPartsPerWhole()), getString(unit.getPartUnitRes())));
                case OUT_OF_RANGE:
                default:
                    return getString(R.string.error_out_of_range,
                            formatLimit(unit, unit.getMinimum()), formatLimit(unit, unit.getMaximum()));
            }
        }
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
