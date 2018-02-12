package com.gophillygo.app;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.support.v4.app.Fragment;
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
import com.gophillygo.app.di.GpgViewModelFactory;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ViewListener;

import java.util.List;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

public class HomeActivity extends AppCompatActivity {

    private static String LOG_LABEL = "HomeActivity";

    LayoutInflater inflater;

    CarouselView carouselView;
    GridView gridView;
    Toolbar toolbar;

    @Inject
    GpgViewModelFactory viewModelFactory;
    DestinationViewModel viewModel;

    // order corresponds to the image URLs below
    String[] testPlaceNames = {
            "Camden County Environmental Education Center",
            "Bartram's Garden",
            "Cobb's Creek Trail"
    };

    // destination wide images, 680x400
    String[] testImageUrls = {
            "https://cleanair-images-prod.s3.amazonaws.com/destinations/e6aa6bc0891247c4a4d651f22c028fe6.jpg",
            "https://cleanair-images-prod.s3.amazonaws.com/destinations/874f2bd93b5f4bc692cf39d1aaba5ead.jpg",
            "https://cleanair-images-prod.s3.amazonaws.com/destinations/ad72d3d20dfb4197b76c7b4d211a8eef.jpg"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        inflater = getLayoutInflater();

        toolbar = findViewById(R.id.home_toolbar);
        // disable default app name title display
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        carouselView = findViewById(R.id.home_carousel);
        carouselView.setPageCount(testPlaceNames.length);
        carouselView.setViewListener(viewListener);
        carouselView.setImageClickListener(position ->
                Log.d(LOG_LABEL, "Clicked item: "+ position));

        gridView = findViewById(R.id.home_grid_view);
        gridView.setAdapter(new PlaceCategoryGridAdapter(this));
        gridView.setOnItemClickListener((parent, v, position, id) ->
                Log.d(LOG_LABEL, "clicked grid view item: " + position));

        if (viewModelFactory == null) {
            Log.d(LOG_LABEL, "viewmodel factory is null!!!!!!!!!!");
        }

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(DestinationViewModel.class);
        viewModel.getDestinations().observe(this, destinationResource -> {
            // TODO: pass .data to provider
            if (destinationResource == null) {
                Log.e(LOG_LABEL, "No destinations retrieved!");
                return;
            }
            List<Destination> destinations = destinationResource.data;
            if (destinations == null) {
                Log.e(LOG_LABEL, "Destination query returned, but found no results");
                Log.e(LOG_LABEL, destinationResource.status.name());
                return;
            }
            for (Destination dest: destinations) {
                Log.d(LOG_LABEL, dest.getAddress());
            }
        });
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

    ViewListener viewListener = new ViewListener() {
        @Override
        public View setViewForPosition(int position) {
            // root here must be null (not the carouselView) to avoid ViewPager stack overflow
            View itemView = inflater.inflate(R.layout.custom_carousel_item, null);
            ImageView carouselImageView = itemView.findViewById(R.id.carousel_item_image);
            TextView carouselPlaceName = itemView.findViewById(R.id.carousel_item_place_name);

            Glide.with(HomeActivity.this)
                    .load(testImageUrls[position])
                    .into(carouselImageView);

            carouselPlaceName.setText(testPlaceNames[position]);
            carouselImageView.setContentDescription(testPlaceNames[position]);
            carouselView.setIndicatorGravity(Gravity.CENTER_HORIZONTAL|Gravity.BOTTOM);
            return itemView;
        }
    };

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
