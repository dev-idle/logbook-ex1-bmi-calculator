package com.comp1786.logbook.bmi.ui;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.comp1786.logbook.bmi.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/** Explains BMI and links to the source of the categories. A fragment survives rotation. */
public class AboutBmiDialogFragment extends DialogFragment {

    static final String TAG = "about_bmi";

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.about_title)
                .setMessage(R.string.about_message)
                .setPositiveButton(R.string.ok, null)
                .setNeutralButton(R.string.about_open_source, (dialog, which) -> openSource())
                .create();
    }

    private void openSource() {
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse(getString(R.string.about_source_url)));
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException noBrowser) {
            Toast.makeText(requireContext(), R.string.error_no_browser, Toast.LENGTH_LONG).show();
        }
    }
}
