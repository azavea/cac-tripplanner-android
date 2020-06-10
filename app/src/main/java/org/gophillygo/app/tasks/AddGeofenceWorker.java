package org.gophillygo.app.tasks;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.gophillygo.app.BuildConfig;

import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class AddGeofenceWorker extends Worker {

    // Must match action name in broadcast filter in the manifest
    public static final String ACTION_GEOFENCE_TRANSITION = "org.gophillygo.app.tasks.ACTION_GEOFENCE_TRANSITION";

    // When in development (debug) trigger entry immediately
    public static final int GEOFENCE_ENTER_TRIGGER = BuildConfig.DEBUG ?
            GeofencingRequest.INITIAL_TRIGGER_ENTER :
            GeofencingRequest.INITIAL_TRIGGER_DWELL;

    // https://developer.android.com/training/location/geofencing
    // Minimum radius should be at least 100 - 150, more for outdoors areas with no WiFi.
    // Greater values reduce battery consumption.
    private static final int GEOFENCE_RADIUS_METERS = 800;

    // Send alert roughly after device has been in geofence for this long.
    // When we are using the DWELL filter, this is about when we will receive notifications.
    private static final int GEOFENCE_LOITERING_DELAY = 0;

    // Set responsiveness high to save battery
    private static final int GEOFENCE_RESPONSIVENESS = BuildConfig.DEBUG ? 60000 : 180000; // 1 or 3 minutes

    private static final String LOG_LABEL = "AddGeofenceWorker";
    private static final int TRANSITION_BROADCAST_REQUEST_CODE = 42;

    public static final String LATITUDES_KEY = "latitudes";
    public static final String LONGITUDES_KEY = "longitudes";
    public static final String GEOFENCE_LABELS_KEY = "geofence_labels";
    public static final String GEOFENCE_NAMES_KEY = "geofence_names";

    // event identifiers used for custom Crashlytics event to note a geofence was added
    private static final String ADD_GEOFENCE_EVENT = "add_geofence";

    public AddGeofenceWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(LOG_LABEL, "Starting add geofence worker");

        Context context = getApplicationContext();

        GeofencingClient geofencingClient = LocationServices.getGeofencingClient(context);
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GEOFENCE_ENTER_TRIGGER);

        double[] latitudes;
        double[] longitudes;
        String[] geofenceLabels;
        String[] geofenceNames;

        // Get the data for the locations to add as primitive arrays with label and coordinates at
        // matching offsets.
        Data data = getInputData();
        Map<String, Object> map = data.getKeyValueMap();
        if (map.containsKey(LATITUDES_KEY) && map.containsKey(LONGITUDES_KEY) &&
                map.containsKey(GEOFENCE_LABELS_KEY) && map.containsKey(GEOFENCE_NAMES_KEY)) {

            latitudes = data.getDoubleArray(LATITUDES_KEY);
            longitudes = data.getDoubleArray(LONGITUDES_KEY);
            geofenceLabels = data.getStringArray(GEOFENCE_LABELS_KEY);
            geofenceNames = data.getStringArray(GEOFENCE_NAMES_KEY);
        } else {
            String message = "Data missing for geofences to add";
            FirebaseCrashlytics.getInstance().log(message);
            Log.e(LOG_LABEL, message);
            return Result.failure();
        }

        if (Objects.requireNonNull(latitudes).length != Objects.requireNonNull(longitudes).length || latitudes.length != Objects.requireNonNull(geofenceLabels).length ||
                latitudes.length != Objects.requireNonNull(geofenceNames).length) {
            String message = "Location data for geofences to add should be arrays of the same length.";
            FirebaseCrashlytics.getInstance().log(message);
            Log.e(LOG_LABEL, message);
            return Result.failure();
        }

        for (int i = 0; i < latitudes.length; i++) {
            Log.d(LOG_LABEL, "Adding geofence for " + geofenceLabels[i]);
            builder.addGeofence(new Geofence.Builder()
                    .setCircularRegion(latitudes[i], longitudes[i], GEOFENCE_RADIUS_METERS)
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setRequestId(geofenceLabels[i])
                    .setLoiteringDelay(GEOFENCE_LOITERING_DELAY)
                    .setNotificationResponsiveness(GEOFENCE_RESPONSIVENESS)
                    .setTransitionTypes(GEOFENCE_ENTER_TRIGGER | Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build());

            FirebaseCrashlytics.getInstance().log(ADD_GEOFENCE_EVENT);
        }

        // Location access permissions prompting is handled by `GpgLocationUtils`.
        Intent intent = new Intent(context, GeofenceTransitionBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                TRANSITION_BROADCAST_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        try {
            geofencingClient.addGeofences(builder.build(), pendingIntent);
            return Result.success();
        } catch (SecurityException ex) {
            String message = "Missing permissions to add geofences";
            Log.e(LOG_LABEL, message);
            FirebaseCrashlytics.getInstance().log(message);
            FirebaseCrashlytics.getInstance().recordException(ex);
            ex.printStackTrace();
            return Result.failure();
        } catch (Exception ex) {
            String message = "Failed to add geofences";
            Log.e(LOG_LABEL, message);
            FirebaseCrashlytics.getInstance().log(message);
            ex.printStackTrace();
            FirebaseCrashlytics.getInstance().recordException(ex);
            return Result.failure();
        }
    }
}
