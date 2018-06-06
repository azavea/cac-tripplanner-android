package com.gophillygo.app.tasks;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.gophillygo.app.R;
import com.gophillygo.app.activities.EventDetailActivity;
import com.gophillygo.app.activities.PlaceDetailActivity;

import androidx.work.Data;
import androidx.work.Worker;

public class GeofenceTransitionWorker extends Worker {

    public static final String HAS_ERROR_KEY = "has_error";
    public static final String ERROR_CODE_KEY = "error_code";
    public static final String TRANSITION_KEY = "transition";
    public static final String TRIGGERING_GEOFENCES_KEY = "triggering_geofences";

    private static final String CHANNEL_ID = "gophillygo-nearby-places";

    private static final String LOG_LABEL = "GeofenceTransition";

    @NonNull
    @Override
    @SuppressLint("StringFormatInvalid")
    public WorkerResult doWork() {
        Log.d(LOG_LABEL, "Starting geofence transition worker");

        // Geofence event data passed along as primitives
        Data data = getInputData();

        if (data.getBoolean(HAS_ERROR_KEY, true)) {
            Log.d(LOG_LABEL, "Found error for geofence transition");
            int error = data.getInt(ERROR_CODE_KEY, GeofenceStatusCodes.DEVELOPER_ERROR);
            handleError(error);
        }

        // if got this far, have no error
        Context context = getApplicationContext();

        // Get the transition type.
        int geofenceTransition = data.getInt(TRANSITION_KEY, Geofence.GEOFENCE_TRANSITION_EXIT);
        Boolean enteredGeofence = geofenceTransition == AddGeofenceWorker.GEOFENCE_ENTER_TRIGGER;
        String[] geofences = data.getStringArray(TRIGGERING_GEOFENCES_KEY);
        String[] geofencePlaceNames = data.getStringArray(AddGeofenceWorker.GEOFENCE_NAMES_KEY);
        double[] latitudes = data.getDoubleArray(AddGeofenceWorker.LATITUDES_KEY);
        double[] longitudes = data.getDoubleArray(AddGeofenceWorker.LONGITUDES_KEY);
        Log.d(LOG_LABEL, "Got geofence transition worker data");

        int geofencesCount = geofences.length;
        Log.d(LOG_LABEL, "Have " + geofencesCount + " geofence transitions to process");
        if (geofencePlaceNames.length != geofences.length || geofences.length != latitudes.length ||
                geofences.length != longitudes.length) {
            Log.e(LOG_LABEL, "Got geofence worker data arrays of differing lengths");
            return WorkerResult.FAILURE;
        }

        if (geofencesCount > 0) {
            // Send notification the main thread
            Handler handler = new Handler(Looper.getMainLooper());
            for (int i = 0; i < geofencesCount; i++) {
                // Need a unique int we can find later, for the notification
                String geofenceLabel = geofences[i];
                String placeName = geofencePlaceNames[i];
                double latitude = latitudes[i];
                double longitude = longitudes[i];

                // Geofence string ID is "d" for destination or "e" for event, followed by the
                // destination or event integer ID.
                int geofenceId = Integer.valueOf(geofenceLabel.substring(1));
                boolean isEvent = geofenceLabel.startsWith("e");
                String notificationTag = isEvent ? "e" : "d";

                if (enteredGeofence) {
                    Log.d(LOG_LABEL, "Entered geofence ID " + geofenceLabel + " for " + placeName);

                    handler.post(() -> {
                        // Get intent for the detail view to open on notification click.
                        Intent intent;
                        if (isEvent) {
                            intent = new Intent(context, EventDetailActivity.class);
                            intent.putExtra(EventDetailActivity.EVENT_ID_KEY, (long)geofenceId);
                        } else {
                            intent = new Intent(context, PlaceDetailActivity.class);
                            intent.putExtra(PlaceDetailActivity.DESTINATION_ID_KEY, (long)geofenceId);
                        }


                        // Add the intent to the stack builder, which inflates the back stack
                        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                        stackBuilder.addNextIntentWithParentStack(intent);
                        // Get the PendingIntent containing the entire back stack
                        PendingIntent resultPendingIntent =
                                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

                        createNotificationChannel(context);
                        // show on UI thread
                        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                                // TODO: use app icon of some sort
                                .setSmallIcon(R.drawable.ic_flag_blue_24dp)
                                .setOnlyAlertOnce(false)
                                .setContentTitle(placeName)
                                .setContentText(context.getString(R.string.place_nearby_notification, placeName))
                                .setContentIntent(resultPendingIntent)
                                .setAutoCancel(true) // close notifcation when tapped
                                .setPriority(NotificationCompat.PRIORITY_HIGH);

                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

                        // The pair of notification string tag and int must be unique for the app
                        notificationManager.notify(notificationTag, geofenceId, mBuilder.build());
                    });

                } else {
                    Log.d(LOG_LABEL, "Exited geofence ID " + geofenceLabel);
                    handler.post(() -> {
                        Log.d(LOG_LABEL, "Removing notification for geofence");
                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                        notificationManager.cancel(notificationTag, geofenceId);
                    });
                }

                Log.d(LOG_LABEL, "Re-registering geofence after transition");
                // remove and re-register geofence, or else it will ignore future events
                RemoveGeofenceWorker.removeOneGeofence(geofenceLabel);
                AddGeofencesBroadcastReceiver.addOneGeofence(longitude, latitude, geofenceLabel, placeName);
            }

            return WorkerResult.SUCCESS;
        } else {
            Log.w(LOG_LABEL, "Received a geofence transition event with no triggering geofences.");
            return WorkerResult.SUCCESS;
        }
    }

    /**
     * Create the NotificationChannel, but only on API 26+ because the NotificationChannel class
     * is new and not in the support library.
     * https://developer.android.com/training/notify-user/build-notification
     * 
     * @param context Application context
     */
    private static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String description = context.getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                NotificationChannel existingChannel = notificationManager.getNotificationChannel(CHANNEL_ID);
                if (existingChannel == null) {
                    Log.d(LOG_LABEL, "Creating new notification channel for app:");
                    notificationManager.createNotificationChannel(channel);
                } else {
                    Log.d(LOG_LABEL, "Have notification channel set up already");
                }
            } else {
                Log.e(LOG_LABEL, "Failed to get notification manager");
            }
        }
    }

    private WorkerResult handleError(int error) {
        // https://developers.google.com/android/reference/com/google/android/gms/location/GeofenceStatusCodes
        switch (error) {
            case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                Log.e(LOG_LABEL, "Geofencing service not available; high accuracy location probably not enabled");
                // This typically happens after NLP (Android's Network Location Provider) is disabled.
                // https://developer.android.com/training/location/geofencing
                // TODO: geofences should be re-registered on PROVIDERS_CHANGED
                // but implicit system broadcast cannot read DB in background to find
                // what to fence.
                break;
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                Log.e(LOG_LABEL, "Too many geofences!");
                break;
            case GeofenceStatusCodes.TIMEOUT:
                Log.w(LOG_LABEL, "Geofence timeout");
                return WorkerResult.RETRY;
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                Log.e(LOG_LABEL, "Too many pending intents to addGeofence. Max is 5.");
                return  WorkerResult.RETRY;
            case GeofenceStatusCodes.API_NOT_CONNECTED:
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
                Log.w(LOG_LABEL, "Geofencing interrupted");
                return  WorkerResult.RETRY;
            default:
                Log.w(LOG_LABEL, "Unrecognized GeofenceStatusCodes value: " + error);
        }
        return WorkerResult.FAILURE;
    }

}
