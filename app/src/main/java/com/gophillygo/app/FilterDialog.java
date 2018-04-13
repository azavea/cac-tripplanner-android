package com.gophillygo.app;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.gophillygo.app.data.models.Filter;

import java.util.ArrayList;


public class FilterDialog extends BottomSheetDialogFragment {

    public interface FilterChangeListener {
        void filterChanged(Filter filter);
    }

    private static final String LOG_LABEL = "FilterDialog";

    private Button doneButton;
    private Button resetButton;

    private GpgToggleButton natureButton, exerciseButton, educationalButton;
    private GpgToggleButton beenButton, wantToGoButton, notInterestedButton, likedButton;
    private GpgToggleButton accessibleButton;

    private ArrayList<GpgToggleButton> filterButtons;

    @Override
    public void onDismiss(DialogInterface dialog) {
        Filter filter = new Filter(natureButton.isChecked(), exerciseButton.isChecked(),
                                   educationalButton.isChecked(), beenButton.isChecked(),
                                   wantToGoButton.isChecked(), notInterestedButton.isChecked(),
                                   likedButton.isChecked(), accessibleButton.isChecked());

        Log.d(LOG_LABEL, "Selected " + String.valueOf(filter.count()) + " filters.");
        FilterChangeListener listener = (FilterChangeListener) getActivity();
        if (listener != null) {
            listener.filterChanged(filter);
        }

        super.onDismiss(dialog);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        View contentView = View.inflate(getContext(), R.layout.filter_modal, null);
        dialog.setContentView(contentView);

        // set up filter buttons
        natureButton = dialog.findViewById(R.id.filter_modal_nature_category_button);
        exerciseButton = dialog.findViewById(R.id.filter_modal_excercise_category_button);
        educationalButton = dialog.findViewById(R.id.filter_modal_educational_category_button);
        beenButton = dialog.findViewById(R.id.filter_modal_been_button);
        wantToGoButton = dialog.findViewById(R.id.filter_modal_want_to_go_button);
        notInterestedButton = dialog.findViewById(R.id.filter_modal_not_interested_button);
        likedButton = dialog.findViewById(R.id.filter_modal_liked_button);
        accessibleButton = dialog.findViewById(R.id.filter_modal_accessible_button);

        filterButtons = new ArrayList<>(8);
        filterButtons.add(natureButton);
        filterButtons.add(exerciseButton);
        filterButtons.add(educationalButton);
        filterButtons.add(beenButton);
        filterButtons.add(wantToGoButton);
        filterButtons.add(notInterestedButton);
        filterButtons.add(likedButton);
        filterButtons.add(accessibleButton);

        // set up modal function buttons
        doneButton = dialog.findViewById(R.id.filter_modal_done_button);
        resetButton = dialog.findViewById(R.id.filter_modal_reset_button);

        doneButton.setOnClickListener(v -> dismiss());

        resetButton.setOnClickListener(v -> {
            for (GpgToggleButton button: filterButtons) {
                button.setChecked(false);
            }
        });

        return dialog;
    }
}
