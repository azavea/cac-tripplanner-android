package com.gophillygo.app.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gophillygo.app.BR;
import com.gophillygo.app.R;
import com.gophillygo.app.data.AttractionViewModel;
import com.gophillygo.app.data.models.Attraction;
import com.gophillygo.app.data.models.AttractionFlag;

import java.util.List;


public class AttractionListAdapter<T extends Attraction> extends RecyclerView.Adapter {

    public interface AttractionListItemClickListener {
        void clickedAttraction(int position);
    }

    private static final String LOG_LABEL = "AttractionListAdapter";

    private final Context context;
    private final LayoutInflater inflater;
    private final AttractionViewModel viewModel;
    private final AttractionListItemClickListener clickListener;
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
     * @param viewModel viewModel for Attraction, used to persist flags
     */
    public AttractionListAdapter(Context context, List<T> attractions, int itemViewId,
                                 AttractionViewModel viewModel,
                                 AttractionListItemClickListener listener) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.attractionList = attractions;
        this.clickListener = listener;
        this.itemViewId = itemViewId;
        this.viewModel = viewModel;
    }

    @SuppressLint("RestrictedApi")
    public void optionsButtonClick(View view, T attraction) {
        Log.d(LOG_LABEL, "Clicked place options button for attraction #" + attraction.getId());
        PopupMenu menu = new PopupMenu(context, view);
        menu.getMenuInflater().inflate(R.menu.place_options_menu, menu.getMenu());
        menu.setOnMenuItemClickListener(item -> {
            AttractionFlag.Option option;
            switch (item.getItemId()) {
                case R.id.place_option_not_interested:
                    option = AttractionFlag.Option.NotInterested;
                    break;
                case R.id.place_option_liked:
                    option = AttractionFlag.Option.Liked;
                    break;
                case R.id.place_option_been:
                    option = AttractionFlag.Option.Been;
                    break;
                case R.id.place_option_want_to_go:
                    option = AttractionFlag.Option.WantToGo;
                    break;
                default:
                    option = AttractionFlag.Option.NotSelected;
            }
            AttractionFlag flag = attraction.getFlag();
            if (flag != null) {
                if (flag.getOption() == option) {
                    option = AttractionFlag.Option.NotSelected;
                }
                flag = new AttractionFlag(flag.getId(), attraction.getId(), attraction.isEvent(), option);
            } else {
                flag = new AttractionFlag(null, attraction.getId(), attraction.isEvent(), option);
            }

            viewModel.updateAttractionFlag(flag);
            attraction.setFlag(flag);
            setImageResource((AppCompatImageButton) view, attraction.getFlagImage());

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
        ViewDataBinding binding = DataBindingUtil.inflate(inflater, itemViewId, parent, false);
        binding.setVariable(BR.adapter, this);
        return new ViewHolder(binding, this.clickListener);
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


    @BindingAdapter("imageSource")
    public static void setImageResource(AppCompatImageButton imageButton, @DrawableRes int drawable) {
        imageButton.setImageResource(drawable);
    }
}
