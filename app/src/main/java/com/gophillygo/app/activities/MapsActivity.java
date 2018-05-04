package com.gophillygo.app.activities;

import android.annotation.SuppressLint;
import android.databinding.DataBindingUtil;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.gophillygo.app.R;
import com.gophillygo.app.databinding.ActivityMapsBinding;
import com.gophillygo.app.databinding.FilterButtonBarBinding;
import com.gophillygo.app.utils.GpgLocationUtils;

import java.lang.ref.WeakReference;

public class MapsActivity extends FilterableListActivity implements OnMapReadyCallback {

    private static final int DEFAULT_ZOOM = 14;
    private static final String LOG_LABEL = "MapsActivity";

    private GoogleMap googleMap;
    private Toolbar toolbar;

    public MapsActivity() {
        this(R.id.map_toolbar);
    }

    public MapsActivity(int toolbarId) {
        super(toolbarId);
    }

    @Override
    protected FilterButtonBarBinding setupDataBinding() {
        // TODO: #11 load destination markers
        ActivityMapsBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_maps);
        return binding.mapFilterButtonBar;
    }

    @Override
    protected void loadData() {
        // TODO: #11 load destination markers
        Log.d(LOG_LABEL, "load data in maps activity");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        if (googleMap == null) {
            return;
        }
        Location location = getCurrentLocation();
        LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, DEFAULT_ZOOM));
    }

    @SuppressLint("MissingPermission")
    @Override
    public void locationOrDestinationsChanged() {
        super.locationOrDestinationsChanged();
        Log.d(LOG_LABEL, "locationOrDestinationsChanged on map");
        if (googleMap != null &&
                GpgLocationUtils.checkFineLocationPermissions(new WeakReference<>(this))) {
            googleMap.setMyLocationEnabled(true);
            panToCurrentLocation();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.action_map_events:
                Log.d(LOG_LABEL, "Selected map events menu item");
                break;
            case R.id.action_map_search:
                Log.d(LOG_LABEL, "Selected map search menu item");
                break;
            case R.id.action_map_view_list:
                // TODO: #11 implement list/map toggle
                Log.d(LOG_LABEL, "Selected to go back to list view from map");
                break;
            default:
                Log.w(LOG_LABEL, "Unrecognized menu item selected: " + String.valueOf(itemId));
                return super.onOptionsItemSelected(item);
        }
        return true;
    }
}
