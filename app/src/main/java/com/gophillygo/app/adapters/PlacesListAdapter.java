package com.gophillygo.app.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.gophillygo.app.R;
import com.gophillygo.app.data.models.Destination;

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
        ImageButton placeOptionsButton;
        ImageView cyclingMarker;
        ImageView watershedAllianceMarker;

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
        viewHolder.placeOptionsButton = parentView.findViewById(R.id.place_list_item_options_button);
        viewHolder.watershedAllianceMarker = parentView.findViewById(R.id.place_list_watershed_alliance_marker);
        viewHolder.cyclingMarker = parentView.findViewById(R.id.place_list_cycling_activity_marker);

        parentView.setTag(viewHolder);
        return viewHolder;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Destination destination = destinationList.get(position);
        ViewHolder viewHolder = (ViewHolder) holder;

        viewHolder.placeNameView.setText(destination.getName());
        viewHolder.distanceView.setText(destination.getFormattedDistance());
        viewHolder.imageView.setContentDescription(destination.getName());
        Glide.with(context)
                .load(destination.getWideImage())
                .into(viewHolder.imageView);

        if (destination.isCycling()) {
            viewHolder.cyclingMarker.setVisibility(View.VISIBLE);
        } else {
            viewHolder.cyclingMarker.setVisibility(View.GONE);
        }

        if (destination.isWatershedAlliance()) {
            viewHolder.watershedAllianceMarker.setVisibility(View.VISIBLE);
        } else {
            viewHolder.watershedAllianceMarker.setVisibility(View.GONE);
        }

        viewHolder.placeOptionsButton.setOnClickListener(v -> {
            Log.d(LOG_LABEL, "Clicked place options button for place #" + destination.getId());
            PopupMenu menu = new PopupMenu(context, viewHolder.placeOptionsButton);
            menu.getMenuInflater().inflate(R.menu.place_options_menu, menu.getMenu());
            menu.setOnMenuItemClickListener(item -> {
                Log.d(LOG_LABEL, "Clicked " + item.toString());
                return true;
            });

            // Force icons to show in the popup menu via the support library API
            // https://stackoverflow.com/questions/6805756/is-it-possible-to-display-icons-in-a-popupmenu
            MenuPopupHelper popupHelper = new MenuPopupHelper(context,
                    (MenuBuilder)menu.getMenu(),
                    viewHolder.placeOptionsButton);
            popupHelper.setForceShowIcon(true);
            popupHelper.show();
        });
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
