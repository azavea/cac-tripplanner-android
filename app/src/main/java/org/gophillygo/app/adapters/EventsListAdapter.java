package org.gophillygo.app.adapters;

import android.content.Context;

import org.gophillygo.app.R;
import org.gophillygo.app.data.models.Destination;
import org.gophillygo.app.data.models.Event;
import org.gophillygo.app.data.models.EventInfo;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventsListAdapter extends AttractionListAdapter<EventInfo> {

    private static final String LOG_LABEL = "EventListAdapter";

    public static final DateFormat monthFormat, dayOfMonthFormat, dayOfWeekFormat, timeFormat, monthDayFormat;

    private final Date now;

    static {
        monthFormat = new SimpleDateFormat("MMM", Locale.US);
        dayOfMonthFormat = new SimpleDateFormat("dd", Locale.US);
        dayOfWeekFormat = new SimpleDateFormat("EEE", Locale.US);
        timeFormat = new SimpleDateFormat("h:mm a", Locale.US);
        monthDayFormat = new SimpleDateFormat("MMM dd", Locale.US);
    }

    private Context context;

    public EventsListAdapter(Context context, List<EventInfo> attractions, AttractionListItemClickListener listener) {
        super(context, attractions, R.layout.event_list_item, listener);
        this.context = context;
        now = new Date();
    }

    public boolean hasMultipleDestinations(Event event) {
        ArrayList<Destination> destinations = event.getDestinations();
        return destinations != null && destinations.size() > 1;
    }

    public boolean isCurrentlyOngoingEvent(Event event) {
        return !event.isSingleDayEvent() && event.getStart().before(now);
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
