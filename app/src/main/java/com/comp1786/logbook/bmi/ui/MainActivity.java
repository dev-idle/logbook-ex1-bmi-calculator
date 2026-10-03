package com.comp1786.logbook.bmi.ui;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.comp1786.logbook.bmi.R;
import com.comp1786.logbook.bmi.databinding.ActivityMainBinding;
import com.comp1786.logbook.bmi.databinding.ItemBmiCategoryBinding;
import com.comp1786.logbook.bmi.domain.BmiCategory;
import com.comp1786.logbook.bmi.domain.BmiResult;
import com.comp1786.logbook.bmi.domain.HeightUnit;
import com.comp1786.logbook.bmi.domain.MeasurementConverter;
import com.comp1786.logbook.bmi.domain.MeasurementInput;
import com.comp1786.logbook.bmi.domain.MeasurementUnit;
import com.comp1786.logbook.bmi.domain.WeightUnit;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The calculator screen. It draws {@link BmiUiState} and forwards actions to
 * {@link BmiViewModel}; it only changes the typed text itself, on a unit switch or Clear.
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
        viewModel = new ViewModelProvider(
                this, ViewModelProvider.Factory.from(BmiViewModel.INITIALIZER))
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

        buildCategoryViews();
        applyWindowInsets();
        setUpMenu();
        binding.calculateButton.setOnClickListener(view -> calculate());
        binding.clearButton.setOnClickListener(view -> clear());
        // Height is the last measurement, so Done on its last field calculates.
        heightFields.setOnDone(this::calculate);
        viewModel.uiState().observe(this, this::render);
        if (savedInstanceState == null) {
            watchEdits();
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Edits are watched only once the fields' text is restored, so that a restore is not
        // taken for an edit that clears the saved errors and result.
        watchEdits();
    }

    // --- Setup ---------------------------------------------------------------

    /** Keeps content clear of the system bars, cutouts and keyboard in the edge-to-edge window. */
    private void applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar, (view, insets) -> {
            Insets bars = insets.getInsets(SYSTEM_BARS_AND_CUTOUT);
            view.setPadding(bars.left, 0, bars.right, 0);
            return insets;
        });
        ViewCompat.setOnApplyWindowInsetsListener(binding.scrollView, (view, insets) -> {
            Insets bars = insets.getInsets(SYSTEM_BARS_AND_CUTOUT);
            int keyboard = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
            // The keyboard shrinks the scroll view, which then scrolls the focused field into
            // view itself; NestedScrollView ignores bottom padding when it does that. The
            // navigation bar only pads it, so content still scrolls behind the bar.
            ViewGroup.MarginLayoutParams params =
                    (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            if (params.bottomMargin != keyboard) {
                params.bottomMargin = keyboard;
                view.setLayoutParams(params);
            }
            view.setPadding(bars.left, 0, bars.right, keyboard > 0 ? 0 : bars.bottom);
            return insets;
        });
    }

    private void watchEdits() {
        weightFields.watchEdits(viewModel::onWeightEdited, viewModel::onWeightPartEdited);
        heightFields.watchEdits(viewModel::onHeightEdited, viewModel::onHeightPartEdited);
    }

    private void setUpMenu() {
        binding.toolbar.setOnMenuItemClickListener(item -> {
            // Resource IDs are not constants under AGP 9, so they are compared with if, not switch.
            if (item.getItemId() == R.id.action_about) {
                FragmentManager fragments = getSupportFragmentManager();
                // showNow commits at once, so a quick second tap finds the open dialog.
                if (fragments.findFragmentByTag(AboutBmiDialogFragment.TAG) == null) {
                    new AboutBmiDialogFragment().showNow(fragments, AboutBmiDialogFragment.TAG);
                }
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

    /** Converts the typed value into the new unit, clearing text that is not an accepted number. */
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
            // Brings the top of the result card into view. When the whole card fits, the scroll
            // stops at the end of the content, so all of it shows above the navigation bar.
            int spacing = getResources().getDimensionPixelSize(R.dimen.screen_padding);
            binding.scrollView.post(() -> binding.scrollView.smoothScrollTo(
                    0, binding.content.getTop() + binding.resultCard.getTop() - spacing));
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
        weightToggle.show(state.weightUnit());
        heightToggle.show(state.heightUnit());
        renderFields(weightFields, MeasurementKind.WEIGHT, state.weightUnit(),
                state.weightErrors());
        renderFields(heightFields, MeasurementKind.HEIGHT, state.heightUnit(),
                state.heightErrors());
        renderResult(state.result(), state.weightUnit());
    }

    private void renderFields(MeasurementFields fields, MeasurementKind kind,
                              MeasurementUnit unit, FieldErrors errors) {
        fields.render(unit, text.symbol(unit), text.range(unit),
                text.primaryError(kind, unit, errors.primary()),
                text.partError(kind, errors.part()));
    }

    private void renderResult(@Nullable BmiResult result, WeightUnit weightUnit) {
        binding.resultCard.setVisibility(result == null ? View.GONE : View.VISIBLE);
        if (result == null) {
            // Cleared so that the same result is announced again when it comes back.
            binding.resultSummary.setContentDescription(null);
            return;
        }
        BmiCategory category = result.category();
        String bmi = text.bmi(result.roundedBmi());
        String label = getString(CategoryAppearance.label(category));
        int color = ContextCompat.getColor(this, CategoryAppearance.color(category));

        binding.bmiValue.setText(bmi);
        binding.categoryLabel.setText(label);
        binding.categoryLabel.setBackgroundTintList(ColorStateList.valueOf(color));
        binding.bmiGauge.setPointer(GaugeScale.position(result.bmi()));
        binding.resultSummary.setContentDescription(
                getString(R.string.result_summary_description, bmi, label));

        binding.healthyRange.setText(getString(R.string.healthy_weight_range,
                text.quantity(result.healthyWeightMinimum(weightUnit), weightUnit),
                text.quantity(result.healthyWeightMaximum(weightUnit), weightUnit)));
        highlightCategory(category);
    }

    /** Builds the gauge and category rows once; each result only moves the highlights. */
    private void buildCategoryViews() {
        BmiCategory[] categories = BmiCategory.values();
        int[] colors = new int[categories.length];
        List<String> boundaries = new ArrayList<>();
        for (BmiCategory category : categories) {
            int color = ContextCompat.getColor(this, CategoryAppearance.color(category));
            colors[category.ordinal()] = color;
            if (Double.isFinite(category.upperBound())) {
                boundaries.add(text.number(category.upperBound()));
            }

            ItemBmiCategoryBinding row = ItemBmiCategoryBinding.inflate(
                    getLayoutInflater(), binding.categoryList, true);
            // The row's tint only shows while it is activated as the user's category.
            ColorStateList tint = ColorStateList.valueOf(color);
            row.swatch.setBackgroundTintList(tint);
            row.getRoot().setBackgroundTintList(tint);
            row.name.setText(CategoryAppearance.label(category));
            row.range.setText(text.categoryRange(category));
            categoryRows.put(category, row);
        }
        binding.bmiGauge.setSegments(colors, boundaries);
    }

    private void highlightCategory(BmiCategory current) {
        categoryRows.forEach((category, row) -> {
            boolean isCurrent = category == current;
            int appearance = isCurrent
                    ? R.style.TextAppearance_BMICalculator_CategoryRow_Current
                    : R.style.TextAppearance_BMICalculator_CategoryRow;
            row.getRoot().setActivated(isCurrent);
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
}
