package com.gophillygo.app.activities;

import com.google.android.gms.maps.GoogleMap;
import com.gophillygo.app.data.models.DestinationInfo;

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
