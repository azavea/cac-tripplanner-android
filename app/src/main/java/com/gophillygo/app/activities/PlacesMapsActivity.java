package com.gophillygo.app.activities;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.gophillygo.app.R;
import com.gophillygo.app.data.DestinationViewModel;
import com.gophillygo.app.data.models.DestinationInfo;
import com.gophillygo.app.data.models.DestinationLocation;
import com.gophillygo.app.data.models.EventInfo;
import com.gophillygo.app.data.networkresource.Resource;
import com.gophillygo.app.data.networkresource.Status;
import com.gophillygo.app.di.GpgViewModelFactory;

import java.util.List;

import javax.inject.Inject;

public class PlacesMapsActivity extends MapsActivity<DestinationInfo> {

    private static final String LOG_LABEL = "PlacesMapsActivity";

    @Override
    public void onMapReady(GoogleMap googleMap) {
        super.onMapReady(googleMap);
    }

    @Override
    public void locationOrDestinationsChanged() {
        attractions = destinationInfos;
        loadData();
    }

    public boolean filterMatches(DestinationInfo attractionInfo) {
        return filter.matches(attractionInfo);
    }
}
