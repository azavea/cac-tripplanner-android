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
import android.support.annotation.LayoutRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
import com.gophillygo.app.BR;
import com.gophillygo.app.R;
import com.gophillygo.app.data.models.Attraction;
import com.gophillygo.app.data.models.AttractionInfo;
import com.gophillygo.app.data.models.DestinationLocation;
import com.gophillygo.app.databinding.MapPopupCardBinding;
import com.gophillygo.app.utils.GpgLocationUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public abstract class MapsActivity<T extends AttractionInfo> extends FilterableListActivity
        implements OnMapReadyCallback {

    private static final int DEFAULT_ZOOM = 12;
    private static final String LOG_LABEL = "MapsActivity";

    private List<Marker> markers;
    private Marker selectedMarker;
    protected GoogleMap googleMap;
    protected List<T> attractions;
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
        Log.d(MapsActivity.LOG_LABEL, "locationOrDestinationsChanged on map");
        if (googleMap != null &&
                GpgLocationUtils.checkFineLocationPermissions(new WeakReference<>(this))) {
            googleMap.setMyLocationEnabled(true);
            panToCurrentLocation();
        }
    }

    public Drawable getFlagImage(AttractionInfo info) {
        if (info == null) { return null; }
        return ContextCompat.getDrawable(this, info.getFlagImage());
    }

    @SuppressLint("RestrictedApi")
    public void optionsButtonClick(View view, T info) {
        PopupMenu menu = new PopupMenu(this, view);
        menu.getMenuInflater().inflate(R.menu.place_options_menu, menu.getMenu());
        menu.setOnMenuItemClickListener(item -> {
            info.updateAttractionFlag(item.getItemId());
            viewModel.updateAttractionFlag(info.getFlag());
            // TODO binding.notify didn't help, had to directly set :(
            popupBinding.mapPopupOptionsButton.setImageResource(info.getFlagImage());
            return true;
        });

        // Force icons to show in the popup menu via the support library API
        // https://stackoverflow.com/questions/6805756/is-it-possible-to-display-icons-in-a-popupmenu
        MenuPopupHelper popupHelper = new MenuPopupHelper(this,
                (MenuBuilder)menu.getMenu(), view);
        popupHelper.setForceShowIcon(true);
        popupHelper.show();
    }

    @Override
    protected void loadData() {
        Log.d(LOG_LABEL, "load data in places maps activity");

        loadMarkers();

        googleMap.setOnMarkerClickListener(marker -> {
            if (selectedMarker != null) {
                selectedMarker.setIcon(markerIcon);
            }
            selectedMarker = marker;
            marker.setIcon(selectedMarkerIcon);
            AttractionInfo attractionInfo = (AttractionInfo)  marker.getTag();
            showPopup(attractionInfo);
            Log.d(LOG_LABEL, "Clicked marker for attraction: " + attractionInfo.getAttraction().getId());
            return false;
        });
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

    private void loadMarkers() {
        if (markers == null) {
            markers = new ArrayList<>(attractions.size());
            for (T attractionInfo : attractions) {
                if (attractionInfo.getLocation() != null) {
                    DestinationLocation location = attractionInfo.getLocation();
                    LatLng latLng = new LatLng(location.getY(), location.getX());
                    float opacity = filterMatches(attractionInfo) ? 1f : 0.5f;
                    Marker marker = googleMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .icon(markerIcon)
                            .alpha(opacity)
                    );
                    marker.setTag(attractionInfo);
                    markers.add(marker);
                }
            }
        } else {
            for (Marker marker : markers) {
                float opacity = filterMatches((T) marker.getTag()) ? 1f : 0.5f;
                marker.setAlpha(opacity);
            }
        }
    }

    private void showPopup(AttractionInfo attractionInfo) {
        popupBinding.setAttractionInfo(attractionInfo);
        popupBinding.setAttraction(attractionInfo.getAttraction());
        popupBinding.notifyPropertyChanged(BR.attraction);
        popupBinding.notifyPropertyChanged(BR.attractionInfo);

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
