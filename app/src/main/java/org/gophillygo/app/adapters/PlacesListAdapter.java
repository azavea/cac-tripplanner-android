package org.gophillygo.app.adapters;

import android.content.Context;

import org.gophillygo.app.R;
import org.gophillygo.app.data.models.DestinationInfo;

import java.util.List;

public class PlacesListAdapter extends AttractionListAdapter<DestinationInfo> {
    public PlacesListAdapter(Context context, List<DestinationInfo> attractions, AttractionListItemClickListener listener) {
        super(context, attractions, R.layout.place_list_item, listener);
    }
}
