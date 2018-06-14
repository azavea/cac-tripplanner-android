package com.gophillygo.app.tasks;

import android.support.annotation.NonNull;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationServices;
import com.gophillygo.app.data.models.AttractionInfo;
import com.gophillygo.app.data.models.EventInfo;

import java.util.ArrayList;
import java.util.Arrays;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
import androidx.work.Worker;

import static com.gophillygo.app.tasks.GeofenceTransitionWorker.DESTINATION_PREFIX;
import static com.gophillygo.app.tasks.GeofenceTransitionWorker.EVENT_PREFIX;

public class RemoveGeofenceWorker extends Worker {

    private static final String REMOVE_GEOFENCES_KEY = "remove_geofences";
    private static final String REMOVE_GEOFENCE_TAG = "gpg-remove-geofences";
    private static final String LOG_LABEL = "RemoveGeofenceWorker";

    @NonNull
    @Override
    public WorkerResult doWork() {
        GeofencingClient geofencingClient = LocationServices.getGeofencingClient(getApplicationContext());

        Data data = getInputData();
        if (data.getKeyValueMap().containsKey(REMOVE_GEOFENCES_KEY)) {
            String[] removeGeofences = data.getStringArray(REMOVE_GEOFENCES_KEY);
            Log.d(LOG_LABEL, "Going to remove " + removeGeofences.length + " geofences");
            geofencingClient.removeGeofences(new ArrayList<>(Arrays.asList(removeGeofences))).addOnSuccessListener(aVoid -> {
                Log.d(LOG_LABEL, removeGeofences.length + " geofence(s) removed successfully");
                Crashlytics.log("Removed geofences");
            }).addOnFailureListener(e -> {
                String errorMsg = "Failed to remove " + removeGeofences.length + " geofences.";
                Log.d(LOG_LABEL, errorMsg);
                Crashlytics.log(errorMsg);
                Crashlytics.logException(e);
            });
            return WorkerResult.SUCCESS;
        } else {
            String errorMsg = "Did not receive data for geofences to remove";
            Log.e(LOG_LABEL, errorMsg);
            Crashlytics.log(errorMsg);
            return WorkerResult.FAILURE;
        }
    }

    /**
     * Start a worker to remove a single geofence with the given place ID.
     *
     * @param geofenceId ID of the geofenced destination to stop geofencing
     */
    public static void removeOneGeofence(String geofenceId) {
        String[] geofences = {geofenceId};

        Data data = new Data.Builder()
                .putStringArray(REMOVE_GEOFENCES_KEY, geofences)
                .build();

        Log.d(LOG_LABEL, "removeOneGeofence");

        OneTimeWorkRequest.Builder workRequestBuilder = new OneTimeWorkRequest.Builder(RemoveGeofenceWorker.class);
        workRequestBuilder.setInputData(data);
        workRequestBuilder.addTag(REMOVE_GEOFENCE_TAG);
        // TODO: set constraints and backoff on builder
        WorkRequest workRequest = workRequestBuilder.build();
        WorkManager.getInstance().enqueue(workRequest);
        Log.d(LOG_LABEL, "Enqueued new work request to remove one geofence");
    }

    public static void removeOneGeofence(AttractionInfo info) {
        String prefix = info instanceof EventInfo ? EVENT_PREFIX : DESTINATION_PREFIX;
        String id = String.valueOf(info.getAttraction().getId());
        removeOneGeofence(prefix + id);
    }
}
