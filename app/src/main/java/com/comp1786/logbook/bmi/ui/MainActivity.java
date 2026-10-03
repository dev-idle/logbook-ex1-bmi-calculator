package com.comp1786.logbook.bmi.ui;

import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;

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
import com.comp1786.logbook.bmi.domain.MeasurementInput;
import com.comp1786.logbook.bmi.domain.MeasurementUnit;
import com.comp1786.logbook.bmi.domain.WeightUnit;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

/**
 * The calculator screen: weight and height inputs with unit selection, and the result.
 *
 * <p>The activity only draws {@link BmiUiState} and forwards user actions to
 * {@link BmiViewModel}. The one thing it changes itself is the text of the input fields: a unit
 * switch converts what the user has already typed, and Clear empties them.
 */
public class MainActivity extends AppCompatActivity {

    private static final int SYSTEM_BARS_AND_CUTOUT =
            WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout();

    private final Map<BmiCategory, ItemBmiCategoryBinding> categoryRows =
            new EnumMap<>(BmiCategory.class);

    private ActivityMainBinding binding;
    private BmiViewModel viewModel;
    private MeasurementText text;
    private MeasurementFields weightFields;
    private MeasurementFields heightFields;
    private UnitToggle<WeightUnit> weightToggle;
    private UnitToggle<HeightUnit> heightToggle;

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

        weightFields = new MeasurementFields(binding.weightLayout, binding.weightCompoundRow,
                binding.stonesLayout, binding.weightPoundsLayout);
        heightFields = new MeasurementFields(binding.heightLayout, binding.heightCompoundRow,
                binding.feetLayout, binding.inchesLayout);
        weightToggle = new UnitToggle<>(binding.weightUnitGroup, Map.of(
                WeightUnit.KILOGRAMS, R.id.button_kilograms,
                WeightUnit.POUNDS, R.id.button_pounds,
                WeightUnit.STONES_AND_POUNDS, R.id.button_stones_and_pounds),
                this::onWeightUnitSelected);
        heightToggle = new UnitToggle<>(binding.heightUnitGroup, Map.of(
                HeightUnit.CENTIMETERS, R.id.button_centimeters,
                HeightUnit.METERS, R.id.button_meters,
                HeightUnit.FEET_AND_INCHES, R.id.button_feet_and_inches),
                this::onHeightUnitSelected);

        buildCategoryScale();
        applyWindowInsets();
        setUpMenu();
        binding.calculateButton.setOnClickListener(view -> calculate());
        binding.clearButton.setOnClickListener(view -> clear());
        // Height is the last measurement, so Done on its last field calculates.
        heightFields.setOnDone(this::calculate);
        viewModel.uiState().observe(this, this::render);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Watchers are added only after the framework has restored the fields' text, so that a
        // restore after rotation is not mistaken for an edit that clears the result.
        weightFields.watchEdits(viewModel::onWeightEdited, viewModel::onWeightPartEdited);
        heightFields.watchEdits(viewModel::onHeightEdited, viewModel::onHeightPartEdited);
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

    private void setUpMenu() {
        binding.toolbar.setOnMenuItemClickListener(item -> {
            // Resource IDs are not constants under AGP 9, so they are compared with if, not switch.
            if (item.getItemId() == R.id.action_about) {
                new AboutBmiDialogFragment()
                        .show(getSupportFragmentManager(), AboutBmiDialogFragment.TAG);
                return true;
            }
            return false;
        });
    }

    // --- User actions --------------------------------------------------------

    private void onWeightUnitSelected(WeightUnit unit) {
        WeightUnit previous = viewModel.currentState().weightUnit();
        if (unit != previous) {
            convertTypedValue(weightFields, previous, unit);
            viewModel.selectWeightUnit(unit);
        }
    }

    private void onHeightUnitSelected(HeightUnit unit) {
        HeightUnit previous = viewModel.currentState().heightUnit();
        if (unit != previous) {
            convertTypedValue(heightFields, previous, unit);
            viewModel.selectHeightUnit(unit);
        }
    }

    /**
     * Rewrites what the user typed in the new unit, so a switch from kilograms to pounds turns
     * 70 into 154.3 instead of reading it as 70 lb. Text that cannot be read is cleared.
     */
    private static <U extends Enum<U> & MeasurementUnit> void convertTypedValue(
            MeasurementFields fields, U from, U to) {
        fields.setInput(to, MeasurementConverter.convert(fields.input(from), from, to)
                .orElse(MeasurementInput.EMPTY));
    }

    private void calculate() {
        BmiUiState state = viewModel.currentState();
        viewModel.calculate(
                weightFields.input(state.weightUnit()),
                heightFields.input(state.heightUnit()));

        BmiUiState next = viewModel.currentState();
        if (next.result() != null) {
            hideKeyboard();
            // The result is the last item, so scrolling to the end reveals all of it, including
            // the bottom padding that keeps it clear of the navigation bar.
            binding.scrollView.post(() -> binding.scrollView.smoothScrollTo(
                    0, binding.scrollView.getChildAt(0).getHeight()));
        } else if (next.weightErrors().any()) {
            weightFields.fieldWithError(next.weightUnit(), next.weightErrors()).requestFocus();
        } else {
            heightFields.fieldWithError(next.heightUnit(), next.heightErrors()).requestFocus();
        }
    }

    private void clear() {
        weightFields.clear();
        heightFields.clear();
        viewModel.clear();
        weightFields.firstField(viewModel.currentState().weightUnit()).requestFocus();
    }

    // --- Rendering -----------------------------------------------------------

    private void render(BmiUiState state) {
        WeightUnit weightUnit = state.weightUnit();
        weightToggle.show(weightUnit);
        weightFields.render(weightUnit, text.symbol(weightUnit), text.range(weightUnit),
                text.primaryError(MeasurementKind.WEIGHT, weightUnit,
                        state.weightErrors().primary()),
                text.partError(MeasurementKind.WEIGHT, state.weightErrors().part()));

        HeightUnit heightUnit = state.heightUnit();
        heightToggle.show(heightUnit);
        heightFields.render(heightUnit, text.symbol(heightUnit), text.range(heightUnit),
                text.primaryError(MeasurementKind.HEIGHT, heightUnit,
                        state.heightErrors().primary()),
                text.partError(MeasurementKind.HEIGHT, state.heightErrors().part()));

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
                text.quantity(result.healthyWeightMinimum(weightUnit), weightUnit),
                text.quantity(result.healthyWeightMaximum(weightUnit), weightUnit)));
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
            focused.requestRectangleOnScreen(
                    new Rect(0, 0, focused.getWidth(), focused.getHeight()), false);
        }
    }
}
