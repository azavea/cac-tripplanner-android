package com.gophillygo.app.tasks;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;
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
                String[] geofences = new String[triggeringGeofences.size()];
                int i = 0;
                for (Geofence fence : triggeringGeofences) {
                    geofences[i] = (fence.getRequestId());
                    i++;
                }

                builder.putStringArray(GeofenceTransitionWorker.TRIGGERING_GEOFENCES, geofences);

            }
        } else {
            builder.putInt(GeofenceTransitionWorker.ERROR_CODE_KEY, geofencingEvent.getErrorCode());
        }

        return builder.build();
    }
}
