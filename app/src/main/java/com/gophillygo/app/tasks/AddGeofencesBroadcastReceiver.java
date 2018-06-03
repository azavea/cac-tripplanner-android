package com.gophillygo.app.tasks;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.gophillygo.app.data.DestinationDao;
import com.gophillygo.app.data.models.Destination;
import com.gophillygo.app.data.models.DestinationLocation;

import java.util.List;

import javax.inject.Inject;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
import dagger.android.AndroidInjection;

/**
 * Add geofences by scheduling one-time worker job.
 *
 * Expects calling intent to set geofence data as three arrays with labels and coordinates.
 *
 * Per dagger docs:
 * `DaggerBroadcastReceiver` should only be used when the BroadcastReceiver is registered in the AndroidManifest.xml.
 * When the BroadcastReceiver is created in your own code, prefer constructor injection instead.
 * https://google.github.io/dagger/android.html
 *
 * This is registered in the manifest to listen for reboots, so we should use the dagger library.
 */
public class AddGeofencesBroadcastReceiver extends BroadcastReceiver {

    @Inject
    DestinationDao destinationDao;

    public static final String ADD_GEOFENCE_TAG = "gpg-add-geofences";

    private static final String LOG_LABEL = "AddGeofenceBroadcast";
    private static final int MAX_GEOFENCES = 100; // cannot set up more than these

    @Override
    public void onReceive(Context context, Intent intent) {
        AndroidInjection.inject(this, context);

        double[] latitudes;
        double[] longitudes;
        String[] labels;

        // Use intent extras when adding geofence(s) in response to user action.
        if (intent.hasExtra(AddGeofenceWorker.LATITUDES_KEY) && intent.hasExtra(AddGeofenceWorker.LONGITUDES_KEY)
                && intent.hasExtra(AddGeofenceWorker.GEOFENCE_LABELS_KEY)) {

            latitudes = intent.getDoubleArrayExtra(AddGeofenceWorker.LATITUDES_KEY);
            longitudes = intent.getDoubleArrayExtra(AddGeofenceWorker.LONGITUDES_KEY);
            labels = intent.getStringArrayExtra(AddGeofenceWorker.GEOFENCE_LABELS_KEY);

        } else {
            Log.d(LOG_LABEL, "Reading data to add geofences from database.");
            // Read datatabase instead of relying on an intent with extras; on boot, have no extras set
            List<Destination> destinations = destinationDao.getGeofenceDestinations().getValue();
            if (destinations == null || destinations.isEmpty()) {
                Log.d(LOG_LABEL, "Have no destinations with geofences to add.");
                return;
            }

            int destinationsCount = destinations.size();
            if (destinationsCount > MAX_GEOFENCES) {
                // FIXME: handle
                Log.e(LOG_LABEL, "Too many destinations with geofences to add.");
                return;
            }

            // TODO: remove any existing geofences first before adding all from database?
            // If got here from reboot event, they shouldn't exist anyways.

            latitudes = new double[destinationsCount];
            longitudes = new double[destinationsCount];
            labels = new String[destinationsCount];

            for (int i = 0; i < destinationsCount; i++) {
                Destination destination = destinations.get(i);
                labels[i] = String.valueOf(destination.getId());
                DestinationLocation location = destination.getLocation();
                latitudes[i] = location.getY();
                longitudes[i] = location.getX();
            }
        }

        // Sanity check the data before starting the worker
        if (latitudes.length == 0 || latitudes.length != longitudes.length ||
                latitudes.length != labels.length) {
            Log.e(LOG_LABEL, "Extras data of zero or mismatched length found");
            return;
        }

        Data data = new Data.Builder()
                .putDoubleArray(AddGeofenceWorker.LATITUDES_KEY, latitudes)
                .putDoubleArray(AddGeofenceWorker.LONGITUDES_KEY, longitudes)
                .putStringArray(AddGeofenceWorker.GEOFENCE_LABELS_KEY, labels)
                .build();

        startWorker(data);
    }

    /**
     * Convenience static method to start a worker without using the broadcast receiver.
     *
     * @param destination Destination with a location to use for the geofence to add.
     */
    public static void addOneGeofence(@NonNull Destination destination) {
        double[] latitudes = {destination.getLocation().getY()};
        double[] longitudes = {destination.getLocation().getX()};
        String[] labels = {String.valueOf(destination.getId())};

        Data data = new Data.Builder()
                .putDoubleArray(AddGeofenceWorker.LATITUDES_KEY, latitudes)
                .putDoubleArray(AddGeofenceWorker.LONGITUDES_KEY, longitudes)
                .putStringArray(AddGeofenceWorker.GEOFENCE_LABELS_KEY, labels)
                .build();

        Log.d(LOG_LABEL, "addOneGeofence");
        startWorker(data);
    }

    private static void startWorker(Data data) {
        // Start a worker to add geofences from a background thread.
        OneTimeWorkRequest.Builder workRequestBuilder = new OneTimeWorkRequest.Builder(AddGeofenceWorker.class);
        workRequestBuilder.setInputData(data);
        workRequestBuilder.addTag(ADD_GEOFENCE_TAG);
        // TODO: set constraints and backoff on builder
        WorkRequest workRequest = workRequestBuilder.build();
        WorkManager.getInstance().enqueue(workRequest);
        Log.d(LOG_LABEL, "Enqueued new work request to add geofence(s)");
    }
}
