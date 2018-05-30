package com.gophillygo.app.activities;

import android.annotation.SuppressLint;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;

import com.gophillygo.app.CarouselViewListener;
import com.gophillygo.app.R;
import com.gophillygo.app.data.DestinationViewModel;
import com.gophillygo.app.data.models.Destination;
import com.gophillygo.app.databinding.ActivityPlaceDetailBinding;
import com.gophillygo.app.di.GpgViewModelFactory;
import com.gophillygo.app.utils.FlagMenuUtils;
import com.gophillygo.app.utils.UserUuidUtils;
import com.synnapps.carouselview.CarouselView;

import javax.inject.Inject;

public class PlaceDetailActivity extends AttractionDetailActivity {

    public static final String DESTINATION_ID_KEY = "placeId";
    private static final String LOG_LABEL = "PlaceDetail";

    private long placeId = -1;
    private String userUuid;

    private ActivityPlaceDetailBinding binding;
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
        binding = DataBindingUtil.setContentView(this, R.layout.activity_place_detail);
        binding.setActivity(this);

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

        carouselView = findViewById(R.id.place_detail_carousel);
        carouselView.setIndicatorGravity(Gravity.CENTER_HORIZONTAL|Gravity.BOTTOM);
        carouselView.setImageClickListener(position ->
                Log.d(LOG_LABEL, "Clicked item: "+ position));

        viewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(DestinationViewModel.class);
        viewModel.getDestination(placeId).observe(this, destinationInfo -> {
            // TODO: handle if destination not found (go to list of destinations?)
            if (destinationInfo == null || destinationInfo.getDestination() == null) {
                Log.e(LOG_LABEL, "No matching destination found for ID " + placeId);
                return;
            }

            this.destinationInfo = destinationInfo;
            // set up data binding object
            binding.setDestination(destinationInfo.getDestination());
            binding.setDestinationInfo(destinationInfo);
            displayDestination();
        });

        // Get or create unique, random UUID for app install for posting user flags
        userUuid = UserUuidUtils.getUserUuid(getApplicationContext());
    }

    @SuppressLint({"RestrictedApi", "RtlHardcoded"})
    private void displayDestination() {
        // set up carousel
        carouselView.setViewListener(new CarouselViewListener(this, false) {
            @Override
            public Destination getDestinationAt(int position) {
                return destinationInfo.getDestination();
            }
        });
        carouselView.setPageCount(1);

        TextView descriptionToggle = findViewById(R.id.detail_description_toggle);
        descriptionToggle.setOnClickListener(toggleClickListener);

        // set count of upcoming events
        int eventCount = destinationInfo.getEventCount();
        TextView upcomingEventsView = findViewById(R.id.place_detail_upcoming_events);
        String upcomingEventsText = getResources()
                .getQuantityString(R.plurals.place_upcoming_activities_count, eventCount, eventCount);
        upcomingEventsView.setText(upcomingEventsText);

        // TODO: #18 go to filtered event list with events for destination on click
        upcomingEventsView.setOnClickListener(v -> Log.d(LOG_LABEL,
                "Clicked upcoming events for destination " +  destinationInfo.getDestination().getName()));

        // show popover for flag options (been, want to go, etc.)
        CardView flagOptionsCard = findViewById(R.id.place_detail_flag_options_card);
        flagOptionsCard.setOnClickListener(v -> {
            Log.d(LOG_LABEL, "Clicked flags button");
            PopupMenu menu = FlagMenuUtils.getFlagPopupMenu(this, flagOptionsCard, destinationInfo.getFlag());
            menu.setOnMenuItemClickListener(item -> {
                destinationInfo.updateAttractionFlag(item.getItemId());
                viewModel.updateAttractionFlag(destinationInfo.getFlag(), userUuid, getString(R.string.user_flag_post_api_key));
                return true;
            });
        });
    }
}
