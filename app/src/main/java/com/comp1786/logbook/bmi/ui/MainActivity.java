package com.comp1786.logbook.bmi.ui;

import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.comp1786.logbook.bmi.R;
import com.comp1786.logbook.bmi.data.SharedPreferencesUnitPreferences;
import com.comp1786.logbook.bmi.data.UnitPreferences;
import com.comp1786.logbook.bmi.databinding.ActivityMainBinding;
import com.comp1786.logbook.bmi.databinding.ItemBmiCategoryBinding;
import com.comp1786.logbook.bmi.domain.BmiCategory;
import com.comp1786.logbook.bmi.domain.BmiResult;
import com.comp1786.logbook.bmi.domain.HeightUnit;
import com.comp1786.logbook.bmi.domain.MeasurementConverter;
import com.comp1786.logbook.bmi.domain.MeasurementConverter.HeightText;
import com.comp1786.logbook.bmi.domain.WeightUnit;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

/**
 * The calculator screen: weight and height inputs with unit selection, and the result.
 *
 * <p>The activity only draws {@link BmiUiState} and forwards user actions to
 * {@link BmiViewModel}. The one thing it changes itself is the text of the input fields, when a
 * unit switch converts what the user has already typed.
 */
public class MainActivity extends AppCompatActivity {

    private static final int SYSTEM_BARS_AND_CUTOUT =
            WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout();

    private final Map<BmiCategory, ItemBmiCategoryBinding> categoryRows =
            new EnumMap<>(BmiCategory.class);

    private ActivityMainBinding binding;
    private BmiViewModel viewModel;
    private MeasurementText text;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Locale locale = getResources().getConfiguration().getLocales().get(0);
        text = new MeasurementText(getResources(), locale);
        UnitPreferences unitPreferences =
                new SharedPreferencesUnitPreferences(getApplicationContext(), locale);
        viewModel = new ViewModelProvider(this, BmiViewModel.factory(unitPreferences))
                .get(BmiViewModel.class);

