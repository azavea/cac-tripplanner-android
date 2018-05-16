package com.gophillygo.app.activities;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.gophillygo.app.BR;
import com.gophillygo.app.R;
import com.gophillygo.app.adapters.PlacesListAdapter;
import com.gophillygo.app.data.DestinationViewModel;
import com.gophillygo.app.data.models.AttractionInfo;
import com.gophillygo.app.data.models.DestinationInfo;
import com.gophillygo.app.data.networkresource.Resource;
import com.gophillygo.app.data.networkresource.Status;
import com.gophillygo.app.databinding.ActivityPlacesListBinding;
import com.gophillygo.app.databinding.FilterButtonBarBinding;
import com.gophillygo.app.di.GpgViewModelFactory;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;


public class PlacesListActivity extends FilterableListActivity implements
        PlacesListAdapter.AttractionListItemClickListener {

    private static final String LOG_LABEL = "PlacesList";

    private LinearLayoutManager layoutManager;
    private RecyclerView placesListView;
    private List<DestinationInfo> destinations;

    @SuppressWarnings("WeakerAccess")
    @Inject
    GpgViewModelFactory viewModelFactory;
    @SuppressWarnings("WeakerAccess")
    DestinationViewModel viewModel;

    public PlacesListActivity() {
        super(R.id.places_list_toolbar);
    }

    /**
     * Go to place detail view when a place in the list clicked.
     *
     * @param position Offset of the position of the list item clicked
     */
    public void clickedAttraction(int position) {
        // Get database ID for place clicked, based on positional offset, and pass it along
        long destinationId = placesListView.getAdapter().getItemId(position);
        Intent intent = new Intent(this, PlaceDetailActivity.class);
        intent.putExtra(PlaceDetailActivity.DESTINATION_ID_KEY, destinationId);
        startActivity(intent);
    }

    public boolean clickedFlagOption(MenuItem item, AttractionInfo destinationInfo, Integer position) {
        destinationInfo.updateAttractionFlag(item.getItemId());
        viewModel.updateAttractionFlag(destinationInfo.getFlag());
        placesListView.getAdapter().notifyItemChanged(position);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set up list of places
        layoutManager = new LinearLayoutManager(this);
        viewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(DestinationViewModel.class);

        LiveData<Resource<List<DestinationInfo>>> data = viewModel.getDestinations();
        data.observe(this, destinationResource -> {
            if (destinationResource != null && destinationResource.status.equals(Status.SUCCESS) &&
                    destinationResource.data != null && !destinationResource.data.isEmpty()) {
                destinations = destinationResource.data;
                loadData();
                // Remove observer after loading full list so updates to the destination flags don't
                // cause unwanted changes to scroll position
                data.removeObservers(this);
            }
        });
    }

    @Override
    protected FilterButtonBarBinding setupDataBinding() {
        ActivityPlacesListBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_places_list);
        return binding.placesListFilterButtonBar;
    }

    @Override
    protected void loadData() {
        List<DestinationInfo> filteredDestinations = getFilteredDestinations();

        TextView noDataView = findViewById(R.id.empty_places_list);
        noDataView.setVisibility(filteredDestinations.isEmpty() ? View.VISIBLE : View.GONE);

        placesListView = findViewById(R.id.places_list_recycler_view);
        PlacesListAdapter adapter = new PlacesListAdapter(this, filteredDestinations, this);
        placesListView.setAdapter(adapter);
        placesListView.setLayoutManager(layoutManager);
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
                startActivity(new Intent(this, MapsActivity.class));
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

    @NonNull
    private List<DestinationInfo> getFilteredDestinations() {
        List<DestinationInfo> filteredDestinations = new ArrayList<>(destinations.size());
        for (DestinationInfo info : destinations) {
            if (filter.matches(info)) {
                filteredDestinations.add(info);
            }
        }
        return filteredDestinations;
    }

}