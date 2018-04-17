package com.gophillygo.app;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;

import com.gophillygo.app.adapters.PlaceCategoryGridAdapter;
import com.gophillygo.app.data.DestinationViewModel;
import com.gophillygo.app.data.models.Destination;
import com.gophillygo.app.data.models.DestinationInfo;
import com.gophillygo.app.data.models.DestinationLocation;
import com.gophillygo.app.data.networkresource.Status;
import com.gophillygo.app.di.GpgViewModelFactory;
import com.synnapps.carouselview.CarouselView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;


public class HomeActivity extends AppCompatActivity {

    private static final String LOG_LABEL = "HomeActivity";

    // maximum number of nearby destinations to show in carousel
    private static final int CAROUSEL_MAX_DESTINATION_COUNT = 8;

    private static final float METERS_TO_MILES = 0.000621371192f;

    private LayoutInflater inflater;

    private CarouselView carouselView;
    private GridView gridView;
    private Toolbar toolbar;

    @SuppressWarnings("WeakerAccess")
    @Inject
    GpgViewModelFactory viewModelFactory;
    @SuppressWarnings("WeakerAccess")
    DestinationViewModel viewModel;

    private List<DestinationInfo> nearestDestinations;
    private Location currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        inflater = getLayoutInflater();

        toolbar = findViewById(R.id.home_toolbar);
        // disable default app name title display
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        gridView = findViewById(R.id.home_grid_view);
        gridView.setAdapter(new PlaceCategoryGridAdapter(this));
        gridView.setOnItemClickListener((parent, v, position, id) -> clickedGridItem(position));

        carouselView = findViewById(R.id.home_carousel);
        carouselView.setIndicatorGravity(Gravity.CENTER_HORIZONTAL|Gravity.BOTTOM);
        carouselView.setImageClickListener(position ->
                Log.d(LOG_LABEL, "Clicked item: "+ position));

        viewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(DestinationViewModel.class);
        viewModel.getDestinations().observe(this, destinationResource -> {
            // shouldn't happen
            if (destinationResource == null) {
                Log.e(LOG_LABEL, "No ApiResponse wrapper returned");
                return;
            }

            // if querying from server, state may update with LOADING status before it finishes
            if (!destinationResource.status.equals(Status.SUCCESS)) {
                return;
            }

            if (destinationResource.data == null || destinationResource.data.isEmpty()) {
                Log.e(LOG_LABEL, "Destination query returned, but found no results");
                return;
            }

            // TODO: #9 use actual user location
            // set to dummy location: City Hall
            currentLocation = new Location("dummy");
            currentLocation.setLatitude(39.954888);
            currentLocation.setLongitude(-75.163206);

            nearestDestinations = findNearestDestinations(destinationResource.data);

            // set up carousel
            carouselView.setViewListener(new CarouselViewListener(this, true) {
                @Override
                public Destination getDestinationAt(int position) {
                    return nearestDestinations.get(position).getDestination();
                }
            });
            carouselView.setPageCount(nearestDestinations.size());
        });
    }

    @NonNull
    private List<DestinationInfo> findNearestDestinations(List<DestinationInfo> destinationInfos) {
        // TODO: #9 move logic to set distances once location service set up
        // set distance to each location, if not set already
        Destination firstDestination = destinationInfos.get(0).getDestination();
        if (firstDestination.getDistance() == 0) {
            List<Destination> destinations = new ArrayList<>(destinationInfos.size());
            for (DestinationInfo info : destinationInfos) {
                destinations.add(info.getDestination());
            }

            for (Destination dest: destinations) {
                Location location = new Location("dummy");
                DestinationLocation coordinates = dest.getLocation();
                location.setLatitude(coordinates.getY());
                location.setLongitude(coordinates.getX());
                float distanceInMeters = currentLocation.distanceTo(location);
                dest.setDistance(distanceInMeters * METERS_TO_MILES);
            }

            // update destinations all at once, so LiveData observer only triggers once
            viewModel.updateMultipleDestinations(destinations);
        }

        // return the nearest destinations
        int numDestinations = CAROUSEL_MAX_DESTINATION_COUNT;
        if (destinationInfos.size() < numDestinations) {
            numDestinations = destinationInfos.size();
        }
        return destinationInfos.subList(0, numDestinations - 1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        switch (itemId) {
            case R.id.action_search:
                Log.d(LOG_LABEL, "Clicked search action");
                break;
            case R.id.action_settings:
                Log.d(LOG_LABEL, "Clicked settings action");
                break;
            case R.id.action_about:
                Log.d(LOG_LABEL, "Clicked about action");
                break;
            case R.id.action_logout:
                Log.d(LOG_LABEL, "Clicked logout action");
                break;
            default:
                Log.w(LOG_LABEL, "Unrecognized menu option selected: " + itemId);
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    @Override
    protected void onPause() {
        carouselView.pauseCarousel();
        super.onPause();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        carouselView.playCarousel();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private void clickedGridItem(int position) {
        Log.d(LOG_LABEL, "clicked grid view item: " + position);

        switch (position) {
            case 0:
                // go to events list
                startActivity(new Intent(this, EventsListActivity.class));
                break;
            default:
                // go to places list
                // TODO: #18 filter list based on selected grid item
                startActivity(new Intent(this, PlacesListActivity.class));
        }
    }
}
