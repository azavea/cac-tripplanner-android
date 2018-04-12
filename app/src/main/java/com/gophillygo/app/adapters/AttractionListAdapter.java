package com.gophillygo.app.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.gophillygo.app.BR;
import com.gophillygo.app.R;
import com.gophillygo.app.data.models.Attraction;

import java.util.List;


public class AttractionListAdapter<T extends Attraction> extends RecyclerView.Adapter {

    public interface AttractionListItemClickListener<T> {
        void clickedAttraction(int position);
        // TODO: Should we pass position here too, instead of the full object?
        boolean clickedFlagOption(MenuItem item, T attraction);
    }

    private static final String LOG_LABEL = "AttractionListAdapter";

    private final Context context;
    private final LayoutInflater inflater;
    private final AttractionListItemClickListener listener;
    private final int itemViewId;

    private List<T> attractionList;

    private static class ViewHolder extends RecyclerView.ViewHolder {
        private final ViewDataBinding binding;

        private ViewHolder(ViewDataBinding binding, final AttractionListItemClickListener listener) {
            super(binding.getRoot());
            binding.getRoot().setOnClickListener(v -> listener.clickedAttraction(getAdapterPosition()));
            this.binding = binding;
        }

        public void bind(Attraction attraction) {
            binding.setVariable(BR.attraction, attraction);
            binding.executePendingBindings();
        }
    }

    /**
     * Construct a generalized list adapter for places or events.
     *
     * @param context List activity displaying the attractions
     * @param attractions The attractions to list
     * @param itemViewId The resource identifier for the list item layout file
     * @param listener Listener for for attraction list item click callbacks
     */
    public AttractionListAdapter(Context context, List<T> attractions, int itemViewId,
                                 AttractionListItemClickListener listener) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.attractionList = attractions;
        this.listener = listener;
        this.itemViewId = itemViewId;
    }

    @SuppressLint("RestrictedApi")
    public void optionsButtonClick(View view, T attraction) {
        Log.d(LOG_LABEL, "Clicked place options button for attraction #" + attraction.getId());
        PopupMenu menu = new PopupMenu(context, view);
        menu.getMenuInflater().inflate(R.menu.place_options_menu, menu.getMenu());
        menu.setOnMenuItemClickListener(item -> listener.clickedFlagOption(item, attraction));

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
        ViewDataBinding binding = DataBindingUtil.inflate(inflater, itemViewId, parent, false);
        binding.setVariable(BR.adapter, this);
        return new ViewHolder(binding, this.listener);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        T attraction = attractionList.get(position);
        ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.bind(attraction);
    }

    @Override
    public long getItemId(int position) {
        T attraction = attractionList.get(position);
        if (attraction != null) {
            return attraction.getId();
        } else {
            Log.w(LOG_LABEL, "Could not find attraction at offset " + String.valueOf(position));
            return -1;
        }
    }

    @Override
    public int getItemCount() {
        return attractionList.size();
    }

    public Drawable getFlagImage(Attraction attraction) {
        return ContextCompat.getDrawable(this.context, attraction.getFlagImage());
    }
}
