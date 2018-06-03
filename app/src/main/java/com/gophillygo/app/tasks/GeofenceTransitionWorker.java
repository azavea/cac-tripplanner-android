package com.gophillygo.app.tasks;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;

import androidx.work.Data;
import androidx.work.Worker;
import dagger.android.AndroidInjection;

public class GeofenceTransitionWorker extends Worker {

    public static final String HAS_ERROR_KEY = "has_error";
    public static final String ERROR_CODE_KEY = "error_code";
    public static final String TRANSITION_KEY = "transition";
    public static final String TRIGGERING_GEOFENCES = "triggering_geofences";

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
                    Log.e(LOG_LABEL, "Geofence not available");
                    // This typically happens after NLP (Android's Network Location Provider) is disabled.
                    // https://developer.android.com/training/location/geofencing
                    // FIXME: is the default backoff appropriate?
                    // TODO: geofences should be re-registered
                    return WorkerResult.RETRY;
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
        Boolean dwellingInGeofence = geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL;
        String[] geofences = data.getStringArray(TRIGGERING_GEOFENCES);

        if (geofences.length > 0) {
            for (String geofenceID : geofences) {
                // TODO: send notification
                if (dwellingInGeofence) {
                    Log.d(LOG_LABEL, "Dwelling in geofence ID " + geofenceID);
                } else {
                    Log.d(LOG_LABEL, "Exited geofence ID " + geofenceID);
                }

            }

            return WorkerResult.SUCCESS;
        } else {
            Log.w(LOG_LABEL, "Received a geofence transition event with no triggering geofences.");
            return WorkerResult.SUCCESS;
        }
    }
}
