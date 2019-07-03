package org.gophillygo.app.activities;

import android.annotation.SuppressLint;
import android.content.Intent;

import androidx.appcompat.widget.PopupMenu;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;

import com.crashlytics.android.Crashlytics;

import org.gophillygo.app.BR;
import org.gophillygo.app.R;
import org.gophillygo.app.adapters.AttractionListAdapter;
import org.gophillygo.app.adapters.EventsListAdapter;
import org.gophillygo.app.data.DestinationViewModel;
import org.gophillygo.app.data.EventViewModel;
import org.gophillygo.app.data.models.AttractionFlag;
import org.gophillygo.app.data.models.AttractionInfo;
import org.gophillygo.app.data.models.DestinationInfo;
import org.gophillygo.app.data.models.EventInfo;
import org.gophillygo.app.databinding.ActivityPlaceDetailBinding;
import org.gophillygo.app.di.GpgViewModelFactory;
import org.gophillygo.app.utils.FlagMenuUtils;
import org.gophillygo.app.utils.UserUtils;

import javax.inject.Inject;

public class PlaceDetailActivity extends AttractionDetailActivity implements AttractionListAdapter.AttractionListItemClickListener {

    public static final String DESTINATION_ID_KEY = "place_id";
    private static final String LOG_LABEL = "PlaceDetail";

    private long placeId = -1;
    private ActivityPlaceDetailBinding binding;

    @SuppressWarnings("WeakerAccess")
    @Inject
    GpgViewModelFactory viewModelFactory;
    @SuppressWarnings("WeakerAccess")
    DestinationViewModel destinationViewModel;
    @SuppressWarnings("WeakerAccess")
    EventViewModel eventViewModel;

