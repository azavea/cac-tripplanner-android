package com.gophillygo.app;

import android.arch.lifecycle.ViewModelProviders;
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

import com.gophillygo.app.adapters.PlacesListAdapter;
import com.gophillygo.app.data.DestinationViewModel;
import com.gophillygo.app.data.networkresource.Status;
import com.gophillygo.app.di.GpgViewModelFactory;

import javax.inject.Inject;

import cn.nekocode.badge.BadgeDrawable;

public class PlacesListActivity extends AppCompatActivity implements FilterDialog.FilterChangeListener {

    private static final String LOG_LABEL = "PlacesList";

    private LinearLayoutManager layoutManager;
    private RecyclerView placesListView;
    private Toolbar toolbar;
    private Button filterButton;

    @SuppressWarnings("WeakerAccess")
    @Inject
    GpgViewModelFactory viewModelFactory;
    @SuppressWarnings("WeakerAccess")
    DestinationViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places_list);

        // set up toolbar
        toolbar = findViewById(R.id.places_list_toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // set up list of places
        placesListView = findViewById(R.id.places_list_recycler_view);
        layoutManager = new LinearLayoutManager(this);
        placesListView.setLayoutManager(layoutManager);
        viewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(DestinationViewModel.class);
        viewModel.getDestinations().observe(this, destinationResource -> {
            if (destinationResource != null && destinationResource.status.equals(Status.SUCCESS) &&
                    destinationResource.data != null && !destinationResource.data.isEmpty()) {

                PlacesListAdapter adapter = new PlacesListAdapter(this, destinationResource.data);
                placesListView.setAdapter(adapter);
            }
        });

        // set up filter button
        filterButton = findViewById(R.id.places_list_filter_button);
        filterButton.setOnClickListener(v -> {
            FilterDialog filterDialog = new FilterDialog();
            filterDialog.show(getSupportFragmentManager(), filterDialog.getTag());
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.places_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.action_events:
                Log.d(LOG_LABEL, "Selected events menu item");
                break;
            case R.id.action_map:
                Log.d(LOG_LABEL, "Selected map menu item");
                break;
            case R.id.action_search:
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
        Drawable filterDrawable;
        if (setFilterCount > 0) {
            filterDrawable = new BadgeDrawable.Builder()
                    .type(BadgeDrawable.TYPE_ONLY_ONE_TEXT)
                    .badgeColor(ContextCompat.getColor(this, R.color.color_white))
                    .padding(10, 10, 10, 10, 10)
                    .textColor(ContextCompat.getColor(this, R.color.color_primary))
                    .text1(String.valueOf(setFilterCount))
                    .build();
        } else {
            filterDrawable = ContextCompat.getDrawable(this,
                    R.drawable.ic_filter_list_white_24px);
        }

        filterButton.setCompoundDrawablesWithIntrinsicBounds(filterDrawable, null, null, null);
    }
}
