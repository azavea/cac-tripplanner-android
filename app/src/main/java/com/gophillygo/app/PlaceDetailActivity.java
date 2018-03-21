package com.gophillygo.app;

import android.annotation.SuppressLint;
import android.arch.lifecycle.ViewModelProviders;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.gophillygo.app.data.DestinationViewModel;
import com.gophillygo.app.data.models.Destination;
import com.gophillygo.app.di.GpgViewModelFactory;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ViewListener;

import javax.inject.Inject;

public class PlaceDetailActivity extends AppCompatActivity {

    public static final String DESTINATION_ID_KEY = "placeId";
    private static final String LOG_LABEL = "PlaceDetail";

    private long placeId = -1;
    private Destination destination;

    private LayoutInflater inflater;
    private CarouselView carouselView;
    private Toolbar toolbar;

    @SuppressWarnings("WeakerAccess")
    @Inject
    GpgViewModelFactory viewModelFactory;
    @SuppressWarnings("WeakerAccess")
    DestinationViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_detail);

        inflater = getLayoutInflater();
        toolbar = findViewById(R.id.place_detail_toolbar);
        // disable default app name title display
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (getIntent().hasExtra(DESTINATION_ID_KEY)) {
            placeId = getIntent().getLongExtra(DESTINATION_ID_KEY, -1);
        }

        if (placeId == -1) {
            Log.e(LOG_LABEL, "Place not found when attempting to load detail view.");
            finish();
        }

        Log.d(LOG_LABEL, "Started detail view for place " + placeId);

        carouselView = findViewById(R.id.place_detail_carousel);
        carouselView.setImageClickListener(position ->
                Log.d(LOG_LABEL, "Clicked item: "+ position));

        viewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(DestinationViewModel.class);
        viewModel.getDestination(placeId).observe(this, destination -> {
            this.destination = destination;
            displayDestination();
        });
    }

    private void displayDestination() {
        Log.d(LOG_LABEL, "Display destination " +  destination.getName());
        // set up carousel
        carouselView.setViewListener(viewListener);
        carouselView.setPageCount(1);


        // set activities list text
        TextView flagTextView = findViewById(R.id.place_detail_activities_list);
        StringBuilder stringBuilder = new StringBuilder("");
        String dot = Html.fromHtml("&nbsp;&#8226;&nbsp;").toString();
        for (String activity: destination.getActivities()) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append(dot);
            }
            stringBuilder.append(activity);
        }
        flagTextView.setText(stringBuilder.toString());

        // toggle label for cycling activity
        TextView cyclingView = findViewById(R.id.place_detail_cycling_label);
        if (destination.isCycling()) {
            cyclingView.setVisibility(View.VISIBLE);
        } else {
            cyclingView.setVisibility(View.INVISIBLE);
        }

        // set count of upcoming activities
        // TODO:
        TextView upcomingEventsView = findViewById(R.id.place_detail_upcoming_events);
        String upcomingEventsText = getResources()
                .getQuantityString(R.plurals.place_upcoming_activities_count, 1, 1);
        upcomingEventsView.setText(upcomingEventsText);
        upcomingEventsView.setVisibility(View.VISIBLE);
    }

    private final ViewListener viewListener = new ViewListener() {
        @Override
        public View setViewForPosition(int position) {
            // root here must be null (not the carouselView) to avoid ViewPager stack overflow
            @SuppressLint("InflateParams") View itemView = inflater.inflate(R.layout.custom_carousel_item, null);
            ImageView carouselImageView = itemView.findViewById(R.id.carousel_item_image);
            TextView carouselPlaceName = itemView.findViewById(R.id.carousel_item_place_name);
            TextView carouselDistance = itemView.findViewById(R.id.carousel_item_distance_label);

            TextView carouselNearby = itemView.findViewById(R.id.carousel_item_nearby_label);
            carouselNearby.setVisibility(View.GONE);

            Glide.with(PlaceDetailActivity.this)
                    .load(destination.getWideImage())
                    .into(carouselImageView);

            carouselPlaceName.setText(destination.getAddress());
            carouselImageView.setContentDescription(destination.getAddress());
            carouselView.setIndicatorGravity(Gravity.CENTER_HORIZONTAL|Gravity.BOTTOM);
            carouselDistance.setText(destination.getFormattedDistance());

            return itemView;
        }
    };
}
