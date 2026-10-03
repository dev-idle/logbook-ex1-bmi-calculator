package com.comp1786.logbook.bmi.ui;

import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Links a group of unit buttons to a unit enum. Button IDs are mapped rather than switched on,
 * because resource IDs are not constants under Android Gradle Plugin 9.
 */
final class UnitToggle<U extends Enum<U>> {

    private final MaterialButtonToggleGroup group;
    private final Map<U, Integer> buttonIds;

    /**
     * {@code onSelected} also runs when {@link #show} checks a button, so it must ignore the
     * unit already shown.
     */
    UnitToggle(MaterialButtonToggleGroup group, Map<U, Integer> buttonIds,
               Consumer<U> onSelected) {
        this.group = group;
        this.buttonIds = buttonIds;
        group.addOnButtonCheckedListener((toggleGroup, checkedId, isChecked) -> {
            if (!isChecked) {
                return;
            }
            buttonIds.forEach((unit, buttonId) -> {
                if (buttonId == checkedId) {
                    onSelected.accept(unit);
                }
            });
        });
    }

    /** Checks the button of {@code unit}. */
    void show(U unit) {
        group.check(Objects.requireNonNull(buttonIds.get(unit), "No button for " + unit));
    }
}