        buildCategoryScale();
        applyWindowInsets();
        setUpUnitToggles();
        setUpActions();
        viewModel.uiState().observe(this, this::render);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Watchers are added only after the framework has restored the fields' text, so that a
        // restore after rotation is not mistaken for an edit that clears the result.
        watchEdits(binding.weightInput, viewModel::onWeightEdited);
        watchEdits(binding.centimetersInput, viewModel::onHeightEdited);
        watchEdits(binding.feetInput, viewModel::onHeightEdited);
        watchEdits(binding.inchesInput, viewModel::onInchesEdited);
    }

    // --- Setup ---------------------------------------------------------------

    /**
     * Pads content away from the system bars, display cutouts and keyboard, since the window is
     * drawn edge to edge. The app bar handles the status bar itself (fitsSystemWindows).
     */
    private void applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar, (view, insets) -> {
            Insets bars = insets.getInsets(SYSTEM_BARS_AND_CUTOUT);
            view.setPadding(bars.left, 0, bars.right, 0);
            return insets;
        });
        ViewCompat.setOnApplyWindowInsetsListener(binding.scrollView, (view, insets) -> {
            Insets bars = insets.getInsets(SYSTEM_BARS_AND_CUTOUT);
            Insets keyboard = insets.getInsets(WindowInsetsCompat.Type.ime());
            view.setPadding(bars.left, 0, bars.right, Math.max(bars.bottom, keyboard.bottom));
            if (keyboard.bottom > 0) {
                view.post(this::keepFocusedFieldVisible);
            }
            return insets;
        });
    }

    private void setUpUnitToggles() {
        binding.weightUnitGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                onWeightUnitSelected(checkedId == R.id.button_pounds
                        ? WeightUnit.POUNDS
                        : WeightUnit.KILOGRAMS);
            }
        });
        binding.heightUnitGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                onHeightUnitSelected(checkedId == R.id.button_feet_and_inches
                        ? HeightUnit.FEET_AND_INCHES
                        : HeightUnit.CENTIMETERS);
            }
        });
    }

    private void setUpActions() {
        binding.calculateButton.setOnClickListener(view -> calculate());
        binding.clearButton.setOnClickListener(view -> clear());

        // "Done" on the last field calculates, saving a tap. A hardware keyboard's Enter key
        // arrives as IME_NULL with a key event instead, so it is handled too.
        TextView.OnEditorActionListener calculateOnDone = (view, actionId, event) -> {
            boolean doneKey = actionId == EditorInfo.IME_ACTION_DONE;
            boolean enterKey = event != null
                    && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_DOWN;
            if (doneKey || enterKey) {
                calculate();
                return true;
            }
            return false;
        };
        binding.centimetersInput.setOnEditorActionListener(calculateOnDone);
        binding.inchesInput.setOnEditorActionListener(calculateOnDone);
    }

    // --- User actions --------------------------------------------------------

    private void onWeightUnitSelected(WeightUnit unit) {
        WeightUnit previous = viewModel.currentState().weightUnit();
        if (unit == previous) {
            // The button was checked while drawing the current state, not by the user.
            return;
        }
        binding.weightInput.setText(MeasurementConverter
                .convertWeight(textOf(binding.weightInput), previous, unit)
                .orElse(""));
        viewModel.selectWeightUnit(unit);
    }

    private void onHeightUnitSelected(HeightUnit unit) {
        HeightUnit previous = viewModel.currentState().heightUnit();
        if (unit == previous) {
            return;
        }
        HeightText converted = MeasurementConverter
                .convertHeight(heightText(previous), textOf(binding.inchesInput), previous, unit)
                .orElse(new HeightText("", ""));
        if (unit == HeightUnit.CENTIMETERS) {
            binding.centimetersInput.setText(converted.primary());
        } else {
            binding.feetInput.setText(converted.primary());
            binding.inchesInput.setText(converted.inches());
        }
        viewModel.selectHeightUnit(unit);
    }

    private void calculate() {
        HeightUnit heightUnit = viewModel.currentState().heightUnit();
        viewModel.calculate(
                textOf(binding.weightInput),
                heightText(heightUnit),
                textOf(binding.inchesInput));

        BmiUiState state = viewModel.currentState();
        if (state.result() != null) {
            hideKeyboard();
            // The result is the last item, so scrolling to the end reveals all of it, including
            // the bottom padding that keeps it clear of the navigation bar.
            binding.scrollView.post(() -> binding.scrollView.smoothScrollTo(
                    0, binding.scrollView.getChildAt(0).getHeight()));
        } else {
            firstFieldWithError(state).requestFocus();
        }
    }

    private void clear() {
        binding.weightInput.setText(null);
        binding.centimetersInput.setText(null);
        binding.feetInput.setText(null);
        binding.inchesInput.setText(null);
        viewModel.clear();
        binding.weightInput.requestFocus();
    }

    // --- Rendering -----------------------------------------------------------

    private void render(BmiUiState state) {
        WeightUnit weightUnit = state.weightUnit();
        binding.weightUnitGroup.check(weightUnit == WeightUnit.POUNDS
                ? R.id.button_pounds
                : R.id.button_kilograms);
        binding.weightLayout.setSuffixText(text.unitLabel(weightUnit));
        binding.weightLayout.setHelperText(text.weightRange(weightUnit));
        binding.weightLayout.setError(text.weightError(state.weightError(), weightUnit));

        HeightUnit heightUnit = state.heightUnit();
        boolean metric = heightUnit == HeightUnit.CENTIMETERS;
        binding.heightUnitGroup.check(metric
                ? R.id.button_centimeters
                : R.id.button_feet_and_inches);
        binding.centimetersLayout.setVisibility(metric ? View.VISIBLE : View.GONE);
        binding.feetAndInchesRow.setVisibility(metric ? View.GONE : View.VISIBLE);

        String heightRange = text.heightRange(heightUnit);
        String heightError = text.heightError(state.heightError(), heightUnit);
        if (metric) {
            binding.centimetersLayout.setHelperText(heightRange);
            binding.centimetersLayout.setError(heightError);
        } else {
            binding.feetLayout.setHelperText(heightRange);
            binding.feetLayout.setError(heightError);
            binding.inchesLayout.setError(text.inchesError(state.inchesError()));
        }

        renderResult(state.result(), weightUnit);
    }

    private void renderResult(@Nullable BmiResult result, WeightUnit weightUnit) {
        binding.resultCard.setVisibility(result == null ? View.GONE : View.VISIBLE);
        if (result == null) {
            return;
        }
        BmiCategory category = result.category();
        String bmi = text.oneDecimal(result.bmi());
        String label = getString(CategoryAppearance.label(category));
        int color = ContextCompat.getColor(this, CategoryAppearance.color(category));

        binding.bmiValue.setText(bmi);
        binding.categoryLabel.setText(label);
        binding.categoryLabel.setBackgroundTintList(ColorStateList.valueOf(color));
        binding.resultCard.setStrokeColor(color);
        binding.resultSummary.setContentDescription(
                getString(R.string.result_summary_description, bmi, label));

        binding.healthyRange.setText(getString(R.string.healthy_weight_range,
                text.oneDecimal(result.healthyWeightMinimum(weightUnit)),
                text.oneDecimal(result.healthyWeightMaximum(weightUnit)),
                text.unitLabel(weightUnit)));
        highlightCategory(category);
    }

    /**
     * Adds one row per category to the scale in the result card. The rows never change, so
     * they are created once; {@link #highlightCategory} marks the user's row.
     */
    private void buildCategoryScale() {
        for (BmiCategory category : BmiCategory.values()) {
            ItemBmiCategoryBinding row = ItemBmiCategoryBinding.inflate(
                    getLayoutInflater(), binding.categoryScale, true);
            row.swatch.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(this, CategoryAppearance.color(category))));
            row.name.setText(CategoryAppearance.label(category));
            row.range.setText(text.categoryRange(category));
            categoryRows.put(category, row);
        }
    }

    private void highlightCategory(BmiCategory current) {
        categoryRows.forEach((category, row) -> {
            boolean isCurrent = category == current;
            int appearance = isCurrent
                    ? R.style.TextAppearance_BMICalculator_CategoryRow_Current
                    : R.style.TextAppearance_BMICalculator_CategoryRow;
            row.getRoot().setBackgroundResource(isCurrent ? R.drawable.bg_category_row_current : 0);
            row.name.setTextAppearance(appearance);
            row.range.setTextAppearance(appearance);
            row.getRoot().setContentDescription(getString(isCurrent
                            ? R.string.category_row_current_description
                            : R.string.category_row_description,
                    row.name.getText(), row.range.getText()));
        });
    }

    // --- Helpers -------------------------------------------------------------

    /** The centimeters field, or the feet field for feet and inches. */
    private String heightText(HeightUnit unit) {
        EditText field = unit == HeightUnit.CENTIMETERS
                ? binding.centimetersInput
                : binding.feetInput;
        return textOf(field);
    }

    private EditText firstFieldWithError(BmiUiState state) {
        if (state.weightError() != null) {
            return binding.weightInput;
        }
        if (state.heightError() != null) {
            return state.heightUnit() == HeightUnit.CENTIMETERS
                    ? binding.centimetersInput
                    : binding.feetInput;
        }
        return binding.inchesInput;
    }

    private void hideKeyboard() {
        WindowCompat.getInsetsController(getWindow(), binding.getRoot())
                .hide(WindowInsetsCompat.Type.ime());
        View focused = getCurrentFocus();
        if (focused != null) {
            focused.clearFocus();
        }
    }

    private void keepFocusedFieldVisible() {
        View focused = getCurrentFocus();
        if (focused != null) {
            scrollIntoView(focused);
        }
    }

    private static void scrollIntoView(View view) {
        view.requestRectangleOnScreen(new Rect(0, 0, view.getWidth(), view.getHeight()), false);
    }

    private static String textOf(EditText field) {
        Editable editable = field.getText();
        return editable == null ? "" : editable.toString();
    }

    /**
     * Calls {@code onEdited} whenever the field's text changes.
     *
     * <p>The keyboard can report a change without altering the text, for example when it
     * attaches to a newly focused field, so the text is compared with its previous value first.
     * Otherwise focusing a field would clear the error the user still needs to read.
     */
    private static void watchEdits(EditText field, Runnable onEdited) {
        field.addTextChangedListener(new TextWatcher() {
            private String previousText = textOf(field);

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Only the final text matters.
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Only the final text matters.
            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();
                if (!text.equals(previousText)) {
                    previousText = text;
                    onEdited.run();
                }
            }
        });
    }
}
