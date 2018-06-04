package com.gophillygo.app.tasks;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.gophillygo.app.data.DestinationDao;
import com.gophillygo.app.data.models.AttractionFlag;
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

    @SuppressLint("StaticFieldLeak")
    @Override
    public void onReceive(Context context, Intent intent) {
        AndroidInjection.inject(this, context);

        double[] latitudes;
        double[] longitudes;
        String[] labels;
        String[] names;

        // Use intent extras when adding geofence(s) in response to user action.
        if (intent.hasExtra(AddGeofenceWorker.LATITUDES_KEY) && intent.hasExtra(AddGeofenceWorker.LONGITUDES_KEY)
                && intent.hasExtra(AddGeofenceWorker.GEOFENCE_LABELS_KEY) &&
                intent.hasExtra(AddGeofenceWorker.GEOFENCE_NAMES_KEY)) {

            latitudes = intent.getDoubleArrayExtra(AddGeofenceWorker.LATITUDES_KEY);
            longitudes = intent.getDoubleArrayExtra(AddGeofenceWorker.LONGITUDES_KEY);
            labels = intent.getStringArrayExtra(AddGeofenceWorker.GEOFENCE_LABELS_KEY);
            names = intent.getStringArrayExtra(AddGeofenceWorker.GEOFENCE_NAMES_KEY);

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
                    .putStringArray(AddGeofenceWorker.GEOFENCE_NAMES_KEY, names)
                    .build();

            startWorker(data);

        } else {
            Log.d(LOG_LABEL, "Reading data to add geofences from database.");

            // Query database on background thread by taking broadcast receiver async briefly
            // https://developer.android.com/guide/components/broadcasts
            final PendingResult pendingResult = goAsync();
            // Read datatabase instead of relying on an intent with extras; on boot, have no extras set
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected void onPostExecute(Void aVoid) {
                    // Need to release BroadcastReceiver since have gone async
                    pendingResult.finish();
                }

                @Override
                protected void onCancelled() {
                    pendingResult.abortBroadcast();
                }

                @Override
                protected Void doInBackground(Void... voids) {
                    List<Destination> destinations = destinationDao
                            .getGeofenceDestinations(AttractionFlag.Option.WantToGo.code);
                    if (destinations == null || destinations.isEmpty()) {
                        Log.d(LOG_LABEL, "Have no destinations with geofences to add.");
                        return null;
                    }

                    int destinationsCount = destinations.size();
                    if (destinationsCount > MAX_GEOFENCES) {
                        // FIXME: handle
                        Log.e(LOG_LABEL, "Too many destinations with geofences to add.");
                        return null;
                    }

                    double[] latitudes = new double[destinationsCount];
                    double[] longitudes = new double[destinationsCount];
                    String[] labels = new String[destinationsCount];
                    String[] names = new String[destinationsCount];

                    for (int i = 0; i < destinationsCount; i++) {
                        Destination destination = destinations.get(i);
                        labels[i] = String.valueOf(destination.getId());
                        names[i] = destination.getName();
                        DestinationLocation location = destination.getLocation();
                        latitudes[i] = location.getY();
                        longitudes[i] = location.getX();
                    }

                    Data data = new Data.Builder()
                            .putDoubleArray(AddGeofenceWorker.LATITUDES_KEY, latitudes)
                            .putDoubleArray(AddGeofenceWorker.LONGITUDES_KEY, longitudes)
                            .putStringArray(AddGeofenceWorker.GEOFENCE_LABELS_KEY, labels)
                            .putStringArray(AddGeofenceWorker.GEOFENCE_NAMES_KEY, names)
                            .build();

                    startWorker(data);
                    return null;
                }
            }.execute();

        }
    }

    /**
     * Convenience static method to start a worker without using the broadcast receiver.
     *
     * @param destination Destination with a location to use for the geofence to add.
     */
    public static void addOneGeofence(@NonNull Destination destination) {
        addOneGeofence(destination.getLocation().getX(), destination.getLocation().getY(),
                String.valueOf(destination.getId()), String.valueOf(destination.getName()));
    }

    public static void addOneGeofence(double x, double y, @NonNull String label, @NonNull String name) {
        double[] latitudes = {y};
        double[] longitudes = {x};
        String[] labels = {label};
        String[] names = {name};

        Data data = new Data.Builder()
                .putDoubleArray(AddGeofenceWorker.LATITUDES_KEY, latitudes)
                .putDoubleArray(AddGeofenceWorker.LONGITUDES_KEY, longitudes)
                .putStringArray(AddGeofenceWorker.GEOFENCE_LABELS_KEY, labels)
                .putStringArray(AddGeofenceWorker.GEOFENCE_NAMES_KEY, names)
                .build();

        Log.d(LOG_LABEL, "addOneGeofence");
        startWorker(data);
    }

    public static void startWorker(Data data) {
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
