package com.gophillygo.app.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
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

import com.gophillygo.app.R;
import com.gophillygo.app.data.models.Event;

import java.util.List;

public class EventsListAdapter extends RecyclerView.Adapter {

    public interface EventListItemClickListener {
        void clickedPlace(int position);
    }

    private static final String LOG_LABEL = "EventListAdapter";

    private final Context context;
    private final LayoutInflater inflater;
    private EventListItemClickListener clickListener;

    private List<Event> eventList;

    private static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView eventNameView;
        TextView distanceView;
        ImageButton eventOptionsButton;
        ImageView cyclingMarker;
        ImageView watershedAllianceMarker;

        private ViewHolder(View parentView, final EventListItemClickListener listener) {
            super(parentView);

            parentView.setOnClickListener(v -> {
                listener.clickedPlace(getAdapterPosition());
            });
        }
    }

    public EventsListAdapter(Context context, List<Event> events, EventListItemClickListener listener) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.eventList = events;
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View parentView = inflater.inflate(R.layout.place_list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(parentView, this.clickListener);

        viewHolder.imageView = parentView.findViewById(R.id.place_list_item_image);
        viewHolder.eventNameView = parentView.findViewById(R.id.place_list_item_name_label);
        viewHolder.distanceView = parentView.findViewById(R.id.place_list_item_distance_label);
        viewHolder.eventOptionsButton = parentView.findViewById(R.id.place_list_item_options_button);
        viewHolder.watershedAllianceMarker = parentView.findViewById(R.id.place_list_watershed_alliance_marker);
        viewHolder.cyclingMarker = parentView.findViewById(R.id.place_list_cycling_activity_marker);

        parentView.setTag(viewHolder);

        return viewHolder;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Event event = eventList.get(position);
        ViewHolder viewHolder = (ViewHolder) holder;

        viewHolder.eventNameView.setText(event.getName());

        /*
        Destination destination = event.getDestination();

        if (destination.isWatershedAlliance()) {
            viewHolder.watershedAllianceMarker.setVisibility(View.VISIBLE);
            viewHolder.watershedAllianceMarker.invalidate();
        } else {
            viewHolder.watershedAllianceMarker.setVisibility(View.GONE);
        }

        if (destination.isCycling()) {
            viewHolder.cyclingMarker.setVisibility(View.VISIBLE);
        } else {
            viewHolder.cyclingMarker.setVisibility(View.GONE);
        }
        */

        viewHolder.eventOptionsButton.setOnClickListener(v -> {
            Log.d(LOG_LABEL, "Clicked event options button for event #" + event.getId());
            PopupMenu menu = new PopupMenu(context, viewHolder.eventOptionsButton);
            menu.getMenuInflater().inflate(R.menu.place_options_menu, menu.getMenu());
            menu.setOnMenuItemClickListener(item -> {
                Log.d(LOG_LABEL, "Clicked " + item.toString());
                return true;
            });

            // Force icons to show in the popup menu via the support library API
            // https://stackoverflow.com/questions/6805756/is-it-possible-to-display-icons-in-a-popupmenu
            MenuPopupHelper popupHelper = new MenuPopupHelper(context,
                    (MenuBuilder)menu.getMenu(),
                    viewHolder.eventOptionsButton);
            popupHelper.setForceShowIcon(true);
            popupHelper.show();
        });
    }

    @Override
    public long getItemId(int position) {
        Event event = eventList.get(position);
        if (event != null) {
            return event.getId();
        } else {
            Log.w(LOG_LABEL, "Could not find event at offset " + String.valueOf(position));
            return -1;
        }
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }
}

