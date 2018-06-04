package com.gophillygo.app.tasks;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
import dagger.android.AndroidInjection;

public class GeofenceTransitionBroadcastReceiver extends BroadcastReceiver {

    private static final String LOG_LABEL = "GeofenceTransitionBR";
    @Override
    public void onReceive(Context context, Intent intent) {
        AndroidInjection.inject(this, context);

        Log.d(LOG_LABEL, "Received geofence transition event");
        // Start a worker to send notifications from a background thread.
        OneTimeWorkRequest.Builder workRequestBuilder = new OneTimeWorkRequest.Builder(GeofenceTransitionWorker.class);
        workRequestBuilder.setInputData(getGeofenceData(intent));
        workRequestBuilder.setInitialDelay(0, TimeUnit.SECONDS);
        // TODO: set constraints and backoff on builder
        WorkRequest workRequest = workRequestBuilder.build();
        WorkManager.getInstance().enqueue(workRequest);
    }

    private Data getGeofenceData(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        Data.Builder builder = new Data.Builder();

        Boolean hasError = geofencingEvent.hasError();
        builder.putBoolean(GeofenceTransitionWorker.HAS_ERROR_KEY, hasError);

        if (!geofencingEvent.hasError()) {
            int geofenceTransition = geofencingEvent.getGeofenceTransition();
            builder.putInt(GeofenceTransitionWorker.TRANSITION_KEY, geofenceTransition);

            if (geofenceTransition == AddGeofenceWorker.GEOFENCE_ENTER_TRIGGER ||
                    geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

                List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
                int numGeofences = triggeringGeofences.size();
                String[] geofences = new String[numGeofences];
                String[] geofenceNames = new String[numGeofences];
                double[] geofenceLatitudes = new double[numGeofences];
                double[] geofenceLongitudes = new double[numGeofences];

                if (intent.hasExtra(AddGeofenceWorker.GEOFENCE_NAMES_KEY) &&
                        intent.hasExtra(AddGeofenceWorker.GEOFENCE_LABELS_KEY) &&
                        intent.hasExtra(AddGeofenceWorker.LATITUDES_KEY) &&
                        intent.hasExtra(AddGeofenceWorker.LONGITUDES_KEY)) {

                    // get full set of info for places that are flagged for geofencing
                    String[] labels = intent.getStringArrayExtra(AddGeofenceWorker.GEOFENCE_LABELS_KEY);
                    String[] names = intent.getStringArrayExtra(AddGeofenceWorker.GEOFENCE_NAMES_KEY);
                    double[] latitudes = intent.getDoubleArrayExtra(AddGeofenceWorker.LATITUDES_KEY);
                    double[] longitudes = intent.getDoubleArrayExtra(AddGeofenceWorker.LONGITUDES_KEY);

                    // map the geofence string IDs to their index in the full value arrays
                    Map<String, Integer> geofenceNameMap = new HashMap<>(labels.length);
                    for (int i = 0; i < labels.length; i++) {
                        geofenceNameMap.put(labels[i], i);
                    }

                    int i = 0;
                    for (Geofence fence : triggeringGeofences) {
                        String fenceId = fence.getRequestId();
                        Log.d(LOG_LABEL, "Handling transition for geofence " + fenceId);
                        try {
                            int index = geofenceNameMap.get(fenceId);
                            geofences[i] = fenceId;
                            geofenceNames[i] = names[index];
                            geofenceLatitudes[i] = latitudes[index];
                            geofenceLongitudes[i] = longitudes[index];
                            i++;
                        } catch (NullPointerException npe) {
                            // FIXME: why does this happen sometimes?
                            Log.e(LOG_LABEL, "Null pointer exception attempting to find geofence in map");
                            Log.e(LOG_LABEL, "Fence ID: " + fenceId);
                            for (String label: labels) {
                                Log.d(LOG_LABEL, "Found label " + label);
                            }
                        }
                    }

                    builder.putStringArray(GeofenceTransitionWorker.TRIGGERING_GEOFENCES, geofences);
                    builder.putStringArray(AddGeofenceWorker.GEOFENCE_NAMES_KEY, geofenceNames);
                    builder.putDoubleArray(AddGeofenceWorker.LATITUDES_KEY, geofenceLatitudes);
                    builder.putDoubleArray(AddGeofenceWorker.LONGITUDES_KEY, geofenceLongitudes);
                } else {
                    Log.e(LOG_LABEL, "Broadcast intent is missing the geofence labels and/or names");
                }
            }
        } else {
            builder.putInt(GeofenceTransitionWorker.ERROR_CODE_KEY, geofencingEvent.getErrorCode());
        }

        return builder.build();
    }
}
