package com.gophillygo.app.activities;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.gophillygo.app.data.DestinationViewModel;
import com.gophillygo.app.data.models.Destination;
import com.gophillygo.app.data.models.DestinationInfo;
import com.gophillygo.app.data.models.DestinationLocation;
import com.gophillygo.app.data.networkresource.Status;
import com.gophillygo.app.di.GpgViewModelFactory;
import com.gophillygo.app.utils.GpgLocationUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Base activity that requests last known location and destination data when opened;
 * if either change, updates the distances to the destinations and calls
 * `locationOrDestinationsChanged`.
 */
public abstract class BaseAttractionActivity extends AppCompatActivity implements GpgLocationUtils.LocationUpdateListener {

    private static final String LOG_LABEL = "BaseAttractionActivity";
    private static final String DUMMY_LOCATION_PROVIDER = "gophillygo";

    private static final float METERS_TO_MILES = 0.000621371192f;

    // maximum number of nearby destinations to show in carousel
    protected static final int NEAREST_DESTINATION_COUNT = 8;

    // City Hall
    private static final double DEFAULT_LATITUDE = 39.954888;
    private static final double DEFAULT_LONGITUDE = -75.163206;

    private Location currentLocation;
    private boolean locationHasChanged = false; // true if distances to new location not yet set

    protected List<DestinationInfo> destinationInfos;
    private List<DestinationInfo> nearestDestinations;

    @Inject
    GpgViewModelFactory viewModelFactory;
    @SuppressWarnings("WeakerAccess")
    DestinationViewModel viewModel;


    private void setDefaultLocation() {
        Log.w(LOG_LABEL, "Using City Hall as default location");
        currentLocation = new Location(DUMMY_LOCATION_PROVIDER);
        currentLocation.setLatitude(DEFAULT_LATITUDE);
        currentLocation.setLongitude(DEFAULT_LONGITUDE);
    }

    private void fetchLastLocationOrUseDefault() {
        // request location, and if request fails, use default
        if (!GpgLocationUtils.getLastKnownLocation(new WeakReference<>(this))) {
            setDefaultLocation();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fetchLastLocationOrUseDefault();

        viewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(DestinationViewModel.class);
        viewModel.getDestinations().observe(this, destinationResource -> {
            // shouldn't happen
            if (destinationResource == null) {
                Log.e(LOG_LABEL, "No ApiResponse wrapper returned");
                return;
            }

            // if querying from server, state may update with LOADING status before it finishes
            if (!destinationResource.status.equals(Status.SUCCESS)) {
                return;
            }

            if (destinationResource.data == null || destinationResource.data.isEmpty()) {
                Log.e(LOG_LABEL, "Destination query returned, but found no results");
                return;
            }

            destinationInfos = destinationResource.data;
            Log.d(LOG_LABEL, "Got destination data");
            nearestDestinations = findNearestDestinations();
            locationOrDestinationsChanged();
        });
    }

    @NonNull
    private List<DestinationInfo> findNearestDestinations() {

        if (currentLocation == null || destinationInfos == null || destinationInfos.isEmpty()) {
            return new ArrayList<>();
        }

        // set distance to each location, if not set already
        int totalDestinations = destinationInfos.size();

        // check if data needs to be updated: only if destinations not set yet, or location changed
        boolean update = true;
        DestinationInfo firstDestination = destinationInfos.get(0);
        if (firstDestination.getDestination().getDistance() > 0) {
            // have distances; only update if location changed
            if (!locationHasChanged) {
                Log.d(LOG_LABEL, "Have distances and location unchanged; not updating destinations");
                update = false;
            }
        }

        if (update) {
            for (DestinationInfo info : destinationInfos) {
                Destination dest = info.getDestination();
                Location location = new Location(DUMMY_LOCATION_PROVIDER);
                DestinationLocation coordinates = dest.getLocation();
                location.setLatitude(coordinates.getY());
                location.setLongitude(coordinates.getX());
                float distanceInMeters = currentLocation.distanceTo(location);
                dest.setDistance(distanceInMeters * METERS_TO_MILES);
            }

            // now distances have been updated, unset flag for need to update
            locationHasChanged = false;
            Log.d(LOG_LABEL, "updating destinations with distances");
            viewModel.updateMultipleDestinations(destinationInfos);
        }

        // return the nearest destinations
        int numDestinations = NEAREST_DESTINATION_COUNT;
        if (totalDestinations < numDestinations) {
            numDestinations = totalDestinations;
        }

        return destinationInfos.subList(0, numDestinations - 1);
    }

    /**
     * Override to listen to changes to data: either location or destinations source data.
     */
    public void locationOrDestinationsChanged() { }

    /**
     * Callback for {@link GpgLocationUtils.LocationUpdateListener}
     *
     * @param location Coordinates of last known location for the device.
     */
    @Override
    public void locationFound(Location location) {

        if (location == null || location.equals(currentLocation)) {
            Log.d(LOG_LABEL, "Location null, or unchanged.");
            return;
        }

        Log.d(LOG_LABEL, "location found: " + location.toString());
        currentLocation = location;
        locationHasChanged = true;
        nearestDestinations = findNearestDestinations();
        locationOrDestinationsChanged();
    }

    /**
     * Get a destination by offset in list of destinations ordered by distance.
     *
     * @param position Offset of destination in list
     */
    public Destination getNearestDestination(int position) {
        try {
            return nearestDestinations.get(position).getDestination();
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    public int getNearestDestinationSize() {
        if (nearestDestinations == null) {
            return 0;
        }
        return nearestDestinations.size();
    }

    /**
     * Handle the location services permission request initiated, if needed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == GpgLocationUtils.PERMISSION_REQUEST_ID) {
            if (grantResults.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(LOG_LABEL, "Re-requesting location after getting permissions");
                    fetchLastLocationOrUseDefault();
                } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    GpgLocationUtils.displayPermissionRequestRationale(getApplicationContext());
                }
            }
        }
    }
}
