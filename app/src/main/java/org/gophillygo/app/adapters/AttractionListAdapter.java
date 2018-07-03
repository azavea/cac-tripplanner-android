package org.gophillygo.app.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.gophillygo.app.BR;
import org.gophillygo.app.data.models.AttractionInfo;
import org.gophillygo.app.utils.FlagMenuUtils;

import java.util.List;


public class AttractionListAdapter<T extends AttractionInfo> extends ListAdapter<T, AttractionListAdapter.ViewHolder> {

    public interface AttractionListItemClickListener {
        void clickedAttraction(int position);
        boolean clickedFlagOption(MenuItem item, AttractionInfo info, Integer position);
    }

    private static final String LOG_LABEL = "AttractionListAdapter";

    private Context context;
    private LayoutInflater inflater;
    private AttractionListItemClickListener listener;
    private int itemViewId;

    private List<T> attractionList;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ViewDataBinding binding;

        private ViewHolder(ViewDataBinding binding, final AttractionListItemClickListener listener) {
            super(binding.getRoot());
            binding.getRoot().setOnClickListener(v -> listener.clickedAttraction(getAdapterPosition()));
            this.binding = binding;
        }

        public void bind(AttractionInfo info, Context context) {
            binding.setVariable(BR.attractionInfo, info);
            binding.setVariable(BR.attraction, info.getAttraction());
            binding.setVariable(BR.position, getAdapterPosition());
            binding.setVariable(BR.context, context);
            binding.executePendingBindings();
        }
    }

    private AttractionListAdapter() {
        super(new DiffUtil.ItemCallback<T>() {

            @Override
            public boolean areItemsTheSame(T oldItem, T newItem) {
                // Returns true if these are for the same attraction; properties may differ.
                return oldItem.getAttraction().getId() == newItem.getAttraction().getId() &&
                        oldItem.getAttraction().isEvent() == newItem.getAttraction().isEvent();
            }

            @Override
            public boolean areContentsTheSame(T oldItem, T newItem) {
                return oldItem.equals(newItem);
            }
        });
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
        this();
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

    @Override
    public void submitList(List<T> list) {
        this.attractionList = list;
        super.submitList(list);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewDataBinding binding = DataBindingUtil.inflate(inflater, itemViewId, parent, false);
        binding.setVariable(BR.adapter, this);
        return new ViewHolder(binding, this.listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        T info = attractionList.get(position);
        holder.bind(info, holder.itemView.getContext());
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
