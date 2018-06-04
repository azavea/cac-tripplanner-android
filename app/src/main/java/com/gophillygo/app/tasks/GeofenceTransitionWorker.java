package com.gophillygo.app.tasks;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.gophillygo.app.R;

import androidx.work.Data;
import androidx.work.Worker;

public class GeofenceTransitionWorker extends Worker {

    public static final String HAS_ERROR_KEY = "has_error";
    public static final String ERROR_CODE_KEY = "error_code";
    public static final String TRANSITION_KEY = "transition";
    public static final String TRIGGERING_GEOFENCES = "triggering_geofences";

    private static final String CHANNEL_ID = "gophillygo-nearby-places";

    private static final String LOG_LABEL = "GeofenceTransition";

    @NonNull
    @Override
    public WorkerResult doWork() {

        // Geofence event data passed along as primitives
        Data data = getInputData();

        if (data.getBoolean(HAS_ERROR_KEY, true)) {
            int error = data.getInt(ERROR_CODE_KEY, GeofenceStatusCodes.DEVELOPER_ERROR);

            switch (error) {
                case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                    Log.e(LOG_LABEL, "Geofence not available; high accuracy location probably not enabled");
                    // This typically happens after NLP (Android's Network Location Provider) is disabled.
                    // https://developer.android.com/training/location/geofencing
                    // TODO: geofences should be re-registered on PROVIDERS_CHANGED
                    // but implicit system broadcast cannot read DB in background to find
                    // what to fence.
                    break;
                case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                    // FIXME: ensure there are not more than 100 geofences. Clear them here?
                    Log.e(LOG_LABEL, "Too many geofences!");
                    break;
                case GeofenceStatusCodes.TIMEOUT:
                    // FIXME: what could cause this?
                    Log.w(LOG_LABEL, "Geofence timeout");
                    return WorkerResult.RETRY;
                case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                    Log.e(LOG_LABEL, "Too many geofence pending intents");
                    break;
                case GeofenceStatusCodes.API_NOT_CONNECTED:
                    // FIXME: what could cause this?
                    Log.e(LOG_LABEL, "Geofencing prevented because API not connected");
                    return WorkerResult.RETRY;
                case GeofenceStatusCodes.CANCELED:
                    Log.w(LOG_LABEL, "Geofencing cancelled");
                    break;
                case GeofenceStatusCodes.ERROR:
                    Log.w(LOG_LABEL, "Geofencing error");
                    break;
                case GeofenceStatusCodes.DEVELOPER_ERROR:
                    Log.e(LOG_LABEL, "Geofencing encountered a developer error");
                    break;
                case GeofenceStatusCodes.INTERNAL_ERROR:
                    Log.e(LOG_LABEL, "Geofencing encountered an internal error");
                    break;
                case GeofenceStatusCodes.INTERRUPTED:
                    // FIXME: what could cause this?
                    Log.w(LOG_LABEL, "Geofencing interrupted");
                    return  WorkerResult.RETRY;
                default:
                    Log.w(LOG_LABEL, "Unrecognized GeofenceStatusCodes value: " + error);
            }
            return WorkerResult.FAILURE;
        }

        // Get the transition type.
        int geofenceTransition = data.getInt(TRANSITION_KEY, Geofence.GEOFENCE_TRANSITION_EXIT);
        Boolean enteredGeofence = geofenceTransition == AddGeofenceWorker.GEOFENCE_ENTER_TRIGGER;
        String[] geofences = data.getStringArray(TRIGGERING_GEOFENCES);

        if (geofences.length > 0) {
            Handler handler = new Handler(Looper.getMainLooper());
            for (String geofenceID : geofences) {
                // TODO: send notification
                if (enteredGeofence) {
                    Log.d(LOG_LABEL, "Entered geofence ID " + geofenceID);

                    // FIXME: set up channel
                    // https://developer.android.com/training/notify-user/build-notification

                    handler.post(() -> {
                        createNotificationChannel();
                        // show on UI thread
                        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                                .setSmallIcon(R.drawable.watershed_alliance_full_icon_300x104px)
                                .setContentTitle("Near a place")
                                .setContentText("Something interesting is nearby")
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());

                        // notificationId is a unique int for each notification that you must define
                        notificationManager.notify(Integer.valueOf(geofenceID), mBuilder.build());
                    });

                } else {
                    Log.d(LOG_LABEL, "Exited geofence ID " + geofenceID);
                    // TODO: remove and re-register geofence, or else it will ignore future events
                    createNotificationChannel();
                    handler.post(() -> {
                        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                                .setSmallIcon(R.drawable.watershed_alliance_full_icon_300x104px)
                                .setContentTitle("Left a place")
                                .setContentText("Passed by something interesting nearby")
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());

                        // notificationId is a unique int for each notification that you must define
                        notificationManager.notify(Integer.valueOf(geofenceID), mBuilder.build());
                    });
                }

            }

            return WorkerResult.SUCCESS;
        } else {
            Log.w(LOG_LABEL, "Received a geofence transition event with no triggering geofences.");
            return WorkerResult.SUCCESS;
        }
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = CHANNEL_ID;
            String description = getApplicationContext().getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getApplicationContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}
