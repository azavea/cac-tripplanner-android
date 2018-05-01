package com.gophillygo.app.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.gophillygo.app.R;
import com.gophillygo.app.utils.GpgLocationUtils;

import java.lang.ref.WeakReference;

public class MapsActivity extends BaseAttractionActivity implements OnMapReadyCallback {

    private static final int DEFAULT_ZOOM = 14;
    private static final String LOG_LABEL = "MapsActivity";

    private GoogleMap googleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        panToCurrentLocation();
    }

    private void panToCurrentLocation() {
        Location location = getCurrentLocation();
        LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());
        this.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, DEFAULT_ZOOM));
    }

    @SuppressLint("MissingPermission")
    @Override
    public void locationOrDestinationsChanged() {
        super.locationOrDestinationsChanged();
        Log.d(LOG_LABEL, "locationOrDestinationsChanged on map");
        if (GpgLocationUtils.checkFineLocationPermissions(new WeakReference<>(this))) {
            googleMap.setMyLocationEnabled(true);
        }
        panToCurrentLocation();
    }
}
