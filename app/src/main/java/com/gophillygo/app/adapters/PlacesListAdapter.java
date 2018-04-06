package com.gophillygo.app.adapters;

import android.content.Context;


import java.util.List;

public class PlacesListAdapter<Destination> extends com.gophillygo.app.adapters.AttractionListAdapter {
    public PlacesListAdapter(Context context, List<Destination> attractions, AttractionListItemClickListener listener) {
        super(context, attractions, listener);
    }
}