    RecyclerView eventsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_place_detail);
        binding.setActivity(this);

        // disable default app name title display
        binding.placeDetailToolbar.setTitle("");
        setSupportActionBar(binding.placeDetailToolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (getIntent().hasExtra(DESTINATION_ID_KEY)) {
            placeId = getIntent().getLongExtra(DESTINATION_ID_KEY, -1);
        }

        if (placeId == -1) {
            Log.e(LOG_LABEL, "Place not found when attempting to load detail view.");
            finish();
        }

        binding.placeDetailCarousel.setIndicatorGravity(Gravity.CENTER_HORIZONTAL|Gravity.BOTTOM);
        binding.placeDetailCarousel.setImageClickListener(position ->
                Log.d(LOG_LABEL, "Clicked item: "+ position));

        destinationViewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(DestinationViewModel.class);
        LiveData<DestinationInfo> data = destinationViewModel.getDestination(placeId);

        eventViewModel = ViewModelProviders.of(this, viewModelFactory).get(EventViewModel.class);

        data.observe(this, destinationInfo -> {
            // TODO: #61 handle if destination not found (go to list of destinations?)
            if (destinationInfo == null || destinationInfo.getDestination() == null) {
                String message = "No matching destination found for ID " + placeId;
                Log.e(LOG_LABEL, message);
                Crashlytics.log(message);
                return;
            }

            this.destinationInfo = destinationInfo;

            // set up data binding object
            binding.setDestination(destinationInfo.getDestination());
            binding.setDestinationInfo(destinationInfo);
            binding.setAttractionInfo(destinationInfo);
            binding.setActivity(this);
            binding.setContext(this);
            binding.placeDetailDescriptionCard.detailDescriptionToggle.setOnClickListener(toggleClickListener);
            displayDestination();

            // set up list of related events
            if (destinationInfo.getEventCount() > 0) {
                eventsList = findViewById(R.id.place_detail_events_recycler_view);
                // set adapter for related events
                eventViewModel.getEventsForDestination(placeId).observe(this, events -> {
                    if (events != null) {
                        if (events.size() != destinationInfo.getEventCount()) {
                            Log.e(LOG_LABEL, "Event count mismatch. Summary has " + destinationInfo.getEventCount() +
                            "but query found " + events.size());
                            return;
                        }

                        eventsList.setAdapter(new EventsListAdapter(this, events, this));
                        eventsList.setLayoutManager(new LinearLayoutManager(this));
                    } else {
                        Log.e(LOG_LABEL, "no events found for destination, but event count is " + destinationInfo.getEventCount());
                        eventsList.setVisibility(View.INVISIBLE);
                    }
                });
            }

        });
    }

    @SuppressLint({"RestrictedApi", "RtlHardcoded"})
    private void displayDestination() {
        setupCarousel(binding.placeDetailCarousel, destinationInfo.getDestination());
    }

    // show popover for flag options (been, want to go, etc.)
    public void userFlagChanged(View view) {
        Log.d(LOG_LABEL, "Clicked flags button");
        PopupMenu menu = FlagMenuUtils.getFlagPopupMenu(this, view, destinationInfo.getFlag());
        menu.setOnMenuItemClickListener(item -> {
            updateFlag(item.getItemId());
            return true;
        });
    }

    private void updateFlag(int itemId) {
        if (destinationInfo == null) {
            String message = "Cannot update flag because destination is not set";
            Log.e(LOG_LABEL, message);
            Crashlytics.log(message);
            return;
        }
        String option = destinationInfo.getFlag().getOption().apiName;
        boolean haveExistingGeofence = option.equals(AttractionFlag.Option.WantToGo.apiName) ||
                option.equals(AttractionFlag.Option.Liked.apiName);
        destinationInfo.updateAttractionFlag(itemId);
        destinationViewModel.updateAttractionFlag(destinationInfo.getFlag(), userUuid, getString(R.string.user_flag_post_api_key), UserUtils.isFlagPostingEnabled(this));
        String optionAfter = destinationInfo.getFlag().getOption().apiName;
        boolean settingGeofence = optionAfter.equals(AttractionFlag.Option.WantToGo.apiName) ||
                optionAfter.equals(AttractionFlag.Option.Liked.apiName);
        addOrRemoveGeofence(destinationInfo, haveExistingGeofence, settingGeofence);
        binding.notifyPropertyChanged(BR.destinationInfo);
    }

    /**
     * Scroll to head of inline events list when calendar count summary clicked.
     *
     * @param view View clicked (required parameter for binding)
     */
    public void goToEvents(View view) {
        ScrollView scrollView = findViewById(R.id.place_detail_scroll_view);
        scrollView.scrollTo(0, findViewById(R.id.place_detail_events_recycler_view).getTop());

    }

    @Override
    protected Class getMapActivity() {
        return PlacesMapsActivity.class;
    }

    @Override
    protected int getAttractionId() {
        return (int) placeId;
    }

    /**
     * Go to event detail when related event clicked
     *
     * @param position Offset of event in list
     */
    @Override
    public void clickedAttraction(int position) {
        long eventId = eventsList.getAdapter().getItemId(position);
        Log.d(LOG_LABEL, "Clicked event with ID: " + eventId);
        Intent intent = new Intent(this, EventDetailActivity.class);
        intent.putExtra(EventDetailActivity.EVENT_ID_KEY, eventId);
        startActivity(intent);
    }

    @Override
    public boolean clickedFlagOption(MenuItem item, AttractionInfo eventInfo, Integer position) {
        Log.d(LOG_LABEL, "clicked flag option on event at " + position);
        String option = eventInfo.getFlag().getOption().apiName;
        boolean haveExistingGeofence = option.equals(AttractionFlag.Option.WantToGo.apiName) ||
                option.equals(AttractionFlag.Option.Liked.apiName);

        eventInfo.updateAttractionFlag(item.getItemId());
        eventViewModel.updateAttractionFlag(eventInfo.getFlag(), userUuid, getString(R.string.user_flag_post_api_key), UserUtils.isFlagPostingEnabled(this));
        eventsList.getAdapter().notifyItemChanged(position);

        // do not attempt to add a geofence for an event with no location (should always exist here,
        // as we know there is an associated place)
        if (((EventInfo)eventInfo).hasDestinationName()) {
            String optionAfter = eventInfo.getFlag().getOption().apiName;
            boolean settingGeofence = optionAfter.equals(AttractionFlag.Option.WantToGo.apiName) ||
                    optionAfter.equals(AttractionFlag.Option.Liked.apiName);
            addOrRemoveGeofence(eventInfo, haveExistingGeofence, settingGeofence);
        }

        return true;
    }
}
