package com.gophillygo.app.tasks;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.gophillygo.app.BuildConfig;

import androidx.work.Data;
import androidx.work.Worker;

public class AddGeofenceWorker extends Worker {

    // https://developer.android.com/training/location/geofencing
    // Minimum radius should be at least 100 - 150, more for outdoors areas with no WiFi.
    // Greater values reduce battery consumption.
    private static final int GEOFENCE_RADIUS_METERS = 300;

    // Send alert roughly after device has been in geofence for this long.
    // Since we are using the DWELL filter, this is about when we will receive notifications.
    // In development (DEBUG build), use no delay.
    private static final int GEOFENCE_LOITERING_DELAY = BuildConfig.DEBUG ? 0 : 300000; // 5 minutes

    // Set responsiveness high to save battery
    private static final int GEOFENCE_RESPONSIVENESS = BuildConfig.DEBUG ? 0 : 300000; // 5 minutes

    private static final String LOG_LABEL = "AddGeofenceWorker";
    private static final int TRANSITION_BROADCAST_REQUEST_CODE = 42;

    public static final String LATITUDES_KEY = "latitudes";
    public static final String LONGITUDES_KEY = "longitudes";
    public static final String GEOFENCE_LABELS_KEY = "geofence_labels";

    @NonNull
    @Override
    public WorkerResult doWork() {
        Context context = getApplicationContext();
        GeofencingClient geofencingClient = LocationServices.getGeofencingClient(context);
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL);

        // Get the data for the locations to add as primitive arrays with label and coordinates at
        // matching offsets.
        Data data = getInputData();
        double[] latitudes = data.getDoubleArray(LATITUDES_KEY);
        double[] longitudes = data.getDoubleArray(LONGITUDES_KEY);
        String[] geofenceLabels = data.getStringArray(GEOFENCE_LABELS_KEY);

        if (latitudes.length != longitudes.length || latitudes.length != geofenceLabels.length) {
            Log.e(LOG_LABEL, "Location data for geofences to add should be arrays of the same length.");
            return WorkerResult.FAILURE;
        }

        for (int i = 0; i < latitudes.length; i++) {
            builder.addGeofence(new Geofence.Builder()
                    .setCircularRegion(latitudes[i], longitudes[i], GEOFENCE_RADIUS_METERS)
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setRequestId(geofenceLabels[i])
                    .setLoiteringDelay(GEOFENCE_LOITERING_DELAY)
                    .setNotificationResponsiveness(GEOFENCE_RESPONSIVENESS)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build());
        }

        // Check permissions here, but do not prompt if they are missing.
        // Location access permissions prompting is handled by `GpgLocationUtils`.
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(context, GeofenceTransitionBroadcastReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                    TRANSITION_BROADCAST_REQUEST_CODE,
                    intent,
                    PendingIntent.FLAG_CANCEL_CURRENT);

            try {
                geofencingClient.addGeofences(builder.build(), pendingIntent);
                return  WorkerResult.SUCCESS;
            } catch (Exception ex) {
                Log.e(LOG_LABEL, "Failed to add geofences");
                Log.e(LOG_LABEL, ex.getMessage());
                return WorkerResult.FAILURE;
            }

        } else {
            Log.e(LOG_LABEL, "Cannot add geofences because permissions are missing");
            return WorkerResult.FAILURE;
        }
    }
}
