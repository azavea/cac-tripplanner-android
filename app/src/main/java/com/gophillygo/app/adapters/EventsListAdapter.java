package com.gophillygo.app.adapters;

import android.content.Context;

import com.gophillygo.app.R;
import com.gophillygo.app.data.AttractionViewModel;
import com.gophillygo.app.data.models.Event;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventsListAdapter extends AttractionListAdapter<Event> {

    private static final String LOG_LABEL = "EventListAdapter";

    public static final DateFormat monthFormat, dayOfMonthFormat, dayOfWeekFormat, timeFormat, monthDayFormat;

    static {
        monthFormat = new SimpleDateFormat("MMM", Locale.US);
        dayOfMonthFormat = new SimpleDateFormat("dd", Locale.US);
        dayOfWeekFormat = new SimpleDateFormat("EEE", Locale.US);
        timeFormat = new SimpleDateFormat("h:mm a", Locale.US);
        monthDayFormat = new SimpleDateFormat("MMM dd", Locale.US);
    }

    private Context context;

    public EventsListAdapter(Context context, List<Event> attractions, AttractionListItemClickListener listener) {
        super(context, attractions, R.layout.event_list_item, listener);
        this.context = context;
    }

    public String getEventTimeString(Event event) {
        Date start = event.getStart();
        Date end = event.getEnd();

        if (start == null || end == null) {
            return "";
        }

        if (event.isSingleDayEvent()) {
            return context.getString(R.string.event_list_item_time_range,
                    timeFormat.format(start),
                    timeFormat.format(end));
        } else {
            // multi-day event
            return context.getString(R.string.event_list_item_time_range,
                    monthDayFormat.format(start),
                    monthDayFormat.format(end));
        }
    }
}
