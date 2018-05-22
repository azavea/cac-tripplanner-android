package com.gophillygo.app.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.gophillygo.app.R;
import com.gophillygo.app.data.models.Attraction;
import com.gophillygo.app.data.models.AttractionInfo;
import com.gophillygo.app.data.models.DestinationLocation;
import com.gophillygo.app.databinding.MapPopupCardBinding;
import com.gophillygo.app.utils.FlagMenuUtils;
import com.gophillygo.app.utils.GpgLocationUtils;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

public abstract class MapsActivity<T extends AttractionInfo> extends FilterableListActivity
        implements OnMapReadyCallback {

    private static final int DEFAULT_ZOOM = 12;
    private static final int ATTRACTION_ZOOM = 12;
    private static final float DEFAULT_OPACITY = 1f, FILTERED_OPACITY = 0.5f;
    private static final String LOG_LABEL = "MapsActivity";
    public static final String X = "x";
    public static final String Y = "y";
    public static final String ATTRACTION_ID = "id";

    private Map<Integer, Marker> markers;
    private Marker selectedMarker;
    private boolean locationSet = false;
    protected GoogleMap googleMap;
    protected Map<Integer, T> attractions;
    protected MapPopupCardBinding popupBinding;
    protected @IdRes int mapId;

    public BitmapDescriptor markerIcon, selectedMarkerIcon;

    public MapsActivity(@IdRes int mapId, @IdRes int toolbarId) {
        super(toolbarId);
        this.mapId = mapId;
    }

    public abstract boolean filterMatches(T attractionInfo);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(mapId);
        mapFragment.getMapAsync(this);

        markerIcon = vectorToBitmap(R.drawable.ic_map_marker);
        selectedMarkerIcon = vectorToBitmap(R.drawable.ic_selected_map_marker);
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
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        panToLocation();
    }

    private void panToLocation() {
        if (googleMap == null || locationSet) {
            return;
        }
        locationSet = true;

        Intent intent = getIntent();
        Location location;
        int zoom;
        if (intent.hasExtra(X) && intent.hasExtra(Y)) {
            location = new Location(DUMMY_LOCATION_PROVIDER);
            location.setLatitude(intent.getDoubleExtra(Y, 0));
            location.setLongitude(intent.getDoubleExtra(X, 0));
            zoom = ATTRACTION_ZOOM;
        } else {
            location = getCurrentLocation();
            zoom = DEFAULT_ZOOM;
        }
        LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, zoom));
    }

    @SuppressLint("MissingPermission")
    @Override
    public void locationOrDestinationsChanged() {
        super.locationOrDestinationsChanged();
        Log.d(MapsActivity.LOG_LABEL, "locationOrDestinationsChanged on map");
        if (googleMap != null &&
                GpgLocationUtils.checkFineLocationPermissions(new WeakReference<>(this))) {
            googleMap.setMyLocationEnabled(true);
            panToLocation();
        }
    }

    public Drawable getFlagImage(AttractionInfo info) {
        if (info == null) { return null; }
        return ContextCompat.getDrawable(this, info.getFlagImage());
    }

    @SuppressLint("RestrictedApi")
    public void optionsButtonClick(View view, T info) {
        PopupMenu menu = FlagMenuUtils.getFlagPopupMenu(this, view, info.getFlag());
        menu.setOnMenuItemClickListener(item -> {
            info.updateAttractionFlag(item.getItemId());
            viewModel.updateAttractionFlag(info.getFlag());
            popupBinding.setAttractionInfo(info);
            popupBinding.setAttraction(info.getAttraction());
            return true;
        });
    }

    @Override
    protected void loadData() {
        Log.d(LOG_LABEL, "load data in places maps activity");

        loadMarkers();
        reloadSelectedAttraction();

        googleMap.setOnMarkerClickListener(this::selectMarker);
    }

    public void openDetail(Attraction attraction) {
        Intent intent;
        String idKey;
        if (attraction.isEvent()) {
            intent = new Intent(this, EventDetailActivity.class);
            idKey = EventDetailActivity.EVENT_ID_KEY;
        } else {
            intent = new Intent(this, PlaceDetailActivity.class);
            idKey = PlaceDetailActivity.DESTINATION_ID_KEY;
        }
        long attractionId = attraction.getId();
        intent.putExtra(idKey, attractionId);
        startActivity(intent);
    }

    @SuppressLint("UseSparseArrays")
    private void loadMarkers() {
        if (markers == null) {
            markers = new HashMap<>(attractions.size());
            for (T attractionInfo : attractions.values()) {
                if (attractionInfo.getLocation() != null) {
                    DestinationLocation location = attractionInfo.getLocation();
                    LatLng latLng = new LatLng(location.getY(), location.getX());
                    float opacity = filterMatches(attractionInfo) ? DEFAULT_OPACITY : FILTERED_OPACITY;
                    Marker marker = googleMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .icon(markerIcon)
                            .alpha(opacity)
                    );
                    Integer id = attractionInfo.getAttraction().getId();
                    marker.setTag(id);
                    markers.put(id, marker);
                }
            }
            if (getIntent().hasExtra(ATTRACTION_ID)) {
                Integer id = getIntent().getIntExtra(ATTRACTION_ID, 0);
                selectMarker(markers.get(id));
            }
        } else {
            for (Map.Entry<Integer, Marker> entry : markers.entrySet()) {
                T info = attractions.get(entry.getKey());
                Marker marker = entry.getValue();
                if (info == null) {
                    marker.remove();
                } else {
                    float opacity = filterMatches(info) ? DEFAULT_OPACITY : FILTERED_OPACITY;
                    marker.setAlpha(opacity);
                }
            }
        }
    }

    private void reloadSelectedAttraction() {
        if (selectedMarker == null) { return; }

        T attractionInfo = selectedAttractionInfo();
        popupBinding.setAttractionInfo(attractionInfo);
        popupBinding.setAttraction(attractionInfo.getAttraction());
    }

    private T selectedAttractionInfo() {
        Integer id = (Integer) selectedMarker.getTag();
        return attractions.get(id);
    }

    private boolean selectMarker(Marker marker) {
        if (selectedMarker != null) {
            selectedMarker.setIcon(markerIcon);
        }
        selectedMarker = marker;
        marker.setIcon(selectedMarkerIcon);
        showPopup();
        return false;
    }

    private void showPopup() {
        T attractionInfo = selectedAttractionInfo();
        popupBinding.setAttractionInfo(attractionInfo);
        popupBinding.setAttraction(attractionInfo.getAttraction());

        // Need to set map padding so "Google" logo is above popup, but we need to wait until the
        // popup is visible in order to measure it's height.
        final Handler handler = new Handler();
        handler.postDelayed(() -> {
            googleMap.setPadding(0, 0, 0, 25 + popupBinding.mapPopupCard.getHeight());
        }, 30);
    }

    private BitmapDescriptor vectorToBitmap(@DrawableRes int id) {
        Drawable vectorDrawable = ResourcesCompat.getDrawable(getResources(), id, null);
        assert vectorDrawable != null;
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}
