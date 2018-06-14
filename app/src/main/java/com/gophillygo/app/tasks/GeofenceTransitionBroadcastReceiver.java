package com.gophillygo.app.tasks;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.gophillygo.app.data.DestinationDao;
import com.gophillygo.app.data.EventDao;
import com.gophillygo.app.data.models.Destination;
import com.gophillygo.app.data.models.DestinationLocation;
import com.gophillygo.app.data.models.EventInfo;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
import dagger.android.AndroidInjection;

import static com.gophillygo.app.tasks.GeofenceTransitionWorker.DESTINATION_PREFIX;
import static com.gophillygo.app.tasks.GeofenceTransitionWorker.EVENT_PREFIX;

public class GeofenceTransitionBroadcastReceiver extends BroadcastReceiver {

    @Inject
    DestinationDao destinationDao;

    @Inject
    EventDao eventDao;

    private static final String LOG_LABEL = "GeofenceTransitionBR";
    @SuppressLint("StaticFieldLeak")
    @Override
    public void onReceive(Context context, Intent intent) {
        AndroidInjection.inject(this, context);

        Log.d(LOG_LABEL, "Received geofence transition event");
        // Start a worker to send notifications from a background thread.

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
                final int numGeofences = triggeringGeofences.size();

                // Query database on background thread by taking broadcast receiver async briefly
                // https://developer.android.com/guide/components/broadcasts
                final PendingResult pendingResult = goAsync();
                new AsyncTask<Void, Void, Data>() {
                    @Override
                    protected void onPostExecute(Data data) {
                        startWorker(data);
                        // Need to release BroadcastReceiver since have gone async
                        pendingResult.finish();
                    }

                    @Override
                    protected void onCancelled() {
                        pendingResult.abortBroadcast();
                    }

                    @Override
                    protected Data doInBackground(Void... voids) {

                        // send arrays of combined values for destinations and events
                        double[] latitudes = new double[numGeofences];
                        double[] longitudes = new double[numGeofences];
                        String[] labels = new String[numGeofences];
                        String[] names = new String[numGeofences];

                        for (int i = 0; i < numGeofences; i++) {
                            Geofence fence = triggeringGeofences.get(i);
                            String fenceId = fence.getRequestId();
                            Log.d(LOG_LABEL, "Handling transition for geofence " + fenceId);
                            // Geofence string ID is "d" for destination or "e" for event, followed by the
                            // destination or event integer ID.
                            int geofenceId = Integer.valueOf(fenceId.substring(1));
                            boolean isEvent = fenceId.startsWith(EVENT_PREFIX);

                            // query for each event or destination synchronously from the database
                            if (isEvent) {
                                EventInfo eventInfo = eventDao.getEventInBackground(geofenceId);
                                if (eventInfo == null) {
                                    Log.e(LOG_LABEL, "Could not find event for geofence " + geofenceId);
                                    continue;
                                }
                                labels[i] = EVENT_PREFIX + String.valueOf(eventInfo.getAttraction().getId());
                                names[i] = eventInfo.getEvent().getName();
                                DestinationLocation location = eventInfo.getLocation();
                                latitudes[i] = location.getY();
                                latitudes[i] = location.getX();
                            } else {
                                Destination destination = destinationDao.getDestinationInBackground(geofenceId);
                                if (destination == null) {
                                    Log.e(LOG_LABEL, "Could not find destination for geofence " + geofenceId);
                                }
                                labels[i] = DESTINATION_PREFIX + String.valueOf(destination.getId());
                                names[i] = destination.getName();
                                DestinationLocation location = destination.getLocation();
                                latitudes[i] = location.getY();
                                longitudes[i] = location.getX();
                            }
                        }

                        return builder.putDoubleArray(AddGeofenceWorker.LATITUDES_KEY, latitudes)
                               .putDoubleArray(AddGeofenceWorker.LONGITUDES_KEY, longitudes)
                               .putStringArray(GeofenceTransitionWorker.TRIGGERING_GEOFENCES_KEY, labels)
                               .putStringArray(AddGeofenceWorker.GEOFENCE_NAMES_KEY, names)
                               .build();
                    }
                }.execute();

            }
        } else {
            Log.e(LOG_LABEL, "Geofencing transition event had an error");
            builder.putInt(GeofenceTransitionWorker.ERROR_CODE_KEY, geofencingEvent.getErrorCode());
            startWorker(builder.build());
        }
    }

    private static void startWorker(Data data) {
        Log.d(LOG_LABEL, "Going to start geofence transition worker");
        OneTimeWorkRequest.Builder workRequestBuilder = new OneTimeWorkRequest.Builder(GeofenceTransitionWorker.class);
        workRequestBuilder.setInputData(data);
        workRequestBuilder.setInitialDelay(0, TimeUnit.SECONDS);
        // TODO: set constraints and backoff on builder
        WorkRequest workRequest = workRequestBuilder.setInitialDelay(0, TimeUnit.SECONDS).build();
        WorkManager.getInstance().enqueue(workRequest);
    }
}
