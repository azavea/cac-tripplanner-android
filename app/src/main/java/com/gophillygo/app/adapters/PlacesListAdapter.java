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

import com.gophillygo.app.R;
import com.gophillygo.app.data.models.Destination;
import com.gophillygo.app.databinding.PlaceListItemBinding;

import java.util.List;


public class PlacesListAdapter extends RecyclerView.Adapter {

    public interface PlaceListItemClickListener {
        void clickedPlace(int position);
    }

    private static final String LOG_LABEL = "PlaceListAdapter";

    private final Context context;
    private final LayoutInflater inflater;
    private final PlaceListItemClickListener clickListener;

    private List<Destination> destinationList;

    private static class ViewHolder extends RecyclerView.ViewHolder {
        private final PlaceListItemBinding binding;

        private ViewHolder(PlaceListItemBinding binding, final PlaceListItemClickListener listener) {
            super(binding.getRoot());
            binding.getRoot().setOnClickListener(v -> listener.clickedPlace(getAdapterPosition()));
            this.binding = binding;
        }

        public void bind(Destination destination) {
            binding.setDestination(destination);
            binding.executePendingBindings();
        }
    }

    public PlacesListAdapter(Context context, List<Destination> destinations,
                             PlaceListItemClickListener listener) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.destinationList = destinations;
        this.clickListener = listener;
    }

    @SuppressLint("RestrictedApi")
    public void optionsButtonClick(View view, Destination destination) {
        Log.d(LOG_LABEL, "Clicked place options button for place #" + destination.getId());
        PopupMenu menu = new PopupMenu(context, view);
        menu.getMenuInflater().inflate(R.menu.place_options_menu, menu.getMenu());
        menu.setOnMenuItemClickListener(item -> {
            Log.d(LOG_LABEL, "Clicked " + item.toString());
            return true;
        });

        // Force icons to show in the popup menu via the support library API
        // https://stackoverflow.com/questions/6805756/is-it-possible-to-display-icons-in-a-popupmenu
        MenuPopupHelper popupHelper = new MenuPopupHelper(context,
                (MenuBuilder)menu.getMenu(), view);
        popupHelper.setForceShowIcon(true);
        popupHelper.show();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        PlaceListItemBinding binding = PlaceListItemBinding.inflate(inflater, parent, false);
        binding.setAdapter(this);
        return new ViewHolder(binding, this.clickListener);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Destination destination = destinationList.get(position);
        ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.bind(destination);
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
