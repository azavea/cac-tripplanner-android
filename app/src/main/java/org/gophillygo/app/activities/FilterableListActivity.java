package org.gophillygo.app.activities;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.widget.Button;

import org.gophillygo.app.BR;
import org.gophillygo.app.FilterDialog;
import org.gophillygo.app.R;
import org.gophillygo.app.data.models.Filter;
import org.gophillygo.app.databinding.FilterButtonBarBinding;

import cn.nekocode.badge.BadgeDrawable;

/**
 * Abstract list activity with a filter popover opened by a filter toolbar button.
 * Toolbar button updates to display count of filters currently applied.
 */

public abstract class FilterableListActivity extends BaseAttractionActivity
        implements FilterDialog.FilterChangeListener, ToolbarFilterListener {

    public final static String FILTER_KEY = "filter";

    private static final String LOG_LABEL = "FilterableActivity";

    private final int toolbarId;

    private Button filterButton;
    private Drawable filterIcon;

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
        filter = new Filter();
        filterBinding.setFilter(filter);

        // set up toolbar
        Toolbar toolbar = findViewById(toolbarId);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        filterIcon = ContextCompat.getDrawable(this, R.drawable.ic_filter_list_white_24dp);

        // set up filter button
        filterButton = findViewById(R.id.filter_bar_filter_button);
        filterButton.setOnClickListener(v -> {
            // Need to give the filter dialog a copy of the filter, or toggling the
            // liked / want to go filters in the dialog will toggle the toolbar buttons too soon
            FilterDialog filterDialog = FilterDialog.newInstance(new Filter(filter));
            filterDialog.show(getSupportFragmentManager(), filterDialog.getTag());
        });

        if (getIntent().hasExtra(FILTER_KEY)) {
            setFilter((Filter) getIntent().getSerializableExtra(FILTER_KEY));
        } else if (savedInstanceState != null && savedInstanceState.containsKey(FILTER_KEY)) {
            setFilter((Filter) savedInstanceState.getSerializable(FILTER_KEY));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(FILTER_KEY, filter);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void setFilter(Filter filter) {
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
    }

    @Override
    public void toggleLiked() {
        filter.setLiked(filterBinding.filterBarLikedButton.isChecked());
        setFilter(filter);
    }

    @Override
    public void toggleWantToGo() {
        filter.setWantToGo(filterBinding.filterBarWantToGoButton.isChecked());
        setFilter(filter);
    }
}
