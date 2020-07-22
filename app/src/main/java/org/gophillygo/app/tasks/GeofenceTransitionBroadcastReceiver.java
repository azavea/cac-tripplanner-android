package org.gophillygo.app.tasks;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.gophillygo.app.data.DestinationDao;
import org.gophillygo.app.data.EventDao;
import org.gophillygo.app.data.models.Destination;
import org.gophillygo.app.data.models.EventInfo;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

public class GeofenceTransitionBroadcastReceiver extends BroadcastReceiver {


    @Inject
    DestinationDao destinationDao;

    @Inject
    EventDao eventDao;

    public static final String GEOFENCE_IMAGES_KEY = "geofence_images";

    private static final String LOG_LABEL = "GeofenceTransitionBR";

    @SuppressLint("StaticFieldLeak")
    @Override
    public void onReceive(Context context, Intent intent) {
        AndroidInjection.inject(this, context);

        Log.d(LOG_LABEL, "Received geofence transition event");
        // Start a worker to send notifications from a background thread.

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        Data.Builder builder = new Data.Builder();

        boolean hasError = geofencingEvent.hasError();
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
                        String[] labels = new String[numGeofences];
                        String[] names = new String[numGeofences];
                        String[] images = new String[numGeofences];

                        for (int i = 0; i < numGeofences; i++) {
                            Geofence fence = triggeringGeofences.get(i);
                            String fenceId = fence.getRequestId();
                            Log.d(LOG_LABEL, "Handling transition for geofence " + fenceId);
                            // Geofence string ID is "d" for destination or "e" for event, followed by the
                            // destination or event integer ID.
                            int geofenceId = Integer.parseInt(fenceId.substring(1));
                            boolean isEvent = fenceId.startsWith(GeofenceTransitionWorker.EVENT_PREFIX);

                            // query for each event or destination synchronously from the database
                            if (isEvent) {
                                EventInfo eventInfo = eventDao.getEventInBackground(geofenceId);
                                if (eventInfo == null) {
                                    Log.e(LOG_LABEL, "Could not find event for geofence " + geofenceId);
                                    continue;
                                }
                                labels[i] = GeofenceTransitionWorker.EVENT_PREFIX + eventInfo.getAttraction().getId();
                                names[i] = eventInfo.getEvent().getName();
                                images[i] = eventInfo.getEvent().getWideImage();
                            } else {
                                Destination destination = destinationDao.getDestinationInBackground(geofenceId);
                                if (destination == null) {
                                    String message = "Could not find destination for geofence " + geofenceId;
                                    Log.e(LOG_LABEL, message);
                                    FirebaseCrashlytics.getInstance().log(message);
                                } else {
                                    labels[i] = GeofenceTransitionWorker.DESTINATION_PREFIX + destination.getId();
                                    names[i] = destination.getName();
                                    images[i] = destination.getWideImage();
                                }
                            }
                        }

                        return builder.putStringArray(GeofenceTransitionWorker.TRIGGERING_GEOFENCES_KEY, labels)
                                .putStringArray(AddGeofenceWorker.GEOFENCE_NAMES_KEY, names)
                                .putStringArray(GEOFENCE_IMAGES_KEY, images)
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
