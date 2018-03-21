package com.gophillygo.app;

import android.annotation.SuppressLint;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
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

    private static final int DESCRIPTION_COLLAPSED_LINE_COUNT = 4;
    private static final int DESCRIPTION_EXPANDED_MAX_LINES = 50;

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

    @SuppressLint("RestrictedApi")
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
        if (stringBuilder.length() == 0) {
            flagTextView.setVisibility(View.GONE);
        } else {
            flagTextView.setVisibility(View.VISIBLE);
        }

        // toggle label for cycling activity
        TextView cyclingView = findViewById(R.id.place_detail_cycling_label);
        if (destination.isCycling()) {
            cyclingView.setVisibility(View.VISIBLE);
        } else {
            cyclingView.setVisibility(View.INVISIBLE);
        }

        // set count of upcoming events
        // TODO: use actual count once events stored
        TextView upcomingEventsView = findViewById(R.id.place_detail_upcoming_events);
        String upcomingEventsText = getResources()
                .getQuantityString(R.plurals.place_upcoming_activities_count, 2, 2);
        upcomingEventsView.setText(upcomingEventsText);
        upcomingEventsView.setVisibility(View.VISIBLE);

        TextView descriptionView = findViewById(R.id.place_detail_description_text);
        descriptionView.setText(Html.fromHtml(destination.getDescription()));
        // TODO: better handle expand/collapse
        descriptionView.setOnClickListener(v -> {
            Log.d(LOG_LABEL, "TODO: expand details view?");
            int current = descriptionView.getMaxLines();
            if (current == DESCRIPTION_COLLAPSED_LINE_COUNT) {
                descriptionView.setMaxLines(DESCRIPTION_EXPANDED_MAX_LINES);
            } else {
                descriptionView.setMaxLines(DESCRIPTION_COLLAPSED_LINE_COUNT);
            }
        });

        // show popover for flag options (been, want to go, etc.)
        CardView flagOptionsCard = findViewById(R.id.place_detail_flag_options_card);
        flagOptionsCard.setOnClickListener(v -> {
            Log.d(LOG_LABEL, "Clicked flags button");
            PopupMenu menu = new PopupMenu(this, flagOptionsCard);
            menu.getMenuInflater().inflate(R.menu.place_options_menu, menu.getMenu());
            menu.setOnMenuItemClickListener(item -> {
                Log.d(LOG_LABEL, "Clicked " + item.toString());
                return true;
            });

            // Force icons to show in the popup menu via the support library API
            // https://stackoverflow.com/questions/6805756/is-it-possible-to-display-icons-in-a-popupmenu
            MenuPopupHelper popupHelper = new MenuPopupHelper(this,
                    (MenuBuilder)menu.getMenu(),
                    flagOptionsCard);
            popupHelper.setForceShowIcon(true);
            popupHelper.setGravity(Gravity.END|Gravity.RIGHT);
            popupHelper.show();
        });

        // toggle watershed alliance logo
        ImageView watershedAllianceView = findViewById(R.id.place_detail_watershed_alliance_icon);
        if (destination.isWatershedAlliance()) {
            watershedAllianceView.setVisibility(View.VISIBLE);
        } else {
            watershedAllianceView.setVisibility(View.GONE);
        }

        // handle button bar button clicks
        Button goToMapButton = findViewById(R.id.place_detail_map_button);
        Button getDirectionsButton = findViewById(R.id.place_detail_directions_button);
        Button goToWebsiteButton = findViewById(R.id.place_detail_website_button);

        // TODO: open within app map view, once implemented
        // for now, open Google Maps externally with a marker at the given location
        goToMapButton.setOnClickListener(v -> {
            String locationString = destination.getLocation().toString();
            Uri gmapsUri = Uri.parse("geo:" + locationString + "?q=" + locationString + "(" +
                destination.getName() + ")");
            Log.d(LOG_LABEL, gmapsUri.toString());
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmapsUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            }
        });

        // open the GoPhillyGo website, passing the destination
        getDirectionsButton.setOnClickListener(v -> {
            // pass parameters destination and destinationText to https://gophillygo.org/
            Uri directionsUri = new Uri.Builder().scheme("https").authority("gophillygo.org")
                    .appendQueryParameter("destination", destination.getLocation().toString())
                    .appendQueryParameter("destinationText", destination.getAddress()).build();
            Intent intent = new Intent(Intent.ACTION_VIEW, directionsUri);
            startActivity(intent);
        });

        String website = destination.getWebsiteUrl();
        goToWebsiteButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(website));
            startActivity(intent);
        });
        if (website.isEmpty()) {
            goToWebsiteButton.setVisibility(View.GONE);
        } else {
            goToWebsiteButton.setVisibility(View.VISIBLE);
        }
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
