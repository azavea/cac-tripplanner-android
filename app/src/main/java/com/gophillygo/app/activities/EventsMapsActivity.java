package com.gophillygo.app.activities;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.GoogleMap;
import com.gophillygo.app.R;
import com.gophillygo.app.data.EventViewModel;
import com.gophillygo.app.data.models.EventInfo;
import com.gophillygo.app.data.networkresource.Resource;
import com.gophillygo.app.data.networkresource.Status;
import com.gophillygo.app.databinding.FilterButtonBarBinding;
import com.gophillygo.app.databinding.ActivityEventsMapsBinding;

import java.util.List;

public class EventsMapsActivity extends MapsActivity<EventInfo> {

    private static final String LOG_LABEL = "EventsMapsActivity";

    @SuppressWarnings("WeakerAccess")
    EventViewModel viewModel;

    public EventsMapsActivity() {
        super(R.id.events_map, R.id.events_map_toolbar);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        super.onMapReady(googleMap);

        viewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(EventViewModel.class);
        LiveData<Resource<List<EventInfo>>> data = viewModel.getEvents();
        data.observe(this, eventsResource -> {
            if (eventsResource != null && eventsResource.status.equals(Status.SUCCESS) &&
                    eventsResource.data != null && !eventsResource.data.isEmpty()) {
                attractions = eventsResource.data;
                loadData();
                // Remove observer after loading full list so updates to the events flags don't
                // cause unwanted changes to map position
                data.removeObservers(this);
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
                // TODO: #11 implement list/map toggle
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
