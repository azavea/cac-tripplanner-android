package com.gophillygo.app.adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.gophillygo.app.BR;
import com.gophillygo.app.R;
import com.gophillygo.app.data.models.CategoryAttraction;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class PlaceCategoryGridAdapter extends ListAdapter<CategoryAttraction, PlaceCategoryGridAdapter.GridViewHolder> {

    private static final String LOG_LABEL = "GridAdapter";

    private List<CategoryAttraction> categoryAttractions;

    private  Context context;
    private LayoutInflater inflater;
    private GridViewHolder.PlaceGridItemClickListener listener;

    public static class GridViewHolder extends RecyclerView.ViewHolder {
        private final ViewDataBinding binding;

        public interface PlaceGridItemClickListener {
            // TODO: click callback
            void clickedGridItem(int position);
        }

        GridViewHolder(ViewDataBinding binding, final PlaceGridItemClickListener listener) {
            super(binding.getRoot());
            binding.getRoot().setOnClickListener(v -> listener.clickedGridItem(getAdapterPosition()));
            this.binding = binding;
        }
        public void bind(CategoryAttraction info) {
            Log.d(LOG_LABEL, "Binding to " + info.getCategory().displayName);
            binding.setVariable(BR.category, info);
            binding.setVariable(BR.position, getAdapterPosition());
            binding.executePendingBindings();
        }
    }

    private PlaceCategoryGridAdapter() {
        super(new DiffUtil.ItemCallback<CategoryAttraction>() {
            @Override
            public boolean areItemsTheSame(CategoryAttraction oldItem, CategoryAttraction newItem) {
                Log.d(LOG_LABEL, "areTheSame");
                if (oldItem == null) {
                    return newItem == null;
                } else {
                    return newItem != null && oldItem.getCategory().equals(newItem.getCategory());
                }
            }

            @Override
            public boolean areContentsTheSame(CategoryAttraction oldItem, CategoryAttraction newItem) {
                Log.d(LOG_LABEL, "areContentsTheSame");
                return Objects.equals(oldItem, newItem);
            }
        });
    }

    public PlaceCategoryGridAdapter(Context context, GridViewHolder.PlaceGridItemClickListener listener) {
        this();
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.listener = listener;

        // FIXME
        this.categoryAttractions = new ArrayList<>(CategoryAttraction.PlaceCategories.size());
        for (CategoryAttraction.PlaceCategories placeCategory : CategoryAttraction.PlaceCategories.values()) {
            categoryAttractions.add(placeCategory.code, new CategoryAttraction(placeCategory.code, ""));
        }
        Log.d(LOG_LABEL, "created category grid adapter");

    }

    @NonNull
    @Override
    public GridViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(LOG_LABEL, "onCreateViewHolder");

        ViewDataBinding binding = DataBindingUtil.inflate(inflater,
                R.layout.place_category_grid_item, parent, false);
        binding.setVariable(BR.adapter, this);
        return new GridViewHolder(binding, this.listener);
    }

    @Override
    public void onBindViewHolder(@NonNull GridViewHolder holder, int position) {
        CategoryAttraction item = getItem(position);
        holder.bind(item);
        Log.d(LOG_LABEL, "bound view holder to " + item.getCategory());
    }

    @Override
    protected CategoryAttraction getItem(int position) {
        Log.d(LOG_LABEL, "getItem");
        return categoryAttractions.get(position);
    }

    @Override
    public long getItemId(int position) {
        CategoryAttraction attraction = getItem(position);
        if (attraction == null) {
            return -1;
        }
        return attraction.getCategory().code;
    }

    @Override
    public void submitList(List<CategoryAttraction> list) {
        super.submitList(list);
        Log.d(LOG_LABEL, "submitted list of size " + list.size());
        this.categoryAttractions = list;
    }

    @Override
    public int getItemCount() {
        Log.d(LOG_LABEL, "Returning item count " + categoryAttractions.size());
        return categoryAttractions.size();
    }
}
