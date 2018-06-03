package com.gophillygo.app.tasks;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.gophillygo.app.BuildConfig;

import java.util.Map;

import androidx.work.Data;
import androidx.work.Worker;

public class AddGeofenceWorker extends Worker {

    // Must match action name in broadcast filter in the manifest
    public static final String ACTION_GEOFENCE_TRANSITION = "com.gophillygo.app.tasks.ACTION_GEOFENCE_TRANSITION";

    // When in development (debug) trigger entry immediately
    public static final int GEOFENCE_ENTER_TRIGGER = BuildConfig.DEBUG ?
            GeofencingRequest.INITIAL_TRIGGER_ENTER :
            GeofencingRequest.INITIAL_TRIGGER_DWELL;

    // https://developer.android.com/training/location/geofencing
    // Minimum radius should be at least 100 - 150, more for outdoors areas with no WiFi.
    // Greater values reduce battery consumption.
    private static final int GEOFENCE_RADIUS_METERS = 300;

    // Send alert roughly after device has been in geofence for this long.
    // When we are using the DWELL filter, this is about when we will receive notifications.
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
        builder.setInitialTrigger(GEOFENCE_ENTER_TRIGGER);

        double[] latitudes;
        double[] longitudes;
        String[] geofenceLabels;

        // Get the data for the locations to add as primitive arrays with label and coordinates at
        // matching offsets.
        Data data = getInputData();
        Map<String, Object> map = data.getKeyValueMap();
        if (map.containsKey(LATITUDES_KEY) && map.containsKey(LONGITUDES_KEY) &&
                map.containsKey(GEOFENCE_LABELS_KEY)) {

            latitudes = data.getDoubleArray(LATITUDES_KEY);
            longitudes = data.getDoubleArray(LONGITUDES_KEY);
            geofenceLabels = data.getStringArray(GEOFENCE_LABELS_KEY);
        } else {
            Log.e(LOG_LABEL, "Data missing for geofences to add");
            return  WorkerResult.FAILURE;
        }

        if (latitudes.length != longitudes.length || latitudes.length != geofenceLabels.length) {
            Log.e(LOG_LABEL, "Location data for geofences to add should be arrays of the same length.");
            return WorkerResult.FAILURE;
        }

        for (int i = 0; i < latitudes.length; i++) {
            Log.d(LOG_LABEL, "Adding geofence for " + geofenceLabels[i]);
            builder.addGeofence(new Geofence.Builder()
                    .setCircularRegion(latitudes[i], longitudes[i], GEOFENCE_RADIUS_METERS)
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setRequestId(geofenceLabels[i])
                    .setLoiteringDelay(GEOFENCE_LOITERING_DELAY)
                    .setNotificationResponsiveness(GEOFENCE_RESPONSIVENESS)
                    .setTransitionTypes(GEOFENCE_ENTER_TRIGGER | Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build());
        }

        // Location access permissions prompting is handled by `GpgLocationUtils`.
        Intent intent = new Intent(context, GeofenceTransitionBroadcastReceiver.class);
        intent.setAction(ACTION_GEOFENCE_TRANSITION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                TRANSITION_BROADCAST_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        try {
            geofencingClient.addGeofences(builder.build(), pendingIntent);
            return WorkerResult.SUCCESS;
        } catch (SecurityException ex) {
            Log.e(LOG_LABEL, "Missing permissions to add geofences");
            ex.printStackTrace();
            return WorkerResult.FAILURE;
        } catch (Exception ex) {
            Log.e(LOG_LABEL, "Failed to add geofences");
            ex.printStackTrace();
            return WorkerResult.FAILURE;
        }

    }
}
