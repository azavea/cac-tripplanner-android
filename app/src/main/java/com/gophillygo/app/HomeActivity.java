package com.gophillygo.app;

import android.annotation.SuppressLint;
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
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.gophillygo.app.adapters.PlaceCategoryGridAdapter;
import com.gophillygo.app.data.DestinationViewModel;
import com.gophillygo.app.data.models.Destination;
import com.gophillygo.app.data.models.DestinationLocation;
import com.gophillygo.app.data.networkresource.Status;
import com.gophillygo.app.di.GpgViewModelFactory;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ViewListener;

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

    private List<Destination> nearestDestinations;
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

            if (!destinationResource.status.equals(Status.SUCCESS)) {
                Log.e(LOG_LABEL, "Destination query failed with status: " +
                        destinationResource.status.name());
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
            carouselView.setViewListener(viewListener);
            carouselView.setPageCount(nearestDestinations.size());
        });
    }

    @NonNull
    private List<Destination> findNearestDestinations(List<Destination> destinations) {
        // TODO: #9 move logic to set distances once location service set up
        // set distance to each location, if not set already
        Destination firstDestination = destinations.get(0);
        if (firstDestination.getDistance() == 0) {
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
        if (destinations.size() < numDestinations) {
            numDestinations = destinations.size();
        }
        return destinations.subList(0, numDestinations - 1);
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

    private final ViewListener viewListener = new ViewListener() {
        @Override
        public View setViewForPosition(int position) {
            // root here must be null (not the carouselView) to avoid ViewPager stack overflow
            @SuppressLint("InflateParams") View itemView = inflater.inflate(R.layout.custom_carousel_item, null);
            ImageView carouselImageView = itemView.findViewById(R.id.carousel_item_image);
            TextView carouselPlaceName = itemView.findViewById(R.id.carousel_item_place_name);
            TextView carouselDistance = itemView.findViewById(R.id.carousel_item_distance_label);

            Destination destination = nearestDestinations.get(position);

            Glide.with(HomeActivity.this)
                    .load(destination.getWideImage())
                    .into(carouselImageView);

            carouselPlaceName.setText(destination.getAddress());
            carouselImageView.setContentDescription(destination.getAddress());
            carouselView.setIndicatorGravity(Gravity.CENTER_HORIZONTAL|Gravity.BOTTOM);
            carouselDistance.setText(destination.getFormattedDistance());

            return itemView;
        }
    };

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private void clickedGridItem(int position) {
        Log.d(LOG_LABEL, "clicked grid view item: " + position);

        Intent intent = new Intent(this, PlacesListActivity.class);
        startActivity(intent);
    }
}
