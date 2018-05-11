package com.gophillygo.app.activities;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.GoogleMap;
import com.gophillygo.app.R;
import com.gophillygo.app.data.models.DestinationInfo;
import com.gophillygo.app.databinding.FilterButtonBarBinding;
import com.gophillygo.app.databinding.ActivityPlacesMapsBinding;

public class PlacesMapsActivity extends MapsActivity<DestinationInfo> {

    private static final String LOG_LABEL = "PlacesMapsActivity";

    public PlacesMapsActivity() {
        super(R.id.places_map, R.id.places_map_toolbar);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        super.onMapReady(googleMap);
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.places_map_action_view_events:
                Log.d(LOG_LABEL, "Selected map events menu item");
                intent = new Intent(this, EventsMapsActivity.class);
                startActivity(intent);
                break;
            case R.id.places_map_action_map_search:
                Log.d(LOG_LABEL, "Selected map search menu item");
                break;
            case R.id.places_map_action_view_list:
                Log.d(LOG_LABEL, "Selected to go back to list view from map");
                intent = new Intent(this, PlacesListActivity.class);
                startActivity(intent);
                break;
            default:
                Log.w(LOG_LABEL, "Unrecognized menu item selected: " + String.valueOf(itemId));
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void locationOrDestinationsChanged() {
        attractions = destinationInfos;
        loadData();
    }

    @Override
    public boolean filterMatches(DestinationInfo attractionInfo) {
        return filter.matches(attractionInfo);
    }
}
