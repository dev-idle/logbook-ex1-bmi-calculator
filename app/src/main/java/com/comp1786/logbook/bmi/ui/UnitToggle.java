package com.comp1786.logbook.bmi.ui;

import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Connects a group of unit buttons to a unit enum: it checks the button of the unit being shown
 * and reports the unit the user picks.
 *
 * <p>Button IDs are looked up in a map rather than a {@code switch}, because resource IDs are
 * not compile-time constants under Android Gradle Plugin 9.
 *
 * @param <U> the unit type, such as {@link com.comp1786.logbook.bmi.domain.WeightUnit}
 */
final class UnitToggle<U extends Enum<U>> {

    private final MaterialButtonToggleGroup group;
    private final Map<U, Integer> buttonIds;

    /**
     * @param buttonIds  the button for every unit
     * @param onSelected called with the unit whose button becomes checked, including when
     *                   {@link #show} checks it, so the callback must ignore the current unit
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
