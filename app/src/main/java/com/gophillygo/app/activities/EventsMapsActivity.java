package com.gophillygo.app.activities;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.gophillygo.app.R;
import com.gophillygo.app.data.EventViewModel;
import com.gophillygo.app.data.models.DestinationInfo;
import com.gophillygo.app.data.models.DestinationLocation;
import com.gophillygo.app.data.models.Event;
import com.gophillygo.app.data.models.EventInfo;
import com.gophillygo.app.data.networkresource.Resource;
import com.gophillygo.app.data.networkresource.Status;

import java.util.List;

public class EventsMapsActivity extends MapsActivity<EventInfo> {

    private static final String LOG_LABEL = "EventsMapsActivity";

    @SuppressWarnings("WeakerAccess")
    EventViewModel viewModel;

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

    public boolean filterMatches(EventInfo attractionInfo) {
        return filter.matches(attractionInfo);
    }
}
