package com.gophillygo.app;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import com.gophillygo.app.adapters.EventsListAdapter;
import com.gophillygo.app.data.EventViewModel;
import com.gophillygo.app.data.networkresource.Status;
import com.gophillygo.app.di.GpgViewModelFactory;

import javax.inject.Inject;

import cn.nekocode.badge.BadgeDrawable;

public class EventsListActivity extends AppCompatActivity
        implements FilterDialog.FilterChangeListener, EventsListAdapter.EventListItemClickListener {

    private static final String LOG_LABEL = "EventsList";

    private LinearLayoutManager layoutManager;
    private RecyclerView eventsListView;
    private Toolbar toolbar;
    private Button filterButton;
    private Drawable filterIcon;

    @SuppressWarnings("WeakerAccess")
    @Inject
    GpgViewModelFactory viewModelFactory;
    @SuppressWarnings("WeakerAccess")
    EventViewModel viewModel;

    /**
     * Go to place detail view when a place in the list clicked.
     *
     * @param position Offset of the position of the list item clicked
     */
    public void clickedPlace(int position) {
        // Get database ID for place clicked, based on positional offset, and pass it along
        long destinationId = eventsListView.getAdapter().getItemId(position);
        Intent intent = new Intent(this, PlaceDetailActivity.class);
        intent.putExtra(PlaceDetailActivity.DESTINATION_ID_KEY, destinationId);
        startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events_list);

        filterIcon = ContextCompat.getDrawable(this, R.drawable.ic_filter_list_white_24px);

        // set up toolbar
        toolbar = findViewById(R.id.events_list_toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // set up list of places
        layoutManager = new LinearLayoutManager(this);
        viewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(EventViewModel.class);
        viewModel.getEvents().observe(this, destinationResource -> {
            if (destinationResource != null && destinationResource.status.equals(Status.SUCCESS) &&
                    destinationResource.data != null && !destinationResource.data.isEmpty()) {

                eventsListView = findViewById(R.id.events_list_recycler_view);
                EventsListAdapter adapter = new EventsListAdapter(this, destinationResource.data, this);
                eventsListView.setAdapter(adapter);
                eventsListView.setLayoutManager(layoutManager);
            }
        });

        // set up filter button
        filterButton = findViewById(R.id.events_list_filter_button);
        filterButton.setOnClickListener(v -> {
            FilterDialog filterDialog = new FilterDialog();
            filterDialog.show(getSupportFragmentManager(), filterDialog.getTag());
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.events_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.action_event_place:
                Log.d(LOG_LABEL, "Selected event place menu item");
                break;
            case R.id.action_event_map:
                Log.d(LOG_LABEL, "Selected map menu item");
                break;
            case R.id.action_event_search:
                Log.d(LOG_LABEL, "Selected search menu item");
                break;
            default:
                Log.w(LOG_LABEL, "Unrecognized menu item selected: " + String.valueOf(itemId));
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void filtersChanged(int setFilterCount) {
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
}
