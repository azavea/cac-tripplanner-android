package org.gophillygo.app.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.databinding.DataBindingUtil;

import org.gophillygo.app.R;
import org.gophillygo.app.data.models.DestinationInfo;
import org.gophillygo.app.databinding.ActivityPlacesMapsBinding;
import org.gophillygo.app.databinding.FilterButtonBarBinding;

import java.util.HashMap;

public class PlacesMapsActivity extends MapsActivity<DestinationInfo> {

    private static final String LOG_LABEL = "PlacesMapsActivity";

    public PlacesMapsActivity() {
        super(R.id.places_map, R.id.places_map_toolbar);
    }

    @Override
    protected FilterButtonBarBinding setupDataBinding() {
        ActivityPlacesMapsBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_places_maps);
        popupBinding = binding.placesMapPopupCard;
        popupBinding.setActivity(this);
        return binding.placesMapFilterButtonBar;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.places_map_menu, menu);
        setupSearch(menu, R.id.places_map_action_map_search);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        Intent intent;
        if (itemId == R.id.places_map_action_view_events) {
            Log.d(LOG_LABEL, "Selected map events menu item");
            intent = new Intent(this, EventsMapsActivity.class);
            intent.putExtra(FILTER_KEY, filter);
            startActivity(intent);
        } else if (itemId == R.id.places_map_action_view_list) {
            Log.d(LOG_LABEL, "Selected to go back to list view from map");
            intent = new Intent(this, PlacesListActivity.class);
            intent.putExtra(FILTER_KEY, filter);
            startActivity(intent);
        } else if (itemId == R.id.places_map_action_map_search) {
            Log.d(LOG_LABEL, "Selected search menu item");
            super.onSearchRequested();
        } else {
            Log.w(LOG_LABEL, "Unrecognized menu item selected: " + itemId);
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @SuppressLint("UseSparseArrays")
    @Override
    public void locationOrDestinationsChanged() {
        super.locationOrDestinationsChanged();
        if (destinationInfos != null && !destinationInfos.isEmpty()) {
            attractions = new HashMap<>(destinationInfos.size());
            for (DestinationInfo destinationInfo : destinationInfos) {
                attractions.put(destinationInfo.getAttraction().getId(), destinationInfo);
            }
            loadData();
        } else {
            Log.d(LOG_LABEL, "Have no destinations for the places list in locationOrDestinationsChanged");
        }
    }

    @Override
    public boolean filterMatches(DestinationInfo attractionInfo) {
        return filter.matches(attractionInfo);
    }
}
