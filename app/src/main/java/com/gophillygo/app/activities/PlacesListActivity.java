package com.gophillygo.app.activities;

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

import com.gophillygo.app.R;
import com.gophillygo.app.adapters.PlacesListAdapter;
import com.gophillygo.app.data.models.AttractionFlag;
import com.gophillygo.app.data.models.AttractionInfo;
import com.gophillygo.app.data.models.Destination;
import com.gophillygo.app.data.models.DestinationInfo;
import com.gophillygo.app.databinding.ActivityPlacesListBinding;
import com.gophillygo.app.databinding.FilterButtonBarBinding;
import com.gophillygo.app.tasks.AddGeofencesBroadcastReceiver;
import com.gophillygo.app.tasks.RemoveGeofenceWorker;

import java.util.ArrayList;
import java.util.List;

public class PlacesListActivity extends FilterableListActivity implements
        PlacesListAdapter.AttractionListItemClickListener {

    private static final String LOG_LABEL = "PlacesList";

    private LinearLayoutManager layoutManager;
    private RecyclerView placesListView;
    PlacesListAdapter placesListAdapter;

    public PlacesListActivity() {
        super(R.id.places_list_toolbar);

        // If destinations were loaded before this activity showed, use them immediately.
        if (destinationInfos != null && !destinationInfos.isEmpty()) {
            loadData();
        } else {
            Log.d(LOG_LABEL, "Have no destinations for the places list");
        }
    }

    @Override
    public void locationOrDestinationsChanged() {
        super.locationOrDestinationsChanged();
        if (destinationInfos != null && !destinationInfos.isEmpty()) {
            loadData();
        } else {
            Log.d(LOG_LABEL, "Have no destinations for the places list in locationOrDestinationsChanged");
        }
    }

    /**
     * Go to place detail view when a place in the list clicked.
     *
     * @param position Offset of the position of the list item clicked
     */
    public void clickedAttraction(int position) {
        // Get database ID for place clicked, based on positional offset, and pass it along
        long detailId = placesListView.getAdapter().getItemId(position);
        Intent intent = new Intent(this, PlaceDetailActivity.class);
        intent.putExtra(PlaceDetailActivity.DESTINATION_ID_KEY, detailId);
        startActivity(intent);
    }

    public boolean clickedFlagOption(MenuItem item, AttractionInfo destinationInfo, Integer position) {
        Boolean haveExistingGeofence = destinationInfo.getFlag().getOption()
                .api_name.equals(AttractionFlag.Option.WantToGo.api_name);

        destinationInfo.updateAttractionFlag(item.getItemId());
        viewModel.updateAttractionFlag(destinationInfo.getFlag(), userUuid, getString(R.string.user_flag_post_api_key));
        placesListAdapter.notifyItemChanged(position);

        if (destinationInfo.getFlag().getOption().api_name.equals(AttractionFlag.Option.WantToGo.api_name)) {
            if (haveExistingGeofence) {
                Log.d(LOG_LABEL, "No change to geofence");
                return true; // no change
            }

            // add geofence
            Log.d(LOG_LABEL, "Add geofence from places list");
            AddGeofencesBroadcastReceiver.addOneGeofence((Destination)destinationInfo.getAttraction());
        } else if (haveExistingGeofence) {
            Log.e(LOG_LABEL, "Removing geofence");
            RemoveGeofenceWorker.removeOneGeofence(String.valueOf(destinationInfo.getAttraction().getId()));
        }
	    return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set up list of places
        layoutManager = new LinearLayoutManager(this);
        placesListView = findViewById(R.id.places_list_recycler_view);
    }

    @Override
    protected FilterButtonBarBinding setupDataBinding() {
        ActivityPlacesListBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_places_list);
        return binding.placesListFilterButtonBar;
    }

    @Override
    protected void loadData() {
        Log.d(LOG_LABEL, "loadData");
        List<DestinationInfo> filteredDestinations = getFilteredDestinations();

        TextView noDataView = findViewById(R.id.empty_places_list);
        noDataView.setVisibility(filteredDestinations.isEmpty() ? View.VISIBLE : View.GONE);

        // Reset list adapter if either it isn't set up, or if a filter was applied/removed.
        if (placesListAdapter == null || filteredDestinations.size() != placesListAdapter.getItemCount()) {
            placesListAdapter = new PlacesListAdapter(this, filteredDestinations, this);
            // must set the list before the adapter for the differ to initialize properly
            placesListAdapter.submitList(filteredDestinations);
            placesListView.setAdapter(placesListAdapter);
            placesListView.setLayoutManager(layoutManager);
        } else {
            Log.d(LOG_LABEL, "submit list for diff");
            // Let the AsyncListDiffer find which have changed, and only update their view holders
            // https://developer.android.com/reference/android/support/v7/recyclerview/extensions/ListAdapter
            placesListAdapter.submitList(filteredDestinations);
        }
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
                startActivity(new Intent(this, PlacesMapsActivity.class));
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
    private ArrayList<DestinationInfo> getFilteredDestinations() {
        ArrayList<DestinationInfo> filteredDestinations = new ArrayList<>(destinationInfos.size());
        for (DestinationInfo info : destinationInfos) {
            if (filter.matches(info)) {
                filteredDestinations.add(info);
            }
        }
        return filteredDestinations;
    }
}
