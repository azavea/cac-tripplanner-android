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
    private static final String FILTER_ARG = "Filter";

    private Button doneButton;
    private Button resetButton;

    private GpgToggleButton natureButton, exerciseButton, educationalButton;
    private GpgToggleButton beenButton, wantToGoButton, notInterestedButton, likedButton;
    private GpgToggleButton accessibleButton;

    private ArrayList<GpgToggleButton> filterButtons;


    public static FilterDialog newInstance(Filter filter) {
        FilterDialog dialog = new FilterDialog();

        Bundle args = new Bundle();
        args.putSerializable(FILTER_ARG, filter);
        dialog.setArguments(args);

        return dialog;
    }

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

        Filter filter = null;
        if (getArguments() != null) {
            filter = getArguments().getParcelable(FILTER_ARG);
        }
        if (filter == null) {
            filter = new Filter();
        }

        // set up filter buttons
        natureButton = dialog.findViewById(R.id.filter_modal_nature_category_button);
        natureButton.setChecked(filter.nature);
        exerciseButton = dialog.findViewById(R.id.filter_modal_excercise_category_button);
        exerciseButton.setChecked(filter.exercise);
        educationalButton = dialog.findViewById(R.id.filter_modal_educational_category_button);
        educationalButton.setChecked(filter.educational);
        beenButton = dialog.findViewById(R.id.filter_modal_been_button);
        beenButton.setChecked(filter.been);
        wantToGoButton = dialog.findViewById(R.id.filter_modal_want_to_go_button);
        wantToGoButton.setChecked(filter.wantToGo);
        notInterestedButton = dialog.findViewById(R.id.filter_modal_not_interested_button);
        notInterestedButton.setChecked(filter.notInterested);
        likedButton = dialog.findViewById(R.id.filter_modal_liked_button);
        likedButton.setChecked(filter.liked);
        accessibleButton = dialog.findViewById(R.id.filter_modal_accessible_button);
        accessibleButton.setChecked(filter.accessible);

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
