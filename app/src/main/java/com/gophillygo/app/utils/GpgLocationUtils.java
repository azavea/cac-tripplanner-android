package com.gophillygo.app.utils;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.gophillygo.app.R;

import java.lang.ref.WeakReference;


public class GpgLocationUtils {

    public interface LocationUpdateListener {
        void locationFound(Location location);
    }

    // identifier for device location access request, if runtime prompt necessary
    // request code must be in lower 8 bits
    public static final int PERMISSION_REQUEST_ID = 11;
    public static final int API_AVAILABILITY_REQUEST_ID = 22;

    private static final String LOG_LABEL = "GpgLocationUtils";

    // Do not instantiate
    private GpgLocationUtils() {
    }

    /**
     * Check if app has permission and access to device location, and that GPS is present and enabled.
     * If so, start receiving location updates.
     *
     * @return True if location updates have been started
     */
    public static boolean getLastKnownLocation(WeakReference<Activity> caller, LocationUpdateListener listener) {
        // check for location service availability and status

        // if calling activity already gone, don't bother attempting to prompt for permissions now
        Activity callingActivity = caller.get();
        if (callingActivity == null) {
            return false;
        }

        GoogleApiAvailability gapiAvailability = GoogleApiAvailability.getInstance();
        int availability = gapiAvailability.isGooglePlayServicesAvailable(callingActivity);

        if (availability != ConnectionResult.SUCCESS) {
            WeakReference<Activity> activityWeakReference = new WeakReference<>(callingActivity);
            // show system dialog to explain
            showApiErrorDialog(activityWeakReference, gapiAvailability, availability);
            return false;
        }

        // in API 23+, permission granting happens at runtime
        if (ActivityCompat.checkSelfPermission(callingActivity,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // in case user has denied location permissions to app previously, tell them why it's needed, then prompt again
            if (ActivityCompat.shouldShowRequestPermissionRationale(callingActivity, Manifest.permission.ACCESS_FINE_LOCATION)) {
                displayPermissionRequestRationale(callingActivity);
                // On subsequent prompts, user will get a "never ask again" option in the dialog
            }

            ActivityCompat.requestPermissions(callingActivity, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_ID);
            return false; // up to the activity to start this service again when permissions granted
        } else {
            // have correct permissions and Play services are available; go get location now
            LocationRequest request = LocationRequest.create().setNumUpdates(12)
                    .setExpirationDuration(10000)
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(callingActivity);
            client.requestLocationUpdates(request, null)
                    .addOnCompleteListener(callingActivity, task -> {
                        Log.d(LOG_LABEL, "Location request complete; go get last");
                        client.getLastLocation().addOnCompleteListener(callingActivity, lastLocationTask -> {
                            if (lastLocationTask.isSuccessful() && lastLocationTask.getResult() != null) {
                                Log.d(LOG_LABEL, "Got result " + lastLocationTask.getResult());
                                listener.locationFound(lastLocationTask.getResult());
                            }
                        });
                    });
            return true;
        }
    }

    private static void showApiErrorDialog(WeakReference<Activity> caller, GoogleApiAvailability gapiAvailability, int errorCode) {
        final Activity callingActivity = caller.get();
        if (callingActivity == null) {
            return;
        }

        Dialog errorDialog = gapiAvailability.getErrorDialog(callingActivity, errorCode, API_AVAILABILITY_REQUEST_ID);
        errorDialog.show();
    }

    // Call this from activity in `onRequestPermissionsResult` if permission denied
    public static void displayPermissionRequestRationale(Context context) {
        Toast toast = Toast.makeText(context, context.getString(R.string.location_fine_permission_rationale), Toast.LENGTH_LONG);
        toast.show();
    }

}
