package org.gophillygo.app.activities;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.GoogleMap;
import org.gophillygo.app.R;
import org.gophillygo.app.data.EventViewModel;
import org.gophillygo.app.data.models.EventInfo;
import org.gophillygo.app.data.networkresource.Resource;
import org.gophillygo.app.data.networkresource.Status;
import org.gophillygo.app.databinding.FilterButtonBarBinding;
import org.gophillygo.app.databinding.ActivityEventsMapsBinding;

import org.gophillygo.app.data.models.EventInfo;

import java.util.HashMap;
import java.util.List;

public class EventsMapsActivity extends MapsActivity<EventInfo> {

    private static final String LOG_LABEL = "EventsMapsActivity";

    @SuppressWarnings("WeakerAccess")
    EventViewModel viewModel;

    public EventsMapsActivity() {
        super(R.id.events_map, R.id.events_map_toolbar);
    }

    @SuppressLint("UseSparseArrays")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        super.onMapReady(googleMap);

        viewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(EventViewModel.class);
        LiveData<Resource<List<EventInfo>>> data = viewModel.getEvents();
        data.observe(this, eventsResource -> {
            if (eventsResource != null && eventsResource.status.equals(Status.SUCCESS) &&
                    eventsResource.data != null && !eventsResource.data.isEmpty()) {
                attractions = new HashMap<>(eventsResource.data.size());
                for (EventInfo eventInfo : eventsResource.data) {
                    attractions.put(eventInfo.getAttraction().getId(), eventInfo);
                }
                loadData();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.events_map_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.events_map_action_view_places:
                Log.d(LOG_LABEL, "Selected map events menu item");
                intent = new Intent(this, PlacesMapsActivity.class);
                startActivity(intent);
                break;
            case R.id.events_map_action_map_search:
                Log.d(LOG_LABEL, "Selected map search menu item");
                break;
            case R.id.events_map_action_view_list:
                Log.d(LOG_LABEL, "Selected to go back to list view from map");
                intent = new Intent(this, EventsListActivity.class);
                startActivity(intent);
                break;
            default:
                Log.w(LOG_LABEL, "Unrecognized menu item selected: " + String.valueOf(itemId));
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    protected FilterButtonBarBinding setupDataBinding() {
        ActivityEventsMapsBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_events_maps);
        popupBinding = binding.eventsMapPopupCard;
        popupBinding.setActivity(this);
        return binding.eventsMapFilterButtonBar;
    }

    @Override
    public boolean filterMatches(EventInfo attractionInfo) {
        return filter.matches(attractionInfo);
    }
}
