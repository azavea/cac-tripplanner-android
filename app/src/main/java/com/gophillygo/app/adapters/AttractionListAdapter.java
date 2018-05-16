package com.gophillygo.app.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.gophillygo.app.BR;
import com.gophillygo.app.data.models.AttractionInfo;
import com.gophillygo.app.utils.FlagMenuUtils;

import java.util.List;


public class AttractionListAdapter<T extends AttractionInfo> extends RecyclerView.Adapter {

    public interface AttractionListItemClickListener {
        void clickedAttraction(int position);
        boolean clickedFlagOption(MenuItem item, AttractionInfo info, Integer position);
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

        public void bind(AttractionInfo info) {
            binding.setVariable(BR.attractionInfo, info);
            binding.setVariable(BR.attraction, info.getAttraction());
            binding.setVariable(BR.position, getAdapterPosition());
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
    public void optionsButtonClick(View view, T info, Integer position) {
        Log.d(LOG_LABEL, "Clicked place options button for attraction #" + info.getAttraction().getId());
        PopupMenu menu = FlagMenuUtils.getFlagPopupMenu(context, view, info.getFlag());
        menu.setOnMenuItemClickListener(item -> listener.clickedFlagOption(item, info, position));
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
        T info = attractionList.get(position);
        ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.bind(info);
    }

    @Override
    public long getItemId(int position) {
        T info = attractionList.get(position);
        if (info != null) {
            return info.getAttraction().getId();
        } else {
            Log.w(LOG_LABEL, "Could not find attraction at offset " + String.valueOf(position));
            return -1;
        }
    }

    @Override
    public int getItemCount() {
        return attractionList.size();
    }

    public Drawable getFlagImage(AttractionInfo info) {
        return ContextCompat.getDrawable(this.context, info.getFlagImage());
    }
}
