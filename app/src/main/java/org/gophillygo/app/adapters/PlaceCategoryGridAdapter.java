package org.gophillygo.app.adapters;

import android.content.Context;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.gophillygo.app.BR;
import org.gophillygo.app.R;
import org.gophillygo.app.data.models.CategoryAttraction;

import java.util.List;
import java.util.Objects;


public class PlaceCategoryGridAdapter extends ListAdapter<CategoryAttraction, PlaceCategoryGridAdapter.GridViewHolder> {

    private static final String LOG_LABEL = "GridAdapter";

    private List<CategoryAttraction> categoryAttractions;

    private LayoutInflater inflater;
    private GridViewHolder.PlaceGridItemClickListener listener;

    public static class GridViewHolder extends RecyclerView.ViewHolder {
        private final ViewDataBinding binding;

        public interface PlaceGridItemClickListener {
            void clickedGridItem(int position);
        }

        GridViewHolder(ViewDataBinding binding, final PlaceGridItemClickListener listener) {
            super(binding.getRoot());
            binding.getRoot().setOnClickListener(v -> listener.clickedGridItem(getAdapterPosition()));
            this.binding = binding;
        }
        public void bind(CategoryAttraction info) {
            binding.setVariable(BR.category, info);
            binding.setVariable(BR.position, getAdapterPosition());
            binding.executePendingBindings();
        }
    }

    private PlaceCategoryGridAdapter() {
        super(new DiffUtil.ItemCallback<CategoryAttraction>() {
            @Override
            public boolean areItemsTheSame(CategoryAttraction oldItem, CategoryAttraction newItem) {
                if (oldItem == null) {
                    return newItem == null;
                } else {
                    return newItem != null && oldItem.getCategory().equals(newItem.getCategory());
                }
            }

            @Override
            public boolean areContentsTheSame(CategoryAttraction oldItem, CategoryAttraction newItem) {
                return Objects.equals(oldItem, newItem);
            }
        });
    }

    public PlaceCategoryGridAdapter(Context context, GridViewHolder.PlaceGridItemClickListener listener) {
        this();
        this.inflater = LayoutInflater.from(context);
        this.listener = listener;
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
        holder.itemView.setTag(item);
    }

    @Override
    protected CategoryAttraction getItem(int position) {
        if (categoryAttractions == null) {
            return null;
        }
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
        this.categoryAttractions = list;
    }

    @Override
    public int getItemCount() {
        return categoryAttractions == null ? 0 : categoryAttractions.size();
    }
}
