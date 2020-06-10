package org.gophillygo.app.activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.cursoradapter.widget.CursorAdapter;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.gophillygo.app.data.DestinationViewModel;
import org.gophillygo.app.data.models.AttractionInfo;
import org.gophillygo.app.data.models.Destination;
import org.gophillygo.app.data.models.DestinationInfo;
import org.gophillygo.app.data.models.DestinationLocation;
import org.gophillygo.app.data.models.EventInfo;
import org.gophillygo.app.data.networkresource.Status;
import org.gophillygo.app.di.GpgViewModelFactory;
import org.gophillygo.app.tasks.AddGeofenceWorker;
import org.gophillygo.app.tasks.AddRemoveGeofencesBroadcastReceiver;
import org.gophillygo.app.tasks.RemoveGeofenceWorker;
import org.gophillygo.app.utils.GpgLocationUtils;
import org.gophillygo.app.utils.UserUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

/**
 * Base activity that requests last known location and destination data when opened;
 * if either change, updates the distances to the destinations and calls
 * `locationsOrDestinationsChanged`.
 */
public abstract class BaseAttractionActivity extends AppCompatActivity
        implements GpgLocationUtils.LocationUpdateListener {

    private static final String LOG_LABEL = "BaseAttractionActivity";
    protected static final String DUMMY_LOCATION_PROVIDER = "gophillygo";

    private static final float METERS_TO_MILES = 0.000621371192f;

    private static final int MAX_DISTANCE_TO_USE_LOCATION_METERS = 500 * 1000;

    // maximum number of nearby destinations to show in carousel
    protected static final int NEAREST_DESTINATION_COUNT = 8;

    // City Hall
    private static final double DEFAULT_LATITUDE = 39.954888;
    private static final double DEFAULT_LONGITUDE = -75.163206;
    private static Location defaultLocation;
    private static final int SUGGEST_COLUMN_INDEX = 3;

    private Location currentLocation;
    private boolean locationHasChanged = false; // true if distances to new location not yet set

    protected List<DestinationInfo> destinationInfos;
    private List<DestinationInfo> nearestDestinations;

    protected String userUuid;

    @Inject
    GpgViewModelFactory viewModelFactory;
    @SuppressWarnings("WeakerAccess")
    DestinationViewModel viewModel;

    protected void addOrRemoveGeofence(AttractionInfo info, Boolean haveExistingGeofence, Boolean settingGeofence) {
        if (settingGeofence) {
            if (haveExistingGeofence) {
                Log.d(LOG_LABEL, "No change to geofence");
                return;
            }
            // add geofence
            Log.d(LOG_LABEL, "Add attraction geofence");
            if (info instanceof EventInfo) {
                AddRemoveGeofencesBroadcastReceiver.addOneGeofence((EventInfo)info);
            } else if (info instanceof DestinationInfo) {
                AddRemoveGeofencesBroadcastReceiver.addOneGeofence(((DestinationInfo) info).getDestination());
            }

        } else if (haveExistingGeofence) {
            Log.e(LOG_LABEL, "Removing attraction geofence");
            RemoveGeofenceWorker.removeOneGeofence(info);
        }
    }


    private void setDefaultLocation() {
        Log.w(LOG_LABEL, "Using City Hall as default location");
        currentLocation = defaultLocation;
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

        // Initialize Firebase Crashlytics crash and usage data logging.
        // Disable if user setting turned off
        boolean enableAnalytics = !UserUtils.isFabricDisabled(this);
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(enableAnalytics);

        defaultLocation = new Location(DUMMY_LOCATION_PROVIDER);
        defaultLocation.setLatitude(DEFAULT_LATITUDE);
        defaultLocation.setLongitude(DEFAULT_LONGITUDE);

        fetchLastLocationOrUseDefault();

        viewModel = new ViewModelProvider(this, viewModelFactory)
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

            // Get or create unique, random UUID for app install for posting user flags
            userUuid = UserUtils.getUserUuid(this);
            FirebaseCrashlytics.getInstance().setUserId(userUuid);
        });
    }

    @NonNull
    private List<DestinationInfo> findNearestDestinations() {

        // getCurrentLocation will set to default if null
        if (getCurrentLocation() == null || destinationInfos == null || destinationInfos.isEmpty()) {
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
                Log.d(LOG_LABEL, "Have distances and location unchanged; not updating distances");
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

        float distance = location.distanceTo(defaultLocation);

        if (distance > MAX_DISTANCE_TO_USE_LOCATION_METERS) {
            Log.d(LOG_LABEL, "User is far away from Philly; use default location instead of actual. Distance from City Hall: " + distance);
            setDefaultLocation();
        } else {
            Log.d(LOG_LABEL, "Location is in range. Distance from City Hall: " + distance);
            currentLocation = location;
        }

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

    public Location getCurrentLocation() {
        if (currentLocation == null) {
            setDefaultLocation();
            locationHasChanged = true;
        }
        return currentLocation;
    }

    /**
     * Handle the location services permission request initiated, if needed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == GpgLocationUtils.PERMISSION_REQUEST_ID) {
            if (grantResults.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(LOG_LABEL, "Re-requesting location after getting fine location permissions");
                    fetchLastLocationOrUseDefault();
                } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    GpgLocationUtils.displayPermissionRequestRationale(getApplicationContext());
                }
            }
        } else if (requestCode == GpgLocationUtils.BACKGROUND_PERMISSION_REQUEST_ID) {
            if (grantResults.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(LOG_LABEL, "Re-requesting location after getting background location permissions");
                    fetchLastLocationOrUseDefault();
                } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    GpgLocationUtils.displayPermissionRequestRationale(getApplicationContext());
                }
            }
        } else if (requestCode == GpgLocationUtils.LOCATION_SETTINGS_REQUEST_ID) {
            if (grantResults.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(LOG_LABEL, "Re-requesting location after getting location network permissions");
                    fetchLastLocationOrUseDefault();
                    Log.d(LOG_LABEL, "Attempting to register geofences from database again");
                    Intent intent = new Intent(getApplicationContext(), AddRemoveGeofencesBroadcastReceiver.class);
                    intent.setAction(AddGeofenceWorker.ACTION_GEOFENCE_TRANSITION);
                    sendBroadcast(intent);
                } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Log.w(LOG_LABEL, "Location network permissions not updated; geofencing may not work");
                }
            }
        }
    }

    /**
     * Set up a search widget in the activity app bar.
     *
     * @param menu Options menu
     * @param searchItem Item in the options menu for searching
     */
    protected void setupSearch(Menu menu, @IdRes int searchItem) {
        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager)getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(searchItem).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(Objects.requireNonNull(searchManager).getSearchableInfo(getComponentName()));

        // handle opening detail intent from search
        // https://developer.android.com/reference/android/widget/SearchView.OnSuggestionListener
        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            final CursorAdapter adapter = searchView.getSuggestionsAdapter();

            @Override
            public boolean onSuggestionSelect(int position) {
                Log.d(LOG_LABEL, "onSuggestionSelect " + position);
                goToAttractionForPosition(adapter, position);
                return true;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                Log.d(LOG_LABEL, "onSuggestionClick " + position);
                goToAttractionForPosition(adapter, position);
                return true;
            }
        });
    }

    protected void goToAttractionForPosition(CursorAdapter adapter, int position) {
        long itemId = adapter.getItemId(position);
        Cursor cursor = adapter.getCursor();
        cursor.moveToPosition(position);
        int isEvent = cursor.getInt(SUGGEST_COLUMN_INDEX);
        if (isEvent == 0) {
            goToPlace(itemId);
        } else {
            goToEvent(itemId);
        }
    }

    protected void goToPlace(long detailId) {
        Log.d(LOG_LABEL, "going to detail view for place ID " + detailId);
        Intent intent = new Intent(this, PlaceDetailActivity.class);
        intent.putExtra(PlaceDetailActivity.DESTINATION_ID_KEY, detailId);
        startActivity(intent);
    }

    protected void goToEvent(long detailId) {
        Log.d(LOG_LABEL, "going to detail view for event ID " + detailId);
        Intent intent = new Intent(this, EventDetailActivity.class);
        intent.putExtra(EventDetailActivity.EVENT_ID_KEY, detailId);
        startActivity(intent);
    }
}
