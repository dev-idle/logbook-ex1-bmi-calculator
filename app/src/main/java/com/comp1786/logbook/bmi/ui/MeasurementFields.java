package com.comp1786.logbook.bmi.ui;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.comp1786.logbook.bmi.domain.MeasurementInput;
import com.comp1786.logbook.bmi.domain.MeasurementUnit;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

/**
 * The input fields of one measurement: a single field for a simple unit, or a whole and a part
 * field for a compound unit such as feet and inches.
 */
final class MeasurementFields {

    private final TextInputLayout singleLayout;
    private final EditText singleInput;
    private final View compoundRow;
    private final TextInputLayout wholeLayout;
    private final EditText wholeInput;
    private final TextInputLayout partLayout;
    private final EditText partInput;

    MeasurementFields(TextInputLayout singleLayout, View compoundRow,
                      TextInputLayout wholeLayout, TextInputLayout partLayout) {
        this.singleLayout = singleLayout;
        this.singleInput = editTextOf(singleLayout);
        this.compoundRow = compoundRow;
        this.wholeLayout = wholeLayout;
        this.wholeInput = editTextOf(wholeLayout);
        this.partLayout = partLayout;
        this.partInput = editTextOf(partLayout);
    }

    /** The text typed for {@code unit}. */
    MeasurementInput input(MeasurementUnit unit) {
        return unit.isCompound()
                ? new MeasurementInput(textOf(wholeInput), textOf(partInput))
                : MeasurementInput.of(textOf(singleInput));
    }

    /** Replaces the text of the fields that {@code unit} uses. */
    void setInput(MeasurementUnit unit, MeasurementInput input) {
        if (unit.isCompound()) {
            wholeInput.setText(input.primary());
            partInput.setText(input.part());
        } else {
            singleInput.setText(input.primary());
        }
    }

    void clear() {
        singleInput.setText(null);
        wholeInput.setText(null);
        partInput.setText(null);
    }

    /**
     * Shows the fields {@code unit} uses, with its range and errors. Only a simple unit shows
     * {@code symbol}; compound fields have their symbols in the layout.
     */
    void render(MeasurementUnit unit, String symbol, String range,
                @Nullable String primaryError, @Nullable String partError) {
        boolean compound = unit.isCompound();
        singleLayout.setVisibility(compound ? View.GONE : View.VISIBLE);
        compoundRow.setVisibility(compound ? View.VISIBLE : View.GONE);
        if (compound) {
            wholeLayout.setHelperText(range);
            wholeLayout.setError(primaryError);
            partLayout.setError(partError);
        } else {
            singleLayout.setSuffixText(symbol);
            singleLayout.setHelperText(range);
            singleLayout.setError(primaryError);
        }
    }

    /** The first field to fix: the part field only when the whole field has no error. */
    EditText fieldWithError(MeasurementUnit unit, FieldErrors errors) {
        if (!unit.isCompound()) {
            return singleInput;
        }
        return errors.primary() == null && errors.part() != null ? partInput : wholeInput;
    }

    /** The field the user types into first for {@code unit}. */
    EditText firstField(MeasurementUnit unit) {
        return unit.isCompound() ? wholeInput : singleInput;
    }

    /** Reports edits to the single or whole field, and to the part field. */
    void watchEdits(Runnable onPrimaryEdited, Runnable onPartEdited) {
        watch(singleInput, onPrimaryEdited);
        watch(wholeInput, onPrimaryEdited);
        watch(partInput, onPartEdited);
    }

    /** Runs {@code action} on Done or a hardware Enter key in the last field. */
    void setOnDone(Runnable action) {
        TextView.OnEditorActionListener listener = (view, actionId, event) -> {
            boolean doneKey = actionId == EditorInfo.IME_ACTION_DONE;
            // A hardware Enter key arrives as IME_NULL with a key event.
            boolean enterKey = event != null
                    && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_DOWN;
            if (doneKey || enterKey) {
                action.run();
                return true;
            }
            return false;
        };
        singleInput.setOnEditorActionListener(listener);
        partInput.setOnEditorActionListener(listener);
    }

    /**
     * Calls {@code onEdited} when the text changes. The keyboard also reports changes when a
     * field gains focus, which must not clear an error the user still needs to read.
     */
    private static void watch(EditText field, Runnable onEdited) {
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

    private static EditText editTextOf(TextInputLayout layout) {
        return Objects.requireNonNull(layout.getEditText(), "TextInputLayout without EditText");
    }

    private static String textOf(EditText field) {
        Editable editable = field.getText();
        return editable == null ? "" : editable.toString();
    }
}
