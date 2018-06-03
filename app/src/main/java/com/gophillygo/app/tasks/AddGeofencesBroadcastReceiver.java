package com.gophillygo.app.tasks;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

/**
 * Add geofences. Expects calling intent to set geofence data as three arrays with
 * labels and coordinates.
 */
public class AddGeofencesBroadcastReceiver extends BroadcastReceiver {

    private static final String LOG_LABEL = "AddGeofenceBroadcast";
    @Override
    public void onReceive(Context context, Intent intent) {
        // Schedule one-time job to set up geofences

        if (intent.hasExtra(AddGeofenceWorker.LATITUDES_KEY) && intent.hasExtra(AddGeofenceWorker.LONGITUDES_KEY)
                && intent.hasExtra(AddGeofenceWorker.GEOFENCE_LABELS_KEY)) {

            double[] latitudes = intent.getDoubleArrayExtra(AddGeofenceWorker.LATITUDES_KEY);
            double[] longitudes = intent.getDoubleArrayExtra(AddGeofenceWorker.LONGITUDES_KEY);
            String[] labels = intent.getStringArrayExtra(AddGeofenceWorker.GEOFENCE_LABELS_KEY);

            Data data = new Data.Builder()
                    .putDoubleArray(AddGeofenceWorker.LATITUDES_KEY, latitudes)
                    .putDoubleArray(AddGeofenceWorker.LONGITUDES_KEY, longitudes)
                    .putStringArray(AddGeofenceWorker.GEOFENCE_LABELS_KEY, labels)
                    .build();

            // Start a worker to send notifications from a background thread.
            OneTimeWorkRequest.Builder workRequestBuilder = new OneTimeWorkRequest.Builder(GeofenceTransitionWorker.class);
            workRequestBuilder.setInputData(data);
            // TODO: set constraints and backoff on builder
            WorkRequest workRequest = workRequestBuilder.build();
            WorkManager.getInstance().enqueue(workRequest);
        } else {
            Log.e(LOG_LABEL, "Missing data to add geofences.");
        }
    }
}
