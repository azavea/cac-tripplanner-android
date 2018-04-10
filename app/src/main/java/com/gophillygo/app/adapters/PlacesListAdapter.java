package com.gophillygo.app.adapters;

import android.content.Context;


import com.gophillygo.app.R;
import com.gophillygo.app.data.models.Destination;
import com.gophillygo.app.data.AttractionViewModel;

import java.util.List;

public class PlacesListAdapter extends AttractionListAdapter<Destination> {
    public PlacesListAdapter(Context context, List<Destination> attractions, AttractionViewModel viewModel, AttractionListItemClickListener listener) {
        super(context, attractions, R.layout.place_list_item, viewModel, listener);
    }
}
