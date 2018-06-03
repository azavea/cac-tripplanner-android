package com.gophillygo.app.tasks;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

public class GeofenceTransitionBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Start a worker to send notifications from a background thread.
        OneTimeWorkRequest.Builder workRequestBuilder = new OneTimeWorkRequest.Builder(GeofenceTransitionWorker.class);
        workRequestBuilder.setInputData(getGeofenceData(intent));
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

            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL ||
                    geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

                List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
                ArrayList<String> geofences = new ArrayList<>(triggeringGeofences.size());
                for (Geofence fence : triggeringGeofences) {
                    geofences.add(fence.getRequestId());
                }

                builder.putStringArray(GeofenceTransitionWorker.TRIGGERING_GEOFENCES,
                        (String[]) geofences.toArray());

            }
        } else {
            builder.putInt(GeofenceTransitionWorker.ERROR_CODE_KEY, geofencingEvent.getErrorCode());
        }

        return builder.build();
    }
}
