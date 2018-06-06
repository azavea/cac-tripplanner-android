package com.gophillygo.app.activities;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.gophillygo.app.R;
import com.gophillygo.app.data.DestinationViewModel;
import com.gophillygo.app.data.EventViewModel;
import com.gophillygo.app.data.models.DestinationInfo;
import com.gophillygo.app.data.models.Event;
import com.gophillygo.app.data.models.EventInfo;
import com.gophillygo.app.databinding.ActivityEventDetailBinding;
import com.gophillygo.app.di.GpgViewModelFactory;
import com.gophillygo.app.utils.FlagMenuUtils;
import com.gophillygo.app.utils.UserUuidUtils;
import com.synnapps.carouselview.CarouselView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

public class EventDetailActivity extends AttractionDetailActivity {

    public static final String EVENT_ID_KEY = "eventId";
    private static final String LOG_LABEL = "EventDetail";

    private static final DateFormat timeFormat, monthDayFormat;

    static {
        timeFormat = new SimpleDateFormat("h:mm a", Locale.US);
        monthDayFormat = new SimpleDateFormat("E MMM dd", Locale.US);
    }

    private long eventId = -1;
    private EventInfo eventInfo;
    private String userUuid;

    private ActivityEventDetailBinding binding;

    @SuppressWarnings("WeakerAccess")
    @Inject
    GpgViewModelFactory viewModelFactory;
    @SuppressWarnings("WeakerAccess")
    EventViewModel viewModel;
    @SuppressWarnings("WeakerAccess")
    DestinationViewModel destinationViewModel;
    private CarouselView carouselView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_event_detail);
        binding.setActivity(this);

        Toolbar toolbar = findViewById(R.id.event_detail_toolbar);
        // disable default app name title display
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (getIntent().hasExtra(EVENT_ID_KEY)) {
            eventId = getIntent().getLongExtra(EVENT_ID_KEY, -1);
        }

        if (eventId == -1) {
            Log.e(LOG_LABEL, "Event not found when attempting to load detail view.");
            finish();
        }

        carouselView = findViewById(R.id.event_detail_carousel);
        carouselView.setIndicatorGravity(Gravity.CENTER_HORIZONTAL|Gravity.BOTTOM);

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(EventViewModel.class);
        destinationViewModel = ViewModelProviders.of(this, viewModelFactory).get(DestinationViewModel.class);
        viewModel.getEvent(eventId).observe(this, eventInfo -> {
            // TODO: handle if event not found (go to list of events?)
            if (eventInfo == null || eventInfo.getEvent() == null) {
                Log.e(LOG_LABEL, "No matching event found for ID " + eventId);
                return;
            }

            this.eventInfo = eventInfo;
            Integer destinationId = eventInfo.getEvent().getDestination();
            if (destinationId != null) {
                LiveData<DestinationInfo> data = destinationViewModel.getDestination(destinationId);
                data.observe(this, destinationInfo -> {
                    this.destinationInfo = destinationInfo;
                    if (destinationInfo != null) {
                        binding.setDestination(destinationInfo.getDestination());
                    } else {
                        Log.e(LOG_LABEL, "No matching destination found for ID " + destinationId);
                    }
                    // Since we call `getDestination(...).observe(...)` every time the event is updated,
                    // we need to remove destination observer every time it is called
                    data.removeObservers(this);
                });
            }
            // set up data binding object
            binding.setEvent(eventInfo.getEvent());
            binding.setEventInfo(eventInfo);
            displayEvent();
        });

        // Get or create unique, random UUID for app install for posting user flags
        userUuid = UserUuidUtils.getUserUuid(getApplicationContext());
    }

    @Override
    // open website for event in browser
    public void goToWebsite(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(eventInfo.getEvent().getWebsiteUrl()));
        startActivity(intent);
    }

    @Override
    protected Class getMapActivity() {
        return EventsMapsActivity.class;
    }

    @Override
    protected int getAttractionId() {
        return (int) eventId;
    }

    // add event in calendar app
    public void addToCalendar(View view) {
        Event event = eventInfo.getEvent();
        long startTime = event.getStart().getTime();
        long endTime = event.getEnd().getTime();
        // For multi-day events, we set the event to be all-day and set the start time to the next reasonable date
        if (!event.isSingleDayEvent()) {
            Calendar now = Calendar.getInstance();
            if (event.getStart().before(now.getTime())) {
                // If the event has already started, set the start date to tomorrow
                now.add(Calendar.DATE, 1);
                startTime = now.getTimeInMillis();
            }
            // All-day events have the same endDate as their startDate
            endTime = startTime;
        }
        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.Events.TITLE, event.getName())
                .putExtra(CalendarContract.Events.DESCRIPTION, event.getHtmlDescription().toString())
                .putExtra(CalendarContract.Events.EVENT_LOCATION, eventInfo.getDestinationName())
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime)
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime)
                .putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, !event.isSingleDayEvent());
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, R.string.event_detail_no_calendar, Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint({"RestrictedApi", "RtlHardcoded"})
    private void displayEvent() {
        setupCarousel(carouselView, eventInfo.getEvent());

        TextView descriptionToggle = findViewById(R.id.detail_description_toggle);
        descriptionToggle.setOnClickListener(toggleClickListener);

        // show popover for flag options (been, want to go, etc.)
        CardView flagOptionsCard = findViewById(R.id.event_detail_flag_options_card);
        flagOptionsCard.setOnClickListener(v -> {
            Log.d(LOG_LABEL, "Clicked flags button");
            PopupMenu menu = FlagMenuUtils.getFlagPopupMenu(this, flagOptionsCard, eventInfo.getFlag());
            menu.setOnMenuItemClickListener(item -> {
                eventInfo.updateAttractionFlag(item.getItemId());
                viewModel.updateAttractionFlag(eventInfo.getFlag(), userUuid, getString(R.string.user_flag_post_api_key));

                return true;
            });
        });
    }

    public String getEventTimeString() {
        Event event = eventInfo.getEvent();
        Date start = event.getStart();
        Date end = event.getEnd();

        if (start == null || end == null) {
            return "";
        }

        if (event.isSingleDayEvent()) {
            return getString(R.string.event_detail_single_day_time_range,
                    monthDayFormat.format(start),
                    timeFormat.format(start),
                    timeFormat.format(end));
        } else {
            // multi-day event
            return getString(R.string.event_list_item_time_range,
                    monthDayFormat.format(start),
                    monthDayFormat.format(end));
        }
    }
}
