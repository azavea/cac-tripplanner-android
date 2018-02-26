package com.gophillygo.app.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.gophillygo.app.R;
import com.gophillygo.app.data.models.Destination;

import java.util.Collections;
import java.util.List;


public class PlacesListAdapter extends RecyclerView.Adapter {

    private static final String LOG_LABEL = "PlaceListAdapter";

    private final Context context;
    private final LayoutInflater inflater;

    private List<Destination> destinationList;

    private static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView placeNameView;
        TextView distanceView;

        private ViewHolder(View parentView) {
            super(parentView);
        }
    }

    public PlacesListAdapter(Context context, List<Destination> destinations) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.destinationList = destinations;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View parentView = inflater.inflate(R.layout.place_list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(parentView);
        viewHolder.imageView = parentView.findViewById(R.id.place_list_item_image);
        viewHolder.placeNameView = parentView.findViewById(R.id.place_list_item_name_label);
        viewHolder.distanceView = parentView.findViewById(R.id.place_list_item_distance_label);
        parentView.setTag(viewHolder);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Destination destination = destinationList.get(position);
        ViewHolder viewHolder = (ViewHolder) holder;

        viewHolder.placeNameView.setText(destination.getName());
        viewHolder.distanceView.setText(destination.getFormattedDistance());
        Glide.with(context)
                .load(destination.getWideImage())
                .into(viewHolder.imageView);
    }

    @Override
    public long getItemId(int position) {
        Destination destination = destinationList.get(position);
        if (destination != null) {
            return destination.getId();
        } else {
            Log.w(LOG_LABEL, "Could not find destination at offset " + String.valueOf(position));
            return -1;
        }
    }

    @Override
    public int getItemCount() {
        return destinationList.size();
    }
}
