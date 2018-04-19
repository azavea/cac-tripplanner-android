package com.gophillygo.app;

import android.app.Dialog;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.gophillygo.app.data.models.Filter;
import com.gophillygo.app.databinding.FilterModalBinding;

import java.util.ArrayList;


public class FilterDialog extends BottomSheetDialogFragment {

    private Filter filter;

    public interface FilterChangeListener {
        void filterChanged(Filter filter);
    }

    private static final String LOG_LABEL = "FilterDialog";
    private static final String FILTER_ARG = "Filter";

    private static @IdRes int[] FILTER_BUTTONS = {
        R.id.filter_modal_nature_category_button,
        R.id.filter_modal_excercise_category_button,
        R.id.filter_modal_educational_category_button,
        R.id.filter_modal_been_button,
        R.id.filter_modal_want_to_go_button,
        R.id.filter_modal_not_interested_button,
        R.id.filter_modal_liked_button,
        R.id.filter_modal_accessible_button,
    };

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
        filter = null;
        if (getArguments() != null) {
            filter = (Filter) getArguments().getSerializable(FILTER_ARG);
        }
        if (filter == null) {
            filter = new Filter();
        }

        Dialog dialog = super.onCreateDialog(savedInstanceState);
        FilterModalBinding binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()),
                R.layout.filter_modal, null, false);
        binding.setFilter(filter);
        binding.setDialog(this);
        dialog.setContentView(binding.getRoot());

        // set up filter buttons
        filterButtons = new ArrayList<>(8);
        for (int id : FILTER_BUTTONS) {
            filterButtons.add(dialog.findViewById(id));
        }

        return dialog;
    }

    public void resetFilters(View view) {
        for (GpgToggleButton button : filterButtons) {
            button.setChecked(false);
        }
    }
}
