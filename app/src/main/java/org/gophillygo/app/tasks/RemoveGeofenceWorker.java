package org.gophillygo.app.tasks;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.gophillygo.app.data.models.AttractionInfo;
import org.gophillygo.app.data.models.EventInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class RemoveGeofenceWorker extends Worker {

    public static final String REMOVE_GEOFENCES_KEY = "remove_geofences";
    public static final String REMOVE_GEOFENCE_TAG = "gpg-remove-geofences";
    private static final String LOG_LABEL = "RemoveGeofenceWorker";

    // event identifier used for custom Crashlytics event to note a geofence was removed
    private static final String REMOVE_GEOFENCE_EVENT = "remove_geofence";

    public RemoveGeofenceWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        GeofencingClient geofencingClient = LocationServices.getGeofencingClient(getApplicationContext());

        Data data = getInputData();
        if (data.getKeyValueMap().containsKey(REMOVE_GEOFENCES_KEY)) {
            String[] removeGeofences = data.getStringArray(REMOVE_GEOFENCES_KEY);
            Log.d(LOG_LABEL, "Going to remove " + Objects.requireNonNull(removeGeofences).length + " geofences");
            geofencingClient.removeGeofences(new ArrayList<>(Arrays.asList(removeGeofences))).addOnSuccessListener(aVoid -> {
                Log.d(LOG_LABEL, removeGeofences.length + " geofence(s) removed successfully");
                FirebaseCrashlytics.getInstance().log(REMOVE_GEOFENCE_EVENT);
            }).addOnFailureListener(e -> {
                String errorMsg = "Failed to remove " + removeGeofences.length + " geofences.";
                Log.d(LOG_LABEL, errorMsg);
                FirebaseCrashlytics.getInstance().log(errorMsg);
                FirebaseCrashlytics.getInstance().recordException(e);
            });
            return Result.success();
        } else {
            String errorMsg = "Did not receive data for geofences to remove";
            Log.e(LOG_LABEL, errorMsg);
            FirebaseCrashlytics.getInstance().log(errorMsg);
            return Result.failure();
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
        WorkRequest workRequest = workRequestBuilder.build();
        WorkManager.getInstance().enqueue(workRequest);
        Log.d(LOG_LABEL, "Enqueued new work request to remove one geofence");
    }

    public static void removeOneGeofence(AttractionInfo info) {
        String prefix = info instanceof EventInfo ? GeofenceTransitionWorker.EVENT_PREFIX : GeofenceTransitionWorker.DESTINATION_PREFIX;
        String id = String.valueOf(info.getAttraction().getId());
        removeOneGeofence(prefix + id);
    }
}
