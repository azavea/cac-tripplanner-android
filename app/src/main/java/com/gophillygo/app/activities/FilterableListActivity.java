package com.gophillygo.app.activities;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.widget.Button;

import com.gophillygo.app.data.models.Filter;
import com.gophillygo.app.databinding.FilterButtonBarBinding;
import com.gophillygo.app.FilterDialog;
import com.gophillygo.app.BR;
import com.gophillygo.app.R;

import cn.nekocode.badge.BadgeDrawable;

/**
 * Abstract list activity with a filter popover opened by a filter toolbar button.
 * Toolbar button updates to display count of filters currently applied.
 */

public abstract class FilterableListActivity extends BaseAttractionActivity
        implements FilterDialog.FilterChangeListener, ToolbarFilterListener {

    private final int toolbarId;

    private Button filterButton;
    private Drawable filterIcon;
    private Toolbar toolbar;

    protected Filter filter;
    private FilterButtonBarBinding filterBinding;

    public FilterableListActivity(int toolbarId) {
        this.toolbarId = toolbarId;
    }

    protected abstract void loadData();
    protected abstract FilterButtonBarBinding setupDataBinding();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        filterBinding = setupDataBinding();
        filterBinding.setListener(this);
        filterBinding.setFilter(filter);

        // set up toolbar
        toolbar = findViewById(toolbarId);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        filterIcon = ContextCompat.getDrawable(this, R.drawable.ic_filter_list_white_24dp);

        // set up filter button
        filter = new Filter();
        filterButton = findViewById(R.id.filter_bar_filter_button);
        filterButton.setOnClickListener(v -> {
            // Need to give the filter dialog a copy of the filter, or toggling the
            // liked / want to go filters in the dialog will toggle the toolbar buttons too soon
            FilterDialog filterDialog = FilterDialog.newInstance(new Filter(filter));
            filterDialog.show(getSupportFragmentManager(), filterDialog.getTag());
        });
    }

    @Override
    public void filterChanged(Filter filter) {
        this.filter = filter;
        filterBinding.setFilter(filter);
        filterBinding.notifyPropertyChanged(BR.filter);
        loadData();

        int setFilterCount = filter.count();
        // Change filter button's left drawable when filters set to either be a badge with the
        // filter count, or the default filter icon, if no filters set.
        if (setFilterCount > 0) {
            Drawable filterDrawable = new BadgeDrawable.Builder()
                    .type(BadgeDrawable.TYPE_ONLY_ONE_TEXT)
                    .badgeColor(ContextCompat.getColor(this, R.color.color_white))
                    .textColor(ContextCompat.getColor(this, R.color.color_primary))
                    .text1(String.valueOf(setFilterCount))
                    .build();
            filterButton.setCompoundDrawablesWithIntrinsicBounds(filterDrawable, null, null, null);
        } else {
            filterButton.setCompoundDrawablesWithIntrinsicBounds(filterIcon, null, null, null);
        }
        String filterTitle = getResources()
                .getQuantityString(R.plurals.filter_button_title, setFilterCount);
        filterButton.setText(filterTitle);

    }

    @Override
    public void toggleLiked() {
        filter.setLiked(filterBinding.filterBarLikedButton.isChecked());
        filterChanged(filter);
    }

    @Override
    public void toggleWantToGo() {
        filter.setWantToGo(filterBinding.filterBarWantToGoButton.isChecked());
        filterChanged(filter);
    }

}
