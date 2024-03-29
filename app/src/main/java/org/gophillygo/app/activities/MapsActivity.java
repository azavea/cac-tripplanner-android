package org.gophillygo.app.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.gophillygo.app.R;
import org.gophillygo.app.data.models.Attraction;
import org.gophillygo.app.data.models.AttractionFlag;
import org.gophillygo.app.data.models.AttractionInfo;
import org.gophillygo.app.data.models.DestinationLocation;
import org.gophillygo.app.databinding.MapPopupCardBinding;
import org.gophillygo.app.utils.FlagMenuUtils;
import org.gophillygo.app.utils.GpgLocationUtils;
import org.gophillygo.app.utils.UserUtils;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class MapsActivity<T extends AttractionInfo> extends FilterableListActivity
        implements OnMapReadyCallback {

    private static final int DEFAULT_ZOOM = 12;
    private static final int ATTRACTION_ZOOM = 14;
    private static final float DEFAULT_OPACITY = 1f, FILTERED_OPACITY = 0.5f;
    private static final String LOCATION_SET_KEY = "location_set";
    private static final String LOG_LABEL = "MapsActivity";

    public static final String X = "x";
    public static final String Y = "y";
    public static final String ATTRACTION_ID = "id";

    private Map<Integer, Marker> markers;
    private int selectedAttractionId = -1;
    private boolean locationSet = false;
    protected GoogleMap googleMap;
    protected Map<Integer, T> attractions;
    protected MapPopupCardBinding popupBinding;
    protected @IdRes
    final
    int mapId;

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
        if (savedInstanceState != null && savedInstanceState.containsKey(LOCATION_SET_KEY)) {
            locationSet = savedInstanceState.getBoolean(LOCATION_SET_KEY);
        }
        if (savedInstanceState != null && savedInstanceState.containsKey(ATTRACTION_ID)) {
            selectedAttractionId = savedInstanceState.getInt(ATTRACTION_ID);
        } else if (getIntent().hasExtra(ATTRACTION_ID)) {
            selectedAttractionId = getIntent().getIntExtra(ATTRACTION_ID, 0);
        }
        Objects.requireNonNull(mapFragment).getMapAsync(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(LOCATION_SET_KEY, locationSet);
        outState.putInt(ATTRACTION_ID, selectedAttractionId);
        super.onSaveInstanceState(outState);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     *
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.setOnMarkerClickListener(this::selectMarker);

        markerIcon = vectorToBitmap(R.drawable.ic_map_marker);
        selectedMarkerIcon = vectorToBitmap(R.drawable.ic_selected_map_marker);

        loadMarkers();
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

            viewModel.updateAttractionFlag(info.getFlag(), userUuid, getString(R.string.user_flag_post_api_key),
                    UserUtils.isFlagPostingEnabled(this));

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
        if (googleMap == null || attractions == null) {
            return;
        }

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
            if (selectedAttractionId != -1) {
                selectMarker(markers.get(selectedAttractionId));
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
        if (selectedAttractionId == -1) { return; }

        T attractionInfo = getSelectedAttractionInfo();
        if (attractionInfo != null) {
            popupBinding.setAttractionInfo(attractionInfo);
            popupBinding.setAttraction(attractionInfo.getAttraction());
        }
    }

    private T getSelectedAttractionInfo() {
        if (attractions == null || attractions.isEmpty() || !attractions.containsKey(selectedAttractionId)) {
            return null;
        }
        return attractions.get(selectedAttractionId);
    }

    private boolean selectMarker(Marker marker) {
        if (selectedAttractionId != -1) {
            Marker prevSelectedMarker = markers.get(selectedAttractionId);
            Objects.requireNonNull(prevSelectedMarker).setIcon(markerIcon);
        }
        //noinspection ConstantConditions
        selectedAttractionId = (Integer) marker.getTag();
        marker.setIcon(selectedMarkerIcon);
        showPopup();
        return false;
    }

    private void showPopup() {
        T attractionInfo = getSelectedAttractionInfo();
        if (attractionInfo != null) {
            popupBinding.setAttractionInfo(attractionInfo);
            popupBinding.setAttraction(attractionInfo.getAttraction());
        }

        // Need to set map padding so "Google" logo is above popup, but we need to wait until the
        // popup is visible in order to measure it's height.
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> googleMap.setPadding(0, 0, 0,
                25 + popupBinding.mapPopupCard.getHeight()), 30);
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
