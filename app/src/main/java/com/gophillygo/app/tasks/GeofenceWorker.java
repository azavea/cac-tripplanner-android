package com.gophillygo.app.tasks;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;


import androidx.work.Worker;

public class GeofenceWorker extends Worker {

    @NonNull
    @Override
    public WorkerResult doWork() {
        Context context = getApplicationContext();
        GeofencingClient geofencingClient = LocationServices.getGeofencingClient(context);
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL);


        builder.addGeofence(new Geofence.Builder().setRequestId("gophillygo")
                .setCircularRegion()
                .setExpirationDuration()
                .setTransitionTypes())
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.


            // FIXME: different intent for the transition worker
            Intent intent = new Intent(context, AddGeofencesBroadcastReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 007, intent , 0);
            geofencingClient.addGeofences(builder.build(), pendingIntent);
        }

        return null;
    }
}
