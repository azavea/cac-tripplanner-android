package org.gophillygo.app.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;

import org.gophillygo.app.R;

import java.lang.ref.WeakReference;


public class GpgLocationUtils {

    public interface LocationUpdateListener {
        void locationFound(Location location);
    }

    // identifier for device location access request, if runtime prompt necessary
    // request code must be in lower 8 bits
    public static final int PERMISSION_REQUEST_ID = 11;
    public static final int BACKGROUND_PERMISSION_REQUEST_ID = 12;
    public static final int API_AVAILABILITY_REQUEST_ID = 22;
    public static final int LOCATION_SETTINGS_REQUEST_ID = 33;

    private static final int LOCATION_REQUESTS_COUNT = 12;
    private static final int LOCATION_REQUEST_EXPIRATION_DURATION_MS = 10000; // 10s


    private static final String LOG_LABEL = "GpgLocationUtils";

    // Do not instantiate
    private GpgLocationUtils() {
    }

    /**
     * Check if fine location service is available and permissions granted to it. If not,
     * will request permissions or show user what the issue is (i.e., Play services out of date.)
     *
     * @param caller Activity that will handle any system dialog results
     * @return True if fine location service is available and permissions have been granted
     */
    public static boolean checkFineLocationPermissions(WeakReference<Activity> caller) {
        // check for location service availability and status

        // if calling activity already gone, don't bother attempting to prompt for permissions now
        Activity callingActivity = caller.get();
        if (callingActivity == null) {
            return false;
        }

        GoogleApiAvailability gapiAvailability = GoogleApiAvailability.getInstance();
        int availability = gapiAvailability.isGooglePlayServicesAvailable(callingActivity);

        // Show system dialog to explain Play Services issue. `availability` here is the error code.
        if (availability != ConnectionResult.SUCCESS) {
            Dialog errorDialog = gapiAvailability.getErrorDialog(callingActivity, availability, API_AVAILABILITY_REQUEST_ID);
            errorDialog.show();
            return false;
        }

        // in API 29+, extra permission for background access must be requested
        String[] accessPermissions;
        if (Build.VERSION.SDK_INT >= 29) {
            accessPermissions = new String[]{
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            };
        } else {
            accessPermissions = new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
            };
        }

        // in API 23+, permission granting happens at runtime
        if (ActivityCompat.checkSelfPermission(callingActivity,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // in case user has denied location permissions to app previously, tell them why it's needed, then prompt again
            if (ActivityCompat.shouldShowRequestPermissionRationale(callingActivity, Manifest.permission.ACCESS_FINE_LOCATION)) {
                displayPermissionRequestRationale(callingActivity);
                // On subsequent prompts, user will get a "never ask again" option in the dialog
            }

            ActivityCompat.requestPermissions(callingActivity, accessPermissions, PERMISSION_REQUEST_ID);
            return false; // up to the activity to start this service again when permissions granted
        } else {
            // in API 29+, extra permission for background access must be requested separately
            if (Build.VERSION.SDK_INT >=29 &&
                    ActivityCompat.checkSelfPermission(callingActivity,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(callingActivity, new String[]{
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_PERMISSION_REQUEST_ID);
            }
        }

        // Check location settings on API < 28
        // Geofencing requires high accuracy location to be enabled in settings on API < 28.
        // This setting no longer exists on P/28, so do not prompt on 28+.
        // https://developer.android.com/training/location/change-location-settings#get-settings
        if (Build.VERSION.SDK_INT < 28) {
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
            SettingsClient client = LocationServices.getSettingsClient(callingActivity);
            Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
            task.addOnFailureListener(e -> {
                if (e instanceof ResolvableApiException) {
                    try {
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(callingActivity, LOCATION_SETTINGS_REQUEST_ID);
                    } catch (IntentSender.SendIntentException e1) {
                        Log.e(LOG_LABEL, "Failed to prompt user for location settings changes");
                        e1.printStackTrace();
                    }
                } else {
                    Log.e(LOG_LABEL, "Received unresolvable location settings exception.");
                    e.printStackTrace();
                }
            }).addOnSuccessListener(locationSettingsResponse -> {
                LocationSettingsStates states = locationSettingsResponse.getLocationSettingsStates();
                if (!states.isNetworkLocationPresent() || !states.isNetworkLocationUsable()) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    // TODO: a dialog would be easier to read
                    Toast toast = Toast.makeText(callingActivity,
                            callingActivity.getString(R.string.location_network_permission_rationale),
                            Toast.LENGTH_LONG);
                    toast.show();
                    callingActivity.startActivity(intent);
                }
            });
        }

        return true;
    }

    /**
     * Get the last known device location, without requesting to update it or prompting the user
     * for permissions if they haven't been granted.
     *
     * Intended for use by background tasks that do not have an Activity.
     *
     * @param context Calling context
     * @param listener Callback for when location found. Must implement {@link LocationUpdateListener}
     *
     * @return True if permissions have been already granted
     */
    public static boolean getLastKnownLocation(Context context, LocationUpdateListener listener) {
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(context);

        // If permissions haven't been granted already, do not ask for them.
        // This is useful for accessing location from background tasks.
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            client.getLastLocation().addOnCompleteListener(lastLocationTask -> {
                if (lastLocationTask.isSuccessful() && lastLocationTask.getResult() != null) {
                    Log.d(LOG_LABEL, "Found location " + lastLocationTask.getResult());
                    listener.locationFound(lastLocationTask.getResult());
                }
            });

            return true;
        }

        return false;
    }

    /**
     * Check if app has permission and access to device location, and that GPS is present and enabled.
     * If so, start receiving location updates.
     *
     * @param caller Activity that will handle any system dialog results;
     *               must implement LocationUpdateListener.
     * @return True if location updates have been started
     */
    @SuppressLint("MissingPermission")
    public static boolean getLastKnownLocation(WeakReference<Activity> caller) {
        // checkFineLocationPermissions handles permissions, so suppress MissingPermissions here
        if (!checkFineLocationPermissions(caller)) {
            return false;
        } else {
            // if calling activity already gone, don't bother attempting to prompt for permissions now
            Activity callingActivity = caller.get();
            if (callingActivity == null) {
                return false;
            }

            // Have correct permissions and Play services are available; go get location now.
            // Fire off request to get location before getting last known, in case location is
            // stale due to no other app having requested it recently.
            LocationRequest request = LocationRequest.create().setNumUpdates(LOCATION_REQUESTS_COUNT)
                    .setExpirationDuration(LOCATION_REQUEST_EXPIRATION_DURATION_MS)
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(callingActivity);
            client.requestLocationUpdates(request, null)
                    .addOnCompleteListener(callingActivity, task -> {
                        Log.d(LOG_LABEL, "Location request complete; go get last");
                        client.getLastLocation().addOnCompleteListener(callingActivity, lastLocationTask -> {
                            if (lastLocationTask.isSuccessful() && lastLocationTask.getResult() != null) {
                                Log.d(LOG_LABEL, "Got result " + lastLocationTask.getResult());
                                ((LocationUpdateListener)callingActivity).locationFound(lastLocationTask.getResult());
                            }
                        });
                    });
            return true;
        }
    }

    // Call this from activity in `onRequestPermissionsResult` if permission denied
    public static void displayPermissionRequestRationale(Context context) {
        Toast toast = Toast.makeText(context, context.getString(R.string.location_fine_permission_rationale), Toast.LENGTH_LONG);
        toast.show();
    }

}
